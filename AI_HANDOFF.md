# AI Handoff

Stand: 21. Juni 2026

## Aktuelle Arbeit

Issue 038 "Dialog Keyboard Handling" ist auf dem Branch
`dialog-keyboard-handling` implementiert. Host-Build und Tests wurden vom
Benutzer als erfolgreich bestätigt. Veröffentlichung und Abschluss stehen noch
aus.

- `StandardConfigurationDialog` berücksichtigt die IME zentral, begrenzt seine
  Höhe auf 85 % des verfügbaren Bereichs und hält die Aktionsleiste außerhalb
  des scrollbaren Formularbereichs.
- WLAN-Profil-, Geräte-, Geräte-WLAN-Zuordnungs- und URL-Import-Felder verwenden
  sinnvolle Weiter-/Fertig-Aktionen.
- Fachlogik, Datenmodelle und Dependencies sind unverändert.

Bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
```

## Zuletzt abgeschlossene Arbeit

Issue 029 "Language Setting" ist implementiert, auf dem Ubuntu-Host geprüft und
nach `main` gemergt.

- GitHub-Issue: #75
- Pull Request: #76
- Merge-Commit: `25484b8f9d12031785700cca9fa749eb12acbf86`
- persistierte Auswahl System / Deutsch / Englisch
- vollständige deutsche und englische Android-String-Ressourcen
- keine Übersetzung von Benutzerdaten oder technischen Eingaben
- keine neuen Cloud-, Tracking-, Analytics- oder Übersetzungsabhängigkeiten

Bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
manuelle Prüfung der Sprachvarianten und Neustartpersistenz

## Zuvor abgeschlossene Arbeit

Issue 025 "Dashboard Widget Layout" ist implementiert und auf dem Ubuntu-Host
geprüft.

- GitHub-Issue: #70
- persistierte Listen- und Widget-Darstellung mit direktem Dashboard-Umschalter
- adaptives Compose-Grid mit gemeinsamer `sortOrder`
- kompakte Sortierpfeile in Listen- und Widget-Ansicht
- stabile Kartenhöhen und verständliche, zeitlich begrenzte Aktionsmeldungen
- kompakte Landscape-Kopfzeile und nur visuell ausgeblendete Aktionsdetails
- Dashboard-Darstellung in Export und rückwärtskompatiblem Import
- keine neuen Netzwerk-, Cloud-, Tracking- oder Analytics-Abhängigkeiten

Bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
manuelle Prüfung von Portrait-/Landscape-Layout, Sortierung und Statusdarstellung
```

## Zuvor abgeschlossene Arbeit

Issue 039 "Unified List Interactions" ist implementiert und manuell geprüft.

- GitHub-Issue: #68
- gemeinsame Swipe-to-delete-Komponente unter `ui/components`
- WLAN-Profile, Geräte und Geräte-WLAN-Zuordnungen nutzen dieselbe
  Listeninteraktion
- Tap auf den Listeneintrag öffnet die Bearbeitung
- Swipe zeigt ausschließlich die Löschaktion; WLAN-Profile, Geräte und
  Geräte-WLAN-Zuordnungen verlangen vor dem Löschen eine Bestätigung
- jeder Tap im Geräteformular beendet einen offenen Swipe der
  Geräte-WLAN-Zuordnungen; horizontale Swipe-Gesten bleiben davon ausgenommen
- höhere horizontale Auslösedistanz reduziert versehentliche Swipes beim
  vertikalen Scrollen
- geschlossene Listeneinträge übernehmen transparent den Hintergrund ihres
  Containers; der Lösch-Callback läuft unabhängig von der Schließanimation
- Bleistift- und direkter Löschbutton der Geräte-WLAN-Zuordnungen entfernt
- Hoch-/Runter-Pfeile sowie bestehende Sortier-, Persistenz- und
  ViewModel-Callbacks unverändert

Bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
manuelle Geräteprüfung der Listeninteraktionen, Löschbestätigungen,
Swipe-Empfindlichkeit und Darstellung
```

## Weitere abgeschlossene Arbeit

Issue 020 "Device Assigned WiFi Order" ist implementiert, auf dem Ubuntu-Host
geprüft und abgeschlossen.

- GitHub-Issue: #63
- robuste Hoch/Runter-Sortierung in der Gerätebearbeitung
- 300 dp hohe Zuordnungsliste mit Scroll-Hinweisen oben und unten
- persistierte Reihenfolge über die bestehende `priority`-Spalte
- gespeicherte Reihenfolge als verbindliche Reihenfolge des Schaltvorgangs
- keine Room-Migration, neuen Berechtigungen oder Änderungen an Zugangsdaten
- Drag-and-Drop nach Gerätetest bewusst zugunsten der Pfeilbedienung verworfen

Bestätigte Prüfungen:

```text
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
manuelle Sortier-, Neustart-, Schalt-, Import- und Exporttests
```

## Weitere zuvor abgeschlossene Arbeit

Issue 026 "Settings UI Rework" ist implementiert, geprüft, veröffentlicht und
nach `main` gemergt.

- GitHub-Issue: #59
- Pull Request: #62
- Merge-Commit: `bf29768`
- Hamburger-Menü mit Einstellungen und Hilfe
- Settings-Tabs für WLAN-Profile, Geräte, System und Backup
- wiederverwendbare Hilfe-, Dialog-, Button- und Tab-Komponenten
- vereinheitlichte WLAN-, Geräte- und Import-/Export-Dialoge
- Dark-Mode-Hintergrund und rahmenlose Settings-Bereiche
- keine Änderung an fachlicher WLAN-, Geräteaktions-, Import-/Export- oder Passwortlogik

Bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```

## Frühere abgeschlossene Arbeit

Issue 027 "WiFi Timeout Analysis And Stabilization" ist implementiert,
geprüft, veröffentlicht und nach `main` gemergt.

- GitHub-Issue: #57
- Pull Request: #58
- Merge-Commit: `7779e1d`

## Umgesetzter Scope Issue 027

- vollständige, zeitgestempelte WLAN-/DHCP-/DNS-/HTTP-Diagnose
- sichtbare Zeitdifferenzen zwischen Diagnoseschritten
- WLAN-Erfolg erst nach WiFi-Transport und gültiger Link-IP
- robustes Handling verspäteter, mehrfacher und verlorener NetworkCallbacks
- netzgebundene DNS-Auflösung und verständliche Timeout-Klassifizierung
- direkte Meldung bei deaktiviertem WLAN
- WPA2/WPA3-Fallback innerhalb eines gemeinsamen Zeitbudgets
- lokal ungeprüfter Status für importierte WLAN-Sicherheitstypen
- einmalige WPA2/WPA3-Erkennung nach Import mit Vordergrund-Scan
- Room-Datenbankversion 5 mit Migration 4 nach 5
- keine zusätzlichen Cloud-, Tracking- oder Logging-Abhängigkeiten

## Bestätigte Prüfungen

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```

Host-Build, Installation, Room-Migration sowie manuelle WLAN-, Import-,
Force-Stop- und Geräteaktionstests wurden vom Benutzer als erfolgreich
bestätigt.

## Wichtige Hinweise für die nächste Session

- Wiederverwendbare Startvorlage: `AI_SESSION_PROMPT.md`
- Die vollständige Issue-Liste einschließlich Status, Priorisierung und
  Umsetzungsreihenfolge steht ausschließlich in `docs/issues/overview.txt`.
