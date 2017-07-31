package com.github.windsekirun.richsocialmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.github.windsekirun.richsocialmanager.RSocialManager
import com.twitter.sdk.android.tweetcomposer.TweetUploadService

class TwitterResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (TweetUploadService.UPLOAD_SUCCESS == intent.action) {
            ObservableObject.instance.updateValue(RSocialManager.POST_SUCCESS)
        } else {
            ObservableObject.instance.updateValue(RSocialManager.POST_FAILED)
        }
    }
}