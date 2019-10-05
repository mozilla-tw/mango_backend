package org.mozilla.msrp.platform.vertical.shopping

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.mozilla.msrp.platform.vertical.content.data.parseContent
import java.io.File

class ShoppingDataTest {

    @Test
    fun testCsvParsing() {
        val file1 = "test_apk_games_banner.csv"
        val file2 = "test_apk_games.csv"
        val classLoader = javaClass.classLoader
        val f1 = File(classLoader.getResource(file1)!!.file)
        val f2 = File(classLoader.getResource(file2)!!.file)

        val response = parseContent(f1.readBytes(), f2.readBytes())

        var sumItems = 0
        for (subcategory in response.subcategories) {
            sumItems += subcategory.items.size
        }

        assert(sumItems == 12)
        assert(response.subcategories.size == 5)
        println("JSON3======${ObjectMapper().writeValueAsString(response)}")
    }
}
