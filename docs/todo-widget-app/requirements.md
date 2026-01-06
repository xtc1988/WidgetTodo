# TODOウィジェットアプリ 要件定義

## 1. プロジェクト概要
- 目的: Androidホーム画面ウィジェットを主役にした個人向けTODOアプリを提供し、今日のタスク確認と完了操作を最速化する。
- 方針: シングルユーザー、完全ローカル保存、通知や同期なし。プライバシーと応答性を最優先し、シンプルかつモダンなUI/UXを追求する。

## 2. 想定ユーザーと利用シナリオ
- 対象: 日々の個人タスクをスマホで管理し、ホーム画面から素早く状況確認したいユーザー。
- 主な行動: 朝に今日の予定を整理→ウィジェットで進行確認→完了後即チェック→夜に履歴をざっと振り返る。

## 3. システム制約
- データは端末内(Room or DataStore)。ネットワーク通信やアカウント連携は行わない。
- 通知・リマインダー機能は非対応。
- オフライン前提で全機能が成立すること。

## 4. データモデル
`TaskEntity` (Room)
```
id: Long
title: String
note: String?
dueDate: LocalDate?
priority: Int (0=Low,1=Med,2=High)
tag: String?
status: TaskStatus (ACTIVE/COMPLETED)
orderIndex: Int
createdAt: Instant
completedAt: Instant?
```
補助: `PreferenceEntity(key TEXT PRIMARY KEY, value TEXT)` または DataStore でテーマ/スタイル設定、バックアップ用 `TaskExportDto` をJSONで入出力。

## 5. アプリ機能仕様
### 5.1 タスク管理
- タスク追加/編集/削除。タイトル必須、ノート/期日/優先度/タグは任意。
- ドラッグでリスト順を更新し、ウィジェット表示順と同期。
- フィルター: 「今日」「すべて」「完了済み」。検索やバックログ?今日の移動をサポート。
- 完了操作後は即反映＆5秒以内Undoのスナックバー表示。
- 履歴画面: 日ごとに完了タスクを表示し、テキスト共有でメモ書き的にエクスポート可能。

### 5.2 クイック追加
- ウィジェットの「+」またはクイック設定タイルからボトムシートを起動。
- フィールド: タイトル、今日に追加トグル、優先度簡易選択。

### 5.3 設定
- テーマ(システム/ライト/ダーク)、ウィジェット密度(コンパクト/スタンダード)。
- 10種スタイルパックから選択。プレビュー付き。
- JSONバックアップ/リストアを端末ファイルへ実行。

## 6. ウィジェット仕様
- レイアウト: 「Today」ヘッダー＋日付＋完了数。最大5件表示、スクロール対応のコレクションで拡張可。
- 各タスク: チェックボックス、タイトル、期日/タグのサブテキスト。完了タスクは非表示または淡表示(設定)。
- インタラクション: チェック→即座にRepository更新→`AppWidgetManager`へ通知。`+`でクイック追加シート起動。
- 更新トリガー: データ変更、日付変更(0:00 WorkManager)、ウィジェット手動更新。
- ダーク/ライト/動的カラーに追従。

## 7. 非機能要件
- パフォーマンス: アプリ起動1秒以内、ウィジェット更新体感300ms以内。
- 安定性: 端末再起動時に受信機で更新予約を再登録。低メモリ時でも状態保持。
- アクセシビリティ: 48dpタッチ領域、TalkBackラベル、コントラスト4.5:1。
- プライバシー: ネットアクセス不要。バックアップJSONの保存先を明示。

## 8. アーキテクチャ方針
- 技術スタック: Kotlin, Jetpack Compose, Glance(AppWidget), Room, Hilt, ViewModel+MVI, WorkManager。
- 層構成: UI(Compose/Glance) → ViewModel → UseCase → Repository → Room/DataStore。
- `TaskRepository` は `TaskDao` と `BackupManager` を統合し、`tasksTodayStream()` などのFlowを提供。
- Widgetは `FlowAppWidgetReceiver` で今日のタスクを購読し、操作は `ActionCallback` 経由でUseCaseへ。

## 9. テスト
- Unit: TaskDao、TaskRepository、ViewModel Reducer。
- UI: Composeスクリーンのスナップショットテスト、ウィジェットのライト/ダーク確認。
- マニュアル: バックアップ/リストア、端末再起動後の動作、TalkBack動作。
