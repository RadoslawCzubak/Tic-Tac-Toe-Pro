package pl.radoslav.game.implementation.findgame.presentation.host

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FindGameHostViewModel
    @Inject
    constructor() : ViewModel() {
        private val _state by lazy { MutableStateFlow(FindGameHostState()) }
        val state = _state.asStateFlow()
    }
