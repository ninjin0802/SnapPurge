package com.meita.snapshelf.ui

import android.net.Uri
import android.content.Context
import android.os.Build
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.meita.snapshelf.core.AppContainer
import com.meita.snapshelf.data.local.ScreenshotEntity
import com.meita.snapshelf.settings.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val screenshots: List<ScreenshotEntity> = emptyList(),
    val query: String = "",
    val category: String = "すべて",
    val isScanning: Boolean = false,
    val message: String? = null,
    val duplicateCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val container: AppContainer) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow("すべて")
    private val isScanning = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    private val baseItems = query.flatMapLatest { container.repository.search(it) }
    val uiState = combine(baseItems, query, category, isScanning, message) {
        items, queryValue, categoryValue, scanning, messageValue ->
        val filtered = if (categoryValue == "すべて") items else items.filter { it.category == categoryValue }
        HomeUiState(
            screenshots = filtered,
            query = queryValue,
            category = categoryValue,
            isScanning = scanning,
            message = messageValue,
            duplicateCount = items.count { it.duplicateGroup != null }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    val themeMode = container.userPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.System)

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategoryChange(value: String) {
        category.value = value
    }

    fun scanScreenshots() {
        viewModelScope.launch {
            isScanning.value = true
            message.value = null
            val count = runCatching { container.screenshotScanner.scanDeviceScreenshots() }.getOrElse {
                message.value = "読み取りに失敗しました。権限と画像の状態を確認してください。"
                0
            }
            if (count > 0) message.value = "${count}件を索引しました"
            isScanning.value = false
        }
    }

    fun importSelected(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            isScanning.value = true
            val count = container.screenshotScanner.importSelectedUris(uris)
            message.value = "${count}件を追加しました"
            isScanning.value = false
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch { container.repository.toggleFavorite(id) }
    }

    fun toggleArchive(id: Long) {
        viewModelScope.launch { container.repository.toggleArchive(id) }
    }

    fun deleteIndexRecords(ids: Set<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            runCatching { container.repository.deleteIndexRecords(ids) }
                .onFailure { message.value = "一覧からの削除に失敗しました。もう一度お試しください。" }
        }
    }

    fun originalDeleteRequest(context: Context, ids: Set<Long>): IntentSenderRequest? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val uris = uiState.value.screenshots.filter { it.id in ids }.mapNotNull { runCatching { Uri.parse(it.uri) }.getOrNull() }
        if (uris.isEmpty()) {
            message.value = "削除できる画像が見つかりませんでした。"
            return null
        }
        return runCatching {
            val pending = android.provider.MediaStore.createDeleteRequest(context.contentResolver, uris)
            IntentSenderRequest.Builder(pending.intentSender).build()
        }.getOrElse {
            message.value = "Androidの削除確認を開けませんでした。画像へのアクセス権限を確認してください。"
            null
        }
    }

    fun deleteOriginalsOnLegacyDevice(context: Context, ids: Set<Long>, onDone: () -> Unit) {
        val targets = uiState.value.screenshots.filter { it.id in ids }
        viewModelScope.launch {
            val deletedIds = targets.mapNotNull { item ->
                runCatching { context.contentResolver.delete(Uri.parse(item.uri), null, null) > 0 }
                    .getOrDefault(false).takeIf { it }?.let { item.id }
            }.toSet()
            runCatching { container.repository.deleteIndexRecords(deletedIds) }
                .onFailure { message.value = "削除結果の更新に失敗しました。" }
            if (deletedIds.size != targets.size) message.value = "一部の画像を削除できませんでした。アクセス権限を確認してください。"
            onDone()
        }
    }

    fun folderWriteRequest(context: Context): IntentSenderRequest? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val uris = uiState.value.screenshots.map { Uri.parse(it.uri) }
        if (uris.isEmpty()) return null
        val pending = android.provider.MediaStore.createWriteRequest(context.contentResolver, uris)
        return IntentSenderRequest.Builder(pending.intentSender).build()
    }

    fun prepareDeleteAll(context: Context, onReady: (Set<Long>, IntentSenderRequest?) -> Unit) {
        viewModelScope.launch {
            val items = container.repository.getAllActiveNow()
            val ids = items.mapTo(mutableSetOf()) { it.id }
            if (items.isEmpty()) {
                message.value = "削除する画像はありません。"
                onReady(emptySet(), null)
                return@launch
            }
            val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) runCatching {
                val uris = items.map { Uri.parse(it.uri) }
                val pending = android.provider.MediaStore.createDeleteRequest(context.contentResolver, uris)
                IntentSenderRequest.Builder(pending.intentSender).build()
            }.getOrElse {
                message.value = "Androidの削除確認を開けませんでした。画像へのアクセス権限を確認してください。"
                null
            } else null
            onReady(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && request == null) emptySet() else ids, request)
        }
    }

    fun organizeIntoCategoryFolders() {
        val items = uiState.value.screenshots
        viewModelScope.launch {
            val count = container.repository.moveToCategoryFolders(items)
            message.value = if (count > 0) "${count}件を分類フォルダーへ移動しました" else "移動できる画像がありませんでした"
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { container.userPreferences.setThemeMode(mode) }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(container) as T
    }
}
