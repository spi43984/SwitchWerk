# Issue 066: QR Code Import HTTP URL

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: Bugfix / Import
- Bereich: QR-Code-Import / Konfigurationsimport / URL-Validierung

## Ziel

Der QR-Code-Import soll neben `https://` auch `http://`-URLs akzeptieren, damit Konfigurationsdateien aus einem internen WLAN ohne TLS geladen werden können.

## Hintergrund

Aktuell meldet die App bei einem QR-Code mit `http://`-URL:

> QR-Code enthält keine gültige https-URL.

Für lokale Geräte, Router, Shelly-APs, OpenWrt-Systeme oder einfache interne Webserver ist HTTPS nicht immer verfügbar. Da SwitchWerk lokale HTTP-Kommunikation grundsätzlich unterstützt, soll der QR-Code-Import HTTP-URLs für interne Downloadquellen zulassen.

## Scope

### URL-Validierung

- QR-Code-Import akzeptiert `https://`-URLs weiterhin.
- QR-Code-Import akzeptiert zusätzlich `http://`-URLs.
- Andere Schemes bleiben ungültig, z. B. `file://`, `ftp://`, `javascript:` oder leere Werte.
- Fehlermeldung darf nicht mehr ausschließlich von `https-URL` sprechen, wenn allgemein eine HTTP- oder HTTPS-URL erwartet wird.

### Importverhalten

- Der bestehende Importablauf bleibt unverändert.
- Der Download über `http://` verwendet dieselben Fehlerpfade wie bisherige Import-Downloads.
- Netzwerkfehler werden weiterhin verständlich angezeigt.

### Betroffene Bereiche

- QR-Code-Import.
- Import-URL-Validierung.
- Deutsche und englische UI-Texte.
- Hilfe-, Info- oder Tooltip-Texte zum QR-Code-Import, falls vorhanden.

## Nicht im Scope

- Keine Unterstützung anderer URL-Schemes.
- Keine Authentifizierung für Importquellen.
- Keine Änderung am Datei- oder JSON-Format.
- Keine Änderung an Exportfunktionen.
- Keine neue externe Abhängigkeit.
- Keine generelle Abschwächung externer Netzwerkregeln außerhalb des Imports.

## Architekturhinweise

- Validierung zentral halten und nicht direkt in Composables duplizieren.
- Bestehende MVVM- und Repository-Struktur beibehalten.
- Netzwerklogik nicht in die UI verschieben.
- Für Beispiele nur neutrale Platzhalter verwenden, z. B. `http://server.domain.com/config.json` oder `http://192.0.2.10/config.json`.
- Keine realen lokalen IP-Adressen, Hostnamen, SSIDs oder Zugangsdaten in Tests, Logs oder Dokumentation aufnehmen.
- HTTPS bleibt bevorzugt, wenn verfügbar; HTTP ist für lokale interne Importquellen zulässig.

## Akzeptanzkriterien

- [x] QR-Code mit `https://server.domain.com/config.json` wird weiterhin akzeptiert.
- [x] QR-Code mit `http://server.domain.com/config.json` wird akzeptiert.
- [x] QR-Code mit neutraler lokaler Beispieladresse wie `http://192.0.2.10/config.json` wird akzeptiert.
- [x] QR-Code mit ungültigem Scheme wird abgelehnt.
- [x] Die Fehlermeldung nennt HTTP/HTTPS korrekt und nicht nur HTTPS.
- [x] Import über HTTP startet denselben Importablauf wie Import über HTTPS.
- [x] Fehler beim HTTP-Download werden sauber angezeigt.
- [x] Deutsche und englische Texte sind konsistent gepflegt.
- [x] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

## Abschluss

- Implementiert in Branch `qr-code-import-http-url`.
- QR-Code-Import und manueller URL-Import akzeptieren `http://` und `https://`.
- Andere Schemes bleiben ungültig.
- Direkte HTTP-Importe mit Portnummer werden akzeptiert.
- HTTPS-Weiterleitungen auf HTTP bleiben blockiert.
- Container-Prüfung: `./gradlew testDebugUnitTest` erfolgreich.
- Host-Prüfungen laut Benutzerrückmeldung erfolgreich: `./gradlew clean assembleDebug`, `./gradlew installDebug`.

## Testhinweise

- QR-Code mit `https://server.domain.com/config.json` testen.
- QR-Code mit `http://server.domain.com/config.json` testen.
- QR-Code mit `http://192.0.2.10/config.json` testen.
- QR-Code mit ungültigem Inhalt testen.
- QR-Code mit nicht unterstütztem Scheme testen.
- HTTP-Import bei nicht erreichbarem Server testen.
- Prüfen, dass keine sensiblen Daten geloggt werden.
