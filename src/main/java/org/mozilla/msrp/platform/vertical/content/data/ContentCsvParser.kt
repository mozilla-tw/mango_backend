package org.mozilla.msrp.platform.vertical.content.data

import com.opencsv.bean.CsvToBeanBuilder
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

object CsvParserConfig {
    const val BANNER_SUBCATEGORY_NAME = "banner"
    const val BANNER_SUBCATEGORY_TYPE = "banner"
    const val BANNER_SUBCATEGORY_ID = 4
}

fun parseContent(bannerBytes: ByteArray?, listItemBytes: ByteArray): Category {

    val subCategories = mutableListOf<ContentSubcategory>()


    // add banner Subcategory
    val bannerSubCategory = parseBanner(bannerBytes)
    if (bannerSubCategory != null) {
        subCategories.add(bannerSubCategory)
    }

    // get other Subcategory
    val otherSubcategory = parseOthers(listItemBytes)

    subCategories.addAll(otherSubcategory)

    return Category(subCategories)
}

private fun parseOthers(bytes: ByteArray): List<ContentSubcategory> {
    val csvEntryList = CsvToBeanBuilder<ContentItem>(InputStreamReader(ByteArrayInputStream(bytes))).withType(ContentItem::class.java).build().parse()

    // collect sub-categories
    val subCategorySet = linkedSetOf<ContentSubcategory>()
    for (entry in csvEntryList) {
        val componentTypeName: String = entry.component_type_name ?: break
        val subcategoryName = entry.subcategory_name ?: break
        val subcategoryId = entry.subcategory_id ?: break

        subCategorySet.add(ContentSubcategory(componentTypeName, subcategoryName, subcategoryId, listOf()))
    }

    // for each sub-categories
    for (subcategory in subCategorySet) {
        val otherItems = mutableListOf<ContentItem>()

        for (content in csvEntryList) {
            if (content.subcategory_id == subcategory.subcategoryId) {
                otherItems.add(content)
            }
        }
        subcategory.items = otherItems
    }
    return subCategorySet.toList()
}

private fun parseBanner(bytes: ByteArray?): ContentSubcategory? {
    if (bytes == null) return null
    val bannerItems = mutableListOf<ContentItem>()
    val csvEntryList = CsvToBeanBuilder<ContentItem>(InputStreamReader(ByteArrayInputStream(bytes))).withType(ContentItem::class.java).build().parse()
    for (content in csvEntryList) {
        bannerItems.add(content)
    }
    return ContentSubcategory(CsvParserConfig.BANNER_SUBCATEGORY_NAME, CsvParserConfig.BANNER_SUBCATEGORY_TYPE, CsvParserConfig.BANNER_SUBCATEGORY_ID, bannerItems)
}
