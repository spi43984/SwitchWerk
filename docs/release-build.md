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

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
./gradlew assembleRelease
```

Die signierte APK liegt anschließend unter
`app/build/outputs/apk/release/app-release.apk`.

## GitHub Release

1. Die signierte APK auf einem Testgerät installieren und prüfen.
2. Einen Versions-Tag aus dem freigegebenen Commit erstellen und pushen.
3. Auf GitHub unter **Releases** einen Release zum Tag erstellen und
   `app-release.apk` als Asset hochladen.
4. In den Release-Notes Versionsnummer, Änderungen und den Hinweis auf die
   Installation aus unbekannten Quellen nennen.

GitHub Release ist der vorgesehene erste Verteilungsweg. Ein Play-Store-Upload
ist nicht Teil dieses Prozesses.
