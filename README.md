# HTTYMD: Night Fury (Fabric, Minecraft 26.3)

A friendly, rideable **Night Fury** that hatches from the **Dragon Egg**. It is the only way to get one.

> Model, animations and texture come from the **HTTYMD (How to Train Your Minecraft Dragon)** mod by
> **The HTTYMD Team** - https://github.com/HTTYMD-Team/HTTYMD-Mod - used with the owner's permission.
> See [CREDITS.md](CREDITS.md).

## How to get a Night Fury

1. Place a **Dragon Egg** on the ground.
2. Right-click it with a **bottle of Dragon's Breath**.
3. The egg glows and shakes for ~8 seconds, then hatches into a Night Fury that is **tamed to you**.

(For testing in creative / with cheats: `/give @s minecraft:dragon_egg` and `/give @s minecraft:dragon_breath`.)

## Controls

| Action | How |
| --- | --- |
| Mount | Right-click your Night Fury |
| Move | W A S D (the dragon turns where you look) |
| Take off | Press **Jump** while riding on the ground |
| Fly | Look where you want to go and hold **W** (S slows / backs up) |
| Land | Fly down to the ground - it lands by itself |
| Dismount | **Sneak** |
| Sit / stand | **Sneak + right-click** the dragon |
| Heal | Right-click with raw/cooked fish |

It follows you, defends you, and takes no fall damage.

On a dedicated server, set `allow-flight=true` in `server.properties`, otherwise Minecraft kicks riders
for "flying".

## Building

Push to GitHub. The **Build mod** workflow (Actions tab) builds the jar; download it from the run's
**Artifacts** section and put it in your `mods` folder together with **Fabric API**.

Needs Java 25. The workflow looks up the newest Fabric Loader / Loom / Fabric API for 26.3 by itself.

## Layout

- `src/main/java/com/httymd/entity` - the dragon (taming, riding, flight)
- `src/main/java/com/httymd/hatching` - egg hatching
- `src/main/java/com/httymd/client` - renderer, model, controls
- `src/main/java/com/httymd/client/model/NightFuryModelData.java` - **generated** from the original model by `tools/convert_model.py`
