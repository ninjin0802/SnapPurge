package com.meita.snapshelf.ui

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(viewModel: DetailViewModel, onBack: () -> Unit) {
    val item by viewModel.screenshot.collectAsState()
    val context = LocalContext.current
    var showOcr by remember { mutableStateOf(false) }
    var deleteChoice by remember { mutableStateOf(false) }
    var confirmOriginalDelete by remember { mutableStateOf(false) }
    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) { viewModel.deleteIndexRecord(); onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("スクリーンショット") },
                navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "戻る") } },
                actions = {
                    IconButton({ viewModel.share(context) }) { Icon(Icons.Outlined.Share, "共有") }
                    IconButton({ deleteChoice = true }) { Icon(Icons.Outlined.MoreVert, "その他") }
                }
            )
        }
    ) { padding ->
        val screenshot = item
        if (screenshot == null) { Box(Modifier.fillMaxSize().padding(padding)) { Text("見つかりません", Modifier.padding(20.dp)) }; return@Scaffold }
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            AsyncImage(
                model = screenshot.uri, contentDescription = screenshot.displayName,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)), contentScale = ContentScale.FillWidth
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(screenshot.category) })
                screenshot.reminderAt?.let { time -> AssistChip(onClick = {}, leadingIcon = { Icon(Icons.Outlined.Notifications, null) }, label = { Text(DateFormat.getDateInstance().format(Date(time))) }) }
            }
            InfoSection("要約", screenshot.summary.ifBlank { "要約はまだありません。" })
            HorizontalDivider()
            Column {
                TextButton(onClick = { showOcr = !showOcr }, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Outlined.TextSnippet, null); Spacer(Modifier.width(8.dp)); Text(if (showOcr) "OCRテキストを閉じる" else "OCRテキストを表示")
                }
                AnimatedVisibility(showOcr) { Text(screenshot.ocrText.ifBlank { "OCR結果はありません。" }, style = MaterialTheme.typography.bodyMedium) }
            }
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(if (screenshot.isFavorite) "お気に入りから外す" else "お気に入りに追加") },
                leadingContent = { Icon(if (screenshot.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder, null) },
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                trailingContent = { Switch(screenshot.isFavorite, { viewModel.toggleFavorite() }) }
            )
            Spacer(Modifier.height(28.dp))
        }
    }

    if (deleteChoice) AlertDialog(
        onDismissRequest = { deleteChoice = false },
        icon = { Icon(Icons.Outlined.DeleteOutline, null) }, title = { Text("削除方法を選択") },
        text = { Text("元画像を残してSnapPurgeの一覧から外すか、端末から完全に削除できます。") },
        confirmButton = { TextButton(onClick = { deleteChoice = false; confirmOriginalDelete = true }) { Text("端末から削除", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { deleteChoice = false; viewModel.deleteIndexRecord(); onBack() }) { Text("一覧から外す") } }
    )

    if (confirmOriginalDelete) AlertDialog(
        onDismissRequest = { confirmOriginalDelete = false },
        title = { Text("元画像を削除しますか？") },
        text = { Text("この操作は端末の写真からも削除します。Androidの確認画面が続きます。") },
        confirmButton = { TextButton(onClick = {
            confirmOriginalDelete = false
            if (Build.VERSION.SDK_INT >= 30) viewModel.originalDeleteRequest(context)?.let(deleteLauncher::launch)
            else viewModel.deleteOriginalOnLegacyDevice(context) { if (it) onBack() }
        }) { Text("削除する", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { confirmOriginalDelete = false }) { Text("キャンセル") } }
    )
}

@Composable private fun InfoSection(title: String, body: String) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text(body, style = MaterialTheme.typography.bodyLarge)
}
