package ru.netology.nmedia.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.model.RegisteredInUserView
import ru.netology.nmedia.util.AndroidUtils.hideKeyboard
import ru.netology.nmedia.viewmodel.RegistrationViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationFragment : Fragment() {
    private val registrationViewModel: RegistrationViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = {
            DependencyContainer.getInstance(requireContext().applicationContext).viewModelFactory
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        registrationViewModel = ViewModelProvider(this, RegistrationViewModelFactory())
//            .get(RegistrationViewModel::class.java)

        val firstNameEditText = view.findViewById<EditText>(R.id.firstname)
        val userNameEditText = view.findViewById<EditText>(R.id.username)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.confirmPassword)
        val registrationButton = view.findViewById<Button>(R.id.registration)
        val loadingProgressBar = view.findViewById<ProgressBar>(R.id.loading)

        registrationViewModel.RegistrationFormState.observe(viewLifecycleOwner,
            Observer { registrationFormState ->
                if (registrationFormState == null) {
                    return@Observer
                }
                registrationButton.isEnabled = registrationFormState.isRegistrationDataValid
                registrationFormState.loginError?.let {
                    userNameEditText.error = getString(it)
                }
                registrationFormState.confirmPasswordError?.let {
                    passwordEditText.error = getString(it)
                }
            }
        )

        registrationViewModel.registrationResult.observe(viewLifecycleOwner,
            Observer { registrationResult ->
                registrationResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                registrationResult.error?.let {
                    showRegistrationFailed(it)
                }
                registrationResult.success?.let {
                    updateUiWithUser(it)
                    requireView().hideKeyboard()
                    findNavController().navigateUp()
                }
            })

        val afterTextChangedListener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable?) {
                registrationViewModel.registrationDataChecked(
                    userNameEditText.text.toString(),
                    passwordEditText.text.toString(),
                    confirmPasswordEditText.text.toString()
                )
            }
        }

        userNameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        confirmPasswordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registrationViewModel.registration(
                    userNameEditText.toString(),
                    passwordEditText.toString(),
                    confirmPasswordEditText.toString()
                )
            }
            false
        }

        registrationButton.setOnClickListener{
            loadingProgressBar.visibility = View.VISIBLE
//            AppAuth.getInstance().setAuth(5, "x-token")
            registrationViewModel.registration(
                firstNameEditText.toString(),
                userNameEditText.toString(),
                passwordEditText.toString()
            )
        }

    }

    private fun updateUiWithUser(model: RegisteredInUserView) {
        val welcome = getString(R.string.welcome)
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showRegistrationFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }


}