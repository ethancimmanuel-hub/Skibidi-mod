# Skibidi Toilet Mod (Fabric, Minecraft 1.20.1)

A hostile mob mod:

- **Skibidi Toilet** is a monster that spawns like a normal hostile mob.
- **Attack:** when it gets near a player, it lunges its head forward (extended
  reach, ~2.4 extra blocks) and deals damage — see `ExtendHeadAttackGoal`.
- **Right-click to flush:** right-clicking the toilet starts a 1.5s "flush"
  sequence — the head spins rapidly, water/bubble particles and sounds play,
  and then it's removed from the world (`SkibidiToiletEntity#beginFlush`).
  It drops no loot when flushed, but drops normal loot if killed by combat.
- A spawn egg is added to the Spawn Eggs creative tab for easy testing.

## Project layout

```
src/main/java/net/skibiditoiletmod/
  SkibidiToiletMod.java              # main mod entrypoint, spawn egg
  entity/
    ModEntities.java                 # entity type registration + attributes
    SkibidiToiletEntity.java         # core mob logic (AI, flush, ticking)
    goal/ExtendHeadAttackGoal.java   # melee goal with extended lunge reach
  client/
    SkibidiToiletModClient.java      # client entrypoint, renderer registration
    render/
      SkibidiToiletEntityModel.java    # bowl + neck + head model, animation
      SkibidiToiletEntityRenderer.java
      ModModelLayers.java
src/main/resources/
  fabric.mod.json
  assets/skibiditoiletmod/
    lang/en_us.json
    textures/entity/skibidi_toilet.png   # placeholder texture (see below)
```

## How the mechanics work

- `SkibidiToiletEntity` tracks three synced values so the client can animate
  it: `HEAD_EXTENSION` (0–1), `FLUSHING` (bool), `SPIN_ANGLE` (degrees).
- **Attacking:** `ExtendHeadAttackGoal` extends `MeleeAttackGoal` and gives a
  bigger `getSquaredMaxAttackDistance`, so the toilet can hit you before it's
  standing right on top of you. On a successful attack it calls
  `startHeadLunge()`, which the entity's `tick()` uses to smoothly push
  `HEAD_EXTENSION` toward `1.0` and back down — the model reads that value
  and stretches the neck/head toward the target.
- **Flushing:** right-clicking calls `interactMob`, which (server-side only)
  starts `beginFlush()`. While `FLUSHING` is true, `tickFlush()` spins
  `SPIN_ANGLE` faster each tick, retracts the head into the bowl, spawns
  splash/bubble particles, and after `FLUSH_DURATION_TICKS` (30 = 1.5s)
  calls `discard()` to remove the entity — this is the "dies" part.

## Building — Option A: let GitHub build it for you (no installs needed)

This project needs to download Minecraft + Fabric's libraries to compile,
which requires internet access to Mojang/Fabric's servers. I can't reach
those from this sandbox, but GitHub's free build runners can, and there's a
workflow already set up for it (`.github/workflows/build.yml`):

1. Create a new repository on [github.com](https://github.com) (public or
   private, either works).
2. Upload this whole folder to it — easiest way: on the repo page, drag the
   unzipped project folder onto "uploading an existing file", or use
   `git init && git add . && git commit -m "init" && git push`.
3. Go to the **Actions** tab of your repo. A workflow run should start
   automatically (or click "Run workflow" if it didn't).
4. Once it finishes (green check, ~2–3 minutes), open that run and scroll to
   **Artifacts** → download `skibidi-toilet-mod-jar`. It's a zip containing
   the built `.jar`.
5. Unzip it and drop the `.jar` into your Fabric `mods/` folder (Minecraft
   1.20.1, with **Fabric Loader** and **Fabric API** installed).
6. In game: creative inventory → Spawn Eggs tab → "Skibidi Toilet" spawn egg,
   or run `/summon skibiditoiletmod:skibidi_toilet`.

No Java or Gradle install needed on your computer for this option.

## Building — Option B: build it locally

1. Install **JDK 17** and make sure it's on your `PATH`.
2. Unzip this project.
3. From the project root, run:
   - Windows: `gradlew.bat build`
   - macOS/Linux: `./gradlew build`
   - (If you don't have a `gradlew` wrapper yet, run `gradle wrapper` once
     with Gradle 8.x installed, or open the folder in IntelliJ IDEA with the
     Minecraft Development / Fabric plugin, which will generate it for you.)
4. The built mod jar will be in `build/libs/skibidi-toilet-mod-1.0.0.jar`.
5. Drop that jar into your Fabric-loader `mods/` folder (Minecraft 1.20.1,
   with **Fabric Loader** and **Fabric API** installed).
6. In game, open the creative inventory → Spawn Eggs tab → find the
   "Skibidi Toilet" spawn egg, or run:
   `/summon skibiditoiletmod:skibidi_toilet`

## About the texture/model

I generated a simple placeholder texture (`skibidi_toilet.png`) and a basic
block model (bowl + neck + head) so the project compiles and renders out of
the box. For a proper look, you'll probably want to:

- Open the texture in a pixel editor and re-paint it (grey/white toilet bowl,
  a face on the head cube).
- Or redo the model in **Blockbench** (export as a Java entity model) if you
  want more detail than plain cuboids — just swap the `getTexturedModelData()`
  contents in `SkibidiToiletEntityModel`.

## Notes / things you may want to tweak

- Damage, speed, health, and reach are all in `SkibidiToiletEntity.createAttributes()`
  and `ExtendHeadAttackGoal.EXTRA_REACH_SQUARED`.
- Flush duration: `SkibidiToiletEntity.FLUSH_DURATION_TICKS`.
- Sounds currently reuse vanilla sound events so no custom `.ogg` files are
  needed — swap `SoundEvents.XYZ` calls for custom sounds if you want a real
  "toilet flush" sfx (you'd add a `sounds.json` + `.ogg` file under
  `assets/skibiditoiletmod/sounds/`).
