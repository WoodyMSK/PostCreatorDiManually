package ru.netology.nmedia.activity

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.di.DependencyContainer


class ExitDialogFragment : DialogFragment() {
    private val container by lazy { DependencyContainer.getInstance(requireContext()) }
    private val auth by lazy { container.auth }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Выйти?")
                .setIcon(R.drawable.ic_exit_24)
                .setCancelable(true)
                .setPositiveButton("Да") { _, _ ->
                    auth.removeAuth()
                    findNavController().navigate(R.id.action_exitDialogFragment_to_feedFragment)
                }
                .setNegativeButton("Нет") { _, _ ->
                    dialog!!.cancel();
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}