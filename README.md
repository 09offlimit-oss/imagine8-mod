# Imagine8 (Fabric, Minecraft 1.21.1)

Horror mod: Imagine8 hunts you at night, chases you with looping scary noises, jumpscares and kills you on touch,
and gives up (despawns) after 30 seconds of chasing.

## Build
Requires JDK 21.
    ./gradlew build        (Windows: gradlew.bat build)
Your mod jar appears in build/libs/imagine8-1.0.0.jar (NOT the -sources jar).

No JDK? Push this folder to a GitHub repo; the included Action builds the jar for you (Actions tab -> Artifacts).

## Publishing
See PUBLISHING.md. Short version: upload build/libs/imagine8-1.0.0.jar to CurseForge/Modrinth, never the source zip
(importing the source zip is what causes "ENOENT ... manifest.json").

## Play / summon
- Spawns by itself at night in the Overworld (survival, non-peaceful).
- /imagine8 [player]  (needs op / cheats)
- Imagine8 Spawn Egg (creative menu)
- Cursed 8 Ball (shapeless: ender pearl + black dye + fermented spider eye + bone) - right-click to summon.
Requires Fabric Loader + Fabric API.

## Tweaks (Imagine8Mod.java)
NIGHT_SPAWN_CHANCE, NIGHT_CHECK_INTERVAL, MAX_CHASE_TICKS. Speed is in Imagine8Entity.createAttributes().
Swap sounds: replace assets/imagine8/sounds/chase_loop.ogg and jumpscare.ogg (mono .ogg Vorbis).
Swap picture: replace assets/imagine8/textures/entity/imagine8.png (update TEX_W/TEX_H in Imagine8Renderer if the size changes).
