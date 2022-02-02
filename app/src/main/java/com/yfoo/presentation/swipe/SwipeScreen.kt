package com.yfoo.presentation.swipe

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.yfoo.R
import com.yfoo.presentation.utils.WindowSize

@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel,
    windowSize: WindowSize,
) {
    SwipeScreen(windowSize)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeScreen(
    windowSize: WindowSize,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                actions = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings),
                    )
                }
            )
        },
        content = {
            val cardSwipeableState = rememberSwipeableState(
                initialValue = 0,
                animationSpec = SpringSpec(dampingRatio = Spring.DampingRatioMediumBouncy),
            )
            val cardInteractionSource = MutableInteractionSource()
            val dragged by cardInteractionSource.collectIsDraggedAsState()

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                val maxCardHeightPercent = if (windowSize == WindowSize.Compact) 0.9f else 0.7f

                SwipeableCard(
                    swipeableState = cardSwipeableState,
                    interactionSource = cardInteractionSource,
                    modifier = Modifier
                        .fillMaxHeight(maxCardHeightPercent)
                        .align(Alignment.Center)
                ) {
                    Box(Modifier.background(if (dragged) Color.Blue else Color.Green))
                }

                Icon(
                    imageVector = Icons.Rounded.Cancel,
                    contentDescription = stringResource(R.string.like),
                    modifier = Modifier.offset { IntOffset(0, 0) }
                )
            }
        }
    )
}