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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(
    private val id: Long,
    private val container: AppContainer
) : ViewModel() {
    val message = MutableStateFlow<String?>(null)
    val screenshot = container.repository.observeById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleFavorite() {
        viewModelScope.launch { container.repository.toggleFavorite(id) }
    }

    fun archive() {
        viewModelScope.launch { container.repository.toggleArchive(id) }
    }

    fun deleteIndexRecord() {
        viewModelScope.launch {
            runCatching { container.repository.deleteIndexRecord(id) }
                .onFailure { message.value = "一覧からの削除に失敗しました。もう一度お試しください。" }
        }
    }

    fun originalDeleteRequest(context: Context): IntentSenderRequest? {
        val item = screenshot.value ?: return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return runCatching {
            val pendingIntent = android.provider.MediaStore.createDeleteRequest(
                context.contentResolver,
                listOf(Uri.parse(item.uri))
            )
            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
        }.getOrElse {
            message.value = "Androidの削除確認を開けませんでした。画像へのアクセス権限を確認してください。"
            null
        }
    }

    fun deleteOriginalOnLegacyDevice(context: Context, onResult: (Boolean) -> Unit) {
        val item = screenshot.value ?: return onResult(false)
        viewModelScope.launch {
            val deleted = runCatching {
                context.contentResolver.delete(Uri.parse(item.uri), null, null) > 0
            }.getOrDefault(false)
            if (deleted) runCatching { container.repository.deleteIndexRecord(id) }
                .onFailure { message.value = "削除結果の更新に失敗しました。" }
            else message.value = "画像を削除できませんでした。アクセス権限を確認してください。"
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
