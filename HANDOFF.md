# Weathering Chamber — Session Handoff

Paste-in context for continuing this project in a new conversation rooted at
`C:\Users\vince\Workspace\weathering-chamber`. Everything described here is
already written to disk.

## What this is

A Fabric mod that makes **sand renewable**. It adds a furnace-like machine, the
**Weathering Chamber**, that mimics erosion:

```
cobblestone ──grind──► gravel ──grind──► sand
```

Cobblestone is infinitely renewable (lava+water generator), so gravel and sand
become renewable too. The machine is **powered by water** (must touch a water
block on any side), not fuel. Each grind ~10s. Hoppers automate it (insert into
input, extract from output).

## Key decisions (already made — don't re-litigate)

- **Loader:** Fabric.
- **Targets:** Minecraft **1.21.1 AND 26.2**, published as two jars under one
  project. Single codebase via **Stonecutter**.
- **Mappings:** **Mojang (official) mappings** everywhere (Stonecutter needs one
  mapping across versions; Yarn is dropped in 26.x anyway).
- **Menu:** vanilla furnace pattern (`AbstractContainerMenu` + `ContainerData` +
  `SimpleContainer` on client). Deliberately NOT using `ExtendedScreenHandlerType`.
- **Art:** reuses vanilla textures — block = cobblestone sides + `furnace_front`;
  GUI = vanilla `furnace.png`. No custom PNGs yet.

## Verified toolchain versions (checked against live Fabric/Stonecutter sources)

| Thing | Version |
|---|---|
| Stonecutter plugin | `0.9.7` |
| loom-back-compat | `0.4.2` |
| Fabric Loom | `1.17-SNAPSHOT` |
| Gradle (wrapper) | `9.6.1` |
| Fabric Loader | `0.19.3` (both versions) |
| Fabric API | `0.116.15+1.21.1` / `0.156.0+26.2` |
| Java | `21` (1.21.1) / `25` (26.2) — per-version toolchain in build.gradle.kts |

## Multi-version mechanics

- Two **token replacements** in `stonecutter.gradle.kts` let the code be written once:
  - `ResourceLocation` → `Identifier` (26.x rename), gate `current.parsed >= "1.21.11"`.
  - `isClientSide` → `isClientSide()` (26.x made `Level.isClientSide` a private field with
    a public accessor), gate `>= "26.1"`. Every use here is a `level.isClientSide` read.
- The genuine API divergences are isolated in **`//?` conditional blocks**
  (search the source for `//?`):
  1. `ModBlocks.java` — 26.x registration needs `ResourceKey` + `Properties.setId(...)`
     (gate `>=1.21.2`); 1.21.1 registers by `ResourceLocation`, no `setId`.
  2. `ModBlocks.java` — creative-tab entry: Fabric renamed the API, so 26.x uses
     `CreativeModeTabEvents.modifyOutputEvent(...)` (`fabric-creative-tab-api-v1`) and
     1.21.1 uses `ItemGroupEvents.modifyEntriesEvent(...)`. Gate `>=26.1`.
  3. `block/entity/WeatheringChamberBlockEntity.java` — NBT: `saveAdditional(ValueOutput)` /
     `loadAdditional(ValueInput)` (26.x, gate `>=26.1`) vs
     `(CompoundTag, HolderLookup.Provider)` (1.21.1). Note `input.getIntOr(name, default)`
     on the 26.x side.
  4. `menu/WeatheringChamberMenu.java` — `addStandardInventorySlots(...)` exists from 1.21.2
     (gate `>=1.21.2`); 1.21.1 adds the 27 inventory + 9 hotbar slots by hand.
  5. `block/WeatheringChamberBlock.java` — content-drop-on-break: 26.x overrides
     `affectNeighborsAfterRemoval(BlockState, ServerLevel, BlockPos, boolean)` (gate
     `>=1.21.2`); 1.21.1 overrides `onRemove(..., BlockState newState, boolean)` with a
     `!state.is(newState.getBlock())` guard. Both call `Containers.dropContents`.
  6. `client/WeatheringChamberScreen.java` — GUI: `GuiGraphicsExtractor.extractBackground`
     + `blit(RenderPipelines.GUI_TEXTURED, …)` (26.x) vs `GuiGraphics.renderBg` +
     `blit(…)` (1.21.1).
- Stonecutter created `versions/1.21.1/` and `versions/26.2/` subprojects.
- **Active version is currently `1.21.1`** (Stonecutter flips the `//?` comments on
  disk when you switch; use the `Set active project to …` Gradle tasks).

## File map

```
settings.gradle.kts          versions 1.21.1 + 26.2, stonecutter + loom-back-compat
stonecutter.gradle.kts       active version, ResourceLocation→Identifier rule
build.gradle.kts             Mojmap mappings, per-version Java (21/25)
stonecutter.properties.toml  per-version deps
gradlew / gradlew.bat / gradle/wrapper/gradle-wrapper.jar   (present, v9.6.1)
src/main/java/com/example/weathering/
  WeatheringChamberMod.java              main entrypoint
  ModBlocks.java  ModBlockEntities.java  ModMenuTypes.java
  block/WeatheringChamberBlock.java
  block/entity/WeatheringChamberBlockEntity.java
  menu/WeatheringChamberMenu.java
  client/WeatheringChamberModClient.java  client/WeatheringChamberScreen.java
src/main/resources/
  fabric.mod.json  (id "weathering", ${version}/${minecraft} injected)
  assets/weathering/{blockstates,models/block,models/item,items,lang}
  data/weathering/{recipe,loot_table/blocks}  data/minecraft/tags/block/mineable
```

## Current state

- ✅ Full project scaffolded; Stonecutter wiring configures cleanly (the earlier
  `registerChiseled` error was fixed — that API is gone in 0.9.x; tasks are auto-provided).
- ✅ Gradle wrapper installed (v9.6.1) — `./gradlew` works.
- ✅ **Compiles and builds BOTH versions.** `./gradlew build` fans out to both subprojects
  and produces `versions/1.21.1/build/libs/weathering-1.0.0+1.21.1.jar` (remapped) and
  `versions/26.2/build/libs/weathering-1.0.0+26.2.jar`, plus `-sources` jars.
- Compile fixes applied to get here (all against the real downloaded 26.2 jars):
  - `SoundType` moved to `net.minecraft.world.level.block.SoundType` (both versions; the
    old `net.minecraft.sounds` import was wrong).
  - `processResources` in `build.gradle.kts` read `mod.mc_compat` off the *task* receiver;
    now qualified as `project.property("mod.mc_compat")` (version-specific Stonecutter prop).
  - Divergences #2, #4, #5 above (itemgroup rename, `addStandardInventorySlots`,
    content-drop hook) and the `isClientSide()` token replacement.
- ⚠️ **Not yet run in-game.** Compilation is verified; runtime (world load, GUI render,
  datapack load) is not.

## Open TODOs / next steps

1. ✅ Done — both versions compile and build. `./gradlew build` fans out to both version
   subprojects (Stonecutter 0.9.7 provides NO `chiseledBuild` task — that name was wrong).
2. ✅ Done — **content-drop-on-break** implemented as divergence #5 in
   `WeatheringChamberBlock.java` (drops input/output stacks; block still drops via loot table).
3. ✅ Done — custom block faces (side/top/bottom/front) + a themed GUI sheet replace the
   reused vanilla art. They're **generated** by `tools/generate_assets.py` (Python/Pillow,
   deterministic seeds), which also emits the GitHub preview art under `docs/img/`. The
   block model now uses `orientable_with_bottom`; the screen points at the custom GUI.
   Regenerate with `python tools/generate_assets.py`.
4. Datapack/asset JSON was reviewed statically and uses the 1.21+ singular dir names
   (`recipe`, `loot_table`, `tags/block`) and `id`-based recipe result — valid for both
   targets; the 1.21.4+ `assets/weathering/items/` model definition covers 26.2 while
   `models/item/` covers 1.21.1. Still worth a **runtime** load check on 26.2 in-game.
5. The `//? if >=26.1` NBT threshold is correct for the two current targets; revisit if
   an intermediate version (e.g. 1.21.5) is added later.

## How to build / run

```
./gradlew tasks --group stonecutter   # list version switch tasks
./gradlew build                        # builds BOTH versions -> jars in each build/libs
```
First run downloads Gradle 9.6.1 + MC/mappings/Fabric (several minutes). Needs JDK 21
(Gradle itself) and JDK 25 (26.2 toolchain; foojay resolver can auto-provision).
Or open the folder in IntelliJ IDEA + Minecraft Development plugin.
