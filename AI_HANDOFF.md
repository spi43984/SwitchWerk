# AI Handoff

Stand: 14. Juni 2026

## Aktuelle Arbeit

GitHub-Issue #25 "Import / Export" ist auf dem vorhandenen Feature-Branch
`import-export` implementiert.

Es wurde noch nichts committet, gepusht oder veröffentlicht.

## Implementierter Scope

- versioniertes JSON-Austauschformat mit `schemaVersion = 1`
- Export von WLAN-Profilen, Geräten, Geräteaktionen, Button-Beschriftungen,
  WLAN-Zuordnungen und deren Reihenfolge
- Standardexport ohne Passwortfelder
- optionaler Klartext-Passwortexport nach Warnung
- Import aus Android-Dateiauswahl
- Import ausschließlich aus HTTPS-URLs
- öffentliche Nextcloud-Dateifreigaben werden bei einer HTML-Vorschauseite
  automatisch über den HTTPS-Endpunkt `<Freigabelink>/download` geladen
- öffentliche Google-Drive-Dateilinks im Format `/file/d/<id>/view` werden in
  den direkten HTTPS-Download umgewandelt
- nicht öffentlich freigegebene Google-Drive-Dateien zeigen einen Hinweis auf
  die Freigabeoption "Jeder mit dem Link"
- Importmodus "Ergänzen / überschreiben"
- Importmodus "Alles ersetzen"
- Zusammenfassung vor jedem Import
- zusätzliche Sicherheitswarnung bei enthaltenen Passwortänderungen
- fehlendes Passwortfeld lässt ein gespeichertes Passwort unverändert
- leeres Passwortfeld löscht das gespeicherte Passwort
- importierte Passwörter werden über den bestehenden `WifiCredentialStore`
  gespeichert
- Room-Schreibvorgänge erfolgen innerhalb einer Datenbanktransaktion
- bestehende Geräte-Sortierwerte bleiben beim Zusammenführen erhalten
- Importgröße ist auf 1 MiB begrenzt
- ungültige Versionen, IDs, Referenzen, Methoden und Sicherheitstypen werden
  abgelehnt

## Neue Hauptklassen

```text
data/transfer/ConfigurationDocument.kt
data/transfer/ConfigurationJsonCodec.kt
data/transfer/ConfigurationImportValidator.kt
data/repository/ConfigurationTransferRepository.kt
data/repository/DefaultConfigurationTransferRepository.kt
```

## Prüfungen im Container

Erfolgreich:

```text
./gradlew compileDebugKotlin
./gradlew testDebugUnitTest
git diff --check
```

Nach der Korrektur des Nextcloud-URL-Imports erneut erfolgreich:

```text
./gradlew testDebugUnitTest
git diff --check
```

Der Container-Build ersetzt nicht die maßgebliche Prüfung auf dem Ubuntu-Host.

## Prüfung auf dem Ubuntu-Host

Vom Benutzer am 14. Juni 2026 erfolgreich bestätigt:

```text
./gradlew clean assembleDebug
./gradlew installDebug
adb shell am force-stop de.piecha.switchwerk
adb shell monkey -p de.piecha.switchwerk 1
```

Das APK wurde erfolgreich auf einem Pixel 10 Pro XL mit Android 16 installiert
und die App anschließend gestartet.

Nicht blockierende Warnungen:

- `local.properties` verweist zusätzlich auf ein nicht vorhandenes SDK-Verzeichnis
- Room-Schema-Exportpfad ist nicht konfiguriert
- der bestehende `EncryptedWifiCredentialStore` verwendet inzwischen als
  deprecated markierte AndroidX-Security-APIs
- `libandroidx.graphics.path.so` wurde ungestrippt paketiert

## Manuelle Prüfung

Der Benutzer hat die Implementierung am 14. Juni 2026 erfolgreich geprüft.

Bestätigt:

- Export und Dateiimport funktionieren
- öffentlicher Nextcloud-Freigabelink funktioniert
- öffentlich freigegebener Google-Drive-Link funktioniert
- Host-Build, Installation und App-Start funktionieren

Issue #25 kann aus fachlicher Sicht veröffentlicht und abgeschlossen werden.

## Veröffentlichung

Erst nach erfolgreicher Rückmeldung des Benutzers und ausdrücklicher
Veröffentlichungsfreigabe:

1. Änderungen committen
2. Branch pushen
3. Pull Request erstellen
4. nach Freigabe mergen
5. lokale Issue-Datei abhaken
6. GitHub-Issue #25 schließen
7. Feature-Branch lokal und remote löschen
