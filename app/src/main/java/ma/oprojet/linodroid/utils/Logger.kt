package ma.oprojet.linodroid.utils

import ma.oprojet.linodroid.BuildConfig


object Logger {

    private const val DEFAULT_TAG = "LINODROID"

    fun d(tag: String = DEFAULT_TAG, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(tag, message)
        }
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            android.util.Log.e(tag, message, throwable)
        } else {
            android.util.Log.e(tag, message)
        }
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(tag, message)
        }
    }

    fun w(tag: String = DEFAULT_TAG, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(tag, message)
        }
    }
}