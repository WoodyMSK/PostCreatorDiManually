package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPictureViewBinding
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class PictureViewFragment : Fragment() {

    companion object {
        var Bundle.ArgPicture: String? by StringArg
        var Bundle.ArgLikes: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()

    private var fragmentBinding: FragmentPictureViewBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPictureViewBinding.inflate(
            inflater,
            container,
            false
        )

        fragmentBinding = binding

        arguments?.ArgLikes
            ?.let(binding.like::setText)

        arguments?.ArgPicture
            ?.let {
                Glide.with(binding.postPicture)
//                    .load("http://10.0.2.2:9999/media/${post.attachment!!.url}")
                    .load("http://192.168.31.104:9999/media/${arguments?.ArgPicture}")
                    .placeholder(R.drawable.ic_like_24dp)
                    .error(R.drawable.ic_like_outlined_24dp)
                    .into(binding.postPicture)
            }

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }


}
