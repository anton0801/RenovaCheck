package com.apprenova.renovacheck

import android.R
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.apprenova.renovacheck.data.handlers.RRNotifPushHandle
import com.apprenova.renovacheck.databinding.ActivityMainStartBinding
import com.apprenova.renovacheck.ui.components.layout.RRGlLayoutUtils
import com.apprenova.renovacheck.ui.components.layout.setupSystemBars
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class MainStartActivity : AppCompatActivity() {

    private val renovaCheckPushHandler by inject<RRNotifPushHandle>()

    private lateinit var binding: ActivityMainStartBinding

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupSystemBars()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val feedMixRootView = findViewById<View>(R.id.content)
        RRGlLayoutUtils().feedMixAssistActivity(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        ViewCompat.setOnApplyWindowInsetsListener(feedMixRootView) { feedMixView, feedMixInsets ->
            val feedMixSystemBars = feedMixInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val feedMixDisplayCutout =
                feedMixInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val feedMixIme = feedMixInsets.getInsets(WindowInsetsCompat.Type.ime())
            val feedMixTopPadding =
                kotlin.comparisons.maxOf(feedMixSystemBars.top, feedMixDisplayCutout.top)
            val feedMixLeftPadding =
                kotlin.comparisons.maxOf(feedMixSystemBars.left, feedMixDisplayCutout.left)
            val feedMixRightPadding =
                kotlin.comparisons.maxOf(feedMixSystemBars.right, feedMixDisplayCutout.right)
            window.setSoftInputMode(MainApplication.batchiungInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                val feedMixBottomInset =
                    kotlin.comparisons.maxOf(feedMixSystemBars.bottom, feedMixDisplayCutout.bottom)
                feedMixView.setPadding(
                    feedMixLeftPadding,
                    feedMixTopPadding,
                    feedMixRightPadding,
                    0
                )
                feedMixView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = feedMixBottomInset
                }
            } else {
                val feedMixBottomInset =
                    kotlin.comparisons.maxOf(
                        feedMixSystemBars.bottom,
                        feedMixDisplayCutout.bottom,
                        feedMixIme.bottom
                    )
                feedMixView.setPadding(
                    feedMixLeftPadding,
                    feedMixTopPadding,
                    feedMixRightPadding,
                    0
                )
                feedMixView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = feedMixBottomInset
                }
            }
            WindowInsetsCompat.CONSUMED
        }
        renovaCheckPushHandler.batchAppHandlePush(intent.extras)

        lifecycleScope.launch {
            feedMixRetriveDeviceGaid()
        }
    }

    suspend fun feedMixRetriveDeviceGaid(): String = withContext(Dispatchers.IO) {
        val gaid = AdvertisingIdClient.getAdvertisingIdInfo(this@MainStartActivity).id
            ?: "00000000-0000-0000-0000-000000000000"
        Log.d("MAIN_APP_TAG", "Gaid: $gaid")
        return@withContext gaid
    }

    override fun onResume() {
        super.onResume()
        setupSystemBars()
    }

}