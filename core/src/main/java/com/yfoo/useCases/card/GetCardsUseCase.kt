package com.yfoo.useCases.card

import com.yfoo.domain.Card
import com.yfoo.domain.ImageProvider
import com.yfoo.domain.ImageSource

class GetCardsUseCase() {
    suspend operator fun invoke(): List<Card> {
        return listOf(
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57771.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57772.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57773.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57774.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57775.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57776.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57777.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57778.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57779.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57780.jpg"),
            ),
            Card(
                ImageProvider.ThisWaifuDoesNotExist,
                ImageSource("https://www.thiswaifudoesnotexist.net/example-57781.jpg"),
            ),
        )
    }
}