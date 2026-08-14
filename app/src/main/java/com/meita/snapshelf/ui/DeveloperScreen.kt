package com.meita.snapshelf.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.meita.snapshelf.update.GitHubUpdateManager
import com.meita.snapshelf.update.UpdateResult

private const val DonationUrl = "https://ofuse.me/ninjin"
private const val XUrl = "https://x.com/_nin82"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(onBack: () -> Unit, onOpenPrivacy: () -> Unit, onOpenTerms: () -> Unit) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "不明"
    fun open(url: String) = context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    val scope = rememberCoroutineScope()
    val updater = remember { GitHubUpdateManager(context.applicationContext) }
    var updateState by remember { mutableStateOf<UpdateResult?>(null) }
    var checking by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Developer") },
            navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "戻る") } }
        )
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Row(Modifier.padding(22.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primary) {
                        Icon(Icons.Outlined.Person, null, Modifier.padding(14.dp).size(28.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("ninjin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("SnapPurge Developer", style = MaterialTheme.typography.bodyMedium)
                        Text("Version $version", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .72f))
                    }
                }
            }

            SectionLabel("アップデート")
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("GitHub Releasesから安全に最新版を確認します。", style = MaterialTheme.typography.bodyMedium)
                    FilledTonalButton(
                        onClick = {
                            val ready = updateState as? UpdateResult.Ready
                            if (ready != null) updater.launchInstaller(ready.apk)
                            else scope.launch { checking = true; updateState = updater.checkAndDownload(); checking = false }
                        },
                        enabled = !checking,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (checking) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Icon(if (updateState is UpdateResult.Ready) Icons.Outlined.InstallMobile else Icons.Outlined.SystemUpdate, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (updateState is UpdateResult.Ready) "v${(updateState as UpdateResult.Ready).version}をインストール" else "アップデートを確認")
                    }
                    when (val result = updateState) {
                        UpdateResult.UpToDate -> Text("最新版です", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        is UpdateResult.Failed -> Text(result.reason, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        is UpdateResult.Ready -> Text("署名とSHA-256を確認済み", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        null -> Unit
                    }
                }
            }

            SectionLabel("サポート・リンク")
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                Column {
                    ListItem(
                        headlineContent = { Text("OFUSEで応援する") },
                        supportingContent = { Text("応援が開発継続と品質改善の力になります") },
                        leadingContent = { Icon(Icons.Outlined.FavoriteBorder, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Outlined.OpenInNew, null) },
                        modifier = Modifier.clickable { open(DonationUrl) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("ninjin on X") }, supportingContent = { Text("@_nin82") },
                        leadingContent = { Icon(Icons.Outlined.AlternateEmail, null) },
                        trailingContent = { Icon(Icons.Outlined.OpenInNew, null) },
                        modifier = Modifier.clickable { open(XUrl) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
            }

            SectionLabel("法的情報")
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                Column {
                    LegalRow("プライバシーポリシー", Icons.Outlined.PrivacyTip, onOpenPrivacy)
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    LegalRow("利用規約", Icons.Outlined.Description, onOpenTerms)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
}

@Composable private fun LegalRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) }, leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Icons.Outlined.ChevronRight, null) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    )
}

val PrivacyPolicyText = """
施行日: 2026年8月14日

SnapPurgeは、スクリーンショットの画像、OCR結果、分類、検索語、タグ、リマインダーを端末内で処理します。これらの情報を開発者のサーバーへ送信、収集、販売、第三者提供しません。

「アップデートを確認」を押した場合に限りGitHub Releasesへ接続し、最新版情報、APK、SHA-256を取得します。スクリーンショットや解析情報は送信せず、バックグラウンド確認も行いません。

アプリは写真へのアクセス権限を、ユーザーが選択した画像の表示・解析・整理・削除のために使用します。通知権限は、ユーザーが設定したリマインダーを知らせるために使用します。

外部リンクとしてOFUSEおよびXを開く場合、遷移先サービスには各サービスのプライバシーポリシーが適用されます。SnapPurgeはリンク先で入力された情報を取得しません。

アプリ内データは、一覧からの削除またはデータ消去操作で削除できます。端末の元画像は、ユーザーが明示的に端末削除を承認した場合のみ削除されます。

お問い合わせは開発者Xアカウント @_nin82 へお願いします。
""".trimIndent()

val TermsText = """
施行日: 2026年8月14日

SnapPurgeは、端末内のスクリーンショットを整理するためのアプリです。本アプリを利用することで、本規約に同意したものとみなされます。

ユーザーは、適用される法令および第三者の権利を守って本アプリを利用してください。画像の削除や移動を実行する前に、対象を確認し、必要に応じてバックアップしてください。

開発者は品質維持に努めますが、分類・OCR・要約・期限抽出の完全な正確性、データの永続保存、特定目的への適合性を保証しません。重要な予定や情報は必ず原本でも確認してください。

法令で認められる範囲において、本アプリの利用または利用不能から生じた間接的・付随的損害について、開発者は責任を負いません。消費者保護法その他の強行法規に基づく権利を制限するものではありません。

機能、提供条件、本規約は必要に応じて変更される場合があります。重要な変更はアプリ更新情報等で告知します。

開発者: ninjin / X: @_nin82
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(title: String, body: String, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "戻る") } }) }) { padding ->
        Text(body, Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), style = MaterialTheme.typography.bodyLarge)
    }
}
