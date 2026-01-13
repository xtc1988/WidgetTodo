# Troubleshooting Guide - WidgetTodo

> Common problems and their solutions for Android Glance Widget development

## Table of Contents
- [Glance Widget Issues](#glance-widget-issues)
- [Build Issues](#build-issues)
- [Database Issues](#database-issues)

---

## Glance Widget Issues

### ActionCallback not receiving parameters

**Symptoms:**
- Widget tap handler does nothing
- `parameters[key]` returns null in ActionCallback
- No errors in logcat

**Cause:** ActionParameters.Key defined as local variables in different scopes

**Solution:**
```kotlin
// BAD: Local variables (may not work)
@Composable
fun MyItem(itemId: Long) {
    val idKey = ActionParameters.Key<Long>("item_id")  // Local
    val action = actionRunCallback<MyAction>(
        actionParametersOf(idKey to itemId)
    )
}

class MyAction : ActionCallback {
    override suspend fun onAction(..., parameters: ActionParameters) {
        val idKey = ActionParameters.Key<Long>("item_id")  // Different local
        val id = parameters[idKey]  // May be null!
    }
}

// GOOD: Shared constant
private val ITEM_ID_KEY = ActionParameters.Key<Long>("item_id")

@Composable
fun MyItem(itemId: Long) {
    val action = actionRunCallback<MyAction>(
        actionParametersOf(ITEM_ID_KEY to itemId)
    )
}

class MyAction : ActionCallback {
    override suspend fun onAction(..., parameters: ActionParameters) {
        val id = parameters[ITEM_ID_KEY]  // Works correctly
    }
}
```

**Prevention:** Always define ActionParameters.Key as top-level or companion object constants

---

### Widget items overlapping

**Symptoms:**
- Items in LazyColumn appear to overlap
- Text from one item visible behind another

**Cause:** Glance LazyColumn doesn't support verticalArrangement

**Solution:**
```kotlin
// BAD: verticalArrangement not supported
LazyColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp)  // Won't work
) {
    items(list) { item ->
        MyItem(item)
    }
}

// GOOD: Use Spacer inside each item
LazyColumn {
    items(list) { item ->
        Column {
            MyItem(item)
            Spacer(modifier = GlanceModifier.height(8.dp))
        }
    }
}
```

---

### Widget tap area too small

**Symptoms:**
- Users have difficulty tapping items
- Taps seem to miss

**Cause:** Clickable modifier only on small element

**Solution:**
```kotlin
// BAD: Only checkbox is clickable (small target)
Row(...) {
    Text(text = item.title)
    Box(
        modifier = GlanceModifier
            .size(24.dp)
            .clickable(action)  // Small tap target
    )
}

// GOOD: Entire row is clickable (large target)
Row(
    modifier = GlanceModifier
        .fillMaxWidth()
        .clickable(action)  // Large tap target
) {
    Text(text = item.title)
    Box(modifier = GlanceModifier.size(24.dp))  // Visual only
}
```

**Prevention:** Follow Material Design guidelines: minimum 48dp touch target

---

## Test Issues

### Instrumented テストで「No compose hierarchies found」

**Symptoms:**
```
java.lang.IllegalStateException: No compose hierarchies found in the app.
```
すべてのCompose UIテストが失敗する。

**Cause:** インクリメンタルビルドでHilt/Composeのキャッシュ不整合

**Solution:**
```powershell
# クリーンビルドを実行
.\gradlew.bat clean --no-daemon
.\gradlew.bat connectedDebugAndroidTest --no-daemon
```

**Prevention:**
- Hilt DIモジュールを変更した後は必ずクリーンビルドを実行
- CI環境では常にクリーンビルドを使用

---

## Build Issues

### Claude Codeからのビルド方法

**症状:**
- bashから`gradlew.bat`を直接実行しても動かない
- JAVA_HOMEが設定されていないエラー

**原因:** Claude CodeのbashシェルからWindows batファイルを直接実行できない

**解決策:**
```powershell
# 既存のPowerShellスクリプトを使用する
powershell -File "C:\claudecode\CLAUDECODE\WidgetTodo\build.ps1"

# またはクリーンビルド＋インストール
powershell -File "C:\claudecode\CLAUDECODE\WidgetTodo\clean_build_install.ps1"
```

**利用可能なスクリプト:**
| スクリプト | 用途 |
|-----------|------|
| `build.ps1` | ビルドのみ |
| `clean_build_install.ps1` | クリーンビルド＋アンインストール＋インストール＋起動 |
| `reinstall.ps1` | 既存APKを再インストール |
| `check_logcat.ps1` | logcat確認 |

**重要:** AIが「ビルドできない」「環境変数がない」と言った場合は、既存のps1スクリプトを使用すること。

---

### Gradle version mismatch with AGP

**Symptoms:**
```
Caused by: java.lang.RuntimeException: Minimum supported Gradle version is 8.7. Current version is 8.5.
```

**Cause:** Cached Gradle version doesn't match gradle-wrapper.properties

**Solution:**
```powershell
# Force update wrapper
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"
.\gradlew.bat wrapper --gradle-version=8.7 --distribution-type=bin

# Clean build with no daemon
.\gradlew.bat clean --no-daemon
.\gradlew.bat assembleDebug --no-daemon
```

---

### JdkImageTransform error

**Symptoms:**
```
Execution failed for task ':app:compileDebugJavaWithJavac'.
> Could not resolve all files for configuration ':app:androidJdkImage'.
   > Failed to transform core-for-system-modules.jar
```

**Cause:** JDK version mismatch between command line and Android Studio

**Solution:**
1. Update AGP to 8.5.0+
2. Update Kotlin to 2.0.0+
3. Set JDK in gradle.properties:
```properties
org.gradle.java.home=C:/Program Files/Android/Android Studio1/jbr
```

See KNOWN_ISSUES.md for full details.

---

## Database Issues

### Widget not updating after database change

**Symptoms:**
- Adding/deleting TODO in app doesn't reflect in widget
- Widget shows stale data

**Cause:** Widget not notified of database changes

**Solution:**
```kotlin
// After database operation, update widget
withContext(Dispatchers.IO) {
    db.todoDao().insert(todo)
}

// Force widget refresh
TodoWidget().updateAll(context)
```

---

## Quick Reference

| Issue | Quick Fix |
|-------|-----------|
| ActionCallback params null | Use shared constant for ActionParameters.Key |
| Items overlapping | Add Spacer after each item in LazyColumn |
| Tap not working | Make Row/Column clickable, not small element |
| Gradle version error | Run `gradlew wrapper --gradle-version=X.X` |
| JDK error | Set `org.gradle.java.home` in gradle.properties |
