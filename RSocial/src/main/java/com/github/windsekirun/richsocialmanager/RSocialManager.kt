package com.github.windsekirun.richsocialmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.github.windsekirun.richsocialmanager.sns.RFacebook
import com.github.windsekirun.richsocialmanager.sns.RKakao
import com.github.windsekirun.richsocialmanager.sns.RNaver
import com.github.windsekirun.richsocialmanager.sns.RTwitter
import com.kakao.auth.Session
import com.kakao.usermgmt.LoginButton
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import org.json.JSONObject


/**
 * RichSocialManager
 * Class: RSocialManager
 * Created by winds on 2017-07-31.
 */

class RSocialManager constructor(val activity: Activity, oAuthClientId: String = "", oAuthClientSecret: String = "", clientName: String = "") {
    private val facebookApi: RFacebook = RFacebook(activity)
    private val kakaoApi: RKakao = RKakao(activity)
    private val naverApi: RNaver = RNaver(activity, oAuthClientId, oAuthClientSecret, clientName)
    private val twitterApi: RTwitter = RTwitter(activity)

    private lateinit var loginListener: OnLoginListener
    private lateinit var postListener: OnPostCallbackListener
    private lateinit var facebookCallbackManager: CallbackManager

    /**
     * Call this method in Activity.onCreate()
     */
    fun init(loginListener: OnLoginListener, postCallbackListener: OnPostCallbackListener) {
        this.loginListener = loginListener
        this.postListener = postCallbackListener

        facebookCallbackManager = CallbackManager.Factory.create()

        kakaoApi.setOnLoginListener { loginListener.onLoginKakao(it) }
        kakaoApi.setOnCallbackListener { postCallbackListener.onKakaoResult(it) }
        facebookApi.setOnLoginListener { loginListener.onLoginFacebook(it) }
        facebookApi.setOnCallbackListener { postCallbackListener.onFacebookResult(it) }
        naverApi.setLoginListener { loginListener.onLoginNaver(it) }
        twitterApi.setOnCallbackListener { postCallbackListener.onTwitterResult(it) }
    }

    /**
     * Call this method in Activity.onResume()
     */
    fun onResume() {
        kakaoApi.addCallback()
    }

    /**
     * Call this method in Activity.onPause()
     */
    fun onPause() {
        kakaoApi.removeCallback()
    }

    /**
     * Call this method in Activity.onActivityResult()
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return
        }

        if (FacebookSdk.isFacebookRequestCode(requestCode)) {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
            return
        }
    }

    /**
     * perform Login using Kakao / Facebook / Naver Api
     *
     * @param[api] name of Api
     */
    fun login(api: String) {
        when (api) {
            KAKAO -> loginAsKakao()
            FACEBOOK -> loginAsFacebook()
            NAVER -> loginAsNaver()
        }
    }

    private fun loginAsKakao() {
        val btnKakao = LoginButton(activity)
        btnKakao.performClick()
    }

    private fun loginAsFacebook() {
        facebookApi.login(facebookCallbackManager)
    }

    private fun loginAsNaver() {
        naverApi.login(activity)
    }

    /**
     * perform Post using Kakao Api
     */
    fun postAsKakao(title: String, imageUrl: String, message: String, buttonTitle: String, buttonUrl: String) {
        kakaoApi.postKakaoLink(title, imageUrl, message, buttonTitle, buttonUrl)
    }

    /**
     * perform Post using Facebook Api
     */
    fun postAsFacebook(content: String, bitmap: Bitmap) {
        facebookApi.postFacebook(content, bitmap, facebookCallbackManager)
    }

    /**
     * perform Post using Twitter Api
     */
    fun postAsTwitter(content: String) {
        twitterApi.postTwitter(content)
    }

    companion object {
        @JvmField val POST_SUCCESS = 0
        @JvmField val POST_FAILED = 1

        @JvmField val FACEBOOK = "facebook"
        @JvmField val KAKAO = "kakao"
        @JvmField val NAVER = "naver"
        @JvmField val TWITTER = "twitter"

        @JvmStatic fun initializeApplication(context: Context, consumerKey: String, consumerSecret: String) {
            val config = TwitterConfig.Builder(context)
                    .twitterAuthConfig(TwitterAuthConfig(consumerKey, consumerSecret))
                    .build()
            Twitter.initialize(config)
        }
    }

    interface OnLoginListener {
        fun onLoginKakao(jObject: JSONObject)
        fun onLoginFacebook(jObject: JSONObject)
        fun onLoginNaver(jObject: JSONObject)
    }

    interface OnPostCallbackListener {
        fun onKakaoResult(resultCode: Int)
        fun onFacebookResult(resultCode: Int)
        fun onTwitterResult(resultCode: Int)
    }
}