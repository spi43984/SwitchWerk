# Issue 057: Encrypted Storage Restore Start Crash

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: Bugfix / Stabilität
- Bereich: App-Start / verschlüsselte lokale Speicherung

## Ziel

Die App darf nach Deinstallation und anschließender Neuinstallation nicht beim Start abstürzen, wenn Android wiederhergestellte oder inkonsistente Backup-Daten bereitstellt.

## Hintergrund

Auf einem Android-Testgerät wurde ein Startabsturz beobachtet:

- App deinstallieren
- App neu installieren
- App starten
- App stürzt sofort ab
- einmaliges Löschen des App-Speichers über Android-App-Info behebt den Start

Die Analyse zeigt einen Fehler beim Erzeugen der verschlüsselten lokalen Speicherung:

- `EncryptedSharedPreferences.create(...)`
- `EncryptedWifiCredentialStore`
- Ursache im Stacktrace: `javax.crypto.AEADBadTagException`
- darunter: `android.security.KeyStoreException: Signature/MAC verification failed`

Das passt zu wiederhergestellten oder inkonsistenten verschlüsselten Keysets nach Android-Backup/Restore.

## Scope

- Startcrash beim Initialisieren des verschlüsselten WLAN-Credential-Stores beheben.
- Wiederhergestellte, nicht mehr entschlüsselbare verschlüsselte SharedPreferences robust behandeln.
- Keine sensiblen Daten loggen.
- App soll bei beschädigten Credential-Daten kontrolliert starten.
- Betroffene gespeicherte WLAN-Passwörter dürfen bei nicht wiederherstellbarer Entschlüsselung verworfen werden.
- Nutzer soll danach WLAN-Passwörter erneut eingeben können.

## Mögliche Lösungsansätze

Zu prüfen:

1. Android Auto Backup für nicht wiederherstellbare verschlüsselte Daten ausschließen.
2. Backup-Regeln für verschlüsselte SharedPreferences und AndroidX-Security-Keysets ergänzen.
3. Beim Initialisieren des Credential-Stores bekannte Crypto-/Keystore-Fehler abfangen.
4. Beschädigte verschlüsselte Store-Dateien kontrolliert löschen und neu anlegen.
5. Sicherstellen, dass Room-Konfigurationsdaten und nicht sensible Einstellungen nicht unnötig verloren gehen.

## Nicht im Scope

- HTTP/HTTPS-Geräteaktionen
- Tastaturverhalten technischer Eingabefelder
- Änderung des Gerätemodells
- Cloud-Synchronisation
- Export/Import-Formatänderungen
- Logging sensibler Daten

## Architekturhinweise

- Fix im Bereich `data/security` und ggf. Android-Manifest-/Backup-Konfiguration.
- Koin-Initialisierung darf durch beschädigte verschlüsselte Credentials nicht mehr die App beenden.
- Bestehende MVVM-/Repository-Struktur beibehalten.
- Keine neue externe Abhängigkeit einführen.
- Sensible Daten bleiben ausschließlich lokal und verschlüsselt gespeichert.

## Akzeptanzkriterien

- [x] App startet nach Deinstallation und Neuinstallation ohne manuelles Löschen des App-Speichers.
- [x] App startet auch dann, wenn verschlüsselte Credential-Daten nicht entschlüsselbar sind.
- [x] Nicht wiederherstellbare WLAN-Passwörter werden kontrolliert verworfen.
- [x] Nicht sensible Konfigurationen bleiben soweit möglich erhalten.
- [x] Es werden keine Passwörter, SSIDs, Hosts, IP-Adressen oder Tokens geloggt.
- [x] Der Nutzer kann betroffene WLAN-Passwörter nach dem Start erneut speichern.
- [x] Bestehende Credential-Speicherung funktioniert nach dem Fix weiterhin.

## Umsetzung

- Android-Backup-Regeln schließen die nicht wiederherstellbaren verschlüsselten
  WLAN-Credential-SharedPreferences und AndroidX-Security-Keysets aus.
- `EncryptedWifiCredentialStore` fängt bekannte Crypto-/Keystore-/Security- und
  IO-Fehler bei Initialisierung sowie späterem Lesen oder Schreiben ab, löscht
  kontrolliert nur die betroffenen Credential-Dateien und initialisiert den
  Store einmal neu.
- Nicht wiederherstellbare WLAN-Passwörter werden verworfen; Room-Daten und
  nicht sensible App-Konfigurationen bleiben erhalten.
- Wenn ein SwitchWerk-verwaltetes WLAN laut erkanntem oder lokal verifiziert
  gespeichertem Sicherheitstyp ein Passwort erwartet, aber kein Passwort mehr
  gespeichert ist, startet die App keinen aussichtslosen Android-WLAN-Request
  und zeigt eine neutrale Passwort-fehlt-Meldung. Passwortlose/offene WLANs
  bleiben möglich, wenn kein geschützter Sicherheitstyp ermittelt oder
  gespeichert ist.
- Es wurden keine neuen externen Abhängigkeiten eingeführt.

## Prüfungen

- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`
- `git diff --check`
- Host: `./gradlew installDebug`
- Host: Start per `adb shell monkey -p de.piecha.switchwerk 1`
- Host: Logcat-Prüfung ohne `AndroidRuntime`-Crash

## Testhinweise

- App mit gespeichertem WLAN-Passwort installieren und starten.
- App deinstallieren.
- App neu installieren.
- App starten.
- Prüfen, dass kein Startcrash auftritt.
- Prüfen, dass WLAN-Passwörter bei Bedarf erneut eingegeben werden können.
- Prüfen, dass App-Start, Einstellungen und Dashboard weiterhin funktionieren.

Zusätzliche Prüfungen:

    adb logcat -c
    adb shell pm uninstall de.piecha.switchwerk || true
    ./gradlew installDebug
    adb shell monkey -p de.piecha.switchwerk 1
    adb logcat -d AndroidRuntime:E '*:S'
