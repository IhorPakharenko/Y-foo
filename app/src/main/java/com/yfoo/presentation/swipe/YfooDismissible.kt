package com.yfoo.presentation.swipe

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeProgress
import androidx.compose.material.swipeable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.round
import androidx.core.graphics.rotationMatrix
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

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

    val directionMultiplier get() = if (this == DismissedToStart) -1 else 1
}

private fun Map<Float, DismissValue>.getOffset(state: DismissValue): Float? {
    return entries.firstOrNull { it.value == state }?.key
}

@Stable
class YfooDismissibleState(
    initialValue: DismissValue = DismissValue.Default,
    private val dismissAnimationSpec: AnimationSpec<Offset> = YfooDismissibleDefaults.DismissAnimSpec,
    private val defaultAnimationSpec: AnimationSpec<Offset> = YfooDismissibleDefaults.DefaultAnimSpec,
) {
    private val animatable = Animatable(
        initialValue = Offset.Zero,
        typeConverter = Offset.VectorConverter,
    )

    private var anchors by mutableStateOf(emptyMap<Float, DismissValue>())

    private val latestNonEmptyAnchorsFlow: Flow<Map<Float, DismissValue>> =
        snapshotFlow { anchors }
            .filter { it.isNotEmpty() }
            .take(1)

    var widthTillGone: Float by mutableStateOf(0f)
        private set

    //TODO set this value as in Swipeable.kt
    var maxRotationDegrees: Float by mutableStateOf(0f)
        private set
    var flingMinVelocity: Float by mutableStateOf(0f)
        private set

    //TODO set currentValue after position change
    var currentValue: DismissValue by mutableStateOf(DismissValue.Default)
        private set

    private var dragStartPosition: DragStartPosition? = null
    val offset by animatable.asState()
    val offsetXPercentTillGone by derivedStateOf { offset.x / widthTillGone }
    val rotation by derivedStateOf {
        offsetXPercentTillGone * maxRotationDegrees * (dragStartPosition?.directionMultiplier ?: 0)
    }

    //TODO make my own copy of SwipeProgress?
    val progress: SwipeProgress<DismissValue>
        get() {
            val bounds = findBounds(offset.x, anchors.keys)
            val from: DismissValue
            val to: DismissValue
            val fraction: Float
            when (bounds.size) {
                0 -> {
                    from = currentValue
                    to = currentValue
                    fraction = 1f
                }
                1 -> {
                    from = anchors.getValue(bounds[0])
                    to = anchors.getValue(bounds[0])
                    fraction = 1f
                }
                else -> {
                    val (a, b) =
                        if (direction > 0f) {
                            bounds[0] to bounds[1]
                        } else {
                            bounds[1] to bounds[0]
                        }
                    from = anchors.getValue(a)
                    to = anchors.getValue(b)
                    fraction = (offset.x - a) / (b - a)
                }
            }
            return SwipeProgress(from, to, fraction)
        }

    val direction: Float
        get() = anchors.getOffset(currentValue)?.let { sign(offset.x - it) } ?: 0f

    internal fun processNewAnchors(widthTillGone: Float) {
        this.widthTillGone = widthTillGone
        anchors = mapOf(
            -widthTillGone to DismissValue.DismissedToStart,
            0f to DismissValue.Default,
            widthTillGone to DismissValue.DismissedToEnd,
        )
    }

    //TODO maybe provide ability to set thresholds from the outside;
    // DEFINITELY synchronize this threshold with the one used in onDragEnd
    internal var thresholds: (Float, Float) -> Float by mutableStateOf({ _, _ -> 0f })

    /**
     * The target value of the state.
     *
     * If a swipe is in progress, this is the value that the [swipeable] would animate to if the
     * swipe finished. If an animation is running, this is the target value of that animation.
     * Finally, if no swipe or animation is in progress, this is the same as the [currentValue].
     */
    @ExperimentalMaterialApi
    val targetValue: DismissValue
        get() {
            val target = if (animatable.isRunning) {
                animatable.targetValue.x
            } else {
                computeTarget(
                    offset = offset.x,
                    lastValue = anchors.getOffset(currentValue) ?: offset.x,
                    anchors = anchors.keys,
                    thresholds = thresholds,
                    velocity = 0f,
                    velocityThreshold = Float.POSITIVE_INFINITY
                )
            }
            return anchors[target] ?: currentValue
        }

    suspend fun animateTo(
        targetValue: DismissValue,
        initialVelocity: Offset = Offset.Zero,
        block: (Animatable<Offset, AnimationVector2D>.() -> Unit)? = null
    ) {
        latestNonEmptyAnchorsFlow.collect { anchors ->
            animateInternalToOffset(
                Offset(anchors.getOffset(targetValue)!!, offset.y),
                when (targetValue) {
                    DismissValue.Default -> defaultAnimationSpec
                    DismissValue.DismissedToEnd, DismissValue.DismissedToStart -> dismissAnimationSpec
                },
                initialVelocity,
                block
            )
        }
    }

    internal suspend fun animateInternalToOffset(
        targetValue: Offset,
        animationSpec: AnimationSpec<Offset>,
        initialVelocity: Offset = Offset.Zero,
        block: (Animatable<Offset, AnimationVector2D>.() -> Unit)? = null
    ) {
        try {
            animatable.animateTo(
                targetValue, animationSpec, initialVelocity, block
            )
        } finally {
            currentValue = anchors[targetValue.x]!!
        }
    }

    suspend fun snapTo(
        targetValue: DismissValue
    ) {
        latestNonEmptyAnchorsFlow.collect { anchors ->
            snapInternalToOffset(Offset(anchors.getOffset(targetValue)!!, offset.y))
        }
    }

    internal suspend fun snapInternalToOffset(
        targetValue: Offset
    ) {
        animatable.snapTo(targetValue)
//
//        val targetOffset = anchors.getOffset(targetValue)
//        requireNotNull(targetOffset) {
//            "The target value must have an associated anchor."
//        }
//        snapInternalToOffset(targetOffset)
//        currentValue = targetValue
    }

    suspend fun stop() = animatable.stop()

    companion object {
        fun Saver(
            dismissAnimationSpec: AnimationSpec<Offset>,
            defaultAnimationSpec: AnimationSpec<Offset>,
        ) = Saver<YfooDismissibleState, DismissValue>(
            save = { it.currentValue },
            restore = { YfooDismissibleState(it, dismissAnimationSpec, defaultAnimationSpec) }
        )
    }
}

@Composable
fun rememberYfooDismissibleState(
    initialValue: DismissValue = DismissValue.Default,
    dismissAnimationSpec: AnimationSpec<Offset> = YfooDismissibleDefaults.DismissAnimSpec,
    defaultAnimationSpec: AnimationSpec<Offset> = YfooDismissibleDefaults.DefaultAnimSpec,
): YfooDismissibleState {
    return rememberSaveable(
        saver = YfooDismissibleState.Saver(
            dismissAnimationSpec = dismissAnimationSpec,
            defaultAnimationSpec = defaultAnimationSpec,
        )
    ) {
        YfooDismissibleState(
            initialValue = initialValue,
            dismissAnimationSpec = dismissAnimationSpec,
            defaultAnimationSpec = defaultAnimationSpec,
        )
    }
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

    suspend fun onDragStart(pointerOffset: Offset) {
        if (dragStartPosition == null) {
            dragStartPosition =
                if (pointerOffset.y < height / 2) DragStartPosition.Top else DragStartPosition.Bottom
        }
        //TODO do we need it
        state.stop()
    }

    suspend fun onDrag(offset: Offset) {
        state.snapInternalToOffset(state.offset + offset)
    }

    suspend fun onDragEnd(velocity: Offset) {
        try {
            when {
                abs(velocity.x) > flingMinVelocity -> {
                    // Dismiss because of fling
                    val directionByFling =
                        if (velocity.x > 0) DismissValue.DismissedToEnd else DismissValue.DismissedToStart
//                    val targetX = widthTillGone * directionByFling.directionMultiplier
                    state.animateTo(
                        targetValue = directionByFling,
                        initialVelocity = velocity,
                    )
                }
                abs(state.offsetXPercentTillGone) > 0.3f -> {
                    // Dismiss because threshold is reached
                    val directionByPointerLocation =
                        if (state.offsetXPercentTillGone < 0f) {
                            DismissValue.DismissedToStart
                        } else {
                            DismissValue.DismissedToEnd
                        }
//                    val targetX =
//                        widthTillGone * directionByPointerLocation.directionMultiplier
                    state.animateTo(
                        targetValue = directionByPointerLocation,
                    )
//                    state.animateTo(
//                        targetValue = Offset(targetX, state.offset.y),
//                        animationSpec = dismissAnimationSpec,
//                    )
                }
                else -> {
                    // Back to starting point
                    state.animateTo(
                        targetValue = DismissValue.Default,
                    )
                }
            }
        } finally {
            dragStartPosition = null
        }
    }

    LaunchedEffect(Unit) {
        //TODO do we even need dragChannel if we can just call onDrag* events directly?
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

    LaunchedEffect(widthTillGone, state) {
        state.processNewAnchors(widthTillGone)
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
            .offset { state.offset.round() }
            .graphicsLayer {
                rotationZ = state.rotation
            }
    ) {
        content()
    }
}

private fun findBounds(
    offset: Float,
    anchors: Set<Float>
): List<Float> {
    // Find the anchors the target lies between with a little bit of rounding error.
    val a = anchors.filter { it <= offset + 0.001 }.maxOrNull()
    val b = anchors.filter { it >= offset - 0.001 }.minOrNull()

    return when {
        a == null ->
            // case 1 or 3
            listOfNotNull(b)
        b == null ->
            // case 4
            listOf(a)
        a == b ->
            // case 2
            // Can't return offset itself here since it might not be exactly equal
            // to the anchor, despite being considered an exact match.
            listOf(a)
        else ->
            // case 5
            listOf(a, b)
    }
}

private fun computeTarget(
    offset: Float,
    lastValue: Float,
    anchors: Set<Float>,
    thresholds: (Float, Float) -> Float,
    velocity: Float,
    velocityThreshold: Float
): Float {
    val bounds = findBounds(offset, anchors)
    return when (bounds.size) {
        0 -> lastValue
        1 -> bounds[0]
        else -> {
            val lower = bounds[0]
            val upper = bounds[1]
            if (lastValue <= offset) {
                // Swiping from lower to upper (positive).
                if (velocity >= velocityThreshold) {
                    return upper
                } else {
                    val threshold = thresholds(lower, upper)
                    if (offset < threshold) lower else upper
                }
            } else {
                // Swiping from upper to lower (negative).
                if (velocity <= -velocityThreshold) {
                    return lower
                } else {
                    val threshold = thresholds(upper, lower)
                    if (offset > threshold) upper else lower
                }
            }
        }
    }
}

//TODO make private
sealed class DragEvent {
    class DragStarted(val pointerOffset: Offset) : DragEvent()
    class Drag(val offset: Offset) : DragEvent()
    class DragStopped(val velocity: Offset) : DragEvent()
    object DragCanceled : DragEvent()
}

//TODO make private
enum class DragStartPosition {
    Top, Bottom;

    val directionMultiplier get() = if (this == Top) 1 else -1
}

object YfooDismissibleDefaults {
    val DismissAnimSpec = SpringSpec<Offset>()
    val DefaultAnimSpec = SpringSpec<Offset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )
}