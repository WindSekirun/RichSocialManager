package com.github.windsekirun.richsocialmanager.sns

import android.content.Context
import com.github.windsekirun.richsocialmanager.RSocialManager
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.tweetcomposer.ComposerActivity
import java.util.*


/**
 * RichSocialManager
 * Class: ${FILE_NAME}
 * Created by winds on 2017-07-31.
 */
class RTwitter constructor(val context: Context) : Observer {
    private var callbackListener: OnPostCallbackListener? = null

    override fun update(observable: Observable?, data: Any?) {
        val resultCode = data as? Int
        if (resultCode is Int) {
            if (resultCode == RSocialManager.POST_SUCCESS) {
                if (callbackListener != null) {
                    callbackListener?.onSuccess(RSocialManager.POST_SUCCESS)
                }
            } else {
                if (callbackListener != null) {
                    callbackListener?.onSuccess(RSocialManager.POST_FAILED)
                }
            }
        }
    }

    /**
     * Set callback on result code of Sending kakaolink
     */
    fun setOnCallbackListener(callback: (Int) -> Unit) {
        this.callbackListener = object : OnPostCallbackListener {
            override fun onSuccess(resultCode: Int) {
                callback.invoke(resultCode)
            }
        }
    }

    /**
     * post into Twitter Api
     */
    fun postCompose(content: String) {
        val session = TwitterCore.getInstance().sessionManager.activeSession
        val intent = ComposerActivity.Builder(context)
                .session(session)
                .text(content)
                .createIntent()

        context.startActivity(intent)
    }

    interface OnPostCallbackListener {
        fun onSuccess(resultCode: Int)
    }

}