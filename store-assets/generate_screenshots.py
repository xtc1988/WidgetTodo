#!/usr/bin/env python3
"""
Google Play Store用スクリーンショット生成スクリプト
WidgetTodoアプリのUIモックアップを生成します
"""

from PIL import Image, ImageDraw, ImageFont
import os

# カラーパレット（アプリのMaterial3テーマ）
MOSS_GREEN = (88, 129, 87)      # Primary
ACCENT_GREEN = (106, 153, 78)   # Accent
LIGHT_BG = (250, 250, 245)      # Background
WHITE = (255, 255, 255)         # Surface
DARK_TEXT = (60, 60, 60)        # onSurface
GRAY_TEXT = (120, 120, 120)     # onSurfaceVariant
OUTLINE = (200, 200, 200)       # Outline

# スクリーンサイズ（Android標準的な解像度）
SCREEN_WIDTH = 1080
SCREEN_HEIGHT = 1920

def get_font(size, bold=False):
    """フォントを取得"""
    try:
        if bold:
            return ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", size)
        return ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", size)
    except:
        return ImageFont.load_default()

def draw_status_bar(draw, width):
    """ステータスバーを描画"""
    status_height = 80
    draw.rectangle([0, 0, width, status_height], fill=LIGHT_BG)

    # 時計
    font = get_font(32)
    draw.text((40, 25), "10:30", fill=DARK_TEXT, font=font)

    # バッテリー・WiFiアイコン（簡易版）
    icon_y = 30
    # WiFi
    draw.arc([width-180, icon_y, width-150, icon_y+30], 0, 180, fill=DARK_TEXT, width=3)
    # バッテリー
    draw.rectangle([width-130, icon_y+5, width-80, icon_y+25], outline=DARK_TEXT, width=2)
    draw.rectangle([width-80, icon_y+10, width-75, icon_y+20], fill=DARK_TEXT)

    return status_height

def draw_top_app_bar(draw, width, y_offset):
    """トップアプリバーを描画"""
    bar_height = 140
    draw.rectangle([0, y_offset, width, y_offset + bar_height], fill=LIGHT_BG)

    # タイトル
    font = get_font(56, bold=True)
    title_bbox = draw.textbbox((0, 0), "やること", font=font)
    title_width = title_bbox[2] - title_bbox[0]
    draw.text(((width - title_width) // 2, y_offset + 40), "やること", fill=DARK_TEXT, font=font)

    # バージョン情報
    version_font = get_font(24)
    draw.text((width - 200, y_offset + 55), "v1.0.0 (1)", fill=GRAY_TEXT, font=version_font)

    return bar_height

def draw_todo_item(draw, x, y, width, title, show_check=True):
    """TODOアイテムを描画"""
    item_height = 140

    # カード背景
    draw.rectangle([x, y, x + width, y + item_height], fill=WHITE)
    draw.rectangle([x, y, x + width, y + item_height], outline=OUTLINE, width=2)

    # 左側のアクセント線
    draw.rectangle([x, y, x + 8, y + item_height], fill=MOSS_GREEN)

    # タスクテキスト
    font = get_font(42)
    draw.text((x + 40, y + 50), title, fill=DARK_TEXT, font=font)

    # チェックマーク
    if show_check:
        check_size = 80
        check_x = x + width - check_size - 30
        check_y = y + (item_height - check_size) // 2

        # 円
        draw.ellipse([check_x, check_y, check_x + check_size, check_y + check_size],
                     outline=MOSS_GREEN, width=3)

        # チェックマーク
        check_points = [
            (check_x + check_size * 0.25, check_y + check_size * 0.5),
            (check_x + check_size * 0.45, check_y + check_size * 0.7),
            (check_x + check_size * 0.75, check_y + check_size * 0.3)
        ]
        draw.line(check_points, fill=MOSS_GREEN, width=6, joint='curve')

    return item_height

def draw_fab(draw, width, height):
    """フローティングアクションボタンを描画"""
    fab_size = 140
    fab_x = width - fab_size - 50
    fab_y = height - fab_size - 50

    # 円形ボタン
    draw.rounded_rectangle(
        [fab_x, fab_y, fab_x + fab_size, fab_y + fab_size],
        radius=32,
        fill=MOSS_GREEN
    )

    # + アイコン
    plus_width = 8
    plus_length = 60
    center_x = fab_x + fab_size // 2
    center_y = fab_y + fab_size // 2

    # 縦線
    draw.rectangle(
        [center_x - plus_width//2, center_y - plus_length//2,
         center_x + plus_width//2, center_y + plus_length//2],
        fill=WHITE
    )
    # 横線
    draw.rectangle(
        [center_x - plus_length//2, center_y - plus_width//2,
         center_x + plus_length//2, center_y + plus_width//2],
        fill=WHITE
    )

def create_screenshot_1_main_with_tasks():
    """スクリーンショット1: メイン画面（タスクあり）"""
    img = Image.new('RGB', (SCREEN_WIDTH, SCREEN_HEIGHT), LIGHT_BG)
    draw = ImageDraw.Draw(img)

    # ステータスバー
    y = draw_status_bar(draw, SCREEN_WIDTH)

    # トップアプリバー
    y += draw_top_app_bar(draw, SCREEN_WIDTH, y)

    # TODOアイテムリスト
    tasks = [
        "買い物に行く",
        "メールを返信する",
        "レポートを完成させる",
        "ジムに行く"
    ]

    y += 40
    for task in tasks:
        item_height = draw_todo_item(draw, 50, y, SCREEN_WIDTH - 100, task)
        y += item_height + 30

    # FAB
    draw_fab(draw, SCREEN_WIDTH, SCREEN_HEIGHT)

    return img

def create_screenshot_2_add_dialog():
    """スクリーンショット2: タスク追加ダイアログ"""
    img = Image.new('RGB', (SCREEN_WIDTH, SCREEN_HEIGHT), LIGHT_BG)
    draw = ImageDraw.Draw(img)

    # ステータスバー
    y = draw_status_bar(draw, SCREEN_WIDTH)

    # トップアプリバー
    y += draw_top_app_bar(draw, SCREEN_WIDTH, y)

    # 背景のタスク（薄く）
    y_task = y + 40
    for i in range(3):
        draw_todo_item(draw, 50, y_task, SCREEN_WIDTH - 100, "タスク")
        y_task += 170

    # オーバーレイ（ダイアログ背景）
    overlay = Image.new('RGBA', (SCREEN_WIDTH, SCREEN_HEIGHT), (0, 0, 0, 180))
    img = Image.alpha_composite(img.convert('RGBA'), overlay).convert('RGB')
    draw = ImageDraw.Draw(img)

    # ダイアログ
    dialog_width = 900
    dialog_height = 500
    dialog_x = (SCREEN_WIDTH - dialog_width) // 2
    dialog_y = (SCREEN_HEIGHT - dialog_height) // 2

    draw.rounded_rectangle(
        [dialog_x, dialog_y, dialog_x + dialog_width, dialog_y + dialog_height],
        radius=28,
        fill=WHITE
    )

    # ダイアログタイトル
    font_title = get_font(52, bold=True)
    draw.text((dialog_x + 60, dialog_y + 50), "新しいやること", fill=DARK_TEXT, font=font_title)

    # テキスト入力フィールド
    input_y = dialog_y + 150
    input_height = 120
    draw.rounded_rectangle(
        [dialog_x + 60, input_y, dialog_x + dialog_width - 60, input_y + input_height],
        radius=16,
        outline=MOSS_GREEN,
        width=3
    )

    # 入力テキスト
    font_input = get_font(40)
    draw.text((dialog_x + 90, input_y + 40), "新しいタスク", fill=DARK_TEXT, font=font_input)

    # ボタン
    button_y = dialog_y + dialog_height - 120
    font_button = get_font(38)

    # キャンセルボタン
    draw.text((dialog_x + dialog_width - 400, button_y), "キャンセル", fill=GRAY_TEXT, font=font_button)

    # 追加ボタン
    draw.text((dialog_x + dialog_width - 150, button_y), "追加", fill=MOSS_GREEN, font=font_button)

    return img

def create_screenshot_3_empty_state():
    """スクリーンショット3: 空の状態"""
    img = Image.new('RGB', (SCREEN_WIDTH, SCREEN_HEIGHT), LIGHT_BG)
    draw = ImageDraw.Draw(img)

    # ステータスバー
    y = draw_status_bar(draw, SCREEN_WIDTH)

    # トップアプリバー
    y += draw_top_app_bar(draw, SCREEN_WIDTH, y)

    # 空の状態メッセージ
    font_zen = get_font(120, bold=True)
    text = "静寂"
    bbox = draw.textbbox((0, 0), text, font=font_zen)
    text_width = bbox[2] - bbox[0]
    text_x = (SCREEN_WIDTH - text_width) // 2
    text_y = SCREEN_HEIGHT // 2 - 200

    draw.text((text_x, text_y), text, fill=(GRAY_TEXT[0], GRAY_TEXT[1], GRAY_TEXT[2], 128), font=font_zen)

    # サブテキスト
    font_sub = get_font(40)
    subtext = "+をタップしてタスクを追加"
    bbox = draw.textbbox((0, 0), subtext, font=font_sub)
    subtext_width = bbox[2] - bbox[0]
    subtext_x = (SCREEN_WIDTH - subtext_width) // 2
    draw.text((subtext_x, text_y + 180), subtext, fill=GRAY_TEXT, font=font_sub)

    # FAB
    draw_fab(draw, SCREEN_WIDTH, SCREEN_HEIGHT)

    return img

def create_screenshot_4_with_snackbar():
    """スクリーンショット4: Undoスナックバー"""
    img = Image.new('RGB', (SCREEN_WIDTH, SCREEN_HEIGHT), LIGHT_BG)
    draw = ImageDraw.Draw(img)

    # ステータスバー
    y = draw_status_bar(draw, SCREEN_WIDTH)

    # トップアプリバー
    y += draw_top_app_bar(draw, SCREEN_WIDTH, y)

    # TODOアイテム（少なめ）
    tasks = [
        "買い物に行く",
        "メールを返信する"
    ]

    y += 40
    for task in tasks:
        item_height = draw_todo_item(draw, 50, y, SCREEN_WIDTH - 100, task)
        y += item_height + 30

    # FAB
    draw_fab(draw, SCREEN_WIDTH, SCREEN_HEIGHT)

    # スナックバー
    snackbar_height = 120
    snackbar_y = SCREEN_HEIGHT - 200
    snackbar_margin = 50

    draw.rounded_rectangle(
        [snackbar_margin, snackbar_y, SCREEN_WIDTH - snackbar_margin, snackbar_y + snackbar_height],
        radius=16,
        fill=(60, 60, 60)
    )

    # スナックバーテキスト
    font_snack = get_font(38)
    draw.text((snackbar_margin + 50, snackbar_y + 40), "タスクを削除しました", fill=WHITE, font=font_snack)

    # Undoボタン
    font_undo = get_font(38, bold=True)
    draw.text((SCREEN_WIDTH - snackbar_margin - 250, snackbar_y + 40), "元に戻す", fill=ACCENT_GREEN, font=font_undo)

    return img

def create_screenshot_5_widget():
    """スクリーンショット5: ホーム画面のウィジェット"""
    img = Image.new('RGB', (SCREEN_WIDTH, SCREEN_HEIGHT), (100, 120, 140))
    draw = ImageDraw.Draw(img)

    # ステータスバー
    draw_status_bar(draw, SCREEN_WIDTH)

    # 壁紙風の背景（グラデーション）
    for y in range(80, SCREEN_HEIGHT):
        alpha = (y - 80) / (SCREEN_HEIGHT - 80)
        r = int(100 * (1 - alpha) + 80 * alpha)
        g = int(120 * (1 - alpha) + 100 * alpha)
        b = int(140 * (1 - alpha) + 120 * alpha)
        draw.line([(0, y), (SCREEN_WIDTH, y)], fill=(r, g, b))

    # ホーム画面の時計（大きく）
    font_clock = get_font(140, bold=True)
    clock_text = "10:30"
    bbox = draw.textbbox((0, 0), clock_text, font=font_clock)
    clock_width = bbox[2] - bbox[0]
    draw.text(((SCREEN_WIDTH - clock_width) // 2, 300), clock_text, fill=WHITE, font=font_clock)

    # 日付
    font_date = get_font(40)
    date_text = "1月14日 (火)"
    bbox = draw.textbbox((0, 0), date_text, font=font_date)
    date_width = bbox[2] - bbox[0]
    draw.text(((SCREEN_WIDTH - date_width) // 2, 480), date_text, fill=(220, 220, 220), font=font_date)

    # ウィジェット
    widget_width = SCREEN_WIDTH - 100
    widget_height = 700
    widget_x = 50
    widget_y = 700

    # ウィジェット背景
    draw.rounded_rectangle(
        [widget_x, widget_y, widget_x + widget_width, widget_y + widget_height],
        radius=28,
        fill=WHITE
    )

    # ウィジェットヘッダー
    font_widget_title = get_font(48, bold=True)
    draw.text((widget_x + 40, widget_y + 40), "やること", fill=DARK_TEXT, font=font_widget_title)

    # ウィジェット内のタスク（コンパクト版）
    tasks_widget = [
        "買い物に行く",
        "メールを返信する",
        "レポートを完成させる"
    ]

    y_widget_task = widget_y + 130
    for task in tasks_widget:
        # チェックボックス
        check_size = 50
        check_x = widget_x + 40
        draw.rounded_rectangle(
            [check_x, y_widget_task, check_x + check_size, y_widget_task + check_size],
            radius=8,
            outline=MOSS_GREEN,
            width=3
        )

        # タスクテキスト
        font_widget_task = get_font(38)
        draw.text((check_x + check_size + 30, y_widget_task + 8), task, fill=DARK_TEXT, font=font_widget_task)

        y_widget_task += 120

    # ホーム画面下部のアプリアイコン（簡易版）
    dock_y = SCREEN_HEIGHT - 250
    icon_size = 140
    icon_spacing = 40
    num_icons = 5
    total_width = num_icons * icon_size + (num_icons - 1) * icon_spacing
    start_x = (SCREEN_WIDTH - total_width) // 2

    for i in range(num_icons):
        icon_x = start_x + i * (icon_size + icon_spacing)
        # アイコン背景
        draw.rounded_rectangle(
            [icon_x, dock_y, icon_x + icon_size, dock_y + icon_size],
            radius=28,
            fill=(200, 200, 200, 100)
        )

    return img

def main():
    """メイン処理"""
    output_dir = os.path.dirname(os.path.abspath(__file__))
    screenshots_dir = os.path.join(output_dir, "screenshots")
    os.makedirs(screenshots_dir, exist_ok=True)

    print("📱 Google Play Store用スクリーンショットを生成中...")
    print(f"   解像度: {SCREEN_WIDTH}x{SCREEN_HEIGHT}px")
    print()

    screenshots = [
        ("01_main_with_tasks.png", "メイン画面（タスクリスト表示）", create_screenshot_1_main_with_tasks),
        ("02_add_task_dialog.png", "タスク追加ダイアログ", create_screenshot_2_add_dialog),
        ("03_empty_state.png", "空の状態（静寂）", create_screenshot_3_empty_state),
        ("04_undo_snackbar.png", "タスク完了とUndoスナックバー", create_screenshot_4_with_snackbar),
        ("05_widget_home.png", "ホーム画面のウィジェット", create_screenshot_5_widget),
    ]

    for filename, description, create_func in screenshots:
        print(f"📸 {description}")
        img = create_func()
        filepath = os.path.join(screenshots_dir, filename)
        img.save(filepath, quality=95)
        print(f"   ✓ {filename} を保存しました")
        print()

    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("✅ すべてのスクリーンショットが正常に生成されました！")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print()
    print(f"📁 保存先: {screenshots_dir}/")
    print()
    print("生成されたファイル:")
    for filename, description, _ in screenshots:
        filepath = os.path.join(screenshots_dir, filename)
        if os.path.exists(filepath):
            size = os.path.getsize(filepath) / 1024
            print(f"  • {filename} ({size:.1f}KB) - {description}")
    print()
    print("📋 次のステップ:")
    print("  1. 生成されたスクリーンショットを確認")
    print("  2. Google Play Consoleの「スクリーンショット」セクションにアップロード")
    print("  3. 各スクリーンショットに説明文を追加（オプション）")
    print()
    print("💡 ヒント:")
    print("  - Google Play Storeでは最低2枚、最大8枚まで掲載可能です")
    print("  - 実機のスクリーンショットも追加することを推奨します")
    print("  - 実機でのキャプチャ: ./capture_screenshots.sh を実行")

if __name__ == "__main__":
    main()
