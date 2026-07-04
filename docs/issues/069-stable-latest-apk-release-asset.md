# Issue 069: Stable Latest APK Release Asset

## Metadaten

* Status: Abgeschlossen
* Priorität: P0
* Typ: Release / Packaging
* Bereich: Release / Updates

## Ziel

Das Release-Script `scripts/release-github.sh` soll bei jeder neuen GitHub Release zusätzlich zur versionierten APK auch eine APK mit stabilem Dateinamen bereitstellen.

Neben dem bestehenden Asset

`SwitchWerk-v1.2.3.apk`

soll zusätzlich immer folgendes Asset hochgeladen werden:

`SwitchWerk.apk`

Dadurch kann dauerhaft auf die neueste APK verlinkt werden, ohne die Versionsnummer zu kennen:

`https://github.com/spi43984/SwitchWerk/releases/latest/download/SwitchWerk.apk`

## Hintergrund

GitHub unterstützt stabile Download-Links auf Assets der neuesten Release über:

`/releases/latest/download/<DATEINAME>`

Das funktioniert nur zuverlässig, wenn der Dateiname des Assets in jeder Release identisch ist.

Aktuell erzeugt `scripts/release-github.sh` ein versioniertes APK-Asset. Dadurch ist der direkte Download-Link versionsabhängig und kann nicht dauerhaft z. B. in README, Website, QR-Code oder Update-Hinweisen verwendet werden.

## Scope

### Release-Script

* `scripts/release-github.sh` soll weiterhin die versionierte APK erzeugen und hochladen.
* Zusätzlich soll dieselbe APK unter dem stabilen Namen `SwitchWerk.apk` bereitgestellt werden.
* Beide Assets sollen aus derselben gebauten und geprüften Release-APK entstehen.
* Der bestehende Release-Ablauf soll ansonsten unverändert bleiben.

### Asset-Namen

* Versioniertes Asset: `SwitchWerk-v<MAJOR.MINOR.PATCH>.apk`
* Stabiles Asset: `SwitchWerk.apk`

Hinweis: Falls das Script aktuell `SwitchWerk-${VERSION}.apk` ohne führendes `v` erzeugt, soll im Rahmen der Umsetzung geprüft werden, ob der bestehende Dateiname bewusst so gewählt wurde oder an das dokumentierte Format `SwitchWerk-v1.2.3.apk` angepasst werden soll.

### GitHub Release Upload

* `gh release create` soll beide APK-Dateien als Assets erhalten.
* Der stabile Dateiname darf nicht die versionierte Datei ersetzen.
* Der stabile Dateiname soll nur zusätzlich bereitgestellt werden.

## Nicht im Scope

* Änderungen am Android-Build.
* Änderungen an App-Version, `versionCode` oder `versionName`.
* Änderungen an der In-App-Update-Prüfung.
* Änderungen an Release Notes.
* Automatische Veröffentlichung über GitHub Actions.
* Änderung des Repository-Namens oder Release-Tags.

## Akzeptanzkriterien

* [x] Nach Ausführung des Release-Scripts enthält die GitHub Release ein versioniertes APK-Asset.
* [x] Nach Ausführung des Release-Scripts enthält dieselbe GitHub Release zusätzlich `SwitchWerk.apk`.
* [x] `SwitchWerk.apk` ist bytegleich oder funktional identisch mit der versionierten Release-APK.
* [x] Der stabile Download-Link `/releases/latest/download/SwitchWerk.apk` funktioniert für die neueste Release.
* [x] Bestehende Release-Funktionalität bleibt unverändert nutzbar.
* [x] Das Script bricht weiterhin bei fehlender oder ungültiger APK sauber ab.
* [x] Die Änderung ist im Script klar nachvollziehbar benannt.

## Abschluss

Umgesetzt in `scripts/release-github.sh`.

Das Script behält das bestehende versionierte Asset `SwitchWerk-${VERSION}.apk`
bei und erzeugt zusätzlich `SwitchWerk.apk` aus derselben geprüften
Release-APK. `gh release create` erhält beide APK-Dateien als Assets.

Geprüft:

* `bash -n scripts/release-github.sh`

Ein echter Release-Lauf und die Download-Prüfung der GitHub-Assets erfolgen beim
nächsten Release auf dem Host.

## Testhinweise

* Release-Script mit einer Testversion prüfen.
* Prüfen, ob beide APK-Assets im GitHub Release sichtbar sind.
* Download des versionierten APK-Assets testen.
* Download von `SwitchWerk.apk` testen.
* Download über `/releases/latest/download/SwitchWerk.apk` testen.
* APK-Signatur der hochgeladenen APK prüfen.
