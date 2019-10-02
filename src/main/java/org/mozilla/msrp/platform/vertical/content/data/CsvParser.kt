package org.mozilla.msrp.platform.vertical.content.data

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvToBeanBuilder
import org.mozilla.msrp.platform.util.hash
import java.io.File
import java.io.FileReader


// TODO: remove the file after parsing. Or just change to IO stream
fun parseContent(version: Int, bannerFile: File?, listFile: File): ContentResponse {

    val subCategories = mutableListOf<Subcategory>()


    // add banner Subcategory
    val bannerSubCategory = parseBanner(bannerFile)
    if (bannerSubCategory != null) {
        subCategories.add(bannerSubCategory)
    }

    // get other Subcategory
    val otherSubcategory = parseOthers(listFile)

    subCategories.addAll(otherSubcategory)

    return ContentResponse(version, subCategories)
}

private fun parseOthers(listFile: File): List<Subcategory> {
    val csvEntryList = CsvToBeanBuilder<CsvEntry>(FileReader(listFile)).withType(CsvEntry::class.java).build().parse()

    // collect sub-categories
    val subCategorySet = linkedSetOf<Subcategory>()
    for (entry in csvEntryList) {
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
                        content.destination?.hash() ?: break,
                        content.price ?: "",
                        content.discount ?: "",
                        content.score ?: 0.0,
                        content.score_reivews ?: 0,
                        content.description ?: "-"

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
                content.destination?.hash() ?: break,
                content.price ?: "",
                content.discount ?: "",
                content.score ?: 0.0,
                content.score_reivews ?: 0,
                content.description ?: "-"
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
        @field:CsvBindByName(required = false) var description: String? = "",
        @field:CsvBindByName(required = false) var score: Double? = null,
        @field:CsvBindByName(required = false) var score_reivews: Int? = null,
        @field:CsvBindByName(required = false) var price: String? = null,
        @field:CsvBindByName(required = false) var discount: String? = null,
        @field:CsvBindByName(required = false) var fresh: Boolean? = null,
        @field:CsvBindByName(required = false) var start_date: Long? = null,
        @field:CsvBindByName(required = false) var end_date: Long? = null
)