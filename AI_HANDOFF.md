# AI Handoff

Stand: 22. Juni 2026

## Aktuelle Arbeit

Issue 036 "Device WiFi Proximity Indicator" ist auf dem Branch
`device-wifi-proximity-indicator` implementiert und lokal als abgeschlossen
dokumentiert. Der Benutzer hat Build, Installation und manuelle Gerätetests
bestätigt; die letzten Änderungen waren die Nachbesserung der rot->grün
Aktualisierung über einen foreground-only Scan-Takt.

- Ein gekapselter `WifiProximityService` startet beim Öffnen bzw. erneuten
  Aktivieren des Dashboards einmalig einen Scan. Solange das Dashboard sichtbar
  ist, verarbeitet er zusätzlich Scan-Abschluss-, WLAN-Status- und aktive
  Netzwerkereignisse von Android. Zusätzlich läuft im Vordergrund ein
  begrenzter Scan-Takt, damit neu sichtbare WLANs auch dann nachgezogen werden,
  wenn Android kein separates Sichtbarkeitsereignis liefert. Beim Verlassen
  oder Pausieren werden Receiver, NetworkCallback und Scan-Takt abgemeldet. Es
  gibt keinen Hintergrundscan und kein Logging von WLAN-Daten.
- Scan-Ergebnisse werden ab Android 11 über den direkten
  `WifiManager.ScanResultsCallback` verarbeitet. Android 8 bis 10 verwenden
  einen kontextregistrierten, exportierten BroadcastReceiver. Der vorherige
  `RECEIVER_NOT_EXPORTED`-Receiver konnte Broadcasts privilegierter
  WLAN-Systemkomponenten übersehen.
- `MainViewModel` vergleicht den Snapshot mit WLAN-Profilen und
  Gerätezuordnungen und veröffentlicht ausschließlich gerätebezogene Statuswerte
  über `StateFlow`.
- Der Rückweg von rot zu grün hängt nicht nur an Events, sondern auch an dem
  foreground-only Scan-Takt. Damit wird die begrenzte Event-Abdeckung von
  Android für neu sichtbare WLANs pragmatisch abgefedert.
- Listen- und Widgetansicht verwenden denselben rechtsbündigen Statuspunkt;
  lange Namen werden gekürzt. Während einer Geräteaktion pulsiert der Punkt in
  seiner aktuellen Farbe.
- Deaktiviertes WLAN, fehlende Berechtigung und Scanfehler führen zu roten
  Statuswerten. Es gibt bewusst keine globale Status- oder Fehlermeldung;
  Accessibility-Texte unterscheiden Nähe, fehlende Zuordnung, Fehler und
  laufende Aktion.
- Global deaktivierte System-Standortdienste werden getrennt von einer
  verweigerten App-Berechtigung erkannt und grau dargestellt. Ein sicher
  erkanntes aktuell verbundenes, zugeordnetes WLAN bleibt dabei grün. Änderungen
  der System-Standortdienste werden über `LocationManager.MODE_CHANGED_ACTION`
  übernommen.
- Keine automatische Geräteaktion, kein WLAN-Wechsel, keine neue Berechtigung
  und keine neue Dependency wurden ergänzt.
- Lokale Issue-Datei und Übersicht sind auf `abgeschlossen` gesetzt.

Geänderte Dateien:

```text
app/src/main/java/de/piecha/switchwerk/data/network/WifiProximityService.kt
app/src/main/java/de/piecha/switchwerk/data/network/AndroidWifiProximityService.kt
app/src/main/java/de/piecha/switchwerk/di/AppModule.kt
app/src/main/java/de/piecha/switchwerk/viewmodel/MainViewModel.kt
app/src/main/java/de/piecha/switchwerk/ui/screens/StartScreen.kt
app/src/main/res/values*/strings.xml
app/src/test/java/de/piecha/switchwerk/viewmodel/MainViewModelTest.kt
app/src/androidTest/java/de/piecha/switchwerk/ui/screens/DeviceWifiProximityIndicatorTest.kt
AI_HANDOFF.md
```

Im Container bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew compileDebugAndroidTestKotlin
```

Offene Prüfungen auf dem Ubuntu-Host:

```text
./gradlew clean assembleDebug
./gradlew installDebug
```

## Weitere unveröffentlichte Arbeit

Issue 038 "Dialog Keyboard Handling" war auf dem Branch
`dialog-keyboard-handling` implementiert. Host-Build und Tests wurden vom
Benutzer als erfolgreich bestätigt; Veröffentlichung und Abschluss waren zum
letzten dokumentierten Stand noch offen.

- `StandardConfigurationDialog` berücksichtigt die IME zentral, begrenzt seine
  Höhe und hält die Aktionsleiste außerhalb des scrollbaren Formularbereichs.
- WLAN-Profil-, Geräte-, Geräte-WLAN-Zuordnungs- und URL-Import-Felder verwenden
  passende Weiter-/Fertig-Aktionen.

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
```

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
