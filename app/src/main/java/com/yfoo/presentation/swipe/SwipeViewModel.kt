package com.yfoo.presentation.swipe

import com.yfoo.presentation.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SwipeViewModel @Inject constructor() :
    MviViewModel<SwipeState, SwipeOneShotEvent>(SwipeState())