package ru.netology.nmedia.activity

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R


class DialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Ошибка авторизации!")
                .setMessage("Выполнить вход?")
                .setIcon(R.drawable.ic_error_24)
                .setCancelable(true)
                .setPositiveButton("Да") { _, _ ->
                    findNavController().navigate(R.id.action_dialogFragment_to_loginFragment)
                }
                .setNegativeButton("Нет") { _, _ ->
                    findNavController().navigateUp()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}