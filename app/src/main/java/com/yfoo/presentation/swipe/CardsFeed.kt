package com.yfoo.presentation.swipe

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.request.ImageResult
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.yfoo.R
import com.yfoo.domain.Card
import com.yfoo.domain.ImageProvider
import com.yfoo.presentation.utils.nameRes
import com.yfoo.presentation.utils.scale
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CardsFeed(
    cards: List<Card>,
    onLike: (Card) -> Unit,
    onDislike: (Card) -> Unit,
    onProviderClick: (Card) -> Unit,
    onImageClick: (Card) -> Unit,
    bottomRowColor: Color,
    errorPlaceholder: @Composable (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        val scope = rememberCoroutineScope()
        val swipeableState = rememberYfooDismissibleState(
            dismissAnimationSpec = TweenSpec(
                durationMillis = 200,
                easing = LinearEasing
            )
        )

        var waitingForDeletion by remember { mutableStateOf(false) }

        val backgroundCard = cards.getOrNull(1)
        backgroundCard?.let { card ->
            val dismissProgress = swipeableState.progress.fraction

            val minProgress = 0.1f
            val maxProgress = 0.3f
            val minScale = 0.98f
            val maxScale = 1f

            val scale = when {
                dismissProgress < minProgress -> minScale
                dismissProgress > maxProgress -> maxScale
                else -> dismissProgress.scale(
                    oldMin = minProgress, oldMax = maxProgress,
                    newMin = minScale, newMax = maxScale,
                )
            }

            val (imageCardState, setImageCardState) = remember {
                mutableStateOf<ImageCardState>(ImageCardState.Loading)
            }

            CardItem(
                card = card,
                onProviderClick = {},
                onImageClick = {},
                imageCardState = imageCardState,
                setImageCardState = setImageCardState,
                swipeableState = rememberYfooDismissibleState(),
                bottomRowColor = bottomRowColor,
                errorPlaceholder = errorPlaceholder,
                modifier = Modifier
                    .scale(scale)
                    .pointerInput(waitingForDeletion) {
                        detectTapGestures(
                            onPress = {
                                if (waitingForDeletion) {
                                    waitingForDeletion = false
                                }
                            }
                        )
                    }
            )
        }

        val foregroundCard = cards.firstOrNull()
//        val foregroundCard = null
        foregroundCard?.let { card ->
            LaunchedEffect(swipeableState.currentValue) {
                if (swipeableState.currentValue == DismissValue.DismissedToEnd) {
//                    swipeableState.snapTo(DismissValue.Default)
                    waitingForDeletion = true
                    onLike(card)
                }
                if (swipeableState.currentValue == DismissValue.DismissedToStart) {
//                    swipeableState.snapTo(DismissValue.Default)
                    waitingForDeletion = true
                    onDislike(card)
                }
            }

//            LaunchedEffect(waitingForDeletion, card) {
//                swipeableState.snapTo(DismissValue.Default)
//            }
            if (waitingForDeletion) {
                LaunchedEffect(waitingForDeletion) {

                }
            }

            LaunchedEffect(card) {
                waitingForDeletion = false
                swipeableState.snapTo(DismissValue.Default)
            }

            val (imageCardState, setImageCardState) = remember {
                mutableStateOf<ImageCardState>(ImageCardState.Loading)
            }

//            transition.AnimatedVisibility(
//                visible = { !it },
//                exit = slideOutHorizontally { it / 2 },
//                enter = EnterTransition.None
//            ) {
            CardItem(
                card = card,
                onProviderClick = onProviderClick,
                onImageClick = onImageClick,
                imageCardState = imageCardState,
                setImageCardState = setImageCardState,
                swipeableState = swipeableState,
                bottomRowColor = bottomRowColor,
                errorPlaceholder = errorPlaceholder,
            )
//            }

            if (imageCardState != ImageCardState.Loading) {
                fun getProgressFor(dismissValue: DismissValue): Float {
                    return if (swipeableState.progress.to == dismissValue) {
                        swipeableState.progress.fraction
                    } else {
                        0f
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(vertical = CardsFeedDefaults.ButtonVerticalPadding)
                ) {
                    ReactionButton(
                        onClick = {
                            scope.launch {
                                swipeableState.animateTo(DismissValue.DismissedToStart)
                            }
                        },
                        percentSelected = getProgressFor(DismissValue.DismissedToStart),
                        primaryColor = Color.Red,
                        secondaryColor = Color.White,
                        modifier = Modifier.size(CardsFeedDefaults.ButtonSize)
                    ) {
                        DislikeIcon()
                    }
                    Spacer(Modifier.width(40.dp))
                    ReactionButton(
                        onClick = {
                            scope.launch {
                                swipeableState.animateTo(DismissValue.DismissedToEnd)
                            }
                        },
                        percentSelected = getProgressFor(DismissValue.DismissedToEnd),
                        primaryColor = Color.Green,
                        secondaryColor = Color.White,
                        modifier = Modifier.size(CardsFeedDefaults.ButtonSize)
                    ) {
                        LikeIcon()
                    }
                }
            }
        }
    }
}

private object CardsFeedDefaults {
    val ButtonSize = 56.dp
    val ButtonVerticalPadding = 8.dp
}

@Composable
private fun CardItem(
    card: Card,
    onProviderClick: (Card) -> Unit,
    onImageClick: (Card) -> Unit,
    imageCardState: ImageCardState,
    setImageCardState: (ImageCardState) -> Unit,
    swipeableState: YfooDismissibleState,
    bottomRowColor: Color,
    errorPlaceholder: @Composable (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    //TODO can replace with SwipeToDismiss if not going to change default animSpec
    YfooDismissible(
        modifier = modifier.fillMaxSize(),
        state = swipeableState,
        enabled = imageCardState != ImageCardState.Loading,
    ) {
        val cardShape = MaterialTheme.shapes.medium

        Card(
            shape = cardShape,
            modifier = modifier
                .fillMaxSize()
                .placeholder(
                    visible = imageCardState == ImageCardState.Loading,
                    color = Color.LightGray,
                    shape = cardShape,
                    highlight = PlaceholderHighlight.shimmer(Color.White),
                )
        ) {
            if (imageCardState is ImageCardState.Error) {
                errorPlaceholder(imageCardState.throwable)
            } else {
                CardContent(
                    card = card,
                    onProviderClick = onProviderClick,
                    onImageClick = onImageClick,
                    onImageLoadingSuccess = { _, _ ->
                        setImageCardState(ImageCardState.Success)
                    },
                    onImageLoadingError = { _, throwable ->
                        setImageCardState(ImageCardState.Error(throwable))
                    },
                    bottomRowColor = bottomRowColor,
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
    onProviderClick: (Card) -> Unit,
    onImageClick: (Card) -> Unit,
    onImageLoadingSuccess: (ImageRequest, ImageResult.Metadata) -> Unit,
    onImageLoadingError: (ImageRequest, Throwable) -> Unit,
    bottomRowColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .background(bottomRowColor)
            .padding(bottom = CardsFeedDefaults.ButtonSize + CardsFeedDefaults.ButtonVerticalPadding)
    ) {
        Image(
            painter = rememberImagePainter(data = card.source.value) {
                listener(
                    onStart = {},
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
                    append('\n')
                    append(card.source.value)
                },
                //TODO enlarge clickable space
                onClick = { onProviderClick(card) },
                style = MaterialTheme.typography.h6.copy(Color.White),
                //TODO set a tiny offset to hide the white line appearing on rotation
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, bottomRowColor)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

}

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
