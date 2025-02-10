package cn.j1angvei

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import kotlin.random.Random

private const val HOMEPAGE_URL = "https://www.12371.cn/special/cidian/#"

fun main(args: Array<String>) {
    val cacheFirst = args.isEmpty() || !args.contains("-f")
    val client = OkHttpClient.Builder().addInterceptor(CacheInterceptor(cacheFirst)).build()
    val text = StringBuilder()
    val homeDoc = requestUrl(client, HOMEPAGE_URL) ?: return
    val title = homeDoc.title()
    println("title $title")
    text.append("# $title").append("\n")
    val navList = homeDoc.select("div#page_body > div.page_wrap  ul > li > a[href]")
    navList.forEachIndexed { index, nav ->
        val navLink = nav.attr("href")
        parseSection(client, navLink, index, text)
        Thread.sleep(Random.nextLong(500, 1000))
    }
    val file = File(System.getProperty("user.dir") + File.separator + "党务知识.md")
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    file.writeText(text.toString())
}

fun parseSection(client: OkHttpClient, link: String, idx: Int, text: StringBuilder) {
    println("start for $link")
    val doc = requestUrl(client, link) ?: return
    val title = doc.select("div#page_body > div.page_wrap div.dyw1027new-content-container div > h4").text()
    println("section title $title")
    text.append("\n\n").append("## ").append(toChinese(idx + 1)).append(". ").append(title).append("\n")

    val subSections = doc.select("div.dyw1027new-content-container section.dyw1027new-catalogue-card")
    if (subSections.isNotEmpty()) {
        subSections.forEachIndexed { _, element ->
            val subTitle = element.select("h5.dyw1027new-catalogue-title-small").text()
            println("subtitle $subTitle")
            text.append("\n").append("## ").append(subTitle).append("\n")
            val paragraphs = element.select("ul.dyw1027new-catalogue-list > li > span.title > a[href]")
            paragraphs.forEachIndexed { pIdx, p ->
                val pLink = p.attr("href")
                val pTitle = p.text()
                println("p link $pLink, p title $pTitle")
                parseParagraph(client, pLink, pIdx, pTitle, text)
                Thread.sleep(Random.nextLong(300, 700))
            }
        }
    } else {
        val paragraphs = doc.select("div#page_body > div.page_wrap div > ul > li > span.title > a[href]")
        paragraphs.forEachIndexed { pIdx, p ->
            val pLink = p.attr("href")
            val pTitle = p.text()
            println("p link $pLink, p title $pTitle")
            parseParagraph(client, pLink, pIdx, pTitle, text)
            Thread.sleep(Random.nextLong(300, 700))
        }
    }
    text.append("\n").append("\n")
}

fun parseParagraph(client: OkHttpClient, link: String, idx: Int, title: String, text: StringBuilder) {
    text.append("\n").append("### ").append(idx + 1).append(". ").append(title).append("\n")
    val subList = requestUrl(client, link)?.select("div.page_wrap p") ?: return
    var len = 0
    subList.forEach {
        val visibleText = it.text()
        text.append(visibleText).append("  ").append("\n")
        len += visibleText.length
    }
    println("sub paragraph size ${subList.size}, content size $len")
    text.append("\n")
}

fun toChinese(num: Int): String {
    return when (num) {
        1 -> "一"
        2 -> "二"
        3 -> "三"
        4 -> "四"
        5 -> "五"
        6 -> "六"
        7 -> "七"
        8 -> "八"
        9 -> "九"
        10 -> "十"
        else -> ""
    }
}

fun requestUrl(client: OkHttpClient, url: String): Document? {
    val resp = client.newCall(Request.Builder().url(url).build()).execute().body?.string()
    if (!resp.isNullOrBlank()) {
        return Jsoup.parse(resp)
    }
    return null
}