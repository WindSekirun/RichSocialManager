package com.github.windsekirun.richsocialmanager.sns

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.github.windsekirun.richsocialmanager.RSocialManager
import com.twitter.sdk.android.tweetcomposer.TweetComposer
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
    fun postTwitter(content: String, imageUrl: String = "") {
        val builder = TweetComposer.Builder(context)
                .text(content)

        if (!TextUtils.isEmpty(imageUrl))
            builder.image(Uri.parse(imageUrl))

        builder.show()
    }

    interface OnPostCallbackListener {
        fun onSuccess(resultCode: Int)
    }

}