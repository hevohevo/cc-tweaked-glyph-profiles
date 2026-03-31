CC: Tweaked 端末グリフプロファイル
=================================

概要
----

ComputerCraft は基本的に英語圏向けの端末環境です。このフォルダには、
固定 256 グリフ端末フォントの範囲で、限定的な非ラテン文字入力・表示を
扱うためのグリフプロファイル関連ファイルを置きます。

これは完全な国際化対応ではありません。各プロファイルは、Unicode 入力を
端末内部の 0..255 グリフ空間へ落とし込み、それに対応するフォント画像を
選択します。


ファイル構成
------------

- term_font.png
  デフォルトの ASCII 端末フォント画像。

- term_font_jp_katakana.png
  日本語カタカナ用の端末フォント画像。

- glyph_map_ascii.json
  デフォルトの ASCII 専用グリフプロファイル。

- glyph_map_jp_katakana.json
  カタカナ本体、alias、IME 設定を含む日本語カタカナ用プロファイル。


プロファイルの選択方法
----------------------

`computercraft-client.toml` の `terminal_glyph_map` で選びます。

例:

  terminal_glyph_map = "computercraft:textures/gui/glyph_map_ascii.json"
  terminal_glyph_map = "computercraft:textures/gui/glyph_map_jp_katakana.json"

この設定が無い、または不正な場合は、次へフォールバックします。

  computercraft:textures/gui/glyph_map_ascii.json


glyph profile JSON が定義するもの
-------------------------------

各 JSON では主に次を定義します。

- schema
  JSON 構造バージョン。

- name
  人間向けのプロファイル名。

- font
  そのプロファイルで使う端末フォント画像。

- fallbackGlyph
  未対応文字に使う glyph index。通常は `63` (`?`)。

- imeEnterDelay
  IME の Enter 保留時間ミリ秒。`0` で無効。

- map
  `U+XXXX` 形式の Unicode BMP コードポイントから、0..255 glyph code
  への対応表。

例:

  "U+30A2": { "glyph": 128, "char": "ア" }

`char` は人間向け確認用で、ローダ側でキーと一致するか検証します。


正規化方針
----------

コード側に言語依存の built-in 正規化は持ちません。

同じ glyph に落としたい別表記がある場合は、JSON の `map` に明示的に
列挙します。全角英数やカタカナの互換入力も同様です。


map が効く範囲
--------------

現在 `map` が適用されるのは主に次の入力経路です。

- 端末ウィジェットでの IME / 直接文字入力
- 端末ウィジェットでの paste

一方で、terminal 内部にすでに入っている既存の 0..255 glyph data には、
現時点では map を再適用しません。既存 terminal 内容は、その値をそのまま
glyph index として、現在選択中のフォント画像で描画します。

要するに:

- 入力経路: Unicode 文字 -> map -> 0..255 glyph code
- 描画経路: 既存の 0..255 glyph code -> 選択中フォント画像の対応セル


IME の既知制約
--------------

一部の IME / OS 組み合わせでは、変換確定 Enter が、確定文字列到着前に
余分な改行として端末へ入ることがあります。

現在の pending Enter ロジックは、後続の Enter を抑えられる場合はありま
すが、最初の IME 確定 Enter を確実に見分けることはできません。

これは macOS の日本語 IME で確認済みです。IME 直接入力は現時点では
experimental / incomplete とみなしてください。


同梱プロファイル
----------------

- glyph_map_ascii.json
  ASCII printable characters のみ。

- glyph_map_jp_katakana.json
  ASCII + カタカナ + 一部の和文記号。
  全角 ASCII などの互換入力は JSON 内で alias として定義します。


フォント画像レイアウト
----------------------

renderer は次の前提で画像を読みます。

- 画像サイズ: 256x256
- glyph grid: 16x16
- 1 glyph の文字本体サイズ: 6x9
- セル進み: 8x11
- glyph 0 の左上原点: x=1, y=1

計算式:

  column = glyph % 16
  row    = glyph / 16
  x = 1 + column * 8
  y = 1 + row * 11

各セルで実際にサンプリングされるのは 6x9 の領域だけです。


予約領域
--------

フォント画像の右端細帯は自由に壊さないでください。

renderer は画像右端近くの小さなパッチを、端末背景描画に使っています。
ここを壊すと、シェル補完や色付き UI の背景色表示がおかしくなることが
あります。

安全側のルール:

- 通常の glyph セルは編集してよい
- 右端の細い予約領域は触らない


画像編集メモ
------------

- 画像サイズは 256x256 を維持する
- 書き出し時に alpha を保持する
- 透明情報を失う変換は避ける
- 色や背景表示が崩れたら alpha 喪失を疑う


ImageMagick 例
--------------

以下はあくまで参考コマンドです。書き出し後は必ず目視確認してください。

このようなコマンドは透明情報を壊す可能性があります。

  magick out.png -strip -monochrome term_font.png

比較的安全な PNG32 書き出し:

  magick out.png PNG32:term_font.png

黒背景を透明化したい場合:

  magick out.png -colorspace Gray -threshold 50% -fuzz 1% -transparent black PNG32:term_font.png

追加で参考になるオプション例:

  magick out.png -alpha on -define png:color-type=6 PNG32:term_font.png
  magick out.png -background none -alpha background PNG32:term_font.png
  magick out.png -strip -alpha on PNG32:term_font.png
  magick out.png -filter point -resize 256x256 PNG32:term_font.png

alpha 確認:

  sips -g hasAlpha term_font.png


実用メモ
--------

- 先に glyph profile JSON を編集する
- その profile で必要な glyph セルだけをフォント画像側で編集する
- 実機確認前は次で再生成してから起動する

  ./gradlew :core:processResources :core:shadowJar :forge:runClient
