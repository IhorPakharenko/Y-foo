package com.yfoo.presentation.swipe

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.size.Scale
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.yfoo.R
import com.yfoo.domain.Card
import com.yfoo.domain.ImageProvider
import com.yfoo.presentation.utils.nameRes

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CardsFeed(
    cards: List<Card>,
    onLike: (Card) -> Unit,
    onDislike: (Card) -> Unit,
    onProviderClick: (Card) -> Unit,
    onImageClick: (Card) -> Unit,
    isCardFadingEnabled: Boolean,
    errorPlaceholder: @Composable (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rememberedCards by remember { mutableStateOf(cards.toSet()) }
    var cardsMarkedForDeletion by remember { mutableStateOf(emptyMap<Card, Deletion?>()) }

    SideEffect {
        //TODO what if cards are removed because of provider settings changes?
        rememberedCards = rememberedCards + cards
    }

    Box(modifier) {
        rememberedCards.take(3).forEach { card ->
            val swipeableState = rememberCardFeedSwipeableState(
                confirmStateChange = { dismissValue ->
                    if (dismissValue == DismissValue.DismissedToEnd) {
                        onLike(card)
                        cardsMarkedForDeletion =
                            cardsMarkedForDeletion + (card to Deletion(
                                shouldAnimate = false,
                                toDismissValue = dismissValue,
                            ))
                    }
                    if (dismissValue == DismissValue.DismissedToStart) {
                        onDislike(card)
                        cardsMarkedForDeletion =
                            cardsMarkedForDeletion + (card to Deletion(
                                shouldAnimate = false,
                                toDismissValue = dismissValue,
                            ))
                    }
                    true
                }
            )

            val cardDeletion = cardsMarkedForDeletion[card]
            if (cardDeletion != null) {
                LaunchedEffect(cardDeletion) {
                    if (cardDeletion.shouldAnimate) {
                        swipeableState.animateTo(cardDeletion.toDismissValue)
                    } else {
                        //TODO
                    }
                    rememberedCards = rememberedCards - card
                    cardsMarkedForDeletion = cardsMarkedForDeletion - card
                    //TODO item is not really deleted and still visible
                }
            }

            swipeableState.offset // Recompose on offset change

            CardItem(
                card = card,
                onLike = {
                    onLike(card)
                    cardsMarkedForDeletion =
                        cardsMarkedForDeletion + (card to Deletion(
                            shouldAnimate = true,
                            toDismissValue = DismissValue.DismissedToEnd,
                        ))
                },
                onDislike = {
                    onDislike(card)
                    cardsMarkedForDeletion =
                        cardsMarkedForDeletion + (card to Deletion(
                            shouldAnimate = true,
                            toDismissValue = DismissValue.DismissedToStart,
                        ))
                },
                onProviderClick = onProviderClick,
                onImageClick = onImageClick,
                swipeableState = swipeableState,
                dismissProgress = swipeableState.progress.fraction,
                dismissValue = swipeableState.progress.to,
                errorPlaceholder = errorPlaceholder,
                isCardFadingEnabled = isCardFadingEnabled,
            )
        }
    }
}

private data class Deletion(val toDismissValue: DismissValue, val shouldAnimate: Boolean)

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CardItem(
    card: Card,
    onLike: (Card) -> Unit,
    onDislike: (Card) -> Unit,
    onProviderClick: (Card) -> Unit,
    onImageClick: (Card) -> Unit,
    swipeableState: SwipeableState<DismissValue>,
    dismissProgress: Float,
    dismissValue: DismissValue,
    errorPlaceholder: @Composable (Throwable) -> Unit,
    modifier: Modifier = Modifier,
    isCardFadingEnabled: Boolean = false,
) {
    var mutableState: ImageCardState by remember { mutableStateOf(ImageCardState.Loading) }
    val state = mutableState

    CardFeedDismissibleItem(
        modifier = modifier.fillMaxSize(),
        state = swipeableState,
        alpha = { progress, toDismissValue ->
            if (isCardFadingEnabled && toDismissValue != DismissValue.Default && progress > 0.5f) {
                1 - progress
            } else {
                1f
            }
        },
        enabled = state != ImageCardState.Loading,
    ) {
        val cardShape = MaterialTheme.shapes.medium

        Card(
            shape = cardShape,
            modifier = modifier
                .fillMaxSize()
                .placeholder(
                    visible = state == ImageCardState.Loading,
                    color = Color.LightGray,
                    shape = cardShape,
                    highlight = PlaceholderHighlight.shimmer(Color.White),
                )
        ) {
            if (state is ImageCardState.Error) {
                errorPlaceholder(state.throwable)
            } else {
                CardContent(
                    card = card,
                    onLike = onLike,
                    onDislike = onDislike,
                    onProviderClick = onProviderClick,
                    onImageClick = onImageClick,
                    onImageLoadingSuccess = { _, _ ->
                        mutableState = ImageCardState.Success
                    },
                    onImageLoadingError = { _, throwable ->
                        mutableState = ImageCardState.Error(throwable)
                    },
                    dismissProgress = dismissProgress,
                    dismissValue = dismissValue,
                    enabled = state != ImageCardState.Loading,
                    bottomRowColor = Color.Black,
                )
            }
        }
    }
}

private sealed class ImageCardState {
    object Loading : ImageCardState()
    data class Error(val throwable: Throwable) : ImageCardState()
    object Success : ImageCardState()
}

@Composable
private fun CardContent(
    card: Card,
    onLike: (Card) -> Unit,
    onDislike: (Card) -> Unit,
    onProviderClick: (Card) -> Unit,
    onImageClick: (Card) -> Unit,
    onImageLoadingSuccess: (ImageRequest, ImageResult.Metadata) -> Unit,
    onImageLoadingError: (ImageRequest, Throwable) -> Unit,
    dismissProgress: Float,
    dismissValue: DismissValue,
    enabled: Boolean,
    bottomRowColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        val buttonSize = 56.dp
        val buttonRowBottomPadding = 8.dp

        Image(
            painter = rememberImagePainter(data = card.source.value) {
                scale(Scale.FILL) //TODO do we need it
                listener(
                    onSuccess = onImageLoadingSuccess,
                    onError = onImageLoadingError,
                )
            },
            contentDescription = stringResource(
                when (card.provider) {
                    ImageProvider.ThisWaifuDoesNotExist -> R.string.image_of_anime_character
                    ImageProvider.ThisAnimeDoesNotExist -> R.string.image_of_anime_character
                    ImageProvider.ThisCatDoesNotExist -> R.string.image_of_cat
                }
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(bottom = buttonSize + buttonRowBottomPadding)
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    onImageClick(card)
                }
        )

        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            ClickableText(
                text = buildAnnotatedString {
                    val providerName = stringResource(card.provider.nameRes)
                    val fullText = stringResource(R.string.by_site, providerName)

                    val providerNameStart = fullText.indexOf(providerName)
                    val providerNameEnd = providerNameStart + providerName.length

                    append(fullText)
                    addStyle(
                        style = SpanStyle(color = MaterialTheme.colors.primary),
                        start = providerNameStart,
                        end = providerNameEnd
                    )
                },
                //TODO enlarge clickable space
                onClick = { onProviderClick(card) },
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, bottomRowColor)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bottomRowColor)
                    .padding(bottom = buttonRowBottomPadding)
            ) {
                ReactionButton(
                    onClick = { onDislike(card) },
                    percentSelected = if (dismissValue == DismissValue.DismissedToStart) {
                        dismissProgress
                    } else {
                        0f
                    },
                    primaryColor = Color.Red,
                    secondaryColor = Color.White,
                    enabled = enabled,
                    modifier = Modifier.size(buttonSize)
                ) {
                    DislikeIcon()
                }
                Spacer(Modifier.width(48.dp))
                ReactionButton(
                    onClick = { onLike(card) },
                    percentSelected = if (dismissValue == DismissValue.DismissedToEnd) {
                        dismissProgress
                    } else {
                        0f
                    },
                    primaryColor = Color.Green,
                    secondaryColor = Color.White,
                    enabled = enabled,
                    modifier = Modifier.size(buttonSize)
                ) {
                    LikeIcon()
                }
            }
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ReactionButton(
    onClick: () -> Unit,
    percentSelected: Float,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val transition = updateTransition(targetState = percentSelected > 0.05f, label = "selected")

    val iconColor by transition.animateColor(label = "icon") { isSelected ->
        if (isSelected) secondaryColor else primaryColor
    }
    val backgroundColor by transition.animateColor(label = "background") { isSelected ->
        if (isSelected) primaryColor else Color.Transparent
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .border(
                width = 1.dp,
                color = primaryColor,
                shape = CircleShape,
            )
            .background(color = backgroundColor, shape = CircleShape)
    ) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            content()
        }
    }

}

@Composable
private fun DislikeIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Rounded.Close,
        contentDescription = stringResource(R.string.nope),
        modifier = modifier,
    )
}

@Composable
private fun LikeIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Rounded.Favorite,
        contentDescription = stringResource(R.string.like),
        modifier = modifier,
    )
}