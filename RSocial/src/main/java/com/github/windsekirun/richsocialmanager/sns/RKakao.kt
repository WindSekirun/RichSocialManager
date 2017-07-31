package com.github.windsekirun.richsocialmanager.sns

import android.content.Context
import com.github.windsekirun.richsocialmanager.RSocialManager
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.ButtonObject
import com.kakao.message.template.ContentObject
import com.kakao.message.template.FeedTemplate
import com.kakao.message.template.LinkObject
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.LogoutResponseCallback
import com.kakao.usermgmt.callback.MeResponseCallback
import com.kakao.usermgmt.response.model.UserProfile
import com.kakao.util.exception.KakaoException
import org.json.JSONObject
import pyxis.uzuki.live.richutilskt.utils.put


/**
 * RichSocialManager
 * Class: RKakao
 * Created by winds on 2017-07-31.
 */
class RKakao constructor(var context: Context) {
    private val callback: SessionCallback
    private var loginListener: OnLoginListener? = null
    private var callbackListener: OnPostCallbackListener? = null

    init {
        callback = SessionCallback()
    }

    /**
     * add callback of Kakao Api
     */
    fun addCallback() {
        Session.getCurrentSession().addCallback(callback)
    }

    /**
     * remove callback of Kakao Api
     */
    fun removeCallback() {
        Session.getCurrentSession().removeCallback(callback)
    }

    /**
     * logout from Kakao Api
     */
    fun logOut() {
        UserManagement.requestLogout(object : LogoutResponseCallback() {
            override fun onCompleteLogout() {
            }
        })
    }

    /**
     * Set LoginListener on Kakao Api
     */
    fun setOnLoginListener(callback: (JSONObject) -> Unit) {
        this.loginListener = object : OnLoginListener {
            override fun onLogin(jObject: JSONObject) {
                callback.invoke(jObject)
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
     * Return as Session is opened.
     */
    fun isOpened(): Boolean {
        return Session.getCurrentSession().isOpened
    }

    /**
     * post kakaoLink
     */
    fun postKakaoLink(title: String, message: String, imageUrl: String,  buttonTitle: String, buttonUrl: String) {
        val linkObject = LinkObject.newBuilder()
                .build()
        val buttonLinkObject = LinkObject.newBuilder()
                .setWebUrl(buttonUrl)
                .setMobileWebUrl(buttonUrl)
                .setAndroidExecutionParams("execparamkey1=1111")
                .setIosExecutionParams("execparamkey1=1111")
                .build()
        val contentObject = ContentObject.newBuilder(title, imageUrl, linkObject)
                .setDescrption(message)
        val buttonObject = ButtonObject(buttonTitle, buttonLinkObject)

        val template = FeedTemplate.newBuilder(contentObject.build())
                .addButton(buttonObject)
                .build()

        KakaoLinkService.getInstance().sendDefault(context, template, object : ResponseCallback<KakaoLinkResponse>() {
            override fun onFailure(errorResult: ErrorResult) {
                if (callbackListener != null) {
                    callbackListener?.onSuccess(RSocialManager.POST_FAILED)
                }
            }

            override fun onSuccess(result: KakaoLinkResponse) {
                if (callbackListener != null) {
                    callbackListener?.onSuccess(RSocialManager.POST_SUCCESS)
                }
            }
        })
    }

    private fun requestMe() {
        UserManagement.requestMe(object : MeResponseCallback() {
            override fun onFailure(errorResult: ErrorResult?) {
                val message = "failed to get user info. msg=${errorResult?.errorMessage}"
                println(message)
            }

            override fun onSessionClosed(errorResult: ErrorResult) {}

            override fun onSuccess(userProfile: UserProfile) {
                System.out.println("userProfile.getId() : ${userProfile.id}")
                System.out.println("userProfile.getNickname() : ${userProfile.nickname}")

                val userObj = JSONObject()
                put(userObj, "id", userProfile.id)
                put(userObj, "name", userProfile.nickname)

                if (loginListener != null) {
                    (loginListener as OnLoginListener).onLogin(userObj)
                }
            }

            override fun onNotSignedUp() {}
        })
    }

    private inner class SessionCallback : ISessionCallback {

        override fun onSessionOpened() {
            requestMe()
        }

        override fun onSessionOpenFailed(exception: KakaoException?) {
            if (exception != null) {
                println("exception : ${exception.message}")
            }

        }
    }

    interface OnLoginListener {
        fun onLogin(jObject: JSONObject)
    }

    interface OnPostCallbackListener {
        fun onSuccess(resultCode: Int)
    }
}