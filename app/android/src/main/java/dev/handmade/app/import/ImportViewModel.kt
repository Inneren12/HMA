package dev.handmade.app.import

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.io.ImageIoError
import core.io.ImageSource
import core.io.SourceImage
import core.io.loadImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ImportUiState(
    val isLoading: Boolean = false,
    val image: SourceImage? = null,
    val error: String? = null
)

class ImportViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val source = ImageSource.UriString(uri.toString())
                val result = withContext(ioDispatcher) {
                    loadImage(source)
                }
                _uiState.update { it.copy(isLoading = false, image = result) }
            } catch (t: Throwable) {
                val msg = when (t) {
                    is ImageIoError -> t.message ?: t::class.simpleName ?: "Image I/O error"
                    else -> t.message ?: "Unexpected error"
                }
                _uiState.update { it.copy(isLoading = false, error = msg, image = null) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
