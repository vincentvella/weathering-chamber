# AGENTS.md

Guidance for working on **Weathering Chamber**. See [`README.md`](README.md) for the
player-facing description.

## What this is

A Fabric mod that makes sand renewable via a water-powered machine that erodes
`cobblestone → gravel → sand`. **One source tree** ([`src/main`](src/main)) builds
**two jars** — Minecraft **1.21.1** and **26.2** — using [Stonecutter](https://stonecutter.kikugie.dev)
and **Mojang (official) mappings** throughout.

## Commands

```bash
./gradlew build                 # builds BOTH versions -> versions/<v>/build/libs/*.jar
                                # (there is NO chiseledBuild task in Stonecutter 0.9.7)
./gradlew :1.21.1:runClient     # launch a dev client (or :26.2:)
python tools/generate_assets.py # regenerate all textures + docs/img previews
```

- Needs **JDK 21** (runs Gradle) and **JDK 25** (the 26.2 toolchain; the foojay
  resolver auto-provisions it). `JAVA_HOME` should point at JDK 21.
- The asset generator needs `pillow` + `numpy`. Vanilla textures for the erosion
  diagram are read from `tools/vanilla/` if present (gitignored — Mojang's assets).

## Multi-version system (the important part)

Version differences are handled two ways; **do not** `if`-check the MC version at
runtime.

1. **Token replacements** in [`stonecutter.gradle.kts`](stonecutter.gradle.kts) — uniform
   renames applied to the newer source so code is written once:
   - `ResourceLocation` → `Identifier` (gate `>= 1.21.11`)
   - `isClientSide` → `isClientSide()` (gate `>= 26.1`)
2. **`//?` conditional blocks** at genuine API divergences (search the source for `//?`):
   | File | divergence | gate |
   |------|-----------|------|
   | `ModBlocks.java` | registration: `ResourceKey`+`setId` vs `ResourceLocation` | `>=1.21.2` |
   | `ModBlocks.java` | creative tab: `CreativeModeTabEvents` vs `ItemGroupEvents` | `>=26.1` |
   | `block/entity/WeatheringChamberBlockEntity.java` | NBT: `ValueOutput/ValueInput` vs `CompoundTag` | `>=26.1` |
   | `menu/WeatheringChamberMenu.java` | `addStandardInventorySlots` vs manual slots | `>=1.21.2` |
   | `block/WeatheringChamberBlock.java` | drop-on-break: `affectNeighborsAfterRemoval` vs `onRemove` | `>=1.21.2` |
   | `client/WeatheringChamberScreen.java` | GUI: `GuiGraphicsExtractor` vs `GuiGraphics.renderBg` | `>=26.1` |

Stonecutter flips the `//?` comments on disk for the **active** version (switch with the
generated `Set active project to …` Gradle tasks). Whichever is active is what the IDE
compiles; `./gradlew build` always builds both regardless.

The menu deliberately uses the **vanilla furnace pattern** (`AbstractContainerMenu` +
`ContainerData` + client-side `SimpleContainer`), not `ExtendedScreenHandlerType`.

## Layout

```
settings.gradle.kts / stonecutter.gradle.kts / build.gradle.kts   build + version wiring
stonecutter.properties.toml     per-version deps + mod.id/name/version
src/main/java/com/example/weathering/   the mod (Mojmap)
src/main/resources/
  fabric.mod.json               id "weathering", ${version}/${minecraft} injected
  assets/weathering/            blockstates, models, textures/, items/, lang, icon.png
  data/weathering/              recipe/, loot_table/blocks/  (+ data/minecraft tags)
tools/generate_assets.py        deterministic texture + preview generator
docs/img/                       README/store art (generated)
.github/workflows/              build.yml (CI) + release.yml (publish on tag)
```

## Conventions

- **Add an erosion step:** one line in the `EROSION` map in
  `WeatheringChamberBlockEntity.java`. `isGrindable()` (same file) gates hopper/GUI input.
- **Textures are generated, not hand-drawn.** Edit the palette/shapes in
  `tools/generate_assets.py` and rerun it; commit the regenerated PNGs.
- **Grind speed:** `maxProgress` (ticks; 20 = 1s) in the block entity.
- **New version divergence:** prefer a token replacement for a uniform rename, else a
  `//?` block. Keep gate thresholds consistent with the table above.

## Releasing

1. Bump `mod.version` in `stonecutter.properties.toml`.
2. Add a `## [x.y.z]` section to [`CHANGELOG.md`](CHANGELOG.md) (release notes are pulled
   from it automatically).
3. Commit, then `git tag vX.Y.Z && git push origin vX.Y.Z`.

`release.yml` builds both jars and publishes each (with its own game versions and a
`<mod.version>+<mc>` version number) to GitHub Releases + Modrinth + CurseForge. Platform
credentials live in repo secrets/variables (`MODRINTH_ID`/`MODRINTH_TOKEN`,
`CURSEFORGE_ID`/`CURSEFORGE_TOKEN`); a platform is skipped if its token is unset.

## Caveats

- **26.2 postdates the assistant's training.** Its signatures here were verified against
  the actual downloaded 26.2 jars — when touching 26.x APIs, confirm against the real jar
  (`javap` on the loom-cached `minecraft-merged-deobf-26.2.jar`) rather than assuming.
- The `>=26.1` NBT gate is correct for the two current targets; revisit if an intermediate
  version (e.g. 1.21.5) is added.
