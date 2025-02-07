package cn.j1angvei

import org.jsoup.Jsoup
import java.io.File
import kotlin.random.Random

private const val HOMEPAGE_URL = "https://www.12371.cn/special/cidian/#"

fun main() {
    val text = StringBuilder()
    val homeDoc = Jsoup.connect(HOMEPAGE_URL).get()
    val title = homeDoc.title()
    println("title $title")
    text.append("# $title").append("\n")
    val navList = homeDoc.select("div#page_body > div.page_wrap  ul > li > a[href]")
    navList.forEachIndexed { index, nav ->
        val navLink = nav.attr("href")
        parseSection(navLink, index, text)
        Thread.sleep(Random.nextLong(1000, 3000))
    }
    val file = File(System.getProperty("user.dir") + File.separator + "党务知识.md")
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    file.writeText(text.toString())
}

fun parseSection(link: String, idx: Int, text: StringBuilder) {
    println("start for $link")
    val doc = Jsoup.connect(link).get()
    val title = doc.select("div#page_body > div.page_wrap div.dyw1027new-content-container div > h4").text()
    println("section title $title")
    text.append("\n\n").append("## ").append(toChinese(idx + 1)).append(". ").append(title).append("\n")
    val paragraphs = doc.select("div#page_body > div.page_wrap div > ul > li > span.title > a[href]")
    paragraphs.forEachIndexed { pIdx, p ->
        val pLink = p.attr("href")
        val pTitle = p.text()
        println("p link $pLink, p title $pTitle")
        parseParagraph(pLink, pIdx, pTitle, text)
        Thread.sleep(Random.nextLong(700, 1500))
    }
    text.append("\n").append("\n")
}

fun parseParagraph(link: String, idx: Int, title: String, text: StringBuilder) {
    text.append("\n").append("### ").append(idx + 1).append(". ").append(title).append("\n")
    val subList = Jsoup.connect(link).get().select("div.page_wrap p")
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