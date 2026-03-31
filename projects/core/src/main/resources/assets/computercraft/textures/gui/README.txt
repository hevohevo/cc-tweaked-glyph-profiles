CC: Tweaked Terminal Glyph Profiles
==================================

Overview
--------

ComputerCraft is primarily an English-first terminal environment. This folder
contains an optional glyph-profile system for limited non-Latin terminal input
and display within the terminal's fixed 256-glyph font.

This is not full internationalisation. Instead, each profile maps Unicode
input onto the terminal's internal 0..255 glyph space and selects a compatible
font texture.


Files
-----

- term_font.png
  Default ASCII terminal font texture.

- term_font_jp_katakana.png
  Japanese katakana terminal font texture.

- glyph_map_ascii.json
  Default ASCII-only glyph profile.

- glyph_map_jp_katakana.json
  Japanese katakana glyph profile with aliases and IME settings.


Selecting a profile
-------------------

Profiles are selected in `computercraft-client.toml` using the
`terminal_glyph_map` setting.

Examples:

  terminal_glyph_map = "computercraft:textures/gui/glyph_map_ascii.json"
  terminal_glyph_map = "computercraft:textures/gui/glyph_map_jp_katakana.json"

If this setting is omitted or invalid, CC falls back to:

  computercraft:textures/gui/glyph_map_ascii.json


What a glyph profile controls
-----------------------------

Each glyph profile JSON may define:

- schema
  JSON structure version.

- name
  Human-readable profile name.

- font
  Font texture resource used by terminal rendering.

- fallbackGlyph
  Glyph index used for unsupported input, usually `63` (`?`).

- imeEnterDelay
  Delay in milliseconds for IME Enter handling.
  Use `0` to disable delayed Enter handling.

- map
  Unicode BMP code point mappings in `U+XXXX` form.

Example entry:

  "U+30A2": { "glyph": 128, "char": "ア" }

The optional `char` field is only for readability and validation.


Normalisation policy
--------------------

Built-in language-specific normalisation is intentionally not used.

If multiple Unicode characters should map to the same terminal glyph, define
them explicitly in the JSON `map`. This includes compatibility forms such as
full-width ASCII, katakana variants, and similar aliases.


Where mapping is applied
------------------------

The glyph profile's `map` is currently applied to:

- IME/direct character input in terminal widgets
- Paste input in terminal widgets

The map is not currently re-applied to existing terminal glyph data already
stored in the terminal. Existing terminal contents are rendered as their raw
0..255 glyph codes using the currently selected font texture.

In practice this means:

- input path: Unicode character -> glyph profile map -> 0..255 glyph code
- render path: existing 0..255 glyph code -> selected font texture cell


Known IME limitation
--------------------

On some IME/OS combinations, confirming a conversion with Enter may still
produce an extra newline before the committed characters arrive.

The current delayed-Enter handling can suppress some follow-up Enter presses,
but it cannot reliably distinguish the very first IME confirmation Enter from
a real terminal Enter.

This has been observed on macOS with Japanese IME. Treat IME direct input as
experimental/incomplete for now.


Current bundled profiles
------------------------

- glyph_map_ascii.json
  ASCII printable characters only.

- glyph_map_jp_katakana.json
  ASCII plus katakana and selected Japanese punctuation.
  Full-width ASCII and similar compatibility forms are represented as aliases
  in the JSON map.


Font texture layout
-------------------

The terminal renderer expects:

- texture size: 256x256
- glyph grid: 16x16
- glyph body size: 6x9
- cell step: 8x11
- glyph 0 origin: x=1, y=1

Texture lookup formula:

  column = glyph % 16
  row    = glyph / 16
  x = 1 + column * 8
  y = 1 + row * 11

Only the 6x9 glyph area is sampled for each cell.


Reserved texture area
---------------------

Do not freely overwrite the far-right edge of the font texture.

The renderer uses a small patch near the far-right side of the image for
terminal background rendering. Damaging this area can cause incorrect colours
in shell completion and other coloured terminal UI.

Safe rule:

- edit normal glyph cells freely
- do not modify the narrow reserved patch at the far right


Image editing notes
-------------------

- Keep the image at 256x256.
- Preserve alpha when exporting.
- Do not flatten the image to opaque black/white.
- If terminal colours look wrong afterwards, verify alpha is still present.


ImageMagick examples
--------------------

The following commands are examples only. Always verify the output image
manually afterwards.

This kind of command may remove transparency and break terminal colours:

  magick out.png -strip -monochrome term_font.png

Safer PNG32 write:

  magick out.png PNG32:term_font.png

Treat black as transparent:

  magick out.png -colorspace Gray -threshold 50% -fuzz 1% -transparent black PNG32:term_font.png

Additional options which may be useful, depending on the source image:

  magick out.png -alpha on -define png:color-type=6 PNG32:term_font.png
  magick out.png -background none -alpha background PNG32:term_font.png
  magick out.png -strip -alpha on PNG32:term_font.png
  magick out.png -filter point -resize 256x256 PNG32:term_font.png

Check alpha afterwards:

  sips -g hasAlpha term_font.png


Practical workflow
------------------

- Change the glyph profile JSON first.
- Edit only the glyph cells required by that profile's font texture.
- Rebuild before launching the client:

  ./gradlew :core:processResources :core:shadowJar :forge:runClient
