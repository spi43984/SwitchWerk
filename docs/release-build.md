# Release-Builds

SwitchWerk wird zunächst über GitHub Releases mit einer signierten APK verteilt.
Das ermöglicht eine einfache, transparente lokale Verteilung ohne Play-Store-Konto.
Nutzende müssen die Installation aus unbekannten Quellen erlauben. Ein Play-Store-
Release bleibt eine spätere Option, wenn der Aufwand für Store-Eintrag,
Datenschutzangaben, Review und Veröffentlichungsprozess sinnvoll ist.

## Lokales Signing

Erzeuge einen stabilen Keystore ausschließlich lokal und lege im Repository-Root
eine nicht versionierte Datei `keystore.properties` an. Sowohl diese Datei als auch
Keystore-Dateien sind durch `.gitignore` ausgeschlossen.

Den Keystore einmalig lokal erzeugen. Der Befehl fragt die Passwörter interaktiv ab:

```bash
mkdir -p "$HOME/.switchwerk"
keytool -genkeypair -v \
  -keystore "$HOME/.switchwerk/switchwerk-release.jks" \
  -alias switchwerk \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

```properties
storeFile=/absoluter/Pfad/zu/switchwerk-release.jks
storePassword=DEIN_STORE_PASSWORT
keyAlias=switchwerk
keyPassword=DEIN_KEY_PASSWORT
```

Bei vollständig gesetzten Werten signiert `assembleRelease` die Release-APK mit
diesem Schlüssel. Ohne diese Datei bleibt ein Release-Build möglich, erzeugt aber
keine für Endnutzende signierte APK. Keystore und Passwörter dürfen weder committed
noch in Tickets, Logs oder Chat-Nachrichten geteilt werden.

## Befehle

Debug-Build und Debug-Installation:

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```

Release-Build und Release-Installation:

```bash
./gradlew lintRelease
./gradlew testDebugUnitTest
./gradlew clean assembleRelease
./gradlew installRelease
```

Das Projekt stellt keinen Task `testReleaseUnitTest` bereit. Die Unit-Tests
werden deshalb für beide Varianten mit `testDebugUnitTest` ausgeführt.
`installRelease` benötigt die oben beschriebene Release-Signierung. Stimmt die
Signatur nicht mit der bereits installierten App überein, muss diese vor dem
Variantenwechsel deinstalliert werden; ihre lokalen App-Daten gehen dabei verloren.

Die signierte APK liegt anschließend unter
`app/build/outputs/apk/release/app-release.apk`.

## GitHub Release

Das Release-APK muss als Asset mit dem Namen `SwitchWerk-<version>.apk`
veröffentlicht werden, zum Beispiel `SwitchWerk-0.7.0.apk`. Die Update-Funktion
der App erkennt ausschließlich diesen Namen.

Vor dem Ausführen muss der freigegebene Stand auf `main` gemergt und lokal
prüfbar sein. Das Skript erstellt keinen Pull Request.

Interaktiv:

```bash
./scripts/release-github.sh
```

Oder mit Versionsargument:

```bash
./scripts/release-github.sh 0.7.0
```

Das Skript prüft `keystore.properties`, die darin referenzierte Keystore-Datei,
das offizielle Repository `spi43984/SwitchWerk`, einen sauberen Worktree,
vorhandene Tags und GitHub-Releases, aktualisiert `versionCode` und
`versionName`, führt `lintRelease`, `testDebugUnitTest` und
`clean assembleRelease` aus, verifiziert die APK mit `apksigner`, kopiert sie
auf den Release-Asset-Namen, öffnet vorgeschlagene Release Notes zur Anpassung
und lädt sie als `SwitchWerk-<version>.apk` hoch.

GitHub Release ist der vorgesehene erste Verteilungsweg. Ein Play-Store-Upload
ist nicht Teil dieses Prozesses.
