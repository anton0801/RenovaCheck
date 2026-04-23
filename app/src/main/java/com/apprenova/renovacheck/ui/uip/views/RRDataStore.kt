package com.apprenova.renovacheck.ui.uip.views

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class RRDataStore : ViewModel() {
    val RRMainApplicationViList: MutableList<RRMainApplicationVi> =
        mutableListOf()
    var feedMixIsFirstCreate = true

    @SuppressLint("StaticFieldLeak")
    lateinit var feedMixContainerView: FrameLayout

    @SuppressLint("StaticFieldLeak")
    lateinit var RRMainApplicationView: RRMainApplicationVi

}