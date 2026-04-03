<!--
SPDX-FileCopyrightText: 2017 The CC: Tweaked Developers

SPDX-License-Identifier: MPL-2.0
-->

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./doc/logo-darkmode.png">
  <source media="(prefers-color-scheme: light)" srcset="./doc/logo.png">
  <img alt="CC: Tweaked" src="./doc/logo.png">
</picture>

# CC: Tweaked Glyph Profiles Fork
## Overview

This fork is based on CC: Tweaked 1.113.1, the final release for Minecraft Java Edition 1.20.1 on Forge, rather than the current upstream head. It makes terminal glyph handling data-driven while preserving the original CC terminal's fixed 256-glyph model as faithfully as possible.

In practice, by providing a terminal font PNG image containing 256 glyphs of size 6x9 and a profile JSON file which defines which Unicode characters map to those glyphs, this fork lets you use the characters you want with the glyph shapes you want for both input and rendering within the 256-glyph limit. A Japanese katakana font image and profile are bundled as one example.

## Included Profiles

- [`glyph_map_ascii.json`](./projects/core/src/main/resources/assets/computercraft/textures/gui/glyph_map_ascii.json): The default ASCII-compatible profile. It defines character-to-glyph mappings and uses the original CC terminal font PNG, [`term_font.png`](./projects/core/src/main/resources/assets/computercraft/textures/gui/term_font.png), for rendering.
- [`glyph_map_jp_katakana.json`](./projects/core/src/main/resources/assets/computercraft/textures/gui/glyph_map_jp_katakana.json): A bundled Japanese katakana example profile. It defines character-to-glyph mappings and points to its matching terminal font PNG, [`term_font_jp_katakana.png`](./projects/core/src/main/resources/assets/computercraft/textures/gui/term_font_jp_katakana.png).

### Screenshot

Monitor output using the bundled `jp_katakana` profile:

![Monitor demo](./doc/screenshots/glyph-profile-monitor.png)

Lua program used for the screenshot:

![Lua program demo](./doc/screenshots/glyph-profile-lua-program.png)

## Try The Bundled Profiles

Important: If you do not change `terminal_glyph_map`, the default ASCII profile is used and behaviour remains the same as the original CC terminal font.

To use the bundled Japanese katakana profile:

1. Launch the game once and then exit.
   This generates CC's client config file if it does not already exist.
2. Open `config/computercraft-client.toml` in your Minecraft instance or modpack folder.
3. Set the following value, and start the game again:

```toml
terminal_glyph_map = "computercraft:textures/gui/glyph_map_jp_katakana.json"
```

## Add Your Own Glyph Profiles

1. Copy [`term_font.png`](./projects/core/src/main/resources/assets/computercraft/textures/gui/term_font.png) to a new file such as `term_font_yourname.png`, then draw your glyphs in the editable area shown in [`terminal-font-edit-example.png`](./doc/screenshots/terminal-font-edit-example.png).
   Each glyph body is 6x9 pixels.

2. Copy [`glyph_map_ascii.json`](./projects/core/src/main/resources/assets/computercraft/textures/gui/glyph_map_ascii.json) to a new file such as `glyph_map_yourname.json`, then edit it so that:
   - the `font` field points to your new PNG file
   - the `map` section assigns the Unicode code points you want to the glyph cells you changed
   - only Unicode BMP code points (`U+0000` to `U+FFFF`) are used

3. Put both files directly into the mod JAR under:

```text
assets/computercraft/textures/gui/
```

Then set `terminal_glyph_map` in `config/computercraft-client.toml` to your new JSON file. For example:

```toml
terminal_glyph_map = "computercraft:textures/gui/glyph_map_hiragana.json"
```

On Windows, a tool such as [7-Zip](https://www.7-zip.org/) can open a JAR directly and let you place files into the required folder without fully extracting the archive.

Example font-editing workflow:

![Glyph profile font editing guide](./doc/screenshots/terminal-font-edit-example.png)

When editing a font texture, it is best to modify only the glyph areas required by your profile and leave the rest of the image unchanged. For detailed texture constraints and export notes, see the bundled documentation in this folder.

Reference files:

- Base terminal ASCII font image (CC original): [`projects/core/src/main/resources/assets/computercraft/textures/gui/term_font.png`](./projects/core/src/main/resources/assets/computercraft/textures/gui/term_font.png)
- Base profile reference: [`projects/core/src/main/resources/assets/computercraft/textures/gui/glyph_map_ascii.json`](./projects/core/src/main/resources/assets/computercraft/textures/gui/glyph_map_ascii.json)
- Example glyph map: [`projects/core/src/main/resources/assets/computercraft/textures/gui/glyph_map_jp_katakana.json`](./projects/core/src/main/resources/assets/computercraft/textures/gui/glyph_map_jp_katakana.json)
- Example font image: [`projects/core/src/main/resources/assets/computercraft/textures/gui/term_font_jp_katakana.png`](./projects/core/src/main/resources/assets/computercraft/textures/gui/term_font_jp_katakana.png)
- Detailed English specification: [`projects/core/src/main/resources/assets/computercraft/textures/gui/TERMINAL_GLYPH_PROFILE_SPEC.txt`](./projects/core/src/main/resources/assets/computercraft/textures/gui/TERMINAL_GLYPH_PROFILE_SPEC.txt)
- Detailed Japanese specification: [`projects/core/src/main/resources/assets/computercraft/textures/gui/TERMINAL_GLYPH_PROFILE_SPEC_ja.txt`](./projects/core/src/main/resources/assets/computercraft/textures/gui/TERMINAL_GLYPH_PROFILE_SPEC_ja.txt)

The bundled `jp_katakana` profile is only an example, not the intended limit of the system. Additional profiles such as Japanese hiragana or Korean Hangul are possible within the same resource-driven structure.

## Current Status

- This is experimental and not an official CC: Tweaked feature.
- Full internationalisation is not the goal.
- IME direct input is still incomplete on some platforms.
- In particular, some IME/OS combinations may still produce an extra newline
  when confirming composition with Enter.


## Contributing
Contributions to this fork are welcome, especially in the following areas:

- IME-related testing reports across different platforms and input methods.
- New language profiles from native or fluent users.
- Refinements to glyph-profile behaviour, input handling, and terminal rendering.

For language-profile contributions, the most useful form is a pair of resources:

- A glyph-map JSON file.
- A matching terminal font PNG.

Profiles for additional writing systems such as Japanese hiragana or Korean Hangul would be especially welcome.


## Upstream Project

This repository is an unofficial fork of CC: Tweaked.

The upstream project is here:
https://github.com/cc-tweaked/CC-Tweaked

This fork is based on the Minecraft 1.20.1 / CC: Tweaked 1.113.1 release line and adds experimental glyph-profile support for terminal rendering and input.

CC: Tweaked is a mod for Minecraft which adds programmable computers, turtles, and related peripherals. It is itself a fork of the much-beloved [ComputerCraft].

This fork is made with deep respect and gratitude to dan200 and SquidDev.
