package com.meita.snapshelf.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.meita.snapshelf.core.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(
    private val id: Long,
    private val container: AppContainer
) : ViewModel() {
    val screenshot = container.repository.observeById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleFavorite() {
        viewModelScope.launch { container.repository.toggleFavorite(id) }
    }

    fun archive() {
        viewModelScope.launch { container.repository.toggleArchive(id) }
    }

    fun deleteIndexRecord() {
        viewModelScope.launch { container.repository.deleteIndexRecord(id) }
    }

    fun originalDeleteRequest(context: Context): IntentSenderRequest? {
        val item = screenshot.value ?: return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val pendingIntent = android.provider.MediaStore.createDeleteRequest(
            context.contentResolver,
            listOf(Uri.parse(item.uri))
        )
        return IntentSenderRequest.Builder(pendingIntent.intentSender).build()
    }

    fun deleteOriginalOnLegacyDevice(context: Context, onResult: (Boolean) -> Unit) {
        val item = screenshot.value ?: return onResult(false)
        viewModelScope.launch {
            val deleted = runCatching {
                context.contentResolver.delete(Uri.parse(item.uri), null, null) > 0
            }.getOrDefault(false)
            if (deleted) container.repository.deleteIndexRecord(id)
            onResult(deleted)
        }
    }

    fun share(context: Context) {
        val item = screenshot.value ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, Uri.parse(item.uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "スクリーンショットを共有"))
    }

    class Factory(private val id: Long, private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DetailViewModel(id, container) as T
    }
}
