package com.apprenova.renovacheck.ui.uip.views

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.apprenova.renovacheck.MainApplication
import com.apprenova.renovacheck.ui.uip.RRSplashFragment
import org.koin.android.ext.android.inject

class RRMainApplicationV : Fragment() {

    private lateinit var eggLabelPhoto: Uri
    private var eggLabelFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val eggLabelTakeFile: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            eggLabelFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
            eggLabelFilePathFromChrome = null
        }

    private val eggLabelTakePhoto: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                eggLabelFilePathFromChrome?.onReceiveValue(arrayOf(eggLabelPhoto))
                eggLabelFilePathFromChrome = null
            } else {
                eggLabelFilePathFromChrome?.onReceiveValue(null)
                eggLabelFilePathFromChrome = null
            }
        }

    private val RRDataStore by activityViewModels<RRDataStore>()


    private val RRMainViFun by inject<RRMainViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(MainApplication.MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (RRDataStore.RRMainApplicationView.canGoBack()) {
                        RRDataStore.RRMainApplicationView.goBack()
                    } else if (RRDataStore.RRMainApplicationViList.size > 1) {
                        RRDataStore.RRMainApplicationViList.removeAt(
                            RRDataStore.RRMainApplicationViList.lastIndex
                        )
                        RRDataStore.RRMainApplicationView.destroy()
                        val previousWebView =
                            RRDataStore.RRMainApplicationViList.last()
                        eggLabelAttachWebViewToContainer(previousWebView)
                        RRDataStore.RRMainApplicationView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (RRDataStore.feedMixIsFirstCreate) {
            RRDataStore.feedMixIsFirstCreate = false
            RRDataStore.feedMixContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return RRDataStore.feedMixContainerView
        } else {
            return RRDataStore.feedMixContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (RRDataStore.RRMainApplicationViList.isEmpty()) {
            RRDataStore.RRMainApplicationView = RRMainApplicationVi(
                requireContext(),
                object :
                    RRCallBack {
                    override fun feedMixHandleCreateWebWindowRequest(RRMainApplicationVi: RRMainApplicationVi) {
                        RRDataStore.RRMainApplicationViList.add(
                            RRMainApplicationVi
                        )
                        RRDataStore.RRMainApplicationView =
                            RRMainApplicationVi
                        RRMainApplicationVi.eggLabelSetFileChooserHandler { callback ->
                            eggLabelHandleFileChooser(callback)
                        }
                        eggLabelAttachWebViewToContainer(RRMainApplicationVi)
                    }

                },
                eggLabelWindow = requireActivity().window
            ).apply {
                eggLabelSetFileChooserHandler { callback ->
                    eggLabelHandleFileChooser(callback)
                }
            }
            RRDataStore.RRMainApplicationView.eggLabelFLoad(
                arguments?.getString(RRSplashFragment.FEED_MIX_D) ?: ""
            )
            RRDataStore.RRMainApplicationViList.add(RRDataStore.RRMainApplicationView)
            eggLabelAttachWebViewToContainer(RRDataStore.RRMainApplicationView)
        } else {
            RRDataStore.RRMainApplicationViList.forEach { webView ->
                webView.eggLabelSetFileChooserHandler { callback ->
                    eggLabelHandleFileChooser(callback)
                }
            }
            RRDataStore.RRMainApplicationView =
                RRDataStore.RRMainApplicationViList.last()

            eggLabelAttachWebViewToContainer(RRDataStore.RRMainApplicationView)
        }
    }

    private fun eggLabelHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        eggLabelFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    eggLabelTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                1 -> {
                    eggLabelPhoto = RRMainViFun.eggLabelSavePhoto()
                    eggLabelTakePhoto.launch(eggLabelPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                callback?.onReceiveValue(null)
                eggLabelFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun eggLabelAttachWebViewToContainer(w: RRMainApplicationVi) {
        RRDataStore.feedMixContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            RRDataStore.feedMixContainerView.removeAllViews()
            RRDataStore.feedMixContainerView.addView(w)
        }
    }


}