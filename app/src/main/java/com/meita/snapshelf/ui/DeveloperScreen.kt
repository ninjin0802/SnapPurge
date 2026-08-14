package com.meita.snapshelf.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ninjin", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("SnapPurgeを作っています。スクショ整理が少しでも快適になればうれしいです。")
                    Text("Version $version", style = MaterialTheme.typography.labelLarge)
                }
            }

            Text("アップデート", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("GitHub Releasesから最新版を確認します。通信はこのボタンを押したときだけ行います。")
            Button(
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
                UpdateResult.UpToDate -> Text("最新版です。", color = MaterialTheme.colorScheme.primary)
                is UpdateResult.Failed -> Text(result.reason, color = MaterialTheme.colorScheme.error)
                is UpdateResult.Ready -> Text("署名とSHA-256を確認済みです。インストール時はAndroidの案内に従ってください。")
                null -> Unit
            }

            Text("SnapPurgeを応援", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("もしSnapPurgeが役に立ったら、OFUSEで応援していただけると開発継続の大きな力になります。いただいた応援は、品質改善と新機能の開発に活用します。")
            Button(onClick = { open(DonationUrl) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.FavoriteBorder, null); Spacer(Modifier.width(8.dp)); Text("OFUSEで応援する")
            }

            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Xでninjinをフォロー") },
                supportingContent = { Text("@_nin82") },
                leadingContent = { Icon(Icons.Outlined.AlternateEmail, null) },
                trailingContent = { Icon(Icons.Outlined.OpenInNew, null) },
                modifier = Modifier.fillMaxWidth(),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
            TextButton(onClick = { open(XUrl) }, modifier = Modifier.fillMaxWidth()) { Text("Xを開く") }

            HorizontalDivider()
            Text("アプリ情報", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            ListItem(headlineContent = { Text("プライバシーポリシー") }, leadingContent = { Icon(Icons.Outlined.PrivacyTip, null) }, trailingContent = { Icon(Icons.Outlined.ChevronRight, null) })
            TextButton(onClick = onOpenPrivacy, modifier = Modifier.fillMaxWidth()) { Text("内容を確認") }
            ListItem(headlineContent = { Text("利用規約") }, leadingContent = { Icon(Icons.Outlined.Description, null) }, trailingContent = { Icon(Icons.Outlined.ChevronRight, null) })
            TextButton(onClick = onOpenTerms, modifier = Modifier.fillMaxWidth()) { Text("内容を確認") }
        }
    }
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
