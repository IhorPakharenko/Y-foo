package com.yfoo.presentation.swipe

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.*
import androidx.core.graphics.rotationMatrix
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    DismissedToStart;

    val multiplier get() = if (this == DismissedToStart) -1 else 1
}

@Stable
class YfooDismissibleState {
    private val animatable = Animatable(
        initialValue = IntOffset.Zero,
        typeConverter = IntOffset.VectorConverter,
    )

    private var anchors by mutableStateOf(emptyMap<IntOffset, DismissValue>())
    private var thresholds by mutableStateOf(emptyMap<IntOffset, DismissValue>())

    var widthTillGone: Float = 0f
    var maxRotationDegrees: Float = 0f
    var flingMinVelocity: Float = 0f

    var currentValue: DismissValue = DismissValue.Default
        private set

    var targetValue: DismissValue = DismissValue.Default

    private var dragStartPosition: DragStartPosition? = null
    val offset by animatable.asState().
    val offsetPercentTillGone = offset.x / widthTillGone
    val rotation =
        offsetPercentTillGone * maxRotationDegrees * (dragStartPosition?.multiplier ?: 0)

    fun ensureInit(widthTillGone: Float) {
        if (anchors.isEmpty()) {

        }
    }

    suspend fun animateTo(
        targetValue: DismissValue,
        animationSpec: AnimationSpec<IntOffset>,
        initialVelocity: IntOffset = IntOffset.Zero,
        block: (Animatable<IntOffset, AnimationVector2D>.() -> Unit)? = null
    ) = animatable.animateTo(
        anchors.gettargetValue, animationSpec, initialVelocity, block
    )

//    suspend fun animateTo(
//        targetValue: IntOffset,
//        animationSpec: AnimationSpec<IntOffset>,
//        initialVelocity: IntOffset = IntOffset.Zero,
//        block: (Animatable<IntOffset, AnimationVector2D>.() -> Unit)? = null
//    ) = animatable.animateTo(
//        targetValue, animationSpec, initialVelocity, block
//    )

    suspend fun snapTo(
        targetValue: DismissValue
    ) = animatable.snapTo(targetValue)
//    suspend fun snapTo(
//        targetValue: IntOffset
//    ) = animatable.snapTo(targetValue)

    suspend fun stop() = animatable.stop()
}

//TODO check if additional rtl support is needed
//TODO look up rememberUpdatedState(), it is probably useful
@Composable
fun YfooDismissible(
    state: YfooDismissibleState,
    modifier: Modifier = Modifier,
    maxRotationDegrees: Float = 20f,
    flingMinVelocity: Float = 3000f,
    enabled: Boolean = true,
    dismissAnimationSpec: AnimationSpec<IntOffset> = YfooDismissibleDefaults.DismissAnimSpec,
    defaultAnimationSpec: AnimationSpec<IntOffset> = YfooDismissibleDefaults.DefaultAnimSpec,
    content: @Composable BoxScope.() -> Unit
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val height = constraints.maxHeight.toFloat()

    // Distance that fully hides content rotated by maxRotationDegrees
    val widthTillGone = remember(width, height) {
        val matrix = floatArrayOf(
            width, 0f
        )
        rotationMatrix(maxRotationDegrees, width / 2, height / 2).mapPoints(matrix)

        matrix.first()
    }

    val dragChannel = remember { Channel<DragEvent>(capacity = Channel.UNLIMITED) }

    var dragStartPosition by remember { mutableStateOf<DragStartPosition?>(null) }
//    val animatable = remember {
//        Animatable(
//            initialValue = IntOffset.Zero,
//            typeConverter = IntOffset.VectorConverter,
//        )
//    }
//    val offset by animatable.asState()
//    val offsetPercentTillGone = offset.x / widthTillGone
//    val rotation =
//        offsetPercentTillGone * maxRotationDegrees * (dragStartPosition?.multiplier ?: 0)

    suspend fun onDragStart(pointerOffset: Offset) {
        if (dragStartPosition == null) {
            dragStartPosition =
                if (pointerOffset.y < height / 2) DragStartPosition.Top else DragStartPosition.Bottom
        }
        //TODO do we need it
        state.stop()
    }

    suspend fun onDrag(offset: Offset) {
        state.snapTo(animatable.value + offset.round())
    }

    suspend fun onDragEnd(velocity: Offset) {
        try {
            when {
                abs(velocity.x) > flingMinVelocity -> {
                    // Dismiss because of fling
                    val directionByFling =
                        if (velocity.x > 0) DismissValue.DismissedToEnd else DismissValue.DismissedToStart
                    val targetX = widthTillGone * directionByFling.multiplier
                    state.animateTo(
                        targetValue = IntOffset(targetX.toInt(), animatable.value.y),
                        animationSpec = dismissAnimationSpec,
                        initialVelocity = velocity.round(),
                    )
                }
                animatable.value.x / widthTillGone > 0.3f -> {
                    // Dismiss because threshold is reached
                    val directionByPointerLocation =
                        if (animatable.value.x / widthTillGone < 0.5f) {
                            DismissValue.DismissedToStart
                        } else {
                            DismissValue.DismissedToEnd
                        }
                    val targetX =
                        widthTillGone * directionByPointerLocation.multiplier
                    state.animateTo(
                        targetValue = IntOffset(targetX.toInt(), animatable.value.y),
                        animationSpec = dismissAnimationSpec,
                    )
                }
                else -> {
                    // Back to starting point
                    state.animateTo(
                        targetValue = IntOffset(
                            0,
                            0
                        ),
                        animationSpec = defaultAnimationSpec,
                    )
                }
            }
        } finally {
            dragStartPosition = null
        }
    }

    LaunchedEffect(Unit) {
        dragChannel.consumeEach { event ->
            when (event) {
                is DragEvent.DragStarted -> {
                    onDragStart(event.pointerOffset)
                }
                is DragEvent.Drag -> {
                    onDrag(event.offset)
                }
                is DragEvent.DragStopped -> {
                    launch {
                        onDragEnd(event.velocity)
                    }
                }
                DragEvent.DragCanceled -> {
                    launch {
                        onDragEnd(Offset.Zero)
                    }
                }
            }
        }
    }

    Box(
        Modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                val velocityTracker = VelocityTracker()
                detectDragGestures(
                    onDragStart = {
                        dragChannel.trySend(DragEvent.DragStarted(it))
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity()
                        dragChannel.trySend(
                            DragEvent.DragStopped(
                                Offset(
                                    x = velocity.x,
                                    y = velocity.y
                                )
                            )
                        )
                        velocityTracker.resetTracking()
                    },
                    onDragCancel = {
                        velocityTracker.resetTracking()
                        dragChannel.trySend(DragEvent.DragCanceled)
                    },
                    onDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        velocityTracker.addPointerInputChange(change)
                        dragChannel.trySend(DragEvent.Drag(dragAmount))
                    })
            }
            .offset { state.offset }
            .rotate(state.rotation)
    ) {
        content()
    }
}

private sealed class DragEvent {
    class DragStarted(val pointerOffset: Offset) : DragEvent()
    class Drag(val offset: Offset) : DragEvent()
    class DragStopped(val velocity: Offset) : DragEvent()
    object DragCanceled : DragEvent()
}

private enum class DragStartPosition {
    Top, Bottom;

    val multiplier get() = if (this == Top) 1 else -1
}

object YfooDismissibleDefaults {
    val DismissAnimSpec = SpringSpec<IntOffset>()
    val DefaultAnimSpec = SpringSpec<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )
}