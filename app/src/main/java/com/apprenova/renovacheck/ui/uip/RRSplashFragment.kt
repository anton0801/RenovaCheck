package com.apprenova.renovacheck.ui.uip

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.apprenova.renovacheck.MainActivity
import com.apprenova.renovacheck.R
import com.apprenova.renovacheck.databinding.FragmentRenovaLoadSplashBinding
import com.apprenova.renovacheck.data.handlers.RROnDeviceStoreManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class RRSplashFragment : Fragment(R.layout.fragment_renova_load_splash) {
    private lateinit var buildStepsProLoadingBinding: FragmentRenovaLoadSplashBinding

    private val RRSplashViewModel by viewModel<RRSplashViewModel>()

    private val RROnDeviceStoreManager by inject<RROnDeviceStoreManager>()

    private var buildStepsProUrl = ""

    private val chickHealthRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            bspToSuccess(buildStepsProUrl)
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                RROnDeviceStoreManager.batchNotificationRequest =
                    (System.currentTimeMillis() / 1000) + 2592000000
                bspToSuccess(buildStepsProUrl)
            } else {
                bspToSuccess(buildStepsProUrl)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 999 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            bspToSuccess(buildStepsProUrl)
        } else {
            // твой код на отказ
            bspToSuccess(buildStepsProUrl)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildStepsProLoadingBinding = FragmentRenovaLoadSplashBinding.bind(view)

        buildStepsProLoadingBinding.feedMixGrandButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val eggLabelPermission = Manifest.permission.POST_NOTIFICATIONS
                chickHealthRequestNotificationPermission.launch(eggLabelPermission)
                RROnDeviceStoreManager.batchNotificationRequestedBefore = true
            } else {
                bspToSuccess(buildStepsProUrl)
                RROnDeviceStoreManager.batchNotificationRequestedBefore = true
            }
        }

        buildStepsProLoadingBinding.feedMixSkipButton.setOnClickListener {
            RROnDeviceStoreManager.batchNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            bspToSuccess(buildStepsProUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                RRSplashViewModel.chickHealthHomeScreenState.collect {
                    when (it) {
                        is FeedMixHomeScreenState.FeedMixLoading -> {
                        }

                        is FeedMixHomeScreenState.FeedMixError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is FeedMixHomeScreenState.FeedMixSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val eggLabelPermission = Manifest.permission.POST_NOTIFICATIONS
                                val eggLabelPermissionRequestedBefore =
                                    RROnDeviceStoreManager.batchNotificationRequestedBefore

                                if (ContextCompat.checkSelfPermission(
                                        requireContext(),
                                        eggLabelPermission
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    bspToSuccess(it.data)
                                } else if (!eggLabelPermissionRequestedBefore && (System.currentTimeMillis() / 1000 > RROnDeviceStoreManager.batchNotificationRequest)) {
                                    // первый раз — показываем UI для запроса
                                    buildStepsProLoadingBinding.feedMixNotiGroup.visibility =
                                        View.VISIBLE
                                    buildStepsProLoadingBinding.feedMixLoadingGroup.visibility =
                                        View.GONE
                                    buildStepsProUrl = it.data
                                } else if (shouldShowRequestPermissionRationale(eggLabelPermission)) {
                                    // временный отказ — через 3 дня можно показать
                                    if (System.currentTimeMillis() / 1000 > RROnDeviceStoreManager.batchNotificationRequest) {
                                        buildStepsProLoadingBinding.feedMixNotiGroup.visibility =
                                            View.VISIBLE
                                        buildStepsProLoadingBinding.feedMixLoadingGroup.visibility =
                                            View.GONE
                                        buildStepsProUrl = it.data
                                    } else {
                                        bspToSuccess(it.data)
                                    }
                                } else {
                                    bspToSuccess(it.data)
                                }
                            } else {
                                bspToSuccess(it.data)
                            }
                        }

                        FeedMixHomeScreenState.FeedMixNotInternet -> {
                            buildStepsProLoadingBinding.feedMixStateGroup.visibility = View.VISIBLE
                            buildStepsProLoadingBinding.feedMixLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun bspToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_RRSplashFragment_to_RRMainApplicationV,
            bundleOf(FEED_MIX_D to data)
        )
    }


    companion object {
        const val FEED_MIX_D = "eggLabelData"
    }

}