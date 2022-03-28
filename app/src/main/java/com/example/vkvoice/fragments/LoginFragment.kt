package com.example.vkvoice.fragments

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vkvoice.MainActivity
import com.example.vkvoice.R
import com.example.vkvoice.ui.RecordViewModel
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment: Fragment(R.layout.login_fragment)  {

    private lateinit var viewModel: RecordViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).recordViewModel

        if (viewModel.loginState) {
            vk.visibility = View.INVISIBLE
            next.text = "Перейти к аудио заметкам"
        }
        vk.setOnClickListener {
            VK.login(activity as MainActivity, arrayListOf(VKScope.DOCS))
        }

        next.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_savedRecordFragment)
        }

    }
}