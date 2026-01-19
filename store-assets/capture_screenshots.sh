#!/bin/bash
# スクリーンショット自動撮影スクリプト
# WidgetTodoアプリのスクリーンショットをGoogle Play Store用に撮影します

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "📱 WidgetTodo - スクリーンショット撮影ツール"
echo "================================================"
echo ""

# デバイスが接続されているか確認
if ! adb devices | grep -q "device$"; then
    echo "❌ エラー: Androidデバイスまたはエミュレータが接続されていません"
    echo ""
    echo "次のいずれかを実行してください："
    echo "  1. Android Studioでエミュレータを起動"
    echo "  2. USB経由で実機を接続し、USBデバッグを有効化"
    echo ""
    echo "確認コマンド: adb devices"
    exit 1
fi

echo "✅ デバイスが検出されました"
echo ""

# スクリーンショット保存ディレクトリ
SCREENSHOTS_DIR="screenshots"
mkdir -p "$SCREENSHOTS_DIR"

echo "📸 スクリーンショット撮影を開始します"
echo "   保存先: $SCRIPT_DIR/$SCREENSHOTS_DIR"
echo ""
echo "⚠️  注意事項:"
echo "   - 各スクリーンショットの前に、アプリを適切な状態にしてください"
echo "   - プロンプトに従って操作してください"
echo ""

# スクリーンショット1: メイン画面（タスクあり）
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📸 スクリーンショット 1/5: メイン画面（タスクリスト表示）"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "アプリに複数のタスクを追加して、メイン画面を表示してください"
echo "例："
echo "  - 買い物に行く"
echo "  - メールを返信する"
echo "  - レポートを完成させる"
echo ""
read -p "準備ができたらEnterキーを押してください..."
adb exec-out screencap -p > "$SCREENSHOTS_DIR/01_main_with_tasks.png"
echo "✓ 保存しました: 01_main_with_tasks.png"
echo ""
sleep 1

# スクリーンショット2: タスク追加ダイアログ
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📸 スクリーンショット 2/5: タスク追加ダイアログ"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "画面右下の「+」ボタンをタップして、タスク追加ダイアログを開いてください"
echo "入力フィールドに何か入力しておくと良いでしょう（例: 新しいタスク）"
echo ""
read -p "準備ができたらEnterキーを押してください..."
adb exec-out screencap -p > "$SCREENSHOTS_DIR/02_add_task_dialog.png"
echo "✓ 保存しました: 02_add_task_dialog.png"
echo ""
sleep 1

# スクリーンショット3: タスク完了アクション
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📸 スクリーンショット 3/5: タスク完了後のUndoスナックバー"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "ダイアログを閉じて、タスクの右側にあるチェックマークをタップしてください"
echo "「タスクを削除しました」というスナックバーが表示されたら撮影します"
echo ""
read -p "準備ができたらEnterキーを押してください..."
adb exec-out screencap -p > "$SCREENSHOTS_DIR/03_undo_snackbar.png"
echo "✓ 保存しました: 03_undo_snackbar.png"
echo ""
sleep 1

# スクリーンショット4: 空の状態
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📸 スクリーンショット 4/5: 空の状態（初期画面）"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "すべてのタスクを完了または削除して、「静寂」が表示される状態にしてください"
echo ""
read -p "準備ができたらEnterキーを押してください..."
adb exec-out screencap -p > "$SCREENSHOTS_DIR/04_empty_state.png"
echo "✓ 保存しました: 04_empty_state.png"
echo ""
sleep 1

# スクリーンショット5: ウィジェット
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📸 スクリーンショット 5/5: ホーム画面のウィジェット"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "ホームボタンを押して、ホーム画面に戻ってください"
echo "WidgetTodoウィジェットをホーム画面に配置してください："
echo "  1. ホーム画面の空白部分を長押し"
echo "  2. 「ウィジェット」を選択"
echo "  3. 「WidgetTodo」を見つけて配置"
echo ""
read -p "準備ができたらEnterキーを押してください..."
adb exec-out screencap -p > "$SCREENSHOTS_DIR/05_widget_home.png"
echo "✓ 保存しました: 05_widget_home.png"
echo ""

# 完了メッセージ
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ スクリーンショット撮影完了！"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "保存されたファイル:"
ls -lh "$SCREENSHOTS_DIR"/*.png 2>/dev/null || echo "  ファイルが見つかりません"
echo ""
echo "📂 場所: $SCRIPT_DIR/$SCREENSHOTS_DIR/"
echo ""
echo "📋 次のステップ:"
echo "  1. 撮影したスクリーンショットを確認"
echo "  2. 必要に応じて画像編集ソフトで調整"
echo "  3. Google Play Consoleにアップロード"
echo ""
echo "💡 ヒント:"
echo "  - スクリーンショットはPNG形式で保存されています"
echo "  - Google Play Storeでは最低2枚、最大8枚まで掲載可能です"
echo "  - より多くのスクリーンショットを撮影したい場合は、このスクリプトを再実行してください"
echo ""
