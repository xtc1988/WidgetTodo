# Known Issues - WidgetTodo

## Active Issues

_現在、アクティブな問題はありません_

---

## Resolved Issues

### [FIXED] GlanceのState更新でウィジェットが再描画されない

**Discovered:** 2026-01-12
**Fixed:** 2026-01-12
**Severity:** High (Core feature broken)

#### Description
TODO追加後、`widget.update()`や`widget.updateAll()`を呼んでもウィジェットのUIが更新されない。

#### Root Cause
**Glanceの仕様:**
- `update()`/`updateAll()`は実行中のセッションでは`provideGlance()`を再起動しない
- `provideGlance()`内でDBから直接データを取得しても、再実行されないため更新されない

#### Fix Applied

**GlanceStateDefinitionを使用してState経由でデータを渡す：**

1. `TodoWidgetStateDefinition.kt` - DataStoreベースのState定義を新規作成
2. `TodoWidgetUpdater.kt` - DBからTODO取得→JSON化→Stateに保存
3. `TodoWidget.kt` - `provideContent`内でStateからJSONを読み取り、パース

**データフロー：**
```
DB変更 → TodoWidgetUpdater
       → updateAppWidgetState(JSON)
       → TodoWidget().updateAll()
       → provideContent再実行
       → State読み取り → UI更新
```

#### Prevention Measures
- [x] Glanceウィジェットではデータを`GlanceStateDefinition`経由で渡す
- [x] `provideContent`内でStateを読み取る（DBアクセスしない）

#### Related
- PR #12: fix: GlanceStateDefinitionを使用してウィジェット更新を確実に行う

---

### [FIXED] TODO追加時にウィジェットが更新されない・既存が消える

**Discovered:** 2026-01-10
**Fixed:** 2026-01-10
**Severity:** High (Core feature broken)

#### Description
TODOを追加すると以下の問題が発生：
1. 既存のTODOが消える場合がある
2. ウィジェットに新規TODOが反映されない

#### Root Cause
**3つの根本原因を特定：**

| 原因 | 重要度 | 詳細 |
|------|--------|------|
| 複数Databaseインスタンス | **CRITICAL** | TodoWidget.kt, CompleteTodoAction で毎回新しいRoom Databaseインスタンスを生成。WALモードでキャッシュ不一致が発生 |
| レース条件 | MEDIUM | `repository.addTodo()` の完了を待たずに `updateWidget()` が実行され、古いデータが取得される |
| State同期問題 | MEDIUM | ウィジェットがSingleton DBを使用していないため、メインアプリとのデータ同期が取れない |

**問題の流れ：**
```
[MainViewModel]      [TodoWidget]
    │                    │
    ▼                    ▼
 Database A          Database B (毎回新規生成)
    │                    │
    ▼                    ▼
 addTodo()実行     ──→ getAllTodosOnce()実行
    │                    │
 [A,B]をinsert         [A]しか見えない (キャッシュ遅延)
```

#### Fix Applied

**1. TodoDatabase.kt - Singleton パターン追加：**
```kotlin
companion object {
    @Volatile
    private var INSTANCE: TodoDatabase? = null

    fun getInstance(context: Context): TodoDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(...)
                .build().also { INSTANCE = it }
        }
    }
}
```

**2. TodoWidget.kt - Singleton Database使用：**
```kotlin
// Before (毎回新規生成)
val db = Room.databaseBuilder(...).build()

// After (Singleton)
val db = TodoDatabase.getInstance(context)
```

**3. MainViewModel.kt / AddTodoActivity.kt - レース条件解消：**
```kotlin
// Before
repository.addTodo(title)
updateWidget()

// After (DB完了を待ってから更新)
withContext(Dispatchers.IO) {
    repository.addTodo(title)
}
updateWidget()
```

#### Prevention Measures
- [x] TodoDatabase に Singleton パターンを実装
- [x] ウィジェットでは常に `TodoDatabase.getInstance()` を使用
- [x] DB操作完了後にウィジェット更新を実行

#### Related Files
- `app/src/main/java/com/example/widgettodo/data/local/TodoDatabase.kt`
- `app/src/main/java/com/example/widgettodo/widget/TodoWidget.kt`
- `app/src/main/java/com/example/widgettodo/ui/main/MainViewModel.kt`
- `app/src/main/java/com/example/widgettodo/ui/add/AddTodoActivity.kt`

---

### [INVESTIGATING] Widget TODO tap not responding

**Discovered:** 2026-01-10
**Status:** Investigating
**Severity:** High (Core feature broken)

#### Description
ウィジェットのTODOアイテムをタップしても、CompleteTodoActionが呼ばれず、アイテムが削除されない。

#### Reproduction Steps
1. ホーム画面にWidgetTodoウィジェットを配置
2. アプリでTODOを追加
3. ウィジェット上のTODOアイテムをタップ
4. → 何も起きない（アイテムが削除されない）

#### Expected Behavior
タップするとTODOが削除され、ウィジェットが更新される

#### Actual Behavior
タップしても何も反応しない

#### Investigation Log (2026-01-10)

**ログ分析結果：**
| 項目 | 結果 |
|------|------|
| CompleteTodoAction ログ | **出力なし** → onAction未呼出 |
| Glance SessionWorker | 正常動作 |
| 例外・エラー | なし |

**ソースコード確認結果：**
| 項目 | 状態 | 行番号 |
|------|------|--------|
| TODO_ID_KEY | ✅ トップレベル定数 | 32 |
| ZenTodoList | ✅ Column使用（LazyColumn回避） | 211-224 |
| ZenTodoItem.clickable | ✅ 外側Boxに設定 | 241-245 |
| CompleteTodoAction | ✅ ログ追加済み | 320-344 |

**根本原因（推定）：**
ソースコードにはログが追加されているが、logcatにログが出力されていない。
→ **現在実行中のAPKは最新のソースコードでビルドされていない可能性が高い**

#### Next Steps
1. [ ] 最新コードでビルド・インストール: `.\gradlew.bat clean assembleDebug installDebug --no-daemon`
2. [ ] ウィジェットを削除して再配置
3. [ ] TODOをタップしてログを確認: `adb logcat -s CompleteTodoAction`
4. [ ] ログが出力されるか確認

#### Related PRs
- PR #4: feat: Widget taller, compact items, fix tap to complete
- PR #5: fix: ウィジェットTODOタップ動作改善（Boxラップ）

---

## Resolved Issues

### [FIXED] Android Studio Gradle Version Cache Mismatch

**Discovered:** 2026-01-05
**Fixed:** 2026-01-05
**Severity:** Critical (Build failure)

#### Description
Android Studioでプロジェクトを開くと、gradle-wrapper.propertiesに8.7が設定されているにもかかわらず、Gradle 8.5が使用され、AGP 8.5.0との互換性エラーが発生する。

#### Error Message
```
Caused by: java.lang.RuntimeException: Minimum supported Gradle version is 8.7. Current version is 8.5.
If using the gradle wrapper, try editing the distributionUrl in
C:\claudecode\CLAUDECODE\WidgetTodo\gradle\wrapper\gradle-wrapper.properties to gradle-8.7-all.zip
```

#### Root Cause
**Gradleキャッシュの不整合：**
- gradle-wrapper.propertiesは正しく8.7に設定されている
- Android StudioがキャッシュされたGradle 8.5を使用
- 前回のビルドで8.5がダウンロードされ、キャッシュに残存

#### Environment
- Android Studio: Ladybug
- Android Gradle Plugin: 8.5.0
- gradle-wrapper.properties: 8.7-bin.zip
- 実際に使用されたGradle: 8.5 (キャッシュ)

#### Fix Applied

**1. Gradle Wrapperを強制更新：**
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"
.\gradlew.bat wrapper --gradle-version=8.7 --distribution-type=bin
```

**2. --no-daemonオプションでビルド実行：**
```powershell
.\gradlew.bat clean --no-daemon
.\gradlew.bat assembleDebug --no-daemon
```

**3. または full_verification.ps1 スクリプトを実行：**
```powershell
powershell -File "C:\claudecode\CLAUDECODE\WidgetTodo\full_verification.ps1"
```

#### Prevention Measures
- [x] full_verification.ps1スクリプトで --no-daemon オプションを使用
- [x] gradle.propertiesでJAVA_HOMEを統一設定
- [x] Android Studioで問題が発生した場合は File → Invalidate Caches を実行

#### Related Issues
- 前提バグ: JdkImageTransform Error (下記参照)

---

### [FIXED] JdkImageTransform Error with AGP 8.2.0 and JDK 21

**Discovered:** 2026-01-05
**Fixed:** 2026-01-05
**Severity:** Critical (Build failure)

#### Description
Android Studioでプロジェクトをビルドしようとすると、`compileDebugJavaWithJavac`タスクで`JdkImageTransform`エラーが発生し、ビルドが失敗する。

#### Error Message
```
Execution failed for task ':app:compileDebugJavaWithJavac'.
> Could not resolve all files for configuration ':app:androidJdkImage'.
   > Failed to transform core-for-system-modules.jar to match attributes
      > Execution failed for JdkImageTransform: ...core-for-system-modules.jar.
         > Error while executing process .../jlink.exe with arguments {...}
```

#### Root Cause
**JDKバージョン不一致問題：**
- コマンドラインビルドで JDK 17.0.2 を使用
- Android Studio は JBR (JetBrains Runtime) JDK 21.0.6 を使用
- AGP (Android Gradle Plugin) 8.2.0 は JDK 21 の `jlink.exe` との互換性に問題がある
- Gradleのtransformキャッシュが異なるJDKで作成され、`jlink.exe`が失敗

#### Environment
- Android Studio: Ladybug (with JBR 21.0.6)
- Android Gradle Plugin: 8.2.0
- Gradle: 8.5
- JDK (Command Line): 17.0.2
- JDK (Android Studio): 21.0.6 (JBR)

#### Fix Applied

**1. AGPとKotlinをJDK 21互換バージョンにアップグレード：**

`build.gradle.kts` (root):
```kotlin
// Before
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.16" apply false
}

// After
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}
```

**2. Gradle Wrapperをアップグレード：**

`gradle-wrapper.properties`:
```properties
# Before
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip

# After
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
```

**3. app/build.gradle.kts にCompose Pluginを追加：**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")  // Added
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}
```

**4. composeOptionsブロックを削除：**
Kotlin 2.0 + Compose Pluginでは不要

**5. gradle.propertiesでJDKパスを統一：**
```properties
org.gradle.java.home=C:/Program Files/Android/Android Studio1/jbr
```

**6. Gradleキャッシュをクリア：**
```powershell
Remove-Item -Path "$env:USERPROFILE\.gradle\caches\transforms-3\*" -Recurse -Force
```

#### Prevention Measures
- [x] gradle.propertiesでorg.gradle.java.homeを設定し、ビルド環境のJDKを統一
- [x] AGP/Kotlin/Gradleバージョンを互換性のある組み合わせに保つ
- [x] このドキュメントを作成し、将来の参照に利用

#### Related Links
- Android Gradle Plugin Release Notes: https://developer.android.com/build/releases/gradle-plugin
- Kotlin 2.0 Compose Compiler Migration: https://developer.android.com/develop/ui/compose/compiler

---

## Version Compatibility Matrix

| AGP Version | Gradle Version | Kotlin Version | JDK Support |
|-------------|----------------|----------------|-------------|
| 8.2.x       | 8.2+           | 1.9.x          | JDK 17      |
| 8.3.x       | 8.4+           | 1.9.x/2.0.x    | JDK 17-21   |
| 8.4.x       | 8.6+           | 2.0.x          | JDK 17-21   |
| 8.5.x       | 8.7+           | 2.0.x          | JDK 17-21   |
