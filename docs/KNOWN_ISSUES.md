# Known Issues - WidgetTodo

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
