package com.github.windsekirun.richsocialmanager.sns

import android.content.Context

import com.kakao.auth.ApprovalType
import com.kakao.auth.AuthType
import com.kakao.auth.IApplicationConfig
import com.kakao.auth.ISessionConfig
import com.kakao.auth.KakaoAdapter

class KakaoSDKAdapter(private val context: Context) : KakaoAdapter() {

    override fun getSessionConfig(): ISessionConfig {

        return object : ISessionConfig {
            override fun getAuthTypes(): Array<AuthType> {
                return arrayOf(AuthType.KAKAO_LOGIN_ALL)
            }

            override fun isUsingWebviewTimer(): Boolean {
                return false
            }

            override fun isSecureMode(): Boolean {
                return false
            }

            override fun getApprovalType(): ApprovalType {
                return ApprovalType.INDIVIDUAL
            }

            override fun isSaveFormData(): Boolean {
                return true
            }
        }

    }

    override fun getApplicationConfig(): IApplicationConfig {
        return IApplicationConfig { context.applicationContext }
    }
}