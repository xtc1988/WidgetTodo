#!/usr/bin/env python3
"""
Google Play Storeアセット生成スクリプト
WidgetTodoアプリのアイコンとフィーチャーグラフィックを生成します
"""

from PIL import Image, ImageDraw, ImageFont
import os

# カラーパレット（アプリのテーマに合わせた禅的な配色）
MOSS_GREEN = (88, 129, 87)      # メインカラー（苔色）
LIGHT_BG = (250, 250, 245)       # 明るい背景
WHITE = (255, 255, 255)          # 白
DARK_TEXT = (60, 60, 60)         # 濃いテキスト
ACCENT_GREEN = (106, 153, 78)    # アクセントグリーン

def create_app_icon(size=512):
    """
    アプリアイコンを生成（512x512px）
    シンプルなチェックリストデザイン
    """
    img = Image.new('RGB', (size, size), LIGHT_BG)
    draw = ImageDraw.Draw(img)

    # 背景を丸角矩形に
    margin = size * 0.05
    draw.rounded_rectangle(
        [margin, margin, size - margin, size - margin],
        radius=size * 0.15,
        fill=WHITE
    )

    # チェックリストのアイテムを描画（3つ）
    item_height = size * 0.15
    start_y = size * 0.25
    spacing = size * 0.2

    for i in range(3):
        y = start_y + (i * spacing)

        # チェックボックス（最初の2つは完了、最後は未完了）
        box_size = size * 0.08
        box_x = size * 0.2

        if i < 2:
            # 完了したアイテム - チェックマーク
            draw.rounded_rectangle(
                [box_x, y, box_x + box_size, y + box_size],
                radius=size * 0.02,
                fill=MOSS_GREEN,
                outline=MOSS_GREEN,
                width=int(size * 0.01)
            )
            # チェックマーク
            check_points = [
                (box_x + box_size * 0.25, y + box_size * 0.5),
                (box_x + box_size * 0.45, y + box_size * 0.7),
                (box_x + box_size * 0.75, y + box_size * 0.3)
            ]
            draw.line(check_points, fill=WHITE, width=int(size * 0.015), joint='curve')
        else:
            # 未完了のアイテム - 空のボックス
            draw.rounded_rectangle(
                [box_x, y, box_x + box_size, y + box_size],
                radius=size * 0.02,
                outline=MOSS_GREEN,
                width=int(size * 0.01)
            )

        # タスクライン（横線）
        line_x_start = box_x + box_size + size * 0.05
        line_x_end = size * 0.8
        line_y = y + box_size * 0.5
        line_width = int(size * 0.015) if i == 2 else int(size * 0.012)

        draw.line(
            [(line_x_start, line_y), (line_x_end, line_y)],
            fill=MOSS_GREEN if i == 2 else ACCENT_GREEN,
            width=line_width
        )

    return img

def create_feature_graphic():
    """
    フィーチャーグラフィックを生成（1024x500px）
    アプリの特徴を示すバナー
    """
    width, height = 1024, 500
    img = Image.new('RGB', (width, height), LIGHT_BG)
    draw = ImageDraw.Draw(img)

    # グラデーション背景風（シンプルな2色）
    for y in range(height):
        alpha = y / height
        r = int(LIGHT_BG[0] * (1 - alpha) + WHITE[0] * alpha)
        g = int(LIGHT_BG[1] * (1 - alpha) + WHITE[1] * alpha)
        b = int(LIGHT_BG[2] * (1 - alpha) + WHITE[2] * alpha)
        draw.line([(0, y), (width, y)], fill=(r, g, b))

    # 左側：アイコン部分
    icon_size = 300
    icon_x = 80
    icon_y = (height - icon_size) // 2

    # 簡易アイコン表示
    icon_bg_size = icon_size * 0.9
    icon_bg_x = icon_x + (icon_size - icon_bg_size) // 2
    icon_bg_y = icon_y + (icon_size - icon_bg_size) // 2

    draw.rounded_rectangle(
        [icon_bg_x, icon_bg_y, icon_bg_x + icon_bg_size, icon_bg_y + icon_bg_size],
        radius=icon_size * 0.15,
        fill=WHITE,
        outline=MOSS_GREEN,
        width=3
    )

    # チェックリストの簡易版
    item_count = 3
    item_height = icon_bg_size * 0.15
    item_start_y = icon_bg_y + icon_bg_size * 0.25
    item_spacing = icon_bg_size * 0.25

    for i in range(item_count):
        y = item_start_y + (i * item_spacing)
        box_size = icon_bg_size * 0.1
        box_x = icon_bg_x + icon_bg_size * 0.15

        if i < 2:
            draw.rounded_rectangle(
                [box_x, y, box_x + box_size, y + box_size],
                radius=5,
                fill=MOSS_GREEN
            )
            # 小さなチェックマーク
            check_points = [
                (box_x + box_size * 0.25, y + box_size * 0.5),
                (box_x + box_size * 0.45, y + box_size * 0.7),
                (box_x + box_size * 0.75, y + box_size * 0.3)
            ]
            draw.line(check_points, fill=WHITE, width=3, joint='curve')
        else:
            draw.rounded_rectangle(
                [box_x, y, box_x + box_size, y + box_size],
                radius=5,
                outline=MOSS_GREEN,
                width=2
            )

        # タスクライン
        line_x_start = box_x + box_size + 15
        line_x_end = icon_bg_x + icon_bg_size * 0.85
        line_y = y + box_size * 0.5
        draw.line(
            [(line_x_start, line_y), (line_x_end, line_y)],
            fill=MOSS_GREEN if i == 2 else ACCENT_GREEN,
            width=3
        )

    # 右側：テキスト部分
    try:
        # システムフォントを試す
        title_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 72)
        subtitle_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 36)
    except:
        # フォントが見つからない場合はデフォルトフォント
        title_font = ImageFont.load_default()
        subtitle_font = ImageFont.load_default()

    # タイトル
    title_text = "WidgetTodo"
    title_x = 450
    title_y = 150
    draw.text((title_x, title_y), title_text, fill=MOSS_GREEN, font=title_font)

    # サブタイトル
    subtitle_lines = [
        "シンプルで禅的な",
        "TODOアプリ"
    ]
    subtitle_y = title_y + 100
    for i, line in enumerate(subtitle_lines):
        draw.text(
            (title_x, subtitle_y + i * 45),
            line,
            fill=DARK_TEXT,
            font=subtitle_font
        )

    # 特徴のアイコン（3つの小さな特徴表示）
    feature_y = subtitle_y + 130
    feature_items = ["✓ ウィジェット対応", "✓ シンプルUI", "✓ 高速動作"]

    try:
        feature_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 24)
    except:
        feature_font = ImageFont.load_default()

    for i, feature in enumerate(feature_items):
        draw.text(
            (title_x, feature_y + i * 35),
            feature,
            fill=ACCENT_GREEN,
            font=feature_font
        )

    return img

def create_promo_graphic():
    """
    プロモーショングラフィックを生成（180x120px）
    Play Storeの検索結果などで使用
    """
    width, height = 180, 120
    img = Image.new('RGB', (width, height), MOSS_GREEN)
    draw = ImageDraw.Draw(img)

    # 簡易チェックマーク
    check_size = 60
    check_x = (width - check_size) // 2
    check_y = (height - check_size) // 2 - 10

    # 白い丸
    draw.ellipse(
        [check_x, check_y, check_x + check_size, check_y + check_size],
        fill=WHITE
    )

    # チェックマーク
    check_points = [
        (check_x + check_size * 0.25, check_y + check_size * 0.5),
        (check_x + check_size * 0.45, check_y + check_size * 0.7),
        (check_x + check_size * 0.75, check_y + check_size * 0.3)
    ]
    draw.line(check_points, fill=MOSS_GREEN, width=8, joint='curve')

    # 下部にテキスト
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 14)
    except:
        font = ImageFont.load_default()

    text = "TODO"
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_x = (width - text_width) // 2
    text_y = check_y + check_size + 5
    draw.text((text_x, text_y), text, fill=WHITE, font=font)

    return img

def main():
    """メイン処理"""
    output_dir = os.path.dirname(os.path.abspath(__file__))

    print("🎨 Google Play Store用アセットを生成中...")

    # アプリアイコン（512x512）
    print("📱 アプリアイコン (512x512) を生成中...")
    app_icon = create_app_icon(512)
    app_icon.save(os.path.join(output_dir, 'app_icon_512.png'))
    print("   ✓ app_icon_512.png を保存しました")

    # フィーチャーグラフィック（1024x500）
    print("🖼️  フィーチャーグラフィック (1024x500) を生成中...")
    feature_graphic = create_feature_graphic()
    feature_graphic.save(os.path.join(output_dir, 'feature_graphic_1024x500.png'))
    print("   ✓ feature_graphic_1024x500.png を保存しました")

    # プロモーショングラフィック（180x120）
    print("🎯 プロモーショングラフィック (180x120) を生成中...")
    promo_graphic = create_promo_graphic()
    promo_graphic.save(os.path.join(output_dir, 'promo_graphic_180x120.png'))
    print("   ✓ promo_graphic_180x120.png を保存しました")

    print("\n✅ すべてのアセットが正常に生成されました！")
    print(f"📁 保存先: {output_dir}")
    print("\n📋 次のステップ:")
    print("   1. 実際のアプリのスクリーンショットを撮影してください")
    print("   2. Google Play Consoleにアップロードしてください")

if __name__ == "__main__":
    main()
