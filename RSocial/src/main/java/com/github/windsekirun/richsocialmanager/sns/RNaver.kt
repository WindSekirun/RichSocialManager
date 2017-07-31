package com.github.windsekirun.richsocialmanager.sns

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import org.json.JSONObject
import pyxis.uzuki.live.richutilskt.utils.getJSONString
import pyxis.uzuki.live.richutilskt.utils.put


/**
 * RichSocialManager
 * Class: RNaver
 * Created by winds on 2017-07-31.
 */
class RNaver constructor(var context: Context, oAuthClientId: String, oAuthClientSecret: String, clientName: String) {
    private var mOAuthLoginModule: OAuthLogin = OAuthLogin.getInstance()
    private var listener: OnLoginListener? = null

    init {
        mOAuthLoginModule.init(context, oAuthClientId, oAuthClientSecret, clientName)
    }

    /**
     * login from Naver Api
     */
    fun login(activity: Activity) {
        mOAuthLoginModule.startOauthLoginActivity(activity, OAuthNaverLoginHandler())
    }

    /**
     * log out from Naver Api
     */
    fun logout() {
        mOAuthLoginModule.logout(context)
    }

    /**
     * Set login listener of Naver Api
     */
    fun setLoginListener(callback: (JSONObject) -> Unit) {
        this.listener = object: OnLoginListener {
            override fun onLogin(jObject: JSONObject) {
               callback.invoke(jObject)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private inner class OAuthNaverLoginHandler : OAuthLoginHandler() {
        override fun run(success: Boolean) {
            if (success) {
                val accessToken = mOAuthLoginModule.getAccessToken(context)
                val authHeader = "Bearer $accessToken"

                Fuel.get("https://openapi.naver.com/v1/nid/me").header("Authorization" to authHeader).responseJson({
                    req, res, result ->

                    result.fold({ d ->
                        val responseObject = d.obj().getJSONObject("response")
                        val id = responseObject.getJSONString("id")
                        val name = responseObject.getJSONString("name")
                        val email = responseObject.getJSONString("email")

                        val userObj = JSONObject()
                        put(userObj, "id", id)
                        put(userObj, "name", name)
                        put(userObj, "email", email)


                        if (listener != null) {
                            listener?.onLogin(userObj)
                        }
                    }, {})
                })
            }
        }
    }

    interface OnLoginListener {
        fun onLogin(jObject: JSONObject)
    }
}