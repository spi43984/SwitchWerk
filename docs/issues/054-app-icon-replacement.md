# 054 App Icon Replacement

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: Branding / App Identity / Android Resources

## Ziel

Das fest konfigurierte Android-App-Icon von SwitchWerk soll durch ein neues offizielles App-Icon ersetzt werden.

Im Rahmen der Implementierung fragt Codex nach dem zu verwendenden Ausgangsbild und dem gewünschten Speicherort bzw. Quellpfad. Aus diesem Ausgangsbild werden alle für Android notwendigen Icon-Ressourcen erzeugt und in die bestehende App-Icon-Konfiguration eingebunden.

## Hintergrund

Issue 035 hat App-Identity, Help und Release Packaging eingeführt und dabei bereits Launcher- und Adaptive-Icons für SwitchWerk bereitgestellt. Dieses Issue ersetzt diese fest konfigurierten Icon-Ressourcen gezielt durch ein neues Icon.

Das Icon bleibt nach der Implementierung fest in der App konfiguriert. Es gibt keinen Icon-Auswahldialog in der App und keinen dynamischen Wechsel zur Laufzeit.

## Scope

### Vor Implementierungsbeginn

Codex muss nach dem Startprompt und vor Dateiänderungen explizit abfragen:

- welches Ausgangsbild als neues App-Icon verwendet werden soll
- wo dieses Ausgangsbild lokal liegt oder wie es bereitgestellt wird
- ob das Ausgangsbild zusätzlich im Repository als Quell-/Referenzdatei abgelegt werden soll
- falls ja: welcher Speicherort im Repository verwendet werden soll

Wenn der Benutzer bereits ein eindeutiges Bild und einen eindeutigen Speicherort genannt hat, darf Codex diese Angaben übernehmen und muss nicht erneut fragen.

### Icon-Erzeugung

Aus dem Ausgangsbild sind alle notwendigen Android-Icon-Ressourcen zu erzeugen bzw. zu aktualisieren:

- Adaptive Icon Vordergrund
- Adaptive Icon Hintergrund, falls benötigt
- Launcher-Icons in den notwendigen Dichten
- runde Launcher-Icons, falls im Projekt vorhanden
- monochromes Icon nur prüfen und nur ergänzen, wenn es zur bestehenden Android-Konfiguration passt

### Integration

- Bestehende App-Icon-Ressourcen gezielt ersetzen
- Bestehende Resource-Namen und Manifest-/Icon-Verweise möglichst beibehalten
- Keine unnötige Änderung an App-Name, Paketname, Versionierung oder Release-Prozess
- Keine neue externe Bild-/Icon-Abhängigkeit einführen

### Dokumentation

- Im Issue dokumentieren, welches Ausgangsbild verwendet wurde
- Im Issue dokumentieren, welche Icon-Dateien erzeugt oder ersetzt wurden
- Falls eine Quell-/Referenzdatei im Repository abgelegt wird, Speicherort im Issue dokumentieren

## Nicht im Scope

- Kein Icon-Auswahldialog in der App
- Kein dynamischer Launcher-Icon-Wechsel zur Laufzeit
- Keine Projekt-/Workspace-Verwaltung
- Keine Änderung des Paketnamens
- Keine Änderung des Release-Signing-Prozesses
- Keine Änderung an WLAN-, Geräte-, HTTP- oder Backup-Logik

## Architekturhinweise

- Änderung auf Android-Ressourcen und ggf. Dokumentation begrenzen
- Bestehende Resource-Namen bevorzugt weiterverwenden, damit Manifest und UI-Verweise stabil bleiben
- Vorhandene App-Identity-Struktur aus Issue 035 berücksichtigen
- Wenn ein Quellbild im Repository abgelegt wird, bevorzugt unter einem nachvollziehbaren Assets-Pfad, z. B. `docs/assets/icons/`
- Für generierte Android-Ressourcen die bestehenden `res/mipmap-*`- und `res/drawable*`-Strukturen respektieren

## Akzeptanzkriterien

- [x] Codex hat vor der Implementierung das zu verwendende Ausgangsbild und den gewünschten Speicherort abgefragt oder bereits eindeutig vorhandene Angaben bestätigt.
- [x] Neues App-Icon ist fest in der App konfiguriert.
- [x] Adaptive Icon funktioniert korrekt.
- [x] Launcher-Icons wurden in allen notwendigen Dichten erzeugt bzw. ersetzt.
- [x] Runde Icon-Variante wurde berücksichtigt, falls im Projekt vorhanden.
- [x] Icon erscheint korrekt im Android Launcher.
- [x] Icon erscheint korrekt in den Systemeinstellungen/App-Infos.
- [x] Bestehende About-/Hilfe-Darstellung bleibt funktionsfähig.
- [x] Keine Änderung an Paketname, App-Name, Versionierung oder Signing.
- [x] Keine neue externe Abhängigkeit.
- [x] Verwendetes Ausgangsbild und erzeugte Dateien sind im Issue dokumentiert.
- [x] Build erfolgreich.

## Testhinweise

- App frisch installieren oder bestehende Installation aktualisieren.
- Launcher-Icon prüfen.
- Runde Icon-Darstellung im Launcher prüfen, falls der Launcher runde Icons verwendet.
- Android App-Info/Systemeinstellungen prüfen.
- About-/Hilfe-Bereich öffnen und prüfen, dass vorhandene Icon-/Logo-Darstellungen nicht unbeabsichtigt beschädigt wurden.
- Debug-Build ausführen:

  ```bash
  ./gradlew clean assembleDebug
  ./gradlew installDebug
  ```

## Abschlussnotizen

Nach der Implementierung hier ergänzen:

- verwendetes Ausgangsbild: `Kabelblitz_gelb.png`
- Ablageort der Quell-/Referenzdatei: `docs/assets/icons/Kabelblitz_gelb.png`
- erzeugte oder ersetzte Ressourcen:
  - `drawable-nodpi/ic_launcher_foreground.png` als adaptiver Vordergrund
  - `drawable-nodpi/ic_launcher_monochrome.png` als monochrome Ebene für
    unterstützte Android-Themed-Icons, erzeugt aus
    `Kabelblitz_mono_simple3.png`
  - `drawable/ic_launcher_background.xml` als adaptiver Hintergrund (`#0E1010`)
  - `mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png`
  - `mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.png`
  - Die bestehenden adaptiven XML-Definitionen für Android 8+
    (`mipmap-anydpi-v26/ic_launcher.xml` und `ic_launcher_round.xml`) bleiben
    unverändert. Die Android-13+-Varianten unter `mipmap-anydpi-v33/` ergänzen
    die monochrome Ebene; die Manifest-Verweise bleiben unverändert.
- manuelle Prüfung auf Gerät: Debug-Build, Installation, Launcher- und
  App-Info-Darstellung sowie About-/Hilfe-Bereich wurden auf dem Ubuntu-Host
  erfolgreich bestätigt. Die Android-13+-Monochrom-Variante ist eingebunden;
  ein leichtes Abschneiden äußerer Kabelenden durch Launcher-Masken ist
  akzeptiert.
