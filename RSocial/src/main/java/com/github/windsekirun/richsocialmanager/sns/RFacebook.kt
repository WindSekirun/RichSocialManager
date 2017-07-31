package com.github.windsekirun.richsocialmanager.sns

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.share.Sharer
import com.facebook.share.widget.ShareDialog
import com.github.windsekirun.richsocialmanager.RSocialManager
import org.json.JSONObject
import pyxis.uzuki.live.richutilskt.utils.getJSONString
import pyxis.uzuki.live.richutilskt.utils.put
import java.util.*
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent


/**
 * RichSocialManager
 * Class: RFacebook
 * Created by winds on 2017-07-31.
 */
class RFacebook constructor(val activity: Activity) {
    private var onLoginListener: OnLoginListener? = null
    private var callbackListener: OnPostCallbackListener? = null

    /**
     * Login from facebook api
     */
    fun login(callbackManager: CallbackManager) {
        LoginManager.getInstance().logInWithReadPermissions(activity,
                Arrays.asList("public_profile", "email", "user_friends"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult) {
                getUserInfo(result)
            }

            override fun onError(error: FacebookException) {
                println("error : " + error)
            }

            override fun onCancel() {}
        })
    }

    /**
     * logout from facebook api
     */
    fun logOut() {
        LoginManager.getInstance().logOut()
    }

    /**
     * check current state has proper permission
     */
    fun hasPermission(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null
    }

    /**
     * check current state has publish permission
     */
    fun hasPublishPermission(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken == null) {
            return false
        } else {
            val permission = AccessToken.getCurrentAccessToken().permissions
            return permission.contains("publish_actions")
        }
    }

    /**
     * set login listener from facebook api
     */
    fun setOnLoginListener(callback: (JSONObject) -> Unit) {
        this.onLoginListener = object : OnLoginListener {
            override fun onLogin(jsonStr: JSONObject) {
                callback.invoke(jsonStr)
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

    fun postFacebook(content: String, bitmap: Bitmap, callbackManager: CallbackManager) {
        val shareDialog = ShareDialog(activity)
        shareDialog.registerCallback(callbackManager, object : FacebookCallback<Sharer.Result> {
            override fun onError(error: FacebookException?) {
                if (callbackListener != null) {
                    callbackListener?.onSuccess(RSocialManager.POST_FAILED)
                }
            }

            override fun onCancel() {
                if (callbackListener != null) {
                    callbackListener?.onSuccess(RSocialManager.POST_FAILED)
                }
            }

            override fun onSuccess(result: Sharer.Result?) {
                if (callbackListener != null) {
                    callbackListener?.onSuccess(RSocialManager.POST_SUCCESS)
                }
            }
        })

        if (ShareDialog.canShow(SharePhotoContent ::class.java)) {
            val photo = SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build()

            val content = SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build()
            shareDialog.show(content)
        }
    }

    private fun getUserInfo(result: LoginResult) {
        val callBack = GraphRequest.GraphJSONObjectCallback {
            jsonObject, response ->
            if (jsonObject == null)
                return@GraphJSONObjectCallback
            val id = jsonObject.getJSONString("id")
            val name = jsonObject.getJSONString("name")
            val email = jsonObject.getJSONString("email")

            val userObj = JSONObject()
            put(userObj, "id", id)
            put(userObj, "name", name)
            put(userObj, "email", email)

            if (onLoginListener != null) {
                onLoginListener?.onLogin(userObj)
                return@GraphJSONObjectCallback
            }
        }

        val request = GraphRequest.newMeRequest(result.accessToken, callBack)
        val parameters = Bundle()
        parameters.putString("fields", "id, name, email, gender, birthday, picture")
        request.parameters = parameters
        request.executeAsync()
    }

    interface OnLoginListener {
        fun onLogin(jsonStr: JSONObject)
    }

    interface OnPostCallbackListener {
        fun onSuccess(resultCode: Int)
    }
}