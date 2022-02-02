package com.yfoo.presentation.swipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    swipeableState: SwipeableState<Int> = rememberSwipeableState(initialValue = 0),
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    border: BorderStroke? = null,
    elevation: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier.aspectRatio(0.8f)
    ) {
        val cardWidth = with(LocalDensity.current) { minWidth.toPx() }
        val offsetTillGone = cardWidth * 1.4f

        val anchors = mapOf(-offsetTillGone to -1, 0f to 0, offsetTillGone to 1)
        val currentOffset = swipeableState.offset.value.roundToInt()
        val currentOffsetPercent = currentOffset / offsetTillGone

        Card(
            onClick = onClick,
            shape = shape,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            border = border,
            elevation = elevation,
            modifier = Modifier
                .fillMaxSize()
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.5f) },
                    orientation = Orientation.Horizontal,
                    interactionSource = interactionSource,
                )
                .offset { IntOffset(currentOffset, -abs(currentOffset / 30)) }
                .rotate(currentOffsetPercent * 20)
                .alpha(
                    if (abs(currentOffsetPercent) > 0.5f) {
                        1 - abs(currentOffsetPercent)
                    } else {
                        1f
                    }
                )
        ) {
            content()
        }
    }
}