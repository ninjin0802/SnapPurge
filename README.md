<div align="center">

# SnapPurge

**スクリーンショットを、端末内だけで見つけやすく整理するAndroidアプリ。**

[![Version](https://img.shields.io/badge/version-0.4.0-6750A4?style=flat-square)](CHANGELOG.md)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Privacy](https://img.shields.io/badge/privacy-local--only-2563EB?style=flat-square&logo=shield&logoColor=white)
[![GitHub last commit](https://img.shields.io/github/last-commit/ninjin0802/SnapPurge?style=flat-square)](https://github.com/ninjin0802/SnapPurge/commits/main)

**日本語** · [English](README_EN.md)

</div>

## SnapPurgeとは

SnapPurgeは、増え続けるスクリーンショットをOCRで読み取り、検索・分類・整理できるAndroidアプリです。画像と解析結果は外部サーバーへ送らず、端末内で処理します。

## 主な機能

- 端末内OCRによる文字抽出と全文検索
- 買い物、予定、仕事、学習、SNS、旅行、金融、メモへの自動分類
- `Pictures/SnapPurge/<カテゴリ>`へのフォルダー整理
- 要約、タグ候補、期限候補、リマインダー
- 類似・重複スクリーンショットの検出
- 個別削除、複数選択削除、すべて削除
- 「一覧から外す」と「端末の元画像を削除」を明確に分離
- システム連動、ライト、ダークのテーマ切替
- 画像解析は完全ローカル。通信はユーザー操作によるGitHub更新確認のみ
- GitHub Releasesからの更新確認、署名・SHA-256検証、APKインストール

## 必要環境

| 項目 | 内容 |
|---|---|
| Android | Android 8.0（API 26）以上 |
| 現在のバージョン | 0.4.0（versionCode 5） |
| 配布方法 | GitHub Releases |
| データ処理 | 端末内のみ |

## インストール

正式な配布APKは[GitHub Releases](https://github.com/ninjin0802/SnapPurge/releases)へ掲載します。

1. Releasesから最新の署名済みAPKをダウンロードします。
2. Androidの案内に従い、ダウンロード元からのインストールを許可します。
3. APKを開いてインストールします。
4. 初回起動後、写真へのアクセス範囲を選択します。

> GitHub以外で再配布されたAPKは使用せず、Releaseに記載されたSHA-256と一致することを確認してください。

## ソースからビルド

Android Studioでこのリポジトリを開き、JDK 17以上とAndroid SDK Platform 37.0を指定します。

```powershell
./gradlew testDebugUnitTest lintDebug assembleDebug
```

生成先：`app/build/outputs/apk/debug/app-debug.apk`

## プライバシーと安全性

- スクリーンショット、OCR結果、検索語、分類結果を外部送信しません。
- 更新確認ボタンを押した場合のみGitHubへ接続します。バックグラウンド通信は行いません。
- 元画像の移動・削除には、Androidが要求するシステム確認を使用します。
- 一覧から削除しただけでは、端末の元画像は削除されません。
- OFUSEやXを開いた後は、各外部サービスの規約が適用されます。

[プライバシーポリシー](PRIVACY_POLICY.md) · [利用規約](TERMS.md)

## バージョン表記と更新ルール

バージョンは一般的なセマンティックバージョニング `MAJOR.MINOR.PATCH` で管理します。

- `MAJOR`: 互換性を壊す大きな変更
- `MINOR`: 後方互換性のある新機能
- `PATCH`: 後方互換性のある不具合修正

アプリを更新するたびに`versionCode`を増やし、`versionName`、アプリ内Developerページ、バージョンバッジ、`CHANGELOG.md`を一致させます。詳細は[VERSIONING.md](VERSIONING.md)を参照してください。

## リリース

GitHub Releasesへ公開する前に、署名、実機試験、Lint、テスト、チェックサム、規約の整合性を確認します。

[変更履歴](CHANGELOG.md) · [リリースチェックリスト](RELEASE_CHECKLIST.md)

## Developer

開発者：**ninjin**

- X: [@_nin82](https://x.com/_nin82)
- OFUSE: [ofuse.me/ninjin](https://ofuse.me/ninjin)

SnapPurgeが役に立ったら、OFUSEで応援していただけるとうれしいです。いただいた応援は、実機検証、品質改善、新機能の開発に活用します。

## ドキュメント

- [開発計画](PLAN.md)
- [UI改善記録](UI_REFINEMENT.md)
- [バージョン規則](VERSIONING.md)
- [変更履歴](CHANGELOG.md)
- [プライバシーポリシー](PRIVACY_POLICY.md)
- [利用規約](TERMS.md)
- [リリースチェックリスト](RELEASE_CHECKLIST.md)
