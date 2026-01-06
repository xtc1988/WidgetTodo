# WidgetTodo - 設計仕様書

## 概要

Androidホーム画面ウィジェットをメインとしたシンプルなTODOアプリ。
ウィジェット上で完結する操作性を重視し、アプリ本体は最小限の機能に留める。

## 基本情報

| 項目 | 値 |
|------|-----|
| アプリ名 | WidgetTodo |
| パッケージ名 | com.example.widgettodo |
| 対応OS | Android 12+ (API 31) |
| 言語 | Kotlin |
| UIフレームワーク | Jetpack Compose / Glance API |
| アーキテクチャ | MVVM |
| DI | Hilt |
| データ保存 | Room（ローカルのみ） |
| リリース先 | Google Play |

---

## ウィジェット仕様

### サイズ・レイアウト

- **サイズ**: 4x2（中サイズ）1種類のみ
- **ヘッダー**: なし
- **背景**: 半透明
- **角丸**: なし（シャープな四角形）
- **テーマ**: システム追従（ダーク/ライト自動切替）

### 表示内容

```
┌─────────────────────────────────┐
│                                 │
│ ☐ TODO 1のテキスト...          │
│ ☐ TODO 2のテキスト...          │
│ ☐ TODO 3のテキスト...          │
│ ☐ TODO 4のテキスト...          │  ← スクロール可能
│                              [📱][+] │  ← FAB（横並び、右下）
└─────────────────────────────────┘
```

### 操作

| 操作 | 動作 |
|------|------|
| チェックボックスタップ | 即座に完了（削除）+ フェードアウトアニメーション |
| +ボタンタップ | TODO追加ダイアログ表示（Activity経由） |
| アプリ起動ボタンタップ | メインアプリを開く |
| リスト部分スクロール | TODOリストをスクロール |

### 空状態

TODOが0件の場合：
- 「+をタップしてタスクを追加」のようなガイドテキストを表示

### フォント

- TODOテキスト: 14sp
- 長いテキスト: 1行で切り捨て（...）

### 更新タイミング

- データ変更時のみ（バッテリー節約）

---

## TODOデータ仕様

### データモデル

```kotlin
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

### 属性

- タイトル（テキスト）のみ
- 期限・優先度なし

### 並び順

- 追加順（新しいものが上）

### 完了時の動作

- 即座に削除（履歴なし）
- ウィジェット: アニメーション付きで消える
- アプリ内: Undoスナックバー表示（数秒間取り消し可能）

---

## メインアプリ仕様

### 機能

- TODOリスト一覧表示
- TODO追加
- TODO削除（スワイプ or 完了チェック）
- Undoスナックバー

### 含まれないもの

- 設定画面
- テーマ選択
- データエクスポート

---

## 技術スタック

### 依存関係

```kotlin
// Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// Compose
implementation("androidx.compose.ui:ui:1.6.0")
implementation("androidx.compose.material3:material3:1.2.0")
implementation("androidx.activity:activity-compose:1.8.2")

// Glance (Widget)
implementation("androidx.glance:glance-appwidget:1.0.0")
implementation("androidx.glance:glance-material3:1.0.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Hilt
implementation("com.google.dagger:hilt-android:2.50")
ksp("com.google.dagger:hilt-compiler:2.50")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
```

### アーキテクチャ構成

```
app/
├── data/
│   ├── local/
│   │   ├── TodoDao.kt
│   │   ├── TodoDatabase.kt
│   │   └── entity/Todo.kt
│   └── repository/
│       └── TodoRepository.kt
├── di/
│   ├── AppModule.kt
│   └── DatabaseModule.kt
├── ui/
│   ├── main/
│   │   ├── MainActivity.kt
│   │   ├── MainScreen.kt
│   │   └── MainViewModel.kt
│   ├── add/
│   │   └── AddTodoActivity.kt
│   └── theme/
│       └── Theme.kt
├── widget/
│   ├── TodoWidget.kt
│   ├── TodoWidgetReceiver.kt
│   └── TodoWidgetContent.kt
└── WidgetTodoApplication.kt
```

---

## テスト

### テスト範囲

| レベル | 対象 | ツール |
|--------|------|--------|
| ユニット | ViewModel, Repository | JUnit5, MockK |
| UI | Compose画面 | Compose UI Test |
| E2E | アプリ全体フロー | Espresso |

### テスト項目

**ユニットテスト**
- [ ] TodoRepository: CRUD操作
- [ ] MainViewModel: 状態管理、削除、Undo

**UIテスト**
- [ ] TODOリスト表示
- [ ] TODO追加フロー
- [ ] スワイプ削除

**E2Eテスト**
- [ ] アプリ起動→追加→完了→削除の一連フロー
- [ ] ウィジェット↔アプリのデータ同期

---

## CI/CD

### GitHub Actions

```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Gradle
        run: ./gradlew build
      - name: Run tests
        run: ./gradlew test
```

---

## エラーハンドリング

- エラーはログのみ（Logcat）
- ユーザーへの通知なし（サイレント）

---

## バックアップ

- Android自動バックアップを有効化
- `android:allowBackup="true"` 設定

---

## 将来の拡張（初期リリースには含めない）

1. **複数リスト機能**
   - カテゴリ/プロジェクト別のリスト管理
   - データモデル拡張が必要

2. **クラウド同期**
   - Firebase Firestore または Supabase
   - 認証機能の追加が必要

---

## 懸念点・トレードオフ

### Glanceの制限

- **ダイアログ**: ウィジェット上で直接表示不可 → Activity経由で実現
- **アニメーション**: 限定的 → フェードアウトは可能だが複雑なアニメーションは困難
- **入力フォーム**: ウィジェット上に配置不可 → 別Activityで入力

### シンプルさ vs 機能性

- 期限・優先度なし → 将来追加時にDB migration必要
- 履歴なし → 誤削除のリカバリ不可（アプリ内Undoで軽減）

### パフォーマンス

- スクロール可能リスト → 大量のTODOで描画コスト増加
- データ変更時のみ更新 → バッテリー節約だがリアルタイム性に欠ける場合あり

---

## 署名

- 新規keystore作成（プロジェクト開始時）
- Google Playアップロード鍵としても使用

---

## 作成日

2026-01-03

