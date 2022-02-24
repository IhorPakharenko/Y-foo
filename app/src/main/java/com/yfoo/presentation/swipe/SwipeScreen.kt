package com.yfoo.presentation.swipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yfoo.R
import com.yfoo.presentation.utils.Intents
import com.yfoo.presentation.utils.WindowSize
import com.yfoo.presentation.utils.iconForUi
import com.yfoo.presentation.utils.messageForUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel,
    windowSize: WindowSize,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    SwipeScreen(state, viewModel.effect, viewModel::onIntent, windowSize, modifier)
}

@Composable
fun SwipeScreen(
    state: SwipeState,
    effect: Flow<SwipeEffect>,
    onIntent: (SwipeIntent) -> Unit,
    windowSize: WindowSize,
    modifier: Modifier = Modifier,
) {
    val androidContext = LocalContext.current
    LaunchedEffect(Unit) {
        effect
            .onEach {
                when (it) {
                    is SwipeEffect.OpenImage -> TODO()
                    is SwipeEffect.OpenUrl -> Intents.toUrl(androidContext, it.url)
                }
            }
            .collect()
    }

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { newValue ->
            onIntent(
                SwipeIntent.SetSettingsVisibility(
                    isVisible = when (newValue) {
                        ModalBottomSheetValue.Expanded, ModalBottomSheetValue.HalfExpanded -> true
                        ModalBottomSheetValue.Hidden -> false
                    }
                )
            )

            true
        }
    )

    LaunchedEffect(state.areSettingsVisible) {
        if (state.areSettingsVisible) bottomSheetState.show() else bottomSheetState.hide()
    }

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
                    if (state.canRevertCard) {
                        IconButton(onClick = { onIntent(SwipeIntent.RevertLastCard) }) {
                            Icon(
                                imageVector = Icons.Filled.Undo,
                                contentDescription = stringResource(R.string.undo_last_choice),
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            onIntent(SwipeIntent.SetSettingsVisibility(isVisible = true))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                        )
                    }
                }
            )
        },
        content = { scaffoldPadding ->
            ModalBottomSheetLayout(
                sheetContent = {
                    ProviderSettings(
                        providerSettings = state.providers,
                        onCheckedChange = { providerSetting, isChecked ->
                            onIntent(SwipeIntent.ToggleProvider(providerSetting, isChecked))
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                sheetState = bottomSheetState,
            ) {
                Box(
                    Modifier
                        .background(Color.LightGray)
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    when (state.content) {
                        is SwipeState.Content.Cards -> {
                            val maxCardSizePercent =
                                if (windowSize == WindowSize.Compact) 1f else 0.8f

                            CardsFeed(
                                cards = state.content.value,
                                onLike = { onIntent(SwipeIntent.Like(it)) },
                                onDislike = { onIntent(SwipeIntent.Dislike(it)) },
                                onProviderClick = { onIntent(SwipeIntent.ViewProvider(it)) },
                                onImageClick = { onIntent(SwipeIntent.ViewImage(it)) },
                                bottomRowColor = Color.Black,
                                errorPlaceholder = { SwipeScreenError(it) },
                                modifier = Modifier
                                    .fillMaxSize(maxCardSizePercent)
                                    .align(Alignment.Center),
                            )
                        }
                        is SwipeState.Content.Error -> {
                            SwipeScreenError(throwable = state.content.value)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SwipeScreenError(
    throwable: Throwable,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            imageVector = throwable.iconForUi,
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(throwable.messageForUi),
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}