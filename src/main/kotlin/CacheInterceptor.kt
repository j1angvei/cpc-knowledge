package cn.j1angvei

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.File

class CacheInterceptor(private val useLocalCache: Boolean) : Interceptor {

    companion object {
        const val MSG_CACHE = "LocalCache"

        private val cacheDir = File(System.getProperty("user.dir") + File.separator + "cache")
    }

    init {
        if (cacheDir.exists() && cacheDir.isFile) {
            cacheDir.delete()
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 使用 URL 的 encodedPath 作为缓存文件名，避免非法字符问题
        val key = request.url.encodedPath.replace("/", "_")
        val cacheFile = File(cacheDir.absolutePath + File.separator + key + ".html")

        // 如果缓存文件存在且不为空，直接返回缓存内容
        if (useLocalCache && cacheFile.exists() && cacheFile.length() > 0) {
            println("use cache for $key")
            val cachedContent = cacheFile.readText()
            return Response.Builder().request(request)
                .protocol(chain.connection()?.protocol() ?: okhttp3.Protocol.HTTP_1_1).code(200).message(MSG_CACHE)
                .body(cachedContent.toResponseBody(null)).build()
        }

        // 没有缓存文件时，发起网络请求
        val response = chain.proceed(request)
        val body = response.body

        // 如果响应成功，保存内容到缓存文件
        if (response.isSuccessful && body != null) {
            val responseBody = body.string()

            // 保存到缓存文件
            cacheFile.writeText(responseBody)

            // 返回新的 Response，重新构建 body
            return response.newBuilder().body(responseBody.toResponseBody(body.contentType())).build()
        }

        return response
    }
}