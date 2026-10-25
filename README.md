# AutoTool (Fabric, Minecraft 26.3)

Clientseitige Mod: Wenn du anfängst, einen Block abzubauen, wechselt sie automatisch auf das
beste Werkzeug in deiner Hotbar, zum Beispiel auf die Axt für Holz, die Spitzhacke für Stein
oder die Schaufel für Erde.

Läuft nur auf dem Client und funktioniert auch auf Vanilla-Servern.

## Funktionen

- Wählt das Werkzeug, mit dem der Block **Drops** gibt (z.B. Spitzhacke bei Erzen), und danach das
  **schnellste** (Effizienz wird mitgezählt).
- Wechselt auch, wenn du die Maustaste gedrückt hältst und mit dem Fadenkreuz von einem Block auf einen
  anderen gehst (Holz → Stein).
- **Zurückwechseln:** Wenn du die Maustaste loslässt, kommt der vorherige Slot zurück.
- **Bruchschutz:** Werkzeuge mit wenig Haltbarkeit (Standard ≤ 5) werden nicht gewählt.
- Im Kreativmodus ist die Mod aus, und bei Blöcken, die sofort abgebaut werden (Blumen, Fackeln),
  passiert nichts.
- Taste **K** schaltet die Mod an und aus (änderbar unter Optionen → Steuerung → AutoTool).

## Konfiguration

`.minecraft/config/autotool.properties` (wird beim ersten Start angelegt):

```properties
enabled=true
switchBack=true
minDurability=5
ignoreCreative=true
```

## Bauen

Du brauchst **Java 25** (JDK).

```bash
./gradlew build        # Windows: gradlew.bat build
```

Die fertige Mod liegt danach in `build/libs/autotool-1.0.0.jar` (nicht die `-sources.jar`).

Alternativ: Lade das Projekt in ein GitHub-Repository hoch. Die mitgelieferte GitHub Action
(`.github/workflows/build.yml`) baut die Jar automatisch und stellt sie unter „Artifacts“ bereit.

## Installieren

1. Installiere den [Fabric Loader](https://fabricmc.net/use/) für Minecraft 26.3.
2. Lege die [Fabric API](https://modrinth.com/mod/fabric-api) (Version für 26.3) und
   `autotool-1.0.0.jar` in den Ordner `.minecraft/mods`.
3. Starte das Fabric-Profil.
