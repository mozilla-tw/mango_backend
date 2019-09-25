package org.mozilla.msrp.platform.vertical.shopping

import com.fasterxml.jackson.databind.ObjectMapper
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvToBeanBuilder
import org.junit.Test
import org.mozilla.msrp.platform.util.hash
import org.mozilla.msrp.platform.vertical.content.data.ContentResponse
import org.mozilla.msrp.platform.vertical.content.data.Item
import org.mozilla.msrp.platform.vertical.content.data.Subcategory

import java.io.File
import java.io.FileReader

class ShoppingDataTest {

    @Test
    fun testCsvParsing() {
        val file1 = "201909Indonesia_shopping_deal_banner_uploading.csv"
        val file2 = "201909Indonesia_shopping_deal_uploading.csv"
        val classLoader = javaClass.classLoader
        val f1 = File(classLoader.getResource(file1)!!.file)
        val f2 = File(classLoader.getResource(file2)!!.file)

        val response = parseContent(f1, f2)

        println("JSON======${ObjectMapper().writeValueAsString(response)}")
    }
}

fun parseContent(bannerFile: File?, listFile: File): ContentResponse {

    val subCategories = mutableListOf<Subcategory>()


    // add banner Subcategory
    val bannerSubCategory = parseBanner(bannerFile)
    if (bannerSubCategory != null) {
        subCategories.add(bannerSubCategory)
    }

    // get other Subcategory
    val otherSubcategory = parseOthers(listFile)

    subCategories.addAll(otherSubcategory)

    return ContentResponse(1, subCategories)
}

private fun parseOthers(listFile: File): List<Subcategory> {
    val csvEntryList = CsvToBeanBuilder<CsvEntry>(FileReader(listFile)).withType(CsvEntry::class.java).build().parse()

    // collect sub-categories
    val subCategorySet = linkedSetOf<Subcategory>()
    for (entry in csvEntryList) {
        // category 14 is banner
        if (entry.category_id == 4) {
            break
        }
        val component_type_name = entry.component_type_name ?: break
        val subcategory_name = entry.subcategory_name ?: break
        val subcategory_id = entry.subcategory_id ?: break

        subCategorySet.add(Subcategory(component_type_name, subcategory_name, subcategory_id, listOf()))
    }

    // for each sub-categories
    for (subcategory in subCategorySet) {
        val otherItems = mutableListOf<Item>()

        for (content in csvEntryList) {
            if (content.subcategory_id == subcategory.subcategoryId) {
                otherItems.add(Item(
                        content.source_name ?: break,
                        content.image ?: break,
                        content.destination ?: break,
                        content.title ?: break,
                        content.destination?.hash() ?: break
                ))
            }
        }
        subcategory.items = otherItems
    }

    return subCategorySet.toList()


}

private fun parseBanner(bannerFile: File?): Subcategory? {
    if (bannerFile == null) return null
    val bannerItems = mutableListOf<Item>()
    val csvEntryList = CsvToBeanBuilder<CsvEntry>(FileReader(bannerFile)).withType(CsvEntry::class.java).build().parse()
    for (content in csvEntryList) {
        bannerItems.add(Item(
                content.source_name ?: break,
                content.image ?: break,
                content.destination ?: break,
                content.title ?: break,
                content.destination?.hash() ?: break
        ))
    }
    return Subcategory("banner", "banner", 4, bannerItems)
}

// TODO: set the required fields for each categories
//  https://docs.google.com/spreadsheets/d/1s--x8TiIsEEISHl3YJA8ulAljxJt4FQUHZ0r5tCxHy0/edit#gid=1268475188
class CsvEntry(
        @field:CsvBindByName(required = false) var created_at: Long? = null,
        @field:CsvBindByName(required = false) var country: String? = null,
        @field:CsvBindByName(required = false) var source_name: String? = null,
        @field:CsvBindByName(required = false) var source_type: String? = null,
        @field:CsvBindByName(required = false) var partner: Boolean? = null,
        @field:CsvBindByName(required = false) var vertical_name: String? = null,
        @field:CsvBindByName(required = false) var vertical_id: Int? = null,
        @field:CsvBindByName(required = false) var category_name: String? = null,
        @field:CsvBindByName(required = false) var category_id: Int? = null,
        @field:CsvBindByName(required = false) var subcategory_name: String? = null,
        @field:CsvBindByName(required = false) var subcategory_id: Int? = null,
        @field:CsvBindByName(required = false) var component_type_name: String? = null,
        @field:CsvBindByName(required = false) var component_type_id: Int? = null,
        @field:CsvBindByName(required = false) var image_type: String? = null,
        @field:CsvBindByName(required = false) var image: String? = null,
        @field:CsvBindByName(required = false) var destination: String? = null,
        @field:CsvBindByName(required = false) var title: String? = null,
        @field:CsvBindByName(required = false) var description: String? = null,
        @field:CsvBindByName(required = false) var score: Double? = null,
        @field:CsvBindByName(required = false) var score_reviews: Int? = null,
        @field:CsvBindByName(required = false) var price: String? = null,
        @field:CsvBindByName(required = false) var discount: String? = null,
        @field:CsvBindByName(required = false) var fresh: Boolean? = null,
        @field:CsvBindByName(required = false) var start_date: Long? = null,
        @field:CsvBindByName(required = false) var end_date: Long? = null
)