# Slab Placer

A client-side Fabric mod for **Minecraft Java Edition 26.3**. All code, controls, and settings are in English.

Slab Placer filters normal slab placements. Aim at the half you want to place: the mod cancels placements that do not match your settings. It does not automatically move a slab to the other half.

## Install

1. Install Fabric Loader **0.19.5 or newer** for Minecraft **26.3**.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) **0.160.5+26.3 or newer for 26.3**.
3. Put `slab-placer-1.1.0+mc26.3.jar` in your Minecraft instance's `mods` folder.
4. Start the Fabric profile and join a world.
5. Press **K** to open **Slab Placer Settings**, or open **Mods > Slab Placer > Configure** with Mod Menu installed.

Minecraft 26.3 requires Java 25. The mod is installed on your client only; a server does not need to install it. It limits your own placements and does not change other players' rules.

You can rebind the settings key under **Options > Controls > Key Binds > Slab Placer**. To access settings through the Mods screen, install [Mod Menu for Minecraft 26.3](https://modrinth.com/mod/modmenu/versions?g=26.3) and its required dependencies, including [Text Placeholder API](https://modrinth.com/mod/placeholder-api). The integration is compiled against Mod Menu 21.0.0-beta.1. Select **Slab Placer** and click its configuration button to edit the same mode setting from the title screen or while playing. Save, Cancel, and Escape return to Mod Menu. Mod Menu is optional; without it, the K shortcut still works. No configuration library is required.

## Settings

Use the single **Mode** button to cycle through four exclusive modes:

**Only Top -> Only Bottom -> Prevent Double -> No Function -> Only Top**

| Mode | Placement behavior |
| --- | --- |
| **Only Top** | Allow only single slabs in the upper half. Bottom slabs and double slabs are blocked. |
| **Only Bottom** | Allow only single slabs in the lower half. Top slabs and double slabs are blocked. |
| **Prevent Double** | Allow either single-slab half, but prevent combining slabs into a full block. |
| **No Function** | Use normal Minecraft placement without any restrictions. |

Exactly one mode is selected at a time. The default is **No Function**. The screen shows a short explanation of the selected mode. Click **Save** to apply it; **Cancel** or **Escape** discards the draft. Settings are available through **K** or **Mod Menu > Slab Placer > Configure**.

The selection is saved in `config/slabplacer.json`, for example:

```json
{
  "mode": "ONLY_TOP"
}
```

Configurations from 1.0.x are loaded automatically with equivalent placement behavior. An enabled `noFunction` takes precedence, followed by `onlyTopSlabs`, `onlyBottomSlabs`, and `preventDoubleSlabs`. If no restriction is active, the mode is **No Function**. Loading leaves the existing file intact; saving writes the new single-mode format.

The mod checks the placement Minecraft would make, including the actual destination block, replaceable blocks, and slab merging. Both hands and Creative/Survival item-use paths are covered. Ordinary block interactions such as opening a chest still run normally. All blocks based on Minecraft's `SlabBlock`, including the 26.3 slabs, use the same filter; other mods that replace the placement pipeline may need compatibility changes.

## Build

Use JDK **25** and the included Gradle wrapper:

```powershell
.\gradlew.bat build
```

On Linux or macOS:

```sh
sh ./gradlew build
```

The installable mod is written to `build/libs/slab-placer-1.1.0+mc26.3.jar`. The `-sources.jar` is for developers, not installation. Gradle downloads its tools and dependencies on the first run.

```powershell
.\gradlew.bat test
.\gradlew.bat runClient
```

Automated tests cover mode cycling, independent drafts, configuration persistence, all legacy setting combinations, malformed files, and all 12 mode/placement combinations.

## Manual gameplay checks

- With Mod Menu installed, open **Mods > Slab Placer > Configure** from the title screen and in-game. Verify Save returns to Mod Menu and Cancel/Escape discards edits.
- Without Mod Menu installed, verify the game still starts and **K** opens the settings in a world.

- In **Only Top** mode, attempt top, bottom, and double placement, including from an offhand stack.
- Repeat in **Only Bottom** mode.
- In **Prevent Double** mode, verify that both single halves work and merging either half fails.
- Click the face of a neighboring block when the placement destination already contains a slab; merging must still be blocked.
- Check side faces above/below the midpoint, top/bottom faces, replaceable grass, underwater placements, Creative, and Survival.
- Open a chest while holding a slab; sneak-place against it to exercise the placement restriction.
- Cycle through all four modes and verify that **No Function** allows normal placement and the next click wraps back to **Only Top**.
- Save, restart, and confirm the selection persists. Confirm Cancel/Escape discards a draft.
- Repeat on a vanilla multiplayer server; blocked attempts must not consume items or create ghost blocks.

## Technical notes

The client mixin evaluates restrictions only when Minecraft reaches item placement, after block interaction handling. When blocked, it returns `FAIL` and suppresses the matching use-on-block packet. This avoids an unwanted server placement or a fallback action with the other hand. Other interaction packets are unaffected.

A malformed configuration is logged and defaults are used without overwriting the original file. If a legacy file enables both half restrictions while bypass is off, **Only Top** takes precedence.

Version choices follow the [official Fabric 26.3 development guidance](https://www.fabricmc.net/2026/09/15/263.html). This project targets exactly 26.3; other Minecraft versions require a separate compatibility check.

Licensed under the MIT License. Not an official Minecraft product; not approved by or associated with Mojang or Microsoft.
