package com.github.windsekirun.richsocialmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.github.windsekirun.richsocialmanager.sns.*
import com.kakao.auth.KakaoSDK
import com.kakao.auth.Session
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import org.json.JSONObject
import pyxis.uzuki.live.richutilskt.utils.confirm


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
    private val twitterAuthClient = TwitterAuthClient()

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

        twitterAuthClient.onActivityResult(requestCode, resultCode, data)
    }

    /*
     * Limitation of RSocialManager
     * Kakao api make must use LoginButton.
     * so, i find other solution..
     */

    /**
     * perform Login using  Facebook / Naver Api
     *
     * @param[api] name of Api
     */
    fun login(api: String) {
        when (api) {
            FACEBOOK -> loginAsFacebook()
            NAVER -> loginAsNaver()
        }
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
    fun postAsKakao(title: String, message: String, imageUrl: String, buttonTitle: String, buttonUrl: String) {
        if (isAppInstalled(KAKAO_PACKAGE)) {
            kakaoApi.postKakaoLink(title, imageUrl, message, buttonTitle, buttonUrl)
        } else {
            activity.confirm(message = "You don't have KakaoTalk App. Do you want to go Play Store?", callback = {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("market://details?id=$KAKAO_PACKAGE")
                activity.startActivity(intent)
            })
        }
    }

    /**
     * perform Post using Facebook Api
     */
    fun postAsFacebook(content: String, bitmap: Bitmap) {
        if (isAppInstalled(FACEBOOK_PACKAGE)) {
            facebookApi.postFacebook(content, bitmap, facebookCallbackManager)
        } else {
            activity.confirm(message = "You don't have Facebook App. Do you want to go Play Store?", callback = {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("market://details?id=$FACEBOOK_PACKAGE")
                activity.startActivity(intent)
            })
        }
    }

    /**
     * perform Post using Twitter Api
     */
    @JvmOverloads fun postAsTwitter(content: String, imageUrl: Uri? = null) {
        twitterApi.login(twitterAuthClient, {
            twitterApi.postTwitter(content, imageUrl)
        })
    }

    fun isAppInstalled(packageName: String): Boolean {
        try {
            activity.packageManager.getApplicationInfo(packageName, 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

    }

    companion object {
        @JvmField val POST_SUCCESS = 0
        @JvmField val POST_FAILED = -1

        @JvmField val FACEBOOK = "facebook"
        @JvmField val KAKAO = "kakao"
        @JvmField val NAVER = "naver"
        @JvmField val TWITTER = "twitter"

        @JvmField val FACEBOOK_PACKAGE = "com.facebook.katana"
        @JvmField val KAKAO_PACKAGE = "com.kakao.talk"

        @JvmStatic fun initializeApplication(context: Context, consumerKey: String, consumerSecret: String) {
            // initialize kakao sdk
            KakaoSDK.init(KakaoSDKAdapter(context))

            // initialize twitter sdk
            val config = TwitterConfig.Builder(context)
                    .twitterAuthConfig(TwitterAuthConfig(consumerKey, consumerSecret))
                    .build()
            Twitter.initialize(config)

            // initialize facebook sdk
            FacebookSdk.sdkInitialize(context)
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