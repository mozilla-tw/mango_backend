package org.mozilla.msrp.platform.vertical.content

import java.time.Instant
import java.text.SimpleDateFormat



fun main() {

    val schedule = "2019-10-06"
    val date1 = SimpleDateFormat("yyyy-MM-dd").parse(schedule)

    println("1======" + Instant.now())
    println("2======" + System.currentTimeMillis())
    println("3======" + date1)
    println("3======" + date1.time)
    println("3======" + date1.toInstant())
    println("3======" + date1.toInstant().epochSecond)
    println("4======" + System.currentTimeMillis())
}