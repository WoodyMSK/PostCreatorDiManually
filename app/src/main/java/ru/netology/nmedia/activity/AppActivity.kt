package ru.netology.nmedia.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.service.FCMService
import ru.netology.nmedia.viewmodel.AuthViewModel


@OptIn(ExperimentalCoroutinesApi::class)
class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private val container by lazy { DependencyContainer.getInstance(application) }
    private val viewModel: AuthViewModel by viewModels(
        factoryProducer = {
            container.viewModelFactory
        }
    )

    private val auth by lazy { container.auth }

    private lateinit var pushBroadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pushBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val extras = intent?.extras
//                Log.e("TAG", "Message received")
                extras?.keySet()?.firstOrNull { it == FCMService.KEY_ACTION }?.let { key ->
//                    Log.e("TAG", "Action key -> $key")
                    when (extras.getString(key)) {
                        FCMService.ACTION_SHOW_MESSAGE -> {
                            extras.getString(FCMService.KEY_MESSAGE)?.let { message ->
//                                Log.e("TAG", "Message key -> $message")
                                Toast.makeText(
                                    applicationContext,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        else -> Log.e("TAG", "No needed key found")
                    }
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(FCMService.INTENT_FILTER)

        registerReceiver(pushBroadcastReceiver, intentFilter)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }

        checkGoogleApiAvailability()
    }

    override fun onDestroy() {
        unregisterReceiver(pushBroadcastReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        menu.let {
            it.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
            it.setGroupVisible(R.id.authenticated, viewModel.authenticated)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signin -> {
                Log.d("AAA", "Кнопка логин нажата")
                // TODO: just hardcode it, implementation must be in homework
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_loginFragment)
//                auth.setAuth(5, "x-token")
                true
            }
            R.id.signup -> {
                // TODO: just hardcode it, implementation must be in homework
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_registrationFragment)
//                auth.setAuth(5, "x-token")
                true
            }
            R.id.signout -> {
                // TODO: just hardcode it, implementation must be in homework
//                findNavController(R.id.nav_host_fragment).navigate(R.id.exitDialogFragment)
                auth.removeAuth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            println(it)
        }
    }
}