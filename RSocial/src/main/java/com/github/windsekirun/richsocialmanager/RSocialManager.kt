package com.github.windsekirun.richsocialmanager

import android.content.Context
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig

/**
 * RichSocialManager
 * Class: RSocialManager
 * Created by winds on 2017-07-31.
 */

class RSocialManager constructor(var context: Context) {

    fun initializeApplication(consumerKey: String, consumerSecret: String) {
        val config = TwitterConfig.Builder(context)
                .twitterAuthConfig(TwitterAuthConfig(consumerKey, consumerSecret))
                .build()
        Twitter.initialize(config)
    }

    companion object {
        @JvmField val POST_SUCCESS = 0
        @JvmField val POST_FAILED = 1
    }
}