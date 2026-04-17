package com.yuncodelab.sku.core.utils

import android.util.Log

/**
 * SKU 模块日志工具
 */
object SkuLogger {

    /** 是否开启调试日志 */
    var DEBUG = true

    private const val TAG = "SKU_DEMO"

    fun d(tag: String = TAG, msg: String) {
        if (DEBUG) {
            if (isUnitTest()) {
                println("D/$tag: $msg")
            } else {
                Log.d(tag, msg)
            }
        }
    }

    fun i(tag: String = TAG, msg: String) {
        if (DEBUG) {
            if (isUnitTest()) {
                println("I/$tag: $msg")
            } else {
                Log.i(tag, msg)
            }
        }
    }

    fun e(tag: String = TAG, msg: String, throwable: Throwable? = null) {
        if (isUnitTest()) {
            System.err.println("E/$tag: $msg")
            throwable?.printStackTrace()
        } else {
            Log.e(tag, msg, throwable)
        }
    }

    private fun isUnitTest(): Boolean {
        return try {
            Class.forName("org.junit.Test")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}