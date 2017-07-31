package com.github.windsekirun.richsocialmanager.sns

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.github.windsekirun.richsocialmanager.RSocialManager
import com.github.windsekirun.richsocialmanager.receiver.ObservableObject
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.tweetcomposer.ComposerActivity
import java.util.*


/**
 * RichSocialManager
 * Class: ${FILE_NAME}
 * Created by winds on 2017-07-31.
 */
class RTwitter constructor(val activity: Activity) : Observer {
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

    fun login(twitterAuthClient: TwitterAuthClient, callback: () -> Unit) {
        twitterAuthClient.authorize(activity, object : Callback<TwitterSession>() {
            override fun failure(exception: TwitterException?) {
                Log.e("failureTwitter", exception?.message)
            }

            override fun success(result: Result<TwitterSession>?) {
                callback.invoke()
            }
        })
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
    fun postTwitter(content: String, imageUrl: Uri? = null) {
        ObservableObject.instance.addObserver(this)
        val session = TwitterCore.getInstance().sessionManager.activeSession
        val intent = ComposerActivity.Builder(activity)
                .session(session)
                .text(content)
                .image(imageUrl)
                .createIntent()

        activity.startActivity(intent)
    }

    interface OnPostCallbackListener {
        fun onSuccess(resultCode: Int)
    }

}