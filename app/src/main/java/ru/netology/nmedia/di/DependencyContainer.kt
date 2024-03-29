package ru.netology.nmedia.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.api.token
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.data.LoginDataSource
import ru.netology.nmedia.data.LoginRepository
import ru.netology.nmedia.data.RegistrationDataSource
import ru.netology.nmedia.data.RegistrationRepository
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.viewmodel.ViewModelFactory
import ru.netology.nmedia.work.WorkerFactoryDelegate

class DependencyContainer private constructor(private val context: Context) {

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: DependencyContainer? = null

        fun getInstance(context: Context): DependencyContainer {
            return instance ?: synchronized(this) {
                instance ?: DependencyContainer(context).also { instance = it }
            }
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val authPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val okhttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            authPrefs.token?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okhttp)
        .build()


    private val api = retrofit.create(ApiService::class.java)

    private val db = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    private val postDao = db.postDao()
    private val postWorkDao = db.postWorkDao()
    private val loginDataSource = LoginDataSource()
    private val registrationDataSource = RegistrationDataSource()

    private val repository = PostRepositoryImpl(
        postDao,
        postWorkDao,
        api
    )

    val auth = AppAuth(
        authPrefs,
        api
    )

    private val loginRepository = LoginRepository(
        auth,
        loginDataSource,
        api
    )

    private val registrationRepository = RegistrationRepository(
        auth,
        registrationDataSource,
        api
    )

    val workManager = run {
        WorkManager.initialize(context, Configuration.Builder()
            .setWorkerFactory(WorkerFactoryDelegate(repository))
            .build())

        WorkManager.getInstance(context)
    }



    val viewModelFactory = ViewModelFactory(
        repository,
        loginRepository,
        registrationRepository,
        workManager,
        auth,
    )


}