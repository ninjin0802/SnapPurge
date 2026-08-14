package com.meita.snapshelf.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.meita.snapshelf.data.local.ScreenshotEntity
import com.meita.snapshelf.settings.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onOpenDetail: (Long) -> Unit, onOpenDeveloper: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val theme by viewModel.themeMode.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var addMenu by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showBatchDelete by remember { mutableStateOf(false) }
    var moreMenu by remember { mutableStateOf(false) }
    var showDeleteAll by remember { mutableStateOf(false) }
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) viewModel.scanScreenshots() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(50)) { viewModel.importSelected(it) }
    val batchDeleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) { viewModel.deleteIndexRecords(selectedIds); selectedIds = emptySet() }
    }
    val folderWriteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.organizeIntoCategoryFolders()
    }
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            if (message.contains("索引しました")) {
                if (Build.VERSION.SDK_INT >= 30) viewModel.folderWriteRequest(context)?.let(folderWriteLauncher::launch)
                else viewModel.organizeIntoCategoryFolders()
            }
            snackbar.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (selectedIds.isEmpty()) "SnapPurge" else "${selectedIds.size}件を選択", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { if (selectedIds.isNotEmpty()) IconButton({ selectedIds = emptySet() }) { Icon(Icons.Outlined.Close, "選択を解除") } },
                actions = {
                    if (selectedIds.isNotEmpty()) IconButton({ showBatchDelete = true }) { Icon(Icons.Outlined.Delete, "まとめて削除") }
                    else {
                        ThemeMenu(theme, viewModel::setThemeMode)
                        Box {
                            IconButton({ moreMenu = true }) { Icon(Icons.Outlined.MoreVert, "その他") }
                            DropdownMenu(moreMenu, { moreMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Developer") },
                                    leadingIcon = { Icon(Icons.Outlined.Person, null) },
                                    onClick = { moreMenu = false; onOpenDeveloper() }
                                )
                                DropdownMenuItem(
                                    text = { Text("すべて削除") },
                                    leadingIcon = { Icon(Icons.Outlined.DeleteSweep, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { moreMenu = false; showDeleteAll = true }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                ExtendedFloatingActionButton(
                    onClick = { addMenu = true },
                    icon = { Icon(Icons.Outlined.AddPhotoAlternate, null) },
                    text = { Text("追加") }
                )
                DropdownMenu(expanded = addMenu, onDismissRequest = { addMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("画像を選んで追加") },
                        leadingIcon = { Icon(Icons.Outlined.PhotoLibrary, null) },
                        onClick = { addMenu = false; picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    )
                    DropdownMenuItem(
                        text = { Text("スクショを再スキャン") },
                        leadingIcon = { Icon(Icons.Outlined.Sync, null) },
                        onClick = {
                            addMenu = false
                            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) viewModel.scanScreenshots()
                            else permissionLauncher.launch(permission)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("分類フォルダーへ整理") },
                        leadingIcon = { Icon(Icons.Outlined.DriveFileMove, null) },
                        onClick = {
                            addMenu = false
                            if (Build.VERSION.SDK_INT >= 30) viewModel.folderWriteRequest(context)?.let(folderWriteLauncher::launch)
                            else viewModel.organizeIntoCategoryFolders()
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SearchField(state.query, viewModel::onQueryChange)
            CategoryRow(state.category, viewModel::onCategoryChange)
            when {
                state.isScanning -> LinearProgressIndicator(Modifier.fillMaxWidth())
                state.screenshots.isEmpty() -> EmptyState { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                else -> ScreenshotGrid(
                    items = state.screenshots, selectedIds = selectedIds,
                    onOpen = { id -> if (selectedIds.isEmpty()) onOpenDetail(id) else selectedIds = selectedIds.toggle(id) },
                    onLongPress = { id -> selectedIds = selectedIds.toggle(id) }
                )
            }
        }
    }

    if (showBatchDelete) AlertDialog(
        onDismissRequest = { showBatchDelete = false },
        icon = { Icon(Icons.Outlined.DeleteSweep, null) },
        title = { Text("${selectedIds.size}件をまとめて削除") },
        text = { Text("端末の元画像を残して一覧から外すこともできます。") },
        confirmButton = { TextButton(onClick = {
            showBatchDelete = false
            if (Build.VERSION.SDK_INT >= 30) viewModel.originalDeleteRequest(context, selectedIds)?.let(batchDeleteLauncher::launch)
            else viewModel.deleteOriginalsOnLegacyDevice(context, selectedIds) { selectedIds = emptySet() }
        }) { Text("端末から削除", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { showBatchDelete = false; viewModel.deleteIndexRecords(selectedIds); selectedIds = emptySet() }) { Text("一覧から外す") } }
    )

    if (showDeleteAll) AlertDialog(
        onDismissRequest = { showDeleteAll = false },
        icon = { Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("すべてのスクショを削除？") },
        text = { Text("SnapPurgeが整理している全スクショが対象です。元画像を残して一覧だけ空にすることもできます。") },
        confirmButton = { TextButton(onClick = {
            showDeleteAll = false
            viewModel.prepareDeleteAll(context) { ids, request ->
                selectedIds = ids
                if (Build.VERSION.SDK_INT >= 30) request?.let(batchDeleteLauncher::launch)
                else viewModel.deleteOriginalsOnLegacyDevice(context, ids) { selectedIds = emptySet() }
            }
        }) { Text("端末からすべて削除", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = {
            showDeleteAll = false
            viewModel.prepareDeleteAll(context) { ids, _ -> viewModel.deleteIndexRecords(ids) }
        }) { Text("一覧だけ空にする") } }
    )
}

@Composable private fun SearchField(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = query, onValueChange = onChange, singleLine = true,
        placeholder = { Text("スクショ内の文字を検索") },
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        trailingIcon = { if (query.isNotEmpty()) IconButton({ onChange("") }) { Icon(Icons.Outlined.Close, "検索を消去") } },
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable private fun CategoryRow(selected: String, onSelect: (String) -> Unit) {
    val categories = listOf("すべて", "メモ", "予定", "買い物", "仕事", "学習", "SNS", "旅行", "金融")
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { label -> FilterChip(selected = selected == label, onClick = { onSelect(label) }, label = { Text(label) }) }
    }
}

@Composable private fun ScreenshotGrid(items: List<ScreenshotEntity>, selectedIds: Set<Long>, onOpen: (Long) -> Unit, onLongPress: (Long) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(132.dp),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        items(items, key = { it.id }) { item -> ScreenshotTile(item, item.id in selectedIds, { onOpen(item.id) }, { onLongPress(item.id) }) }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable private fun ScreenshotTile(item: ScreenshotEntity, selected: Boolean, onClick: () -> Unit, onLongPress: () -> Unit) {
    Column(Modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress)) {
        Box {
            AsyncImage(
                model = item.uri,
                contentDescription = "${item.category}、${item.summary.ifBlank { item.displayName }}",
                modifier = Modifier.fillMaxWidth().aspectRatio(.78f).clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
            if (item.isFavorite) {
                Surface(Modifier.align(Alignment.TopEnd).padding(8.dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface.copy(alpha = .88f)) {
                    Icon(Icons.Outlined.Star, "お気に入り", Modifier.padding(6.dp).size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (selected) {
                Surface(Modifier.matchParentSize(), color = MaterialTheme.colorScheme.primary.copy(alpha = .22f), shape = RoundedCornerShape(18.dp)) {}
                Surface(Modifier.align(Alignment.TopStart).padding(8.dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Outlined.Check, "選択済み", Modifier.padding(5.dp).size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(item.summary.ifBlank { item.displayName }, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(item.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Set<Long>.toggle(id: Long): Set<Long> = if (id in this) this - id else this + id

@Composable private fun EmptyState(onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Collections, null, Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(18.dp)); Text("スクショを追加しましょう", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp)); Text("画像と解析結果は、この端末の外へ送信されません。", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(22.dp)); Button(onClick = onAdd) { Text("画像を選ぶ") }
    }
}

@Composable private fun ThemeMenu(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton({ open = true }) { Icon(Icons.Outlined.Contrast, "表示テーマ") }
        DropdownMenu(open, { open = false }) {
            listOf(ThemeMode.System to "システム", ThemeMode.Light to "ライト", ThemeMode.Dark to "ダーク").forEach { (mode, label) ->
                DropdownMenuItem(text = { Text(label) }, trailingIcon = { if (current == mode) Icon(Icons.Outlined.Check, null) }, onClick = { onSelect(mode); open = false })
            }
        }
    }
}
