package com.yfoo.presentation.swipe

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

enum class DismissValue {
    /**
     * Indicates the component has not been dismissed yet.
     */
    Default,

    /**
     * Indicates the component has been dismissed in the reading direction.
     */
    DismissedToEnd,

    /**
     * Indicates the component has been dismissed in the reverse of the reading direction.
     */
    DismissedToStart
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun rememberCardFeedSwipeableState(
    initialValue: DismissValue = DismissValue.Default,
    animationSpec: AnimationSpec<Float> = SwipeScreenCardContainerDefaults.AnimationSpec,
    confirmStateChange: (newValue: DismissValue) -> Boolean = { true }
) = rememberSwipeableState(initialValue, animationSpec, confirmStateChange)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CardFeedDismissibleItem(
    modifier: Modifier = Modifier,
    state: SwipeableState<DismissValue> = rememberSwipeableState(
        initialValue = DismissValue.Default,
        animationSpec = SwipeScreenCardContainerDefaults.AnimationSpec,
    ),
    offsetY: (Float, DismissValue) -> Dp = { progress, toDismissValue ->
        when (toDismissValue) {
            DismissValue.Default -> 0.dp
            DismissValue.DismissedToEnd, DismissValue.DismissedToStart -> -(progress * 16).dp
        }
    },
    rotateDegrees: (Float, DismissValue) -> Float = { progress, toDismissValue ->
        when (toDismissValue) {
            DismissValue.Default -> 0f
            DismissValue.DismissedToEnd -> progress * 20f
            DismissValue.DismissedToStart -> -progress * 20f
        }
    },
    alpha: (Float, DismissValue) -> Float = { _, _ -> 1f },
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val anchors = mapOf(
        0f to DismissValue.Default,
        width to DismissValue.DismissedToEnd,
        -width to DismissValue.DismissedToStart,
    )

    val progress = state.progress.fraction
    val toDismissValue = state.progress.to

    val alphaAnimated by animateFloatAsState(targetValue = alpha(progress, toDismissValue))

    Box(
        Modifier
            .swipeable(
                state = state,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
                orientation = Orientation.Horizontal,
                enabled = enabled,
                reverseDirection = isRtl,
                resistance = ResistanceConfig(basis = width)
            )
            .offset { IntOffset(x = state.offset.value.toInt(), y = 0) }
            .offset(y = offsetY(progress, toDismissValue))
            .rotate(rotateDegrees(progress, toDismissValue))
            .alpha(alphaAnimated)
    ) {
        content()
    }
}

object SwipeScreenCardContainerDefaults {
    val AnimationSpec = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
    )
}