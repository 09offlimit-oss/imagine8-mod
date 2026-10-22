# Publishing Imagine8 to CurseForge and Modrinth

## IMPORTANT: upload the built .jar, never this source zip
Both sites want the compiled mod jar only: `build/libs/imagine8-1.0.0.jar`
(NOT `imagine8-1.0.0-sources.jar`, and NOT a zip of this folder).

If you drag the source zip into the CurseForge app / Modrinth App "import" box, it treats it as a modpack and fails with:
`ENOENT: no such file or directory ... imagine8-mod/manifest.json`
That is not a bug in the mod. A modpack zip needs a manifest.json; a mod is just a .jar.

## Build the jar
1. Install JDK 21, then run `./gradlew build` (Windows: `gradlew.bat build`)
   or push to GitHub and download the artifact from the Actions tab.
2. Take `build/libs/imagine8-1.0.0.jar`.

## Before you publish
- In `src/main/resources/fabric.mod.json` and `LICENSE`, replace YOUR_NAME_HERE and the contact links.
- Bump `mod_version` in gradle.properties for each release.

## Modrinth (modrinth.com -> Create a project -> Mod)
- Upload the jar as a version. Loader: Fabric. Game version: 1.21.1.
- Dependency: Fabric API (required). Environment: client and server both required.
- Modrinth reads fabric.mod.json, so most fields auto-fill.

## CurseForge (curseforge.com/minecraft -> Author Console -> Create project)
- Project type: Mods. Upload the same jar.
- Game version 1.21.1, loader Fabric, Java 21.
- Relations: Fabric API as a required dependency.
- Set the project logo separately (use assets/imagine8/icon.png).
