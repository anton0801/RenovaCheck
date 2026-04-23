package com.apprenova.renovacheck.data.domain.model

import com.google.gson.annotations.SerializedName

private const val FEED_MIX_A = "com.apprenova.renovacheck"
private const val FEED_MIX_B = "renovacheck"


data class BSPMainParam (
    @SerializedName("af_id")
    val rrAfId: String,
    @SerializedName("bundle_id")
    val feedMixBundleId: String = FEED_MIX_A,
    @SerializedName("os")
    val feedMixOs: String = "Android",
    @SerializedName("store_id")
    val feedMixStoreId: String = FEED_MIX_A,
    @SerializedName("locale")
    val rrLocale: String,
    @SerializedName("push_token")
    val rrPushToken: String,
    @SerializedName("firebase_project_id")
    val feedMixFirebaseProjectId: String = FEED_MIX_B,
)
data class BSPEntity (
    @SerializedName("ok")
    val feedMixOk: String,
    @SerializedName("url")
    val feedMixUrl: String,
    @SerializedName("expires")
    val feedMixExpires: Long,
)