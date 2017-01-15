package hr.fer.zemris.sandworm.packer

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDateTime

class RemoteLogger(val endpoint: String) {

    companion object {
        const val PARAM_TAG = "tag"
        const val PARAM_CONTENT = "content"
        const val PARAM_TIMESTAMP = "timestamp"
    }

    private val client = OkHttpClient()

    fun log(tag: String, message: String) {
        val request = Request.Builder()
                .url("$endpoint/packer-test")
                .post(FormBody.Builder()
                        .add(PARAM_TAG, tag)
                        .add(PARAM_CONTENT, message)
                        .add(PARAM_TIMESTAMP, LocalDateTime.now().toString())
                        .build()
                )
                .build()

        client.newCall(request).execute()
    }

}
