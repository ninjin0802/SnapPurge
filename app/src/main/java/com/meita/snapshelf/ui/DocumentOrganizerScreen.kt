package com.meita.snapshelf.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.DateFormat
import java.util.Date

private val DocumentMimeTypes = arrayOf(
    "application/pdf", "text/plain", "text/csv", "application/rtf",
    "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
)

private data class LocalDocument(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val size: Long,
    val modifiedAt: Long,
    val hash: String?,
    val reason: String?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentOrganizerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var documents by remember { mutableStateOf<List<LocalDocument>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var scanning by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            scanning = true
            documents = scanDocuments(context, uris)
            selected = emptySet()
            scanning = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text("ドキュメント整理") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Outlined.ArrowBack, "戻る") } }) },
        bottomBar = {
            if (selected.isNotEmpty()) Surface(shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("${selected.size}件を選択", fontWeight = FontWeight.SemiBold)
                        Text(formatBytes(documents.filter { it.uri in selected }.sumOf { it.size }), style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { confirmDelete = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Outlined.DeleteOutline, null); Spacer(Modifier.width(8.dp)); Text("削除")
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                SegmentedButton(false, onClick = onBack, shape = SegmentedButtonDefaults.itemShape(0, 2), label = { Text("スクショ") }, icon = { Icon(Icons.Outlined.PhotoLibrary, null, Modifier.size(18.dp)) })
                SegmentedButton(true, onClick = {}, shape = SegmentedButtonDefaults.itemShape(1, 2), label = { Text("ドキュメント") }, icon = { Icon(Icons.Outlined.Description, null, Modifier.size(18.dp)) })
            }
            if (scanning) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (documents.isEmpty() && !scanning) DocumentEmptyState { picker.launch(DocumentMimeTypes) }
            else LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("確認して削除", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("候補を確認し、削除する文書だけを選んでください。", style = MaterialTheme.typography.bodySmall)
                        }
                        FilledTonalButton(onClick = { picker.launch(DocumentMimeTypes) }) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(6.dp)); Text("選択") }
                    }
                }
                items(documents, key = { it.uri.toString() }) { doc ->
                    DocumentRow(doc, doc.uri in selected, {
                        if (doc.uri in selected) selected -= doc.uri
                        else {
                            val sameContent = documents.filter { it.hash != null && it.hash == doc.hash }
                            if (sameContent.size > 1 && sameContent.all { it.uri == doc.uri || it.uri in selected }) {
                                scope.launch { snackbar.showSnackbar("同じ内容の文書を少なくとも1件残してください。") }
                            } else selected += doc.uri
                        }
                    }) {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, doc.uri).apply { setDataAndType(doc.uri, doc.mimeType); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }) }
                    }
                }
            }
        }
    }

    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        icon = { Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("選択した文書を削除しますか？") },
        text = { Text("${selected.size}件（${formatBytes(documents.filter { it.uri in selected }.sumOf { it.size })}）を端末から削除します。この操作は元に戻せない場合があります。") },
        confirmButton = { TextButton(onClick = {
            confirmDelete = false
            scope.launch {
                val targets = selected
                val targetDocuments = documents.filter { it.uri in targets }
                val deleted = withContext(Dispatchers.IO) { targetDocuments.filterTo(mutableSetOf()) { deleteDocumentSafely(context, it) }.mapTo(mutableSetOf()) { it.uri } }
                documents = documents.filterNot { it.uri in deleted }
                selected -= deleted
                snackbar.showSnackbar(if (deleted.size == targets.size) "${deleted.size}件を削除しました" else "${deleted.size}件を削除しました。削除できない文書は一覧に残しています。")
            }
        }) { Text("削除する", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("キャンセル") } },
    )
}

@Composable private fun DocumentEmptyState(onPick: () -> Unit) = Column(
    Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
) {
    Icon(Icons.Outlined.FolderOpen, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(16.dp)); Text("整理する文書を選択", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("PDF・テキスト・Office文書を端末から選び、古いファイルや完全重複を確認できます。", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(20.dp)); Button(onClick = onPick) { Text("ドキュメントを選択") }
}

@Composable private fun DocumentRow(doc: LocalDocument, selected: Boolean, onToggle: () -> Unit, onPreview: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(selected, onCheckedChange = { onToggle() })
            Icon(if (doc.mimeType == "application/pdf") Icons.Outlined.PictureAsPdf else Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(doc.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text("${formatBytes(doc.size)} ・ ${if (doc.modifiedAt > 0) DateFormat.getDateInstance().format(Date(doc.modifiedAt)) else "更新日不明"}", style = MaterialTheme.typography.bodySmall)
                doc.reason?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error) }
            }
            IconButton(onClick = onPreview) { Icon(Icons.Outlined.OpenInNew, "開く") }
        }
    }
}

private suspend fun scanDocuments(context: Context, uris: List<Uri>): List<LocalDocument> = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    val raw = uris.distinct().map { uri ->
        var name = uri.lastPathSegment ?: "Document"
        var size = 0L
        var modified = 0L
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE, DocumentsContract.Document.COLUMN_LAST_MODIFIED), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = cursor.getString(it) ?: name }
                    cursor.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { size = if (cursor.isNull(it)) 0 else cursor.getLong(it) }
                    cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED).takeIf { it >= 0 }?.let { modified = if (cursor.isNull(it)) 0 else cursor.getLong(it) }
                }
            }
        }
        val hash = runCatching { sha256(context, uri) }.getOrNull()
        LocalDocument(uri, name, context.contentResolver.getType(uri) ?: "application/octet-stream", size, modified, hash, null)
    }
    val duplicateHashes = raw.mapNotNull { it.hash }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    raw.map { doc ->
        val reason = when {
            doc.hash in duplicateHashes -> "完全に同じ内容の文書があります"
            doc.size == 0L -> "空またはサイズ不明（内容を確認してください）"
            doc.modifiedAt > 0 && now - doc.modifiedAt > 180L * 24 * 60 * 60 * 1000 -> "180日以上前の文書"
            else -> null
        }
        doc.copy(reason = reason)
    }.sortedWith(compareByDescending<LocalDocument> { it.reason != null }.thenByDescending { it.modifiedAt })
}

private fun sha256(context: Context, uri: Uri): String {
    val digest = MessageDigest.getInstance("SHA-256")
    context.contentResolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(64 * 1024)
        while (true) { val read = input.read(buffer); if (read < 0) break; digest.update(buffer, 0, read) }
    } ?: error("文書を開けません")
    return digest.digest().joinToString("") { "%02x".format(it) }
}

private fun deleteDocumentSafely(context: Context, document: LocalDocument): Boolean = runCatching {
    val scannedHash = document.hash ?: return@runCatching false
    check(sha256(context, document.uri) == scannedHash) { "選択後に内容が変更されました" }
    DocumentsContract.deleteDocument(context.contentResolver, document.uri)
}.getOrDefault(false)

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024 * 1024 -> "%.1f GB".format(bytes / (1024.0 * 1024 * 1024))
    bytes >= 1024L * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
