package org.mozilla.msrp.platform.vertical.game

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

interface UiModel

data class GameItem(
    val id: String,
    val name: String,
    val imageUrl: String,
    val linkUrl: String
) : UiModel

data class BannerItem(
    val id: String,
    val imageUrl: String,
    val linkUrl: String
) : UiModel

data class CarouselBanner(val banners: List<BannerItem>) : UiModel

data class GameCategory(
    val id: String,
    val title: String,
    val gameList: List<GameItem>
) : UiModel


@RestController
class GameController {

    @GetMapping("/api/v1/game")
    internal fun games(
        @RequestParam(value = "language") language: String,
        @RequestParam(value = "country") country: String): List<UiModel> {
        return generateFakeData()
    }

    private fun generateFakeData(): List<UiModel> =
        listOf(
            CarouselBanner(listOf(
                BannerItem(
                    UUID.randomUUID().toString(),
                    "http://www.gameloft.com/central/upload/Asphalt-9-Legends-Slider-logo-2.jpg",
                    "http://www.gameloft.com/central/category/asphalt/asphalt-9-legends/"
                ),
                BannerItem(
                    UUID.randomUUID().toString(),
                    "http://www.gameloft.com/central/upload/MCB_Blog-FrontRodolex_2048x700.jpg",
                    "http://www.gameloft.com/central/modern-combat-blackout/modern-combat-blackout-coming-nintendo-switch/"
                ),
                BannerItem(
                    UUID.randomUUID().toString(),
                    "http://www.gameloft.com/central/upload/Slider-1.jpg",
                    "http://www.gameloft.com/central/dungeon-hunter/dungeon-hunter-champions-brings-you-epic-action/"
                )
            )),
            GameCategory(UUID.randomUUID().toString(),
                "Game of the week",
                generateTestingGameList1()
            ),
            GameCategory(UUID.randomUUID().toString(),
                "Strategy",
                generateTestingGameList2()
            ),
            GameCategory(UUID.randomUUID().toString(),
                "Adventure",
                generateTestingGameList1()
            ),
            GameCategory(UUID.randomUUID().toString(),
                "Action",
                generateTestingGameList2()
            ),
            GameCategory(UUID.randomUUID().toString(),
                "Arcade",
                generateTestingGameList1()
            ),
            GameCategory(UUID.randomUUID().toString(),
                "Puzzle & Logic",
                generateTestingGameList2()
            ),
            GameCategory(UUID.randomUUID().toString(),
                "Sport & Racing",
                generateTestingGameList1()
            )
        )

    private fun generateTestingGameList1(): List<GameItem> {
        return listOf(
            GameItem(
                UUID.randomUUID().toString(),
                "BoboiBoy Galaxy Run",
                "https://media07-gl-ssl-gzip.gameloft.com/products/3662/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/boBoiBoyRunFree/?ms_sid=4&phoneId=32225&game=16003785&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "Kitchen Bazar",
                "https://media07-gl-ssl-gzip.gameloft.com/products/3796/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/kitchenBazarFree/?ms_sid=4&phoneId=32225&game=16086981&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "Ludibubbles",
                "https://media07-gl-ssl-gzip.gameloft.com/products/2920/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/ludibubblesFree/?ms_sid=4&phoneId=32225&game=15153437&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "Danger Dash",
                "https://media07-gl-ssl-gzip.gameloft.com/products/3232/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/dangerDashFree/?ms_sid=4&phoneId=32225&game=15487866&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "Puzzle Pets: Pairs",
                "https://media07-gl-ssl-gzip.gameloft.com/products/2794/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/puzzlePetsPairsFree/?ms_sid=4&phoneId=32225&game=15084939&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "Meow Meow Life",
                "https://media07-gl-ssl-gzip.gameloft.com/products/3598/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/meowMeowLifeFree/?ms_sid=4&phoneId=32225&game=15928205&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            )
        )
    }

    private fun generateTestingGameList2(): List<GameItem> {
        return listOf(
            GameItem(
                UUID.randomUUID().toString(),
                "Castle of Magic",
                "https://media07-gl-ssl-gzip.gameloft.com/products/3624/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/castleOfMagicFree/?ms_sid=4&phoneId=32225&game=15966327&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "Ninja UP!",
                "https://media07-gl-ssl-gzip.gameloft.com/products/2817/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/ninjaUpFree/?ms_sid=4&phoneId=32225&game=15118898&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "Kite",
                "https://media07-gl-ssl-gzip.gameloft.com/products/2564/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/kiteFree/?ms_sid=4&phoneId=32225&game=14355923&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "Pirates: Path of the Buccaneer",
                "https://media07-gl-ssl-gzip.gameloft.com/products/3297/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/piratesPathOfTheBuccaneerFree/?ms_sid=4&phoneId=32225&game=15574419&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "NitroStreet: DragMode",
                "https://media07-gl-ssl-gzip.gameloft.com/products/2794/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/nitroStreetDragModeFree/?ms_sid=4&phoneId=32225&game=15104944&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            ),
            GameItem(
                UUID.randomUUID().toString(),
                "Fantasy Sushi Diver",
                "https://media07-gl-ssl-gzip.gameloft.com/products/2665/default/html5/icon/114/icon.png",
                "https://cdn.ludigames.com/h5/fantasySushiDiverFree/?ms_sid=4&phoneId=32225&game=14583641&fromPartner=gameloft&sv=126ehy3kvewfvbn64lr4zs3rw&c=206&utm_source=gameloft&utm_medium=bookmark&utm_campaign=PI39"
            )
        )
    }
}