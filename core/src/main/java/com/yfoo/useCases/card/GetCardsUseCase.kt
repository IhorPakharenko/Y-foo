package com.yfoo.useCases.card

import com.yfoo.domain.Card

class GetCardsUseCase() {
    suspend operator fun invoke(): List<Card> {
        return listOf(
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57776.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57771.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57772.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57773.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57774.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57775.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57777.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57778.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57779.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57780.jpg")
            ),
            Card(
                Card.Provider.ThisWaifuDoesNotExist,
                Card.ImageSource.Url("https://www.thiswaifudoesnotexist.net/example-57781.jpg")
            ),
        )
    }
}