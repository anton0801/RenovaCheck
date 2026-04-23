package com.apprenova.renovacheck.data.domain.usecases

import com.apprenova.renovacheck.data.domain.data.RRRepositoriesImpl
import com.apprenova.renovacheck.data.domain.data.RRPushTokenUC
import com.apprenova.renovacheck.data.domain.data.RRSystemsSerereI
import com.apprenova.renovacheck.data.domain.model.BSPEntity
import com.apprenova.renovacheck.data.domain.model.BSPMainParam

class BSPAllUseCaseInApplication(
    private val RRRepositoriesImpl: RRRepositoriesImpl,
    private val RRSystemsSerereI: RRSystemsSerereI,
    private val RRPushTokenUC: RRPushTokenUC,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?): BSPEntity? {
        val params = BSPMainParam(
            rrLocale = RRSystemsSerereI.getLocaleOfUserFeedMix(),
            rrPushToken = RRPushTokenUC.batchManagerGetToken(),
            rrAfId = RRSystemsSerereI.getAppsflyerIdForApp()
        )
        return RRRepositoriesImpl.RRAppObtainClie(params, conversion)
    }


}