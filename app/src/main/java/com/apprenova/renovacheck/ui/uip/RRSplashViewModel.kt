package com.apprenova.renovacheck.ui.uip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apprenova.renovacheck.data.BatchManagerAppsFlyerState
import com.apprenova.renovacheck.data.domain.data.RRSystemsSerereI
import com.apprenova.renovacheck.data.domain.usecases.BSPAllUseCaseInApplication
import com.apprenova.renovacheck.data.handlers.RROnDeviceStoreManager
import com.apprenova.renovacheck.MainApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RRSplashViewModel(
    private val BSPAllUseCaseInApplication: BSPAllUseCaseInApplication,
    private val RROnDeviceStoreManager: RROnDeviceStoreManager,
    private val RRSystemsSerereI: RRSystemsSerereI
) : ViewModel() {

    private val _chickHealthHomeScreenState: MutableStateFlow<FeedMixHomeScreenState> =
        MutableStateFlow(FeedMixHomeScreenState.FeedMixLoading)
    val chickHealthHomeScreenState = _chickHealthHomeScreenState.asStateFlow()

    private var eggLabelGetApps = false

    init {
        viewModelScope.launch {
            when (RROnDeviceStoreManager.batchAppState) {
                0 -> {
                    if (RRSystemsSerereI.feedMixCheckInternetConnection()) {
                        MainApplication.BatchingManagerConversionFlow.collect {
                            when (it) {
                                BatchManagerAppsFlyerState.BatchManagerDefault -> {}
                                BatchManagerAppsFlyerState.BatchManagerError -> {
                                    RROnDeviceStoreManager.batchAppState = 2
                                    _chickHealthHomeScreenState.value =
                                        FeedMixHomeScreenState.FeedMixError
                                    eggLabelGetApps = true
                                }

                                is BatchManagerAppsFlyerState.BatchManagerSuccess -> {
                                    if (!eggLabelGetApps) {
                                        feedMixGetData(it.feedMixxChickkData)
                                        eggLabelGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _chickHealthHomeScreenState.value =
                            FeedMixHomeScreenState.FeedMixNotInternet
                    }
                }

                1 -> {
                    if (RRSystemsSerereI.feedMixCheckInternetConnection()) {
                        if (MainApplication.BATCH_MANAGER_LI != null) {
                            _chickHealthHomeScreenState.value =
                                FeedMixHomeScreenState.FeedMixSuccess(
                                    MainApplication.BATCH_MANAGER_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > RROnDeviceStoreManager.batchExpired) {
                            Log.d(
                                MainApplication.MAIN_TAG,
                                "Current time more then expired, repeat request"
                            )
                            MainApplication.BatchingManagerConversionFlow.collect {
                                when (it) {
                                    BatchManagerAppsFlyerState.BatchManagerDefault -> {}
                                    BatchManagerAppsFlyerState.BatchManagerError -> {
                                        _chickHealthHomeScreenState.value =
                                            FeedMixHomeScreenState.FeedMixSuccess(
                                                RROnDeviceStoreManager.batchSavedUrl
                                            )
                                        eggLabelGetApps = true
                                    }

                                    is BatchManagerAppsFlyerState.BatchManagerSuccess -> {
                                        if (!eggLabelGetApps) {
                                            feedMixGetData(it.feedMixxChickkData)
                                            eggLabelGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(
                                MainApplication.MAIN_TAG,
                                "Current time less then expired, use saved url"
                            )
                            _chickHealthHomeScreenState.value =
                                FeedMixHomeScreenState.FeedMixSuccess(
                                    RROnDeviceStoreManager.batchSavedUrl
                                )
                        }
                    } else {
                        _chickHealthHomeScreenState.value =
                            FeedMixHomeScreenState.FeedMixNotInternet
                    }
                }

                2 -> {
                    _chickHealthHomeScreenState.value =
                        FeedMixHomeScreenState.FeedMixError
                }
            }
        }
    }


    private suspend fun feedMixGetData(conversation: MutableMap<String, Any>?) {
        val eggLabelData = BSPAllUseCaseInApplication.invoke(conversation)
        if (RROnDeviceStoreManager.batchAppState == 0) {
            if (eggLabelData == null) {
                RROnDeviceStoreManager.batchAppState = 2
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixError
            } else {
                RROnDeviceStoreManager.batchAppState = 1
                RROnDeviceStoreManager.apply {
                    batchExpired = eggLabelData.feedMixExpires
                    batchSavedUrl = eggLabelData.feedMixUrl
                }
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixSuccess(eggLabelData.feedMixUrl)
            }
        } else {
            if (eggLabelData == null) {
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixSuccess(RROnDeviceStoreManager.batchSavedUrl)
            } else {
                RROnDeviceStoreManager.apply {
                    batchExpired = eggLabelData.feedMixExpires
                    batchSavedUrl = eggLabelData.feedMixUrl
                }
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixSuccess(eggLabelData.feedMixUrl)
            }
        }
    }



}

sealed class FeedMixHomeScreenState {
    data object FeedMixLoading : FeedMixHomeScreenState()
    data object FeedMixError : FeedMixHomeScreenState()
    data class FeedMixSuccess(val data: String) : FeedMixHomeScreenState()
    data object FeedMixNotInternet : FeedMixHomeScreenState()
}