# Google Play Store アセット

このディレクトリには、WidgetTodoアプリをGoogle Play Storeに公開するために必要な画像アセットが含まれています。

## 📦 生成されたアセット

### 1. アプリアイコン
- **ファイル名**: `app_icon_512.png`
- **サイズ**: 512 x 512 px
- **形式**: PNG（アルファチャンネルなし）
- **用途**: Google Play Consoleのストアリスティング

### 2. フィーチャーグラフィック
- **ファイル名**: `feature_graphic_1024x500.png`
- **サイズ**: 1024 x 500 px
- **形式**: PNG
- **用途**: Play Storeのトップページやおすすめセクションで表示される横長バナー

### 3. プロモーショングラフィック
- **ファイル名**: `promo_graphic_180x120.png`
- **サイズ**: 180 x 120 px
- **形式**: PNG
- **用途**: Play Storeの検索結果や一部のプロモーションで使用（オプション）

## 📱 スクリーンショットの作成方法

Google Play Storeでは、**最低2枚、最大8枚**のスクリーンショットが必要です。

### 推奨するスクリーンショット構成

1. **メイン画面（タスクリスト表示）** - 複数のタスクが表示されている状態
2. **タスク追加ダイアログ** - 新しいタスクを追加している様子
3. **空の状態** - "静寂"が表示されている初期画面
4. **ウィジェット表示** - ホーム画面にウィジェットが配置されている様子
5. **タスク完了アクション** - タスクを完了する瞬間やUndoスナックバー

### スクリーンショット撮影手順

#### 方法1: Android Studioのエミュレータを使用

1. Android Studioでプロジェクトを開く
2. エミュレータでアプリを起動
3. エミュレータの右側のツールバーで「カメラ」アイコンをクリック
4. スクリーンショットが保存される

#### 方法2: 実機を使用

1. 実機でアプリをインストール
2. 以下のいずれかの方法でスクリーンショットを撮影：
   - **Android 11以降**: 電源ボタン + 音量下ボタンを同時押し
   - **一部のデバイス**: 電源ボタン長押し → 「スクリーンショット」を選択
3. `adb pull` コマンドで画像をPCに転送：
   ```bash
   adb pull /sdcard/Pictures/Screenshots/screenshot.png ./store-assets/
   ```

#### 方法3: adbコマンドを使用（推奨）

エミュレータまたは実機が接続されている状態で：

```bash
# スクリーンショットを撮影してPCに保存
adb exec-out screencap -p > screenshot_1.png

# または、デバイスに保存してから転送
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png ./store-assets/
adb shell rm /sdcard/screenshot.png
```

### スクリーンショットの要件

- **形式**: JPEG または 24ビット PNG（アルファチャンネルなし）
- **最小寸法**: 320 px
- **最大寸法**: 3840 px
- **アスペクト比**: 最大寸法は最小寸法の2倍以内
- **推奨サイズ**:
  - スマートフォン: 1080 x 1920 px（縦向き）または 1920 x 1080 px（横向き）
  - タブレット: 1200 x 1920 px（7インチ）、1600 x 2560 px（10インチ）

### サンプルスクリーンショット生成スクリプト

以下のスクリプトで自動的にスクリーンショットを撮影できます：

```bash
#!/bin/bash
# screenshot_capture.sh - 自動スクリーンショット撮影

cd /home/user/WidgetTodo/store-assets

# エミュレータまたは実機が接続されているか確認
adb devices

# 1. メイン画面（タスクあり）
echo "📸 スクリーンショット1: メイン画面"
sleep 2
adb exec-out screencap -p > screenshot_01_main_with_tasks.png

# 2. タスク追加ダイアログ
echo "📸 スクリーンショット2: タスク追加（+ボタンをタップしてください）"
sleep 5
adb exec-out screencap -p > screenshot_02_add_task_dialog.png

# 3. 空の状態
echo "📸 スクリーンショット3: 空の状態（すべてのタスクを削除してください）"
sleep 5
adb exec-out screencap -p > screenshot_03_empty_state.png

echo "✅ スクリーンショット撮影完了！"
```

## 🎨 デザインコンセプト

### カラーパレット
- **モスグリーン**: `#588157` - メインカラー
- **アクセントグリーン**: `#6A994E` - アクセント
- **明るい背景**: `#FAFAF5` - 背景
- **白**: `#FFFFFF` - カード背景
- **ダークテキスト**: `#3C3C3C` - テキスト

### デザイン理念
- **禅的なシンプルさ**: 余計な装飾を排除したミニマルデザイン
- **自然な配色**: 落ち着いた緑色をアクセントに使用
- **直感的なUI**: すぐに理解できるわかりやすいインターフェース

## 📤 Google Play Consoleへのアップロード手順

1. [Google Play Console](https://play.google.com/console) にアクセス
2. アプリを選択（または新規作成）
3. 左メニューから「ストアの設定」→「メインのストア情報」を選択
4. 以下をアップロード：
   - **アプリアイコン**: `app_icon_512.png`
   - **フィーチャーグラフィック**: `feature_graphic_1024x500.png`
5. 「スクリーンショット」セクションで、撮影したスクリーンショットをアップロード
6. 「保存」をクリック

## 🔄 アセットの再生成

アセットを再生成する場合は、以下のコマンドを実行：

```bash
cd /home/user/WidgetTodo/store-assets
python3 generate_icons.py
```

## 📝 チェックリスト

ストア公開前に以下を確認：

- [ ] アプリアイコン（512x512）をアップロード
- [ ] フィーチャーグラフィック（1024x500）をアップロード
- [ ] スクリーンショットを最低2枚アップロード
- [ ] アプリの説明文を作成
- [ ] プライバシーポリシーのURLを設定（`docs/PRIVACY_POLICY.md`を参照）
- [ ] アプリのカテゴリを選択
- [ ] 対象年齢を設定
- [ ] APKまたはAABファイルをアップロード

## 📚 参考リンク

- [Google Play Console ヘルプ - グラフィック アセット、スクリーンショット、動画](https://support.google.com/googleplay/android-developer/answer/9866151)
- [アプリのグラフィック アセットの仕様](https://support.google.com/googleplay/android-developer/answer/1078870)
- [ストアの掲載情報を作成する](https://support.google.com/googleplay/android-developer/answer/9859455)
