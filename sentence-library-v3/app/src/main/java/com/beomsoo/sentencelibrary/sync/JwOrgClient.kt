package com.beomsoo.sentencelibrary.sync

import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class JwOrgClient(
    private val client:OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15,TimeUnit.SECONDS)
        .readTimeout(25,TimeUnit.SECONDS)
        .followRedirects(false)
        .build()
) {
    suspend fun fetch(url:String):String {
        require(OfficialHostPolicy.isAllowed(url)) { "허용되지 않은 공식 자료 주소입니다." }
        var last:Throwable?=null
        repeat(3) { attempt ->
            try { return fetchOnce(url) } catch(t:Throwable) {
                last=t
                if(attempt<2) delay(if(attempt==0) 700 else 1800)
            }
        }
        throw last ?: IllegalStateException("공식 자료 요청 실패")
    }

    private suspend fun fetchOnce(initial:String):String = withContext(Dispatchers.IO) {
        var current=initial
        repeat(6) {
            require(OfficialHostPolicy.isAllowed(current)) { "공식 허용 도메인을 벗어난 리디렉션입니다." }
            val req=Request.Builder().url(current).header("User-Agent","SentenceLibrary/3.0 Android").build()
            client.newCall(req).execute().use { response ->
                if(response.code in 300..399) {
                    val loc=response.header("Location") ?: error("리디렉션 위치 없음")
                    current=URI(current).resolve(loc).toString()
                    return@use
                }
                if(!response.isSuccessful) error("HTTP ${response.code}")
                return@withContext response.body?.string() ?: error("빈 응답")
            }
        }
        error("리디렉션 횟수 초과")
    }
}
