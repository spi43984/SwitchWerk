# AI Handoff

Stand: 24. Juni 2026

## Aktuelle Arbeit

Issue 019 „Configurable WiFi List Sorting“ ist auf dem Branch
`configurable-wifi-list-sorting` implementiert, vom Benutzer erfolgreich
geprüft und auf dessen Anweisung abgeschlossen; die Änderungen sind noch nicht
veröffentlicht.

- GitHub-Issue: #115
- WLAN-Profile werden im `SettingsViewModel` nach dem persistent gespeicherten
  Kriterium Profilname oder SSID auf- oder absteigend sortiert; beide Vergleiche sind
  groß-/kleinschreibungsunabhängig und verwenden die Profil-ID als zweiten
  Sortierschlüssel. Die Geräteverwaltung zeigt Geräte unabhängig von
  `sortOrder` alphabetisch nach sichtbarem Gerätenamen.
- Die WLAN-Liste besitzt zwischen Info- und Plus-Schaltfläche ein Sortiermenü
  mit markiertem aktivem Kriterium und aktiver Richtung. In der Geräteverwaltung gibt es kein
  Sortier-Icon.
- Im Container waren `lintDebug` (0 Fehler, 93 vorhandene Warnungen) und
  `testDebugUnitTest` erfolgreich; der Benutzer hat die Prüfungen erfolgreich
  bestätigt. GitHub-Issue #114 wurde als Duplikat von #115 geschlossen.
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 021
  „HTTP/HTTPS Device Actions“.

Issue 053 „Hamburger And About Release Metadata“ ist veröffentlicht und
abgeschlossen.

- GitHub-Issue: #112
- Pull Request: #113
- Merge-Commit: `d01d402`
- Das Hamburger-Menü zeigt unter den kompakten Navigationseinträgen die
  laufende App-Version, das Release-Datum und das bestehende About-Icon. Der
  untere Bereich bleibt auf kleinen Displays responsiv, sodass keine
  Navigationseinträge überdeckt werden. Version und Datum stehen direkt über
  dem Icon; die deutschen und englischen Texte sind gepflegt.
- Das About-Menü zeigt das Release-Datum unter der Version; die redundante
  Bezeichnung unter dem Icon wurde entfernt.
- Host-Build, Installation und manuelle Menüprüfungen wurden bestätigt.
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 019
  „Configurable WiFi List Sorting“.

Issue 054 „App Icon Replacement“ ist veröffentlicht und abgeschlossen.

- GitHub-Issue: #110
- Pull Request: #111
- Merge-Commit: `59cf775`
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 053
  „Hamburger And About Release Metadata“.
- `docs/assets/icons/Kabelblitz_gelb.png` ist die im Repository verbleibende
  Quell- und Referenzdatei. Sie ersetzt die Android-Launcher-Rasterressourcen
  in allen fünf Dichten sowie den adaptiven Vordergrund. Der adaptive
  Hintergrund verwendet jetzt `#0E1010`. Für Android-13+-Themed-Icons
  verwenden die `mipmap-anydpi-v33`-Varianten zusätzlich
  `docs/assets/icons/Kabelblitz_mono_simple3.png` als monochrome Ebene; die
  Android-8+-Varianten und Manifest-Verweise bleiben unverändert.
- Host-Build, Installation sowie Prüfung in Launcher, App-Info und
  About-/Hilfe-Bereich wurden erfolgreich bestätigt.

Issue 042 „Action Cancellation And Fast-Fail WiFi“ ist veröffentlicht und
abgeschlossen.

- GitHub-Issue: #108
- Pull Request: #109
- Merge-Commit: `48e6f73`
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 054
  „App Icon Replacement“.
- Laufende Geräteaktionen sind pro Gerät abbrechbar. Die aktive Kachel wird
  bei Start sowie beim Öffnen der Aktionsdetails ins Sichtfeld gescrollt und
  zeigt Spinner links unten sowie ein X rechts unten. Der Abbruch zeigt eine
  lokale Rückmeldung und einen Diagnoseeintrag.
- Coroutine-Cancellation gibt aktive WLAN-Callbacks und HTTP/RPC-Aufrufe frei.
  DNS-Cancellation bleibt für die UI sofort wirksam, auch wenn ein Android-
  Resolver seinen Thread-Interrupt bis zum eigenen Timeout ignoriert.
- Der Benutzer hat `lintDebug`, `testDebugUnitTest`, `clean assembleDebug`,
  `installDebug` sowie die manuellen Abbruchszenarien erfolgreich bestätigt.

Issue 040 „Import/Export Password Handling“ ist veröffentlicht und abgeschlossen.

- GitHub-Issue: #106
- Pull Request: #107
- Merge-Commit: `a98d2fe`
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 042
  „Action Cancellation And Fast-Fail WiFi“.
- Der Backup-Export enthält Passwörter ausschließlich nach expliziter Aktivierung
  des standardmäßig deaktivierten Schalters. Ohne diese Aktivierung werden keine
  Passwortfelder serialisiert.
- Ein einzelner Importdialog führt durch Quelle, Importoptionen und eine
  dynamische Zusammenfassung. Passwortfelder werden standardmäßig ignoriert;
  die aktivierbare Übernahme zeigt ihre Auswirkungen einschließlich leerer
  Löschanweisungen vor dem Import. Die Zusammenfassung wird beim Wechsel des
  Importmodus ohne erneutes Einlesen aktualisiert.
- Der Benutzer hat Build, Unit-Tests und die manuellen Import-/Export- und
  Dialogszenarien erfolgreich bestätigt.

Issue 034 „Collapsible Action Details Panel“ ist veröffentlicht und abgeschlossen.

- GitHub-Issue: #104
- Pull Request: #105
- Merge-Commit: `30ee5e8`
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 040
  „Import/Export Password Handling“.
- Aktionsdetails verwenden lokalen Compose-UI-State: Bei keiner Aktion und
  keinen Meldungen bleibt nur eine kompakte, erklärende Leiste sichtbar.
  Aktionen und neue Meldungen öffnen den Bereich, der Titel minimiert ihn und
  `ON_PAUSE` setzt ihn wieder zurück. Das Leeren über den Mülleimer entfernt
  ausschließlich Meldungen.
- Die Host-Prüfungen für Build, Installation und manuelle Szenarien wurden
  erfolgreich bestätigt.

Issue 033 „Android-managed WiFi networks“ ist veröffentlicht und abgeschlossen.

- GitHub-Issue: #101
- Pull Request: #102
- Merge-Commit: `f287ed8`
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 034
  „Collapsible Action Details Panel“.
- `WifiProfile` besitzt den Verbindungsmodus `SWITCHWERK_MANAGED` oder
  `ANDROID_MANAGED`; die Room-Migration 5→6 ergänzt das Feld mit dem sicheren
  Standard `SWITCHWERK_MANAGED`.
- Android-verwaltete Profile speichern keine Zugangsdaten: Beim Speichern wird
  ein vorhandenes Passwort aus dem verschlüsselten Store entfernt. Die
  Einstellungen blenden das Passwortfeld aus, erklären den Android-verwalteten
  Ablauf, öffnen die Android-WLAN-Einstellungen und übernehmen SSIDs aus
  sichtbaren Scan-Ergebnissen.
- Eine Geräteaktion sucht bei Android-verwalteten Profilen unter allen von
  Android bekannten aktiven WLAN-Netzwerken das WLAN für den HTTP/RPC-Aufruf.
  Die aktuelle SSID wird dabei über `WifiManager.connectionInfo` ermittelt,
  nicht über die pro Netzwerk gelieferten `NetworkCapabilities`. Damit bleibt
  der Aufruf korrekt, wenn Mobilfunk oder ein VPN das Android-Standardnetzwerk
  ist. Andernfalls wird ohne
  `WifiNetworkSpecifier`-Anfrage eine verständliche Fehlermeldung
  zurückgegeben.
- Ist ein Android-verwaltetes Ziel-WLAN nicht aktiv, öffnet die UI nach der
  Aktion nach einer Bestätigung die Android-WLAN-Einstellungen. Der Dialog
  nennt die Ziel-SSID ausschließlich lokal im UI. Die Aktionsdetails enthalten vor
  jedem Verbindungsversuch die Profilzuordnung und ihre Position in der
  konfigurierten Reihenfolge, ohne SSIDs zu protokollieren.
- Der Benutzer hat Build, Installation und manuelle Prüfungen der verbundenen,
  nicht verbundenen und Android-verwalteten WLAN-Flows erfolgreich bestätigt.

Issue 030 „WiFi Profile Deletion Safety“ ist veröffentlicht und abgeschlossen.

- GitHub-Issue: #98
- Pull Request: #99
- Merge-Commit: `74dd238`
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 033
  „Android-managed WiFi networks“.
- Der Löschdialog ermittelt im `SettingsViewModel` die betroffenen Geräte und
  zeigt Anzahl sowie Namen an. Singular und Plural werden über Android-
  Pluralressourcen getrennt ausgegeben; die Abbruchaktion bleibt rechts.
- `RoomWifiProfileRepository` löscht beim Entfernen eines WLAN-Profils dessen
  Gerätezuordnungen. Damit verbleiben keine verwaisten WLAN-Referenzen und
  Geräte zeigen nicht mehr aufgrund dieser Referenzen „Unbekanntes WLAN“.
- Datenbankmigrationen blieben unverändert. `lintDebug`, `testDebugUnitTest`,
  Host-Build, Installation und manuelle Löschszenarien wurden erfolgreich
  bestätigt.

Issue 046 „UI State And Orientation Polish“ ist veröffentlicht und
abgeschlossen.

- GitHub-Issue: #95
- Pull Request: #96
- Merge-Commit: `a8d2078`
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 030
  „WiFi Profile Deletion Safety“.
- Navigation, der aktive Einstellungsbereich sowie relevante lokale
  Import-/Export-/Dialogzustände werden über den Activity-Speicher bei einer
  Recreation wiederhergestellt. Bearbeitungsdialoge bleiben über ihr
  ViewModel erhalten.
- Die JourneyApps-Scanner-Activity überschreibt ihre Landscape-Vorgabe mit
  `screenOrientation="unspecified"`; der Scanner sperrt danach die beim Start
  bestehende Geräteorientierung.
- Der Benutzer hat die Host- und Gerätetests als erfolgreich bestätigt.

Issue 045 „WiFi Proximity Indicator Stabilization“ ist veröffentlicht und
abgeschlossen.

- GitHub-Issue: #93
- Pull Request: #94
- Merge-Commit: `e012223`
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 030
  „WiFi Profile Deletion Safety“.

- Der Statuspunkt verwendet künftig Grün für positive Bestätigung, Grau für
  „WLAN noch nicht bestätigt“ und Rot für eine konkrete fehlgeschlagene
  WLAN-Verbindungsanfrage. Ein einzelner fehlender Scan setzt nicht Rot.
- Die Geräteprüfung am Pixel zeigte, dass eine SSID gleichzeitig sichtbar sein
  und beide `WifiNetworkSpecifier`-Sicherheitsversuche ablehnen kann. Deshalb
  hat der konkrete Verbindungsfehler Vorrang vor einem sichtbaren Scan-Treffer;
  nur eine erfolgreiche Verbindung hebt Rot wieder auf Grün.
- Android-Systemscan und `WifiManager.getScanResults()` lieferten auf dem
  Test-Pixel nachweislich unterschiedliche Ergebnisse; die Scan-Drosselung
  wird nicht als Nutzeranforderung umgangen. Der frühere periodische aktive
  45-Sekunden-Scan wurde entfernt; der einmalige Refresh beim Aktivieren des
  Dashboards und die passive Beobachtung bleiben bestehen.
- Verbindungsbestätigungen aus `WifiNetworkSpecifier` werden über einen
  in-memory Store an die Näheanzeige weitergegeben. `SecurityTypesFailed` wird
  als Verbindungsfehler behandelt.
- Die direkte Pixel-Prüfung zeigte sowohl temporäre
  `WifiNetworkSpecifier`-Timeouts als auch erfolgreiche Verbindungen mit
  derselben sichtbaren SSID. Der Android-Dialog erschien zeitnah; die Ursache
  der Aussetzer ist damit noch nicht abschließend bestätigt. Ein einzelner
  erkannter Sicherheitstyp erhält das volle 30-Sekunden-Budget; nur zwei von
  Android erkannte Sicherheitstypen teilen sich das Budget. Ein WPA3-Fallback
  folgt nur, wenn Android WPA3 für die SSID erkennt; `Unavailable` und
  Timeouts starten keinen zweiten Sicherheitsversuch. Die Android-Plattform
  übernimmt den Request-Timeout über `requestNetwork`.
- Jeder Verbindungsversuch erhält eine nur im Speicher lebende, monotone
  Request-ID. Logcat protokolliert ausschließlich ID und Ablaufereignisse
  `requested`, `available`, `ip_ready`, `unavailable`, `lost`,
  `timeout_before_available`, `timeout_after_available`, `cancelled` und
  `released`; SSID, Zugangsdaten, BSSID, IP-Adresse, Geräte- und URL-Daten
  werden dabei nicht geloggt.
- Unit-Tests und Android-Test-Kompilierung im Container waren erfolgreich.
  Der Benutzer hat `lintDebug`, `clean assembleDebug` und `installDebug` auf
  dem Host erfolgreich bestätigt. Die Verbindungsprüfung am Pixel zeigte
  erfolgreiche Aufbauten bis knapp 28 Sekunden, Android-`Unavailable`- und
  Timeout-Fälle vor der IP-Zuweisung sowie getrennte HTTP-Gerätefehler.
  Insbesondere darf keine Toraktion ohne sichere Testsituation ausgelöst
  werden.
- Der Feature-Branch wurde nach erfolgreicher Host- und Pixel-Prüfung gemergt.

Issue 044 "GitHub Actions Resource Optimization" ist implementiert, aber noch
nicht veröffentlicht oder abgeschlossen. Für neue Sessions ist
`AI_SESSION_PROMPT.md` als wiederverwendbare Startvorlage zu verwenden.

- `.github/workflows/android-build.yml` führt Android-Qualitätsprüfungen nur
  noch bei Pull Requests nach `main` aus. Änderungen ausschließlich unter
  `docs/` oder ausschließlich an Markdown-Dateien sind über `paths-ignore`
  ausgeschlossen; Pushes auf `main` und Feature-Branches lösen keinen
  Android-Workflow mehr aus.
- Die PR-Prüfung verwendet ohne `clean` nur `lintDebug` und
  `testDebugUnitTest`; laufende veraltete PR-Läufe werden per Concurrency
  abgebrochen. `gradle/actions/setup-gradle` bleibt für Gradle- und
  Dependency-Caching erhalten. Ein separater Cache für Build-Ausgaben wird
  nicht verwendet.
- `.github/workflows/apk-build.yml` erzeugt und lädt das Debug-APK weiterhin
  hoch, aber nur für Tags mit Präfix `v` sowie manuell von `main`. Damit
  entfällt die doppelte APK-Erzeugung bei jedem Push auf `main`.
- `GITHUB_WORKFLOW.md`, `TESTING.md` und `AI_SESSION_PROMPT.md` dokumentieren
  die Trigger sowie die lokalen Pflichtprüfungen `lintDebug`,
  `testDebugUnitTest`, `clean assembleDebug` und `installDebug`.
- Im Container waren `git diff --check` erfolgreich. `actionlint`, Ruby und
  PyYAML sind nicht installiert; eine vollständige Workflow-Lint-Prüfung steht
  daher auf dem Host oder in GitHub Actions aus. Für die reine Workflow- und
  Dokumentationsänderung wurden keine Gradle-Aufgaben im Container ausgeführt.


## Zuletzt abgeschlossene Arbeit

Issue 035 "App Identity, Help And Release Packaging" ist veröffentlicht und
abgeschlossen. GitHub-Issue: #90, Pull Request: #92, Release:
[`v0.1.0`](https://github.com/spi43984/SwitchWerk/releases/tag/v0.1.0).

- SwitchWerk verwendet Launcher- und Adaptive-Icons aus
  `docs/assets/icons/App_Icon_transparent_weiss.png`; der Vordergrund ist weiß
  und kontraststark. Hilfe, About und kontextbezogene Hinweise sind integriert.
- `.gitignore` schließt `keystore.properties`, `.jks`- und `.keystore`-Dateien
  aus. Der Release-Keystore und seine Zugangsdaten bleiben lokal.
- Die Host-Prüfungen `lintDebug`, `testDebugUnitTest`, `clean assembleDebug`,
  `installDebug`, `clean assembleRelease`, `apksigner verify` sowie die
  Installation der signierten APK waren erfolgreich.
- Die signierte APK ist im GitHub Release `v0.1.0` veröffentlicht. GitHub
  Releases bleiben der vorgesehene erste Verteilungsweg.

Issue 043 "Verify Issue 036 Lint And Codex Session" ist veröffentlicht und abgeschlossen. GitHub-Issue: #84.

Die Verifikation am 22. Juni 2026 ergibt:

- Issue 036 ist in `main` enthalten. Relevante Commits sind `6af855f`
  (Implementierung), `41ebbe1` (Wiederherstellung der im ursprünglichen Merge
  fehlenden Service- und Testdateien) und `2e4d40a` (Lint-Korrektur). Der
  Prüf-Branch enthält gegenüber `main` keinen Code-Diff.
- Der historische Actions-Run `27939718473` endete für `6af855f` mit
  `failure`. `gh run view --log-failed` liefert über das aktuelle Token keine
  Fehlerausgabe; der Abruf der Annotations des zugehörigen Check-Runs ist nicht
  erlaubt (HTTP 403). Die konkrete frühere MissingPermission-Stelle ist im
  Commit `2e4d40a` nachvollziehbar und wurde lokal vollständig erneut gelintet.
- `AndroidWifiProximityService` prüft vor jedem Zugriff auf `startScan()` und
  `scanResults` die passende Laufzeitberechtigung; die Zugriffe behandeln
  `SecurityException`. Fehlende Berechtigung oder Scanfehler ergeben keinen
  grünen Status, sondern einen nicht verfügbaren Status.
- Es gibt keine `WifiNetworkSpecifier`-, `requestNetwork`- oder
  `bindProcessToNetwork`-Nutzung. Die Aktualisierung ist auf das sichtbare
  Dashboard begrenzt: Receiver, Netzwerk-Callback und 30-Sekunden-Scan-Takt
  werden bei Pause abgemeldet. Es gibt keine Hintergrundüberwachung und kein
  Logging von SSIDs, Passwörtern oder Scan-Ergebnissen.
- `MainViewModel` veröffentlicht den Status über `StateFlow`; Compose rendert
  nur den Statuspunkt. Der Status behandelt verbundenes WLAN, frische
  Scan-Ergebnisse, fehlende Berechtigung, deaktiviertes WLAN,
  deaktivierte System-Standortdienste und Scanfehler.
- `docs/issues/036-device-wifi-proximity-indicator.md` und
  `docs/issues/overview.txt` führen Issue 036 übereinstimmend als
  abgeschlossen. Der vorherige Hinweis auf einen aktiven Feature-Branch war
  veraltet und ist hiermit korrigiert. `ai-context.md` benötigt keine Änderung.

Im Container bestätigte Prüfungen für Issue 043:

```text
./gradlew lintDebug
./gradlew clean assembleDebug
git status
git log --oneline --decorate --graph --max-count=20
git diff main...HEAD --stat
git diff main...HEAD
```

Die beiden Gradle-Prüfungen waren erfolgreich. Vor dem Merge sind auf dem
Ubuntu-Host noch Build und Installation zu bestätigen; danach können
Dokumentation, Pull Request und GitHub-Issue abgeschlossen werden.

## Verifizierter Implementierungsstand Issue 036

Issue 036 "Device WiFi Proximity Indicator" ist in `main` implementiert und
fachlich geprüft. Der Benutzer hat Build, Installation und manuelle
Gerätetests bestätigt; die letzten Änderungen waren die Nachbesserung der
rot->grün-Aktualisierung über einen foreground-only Scan-Takt.

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
