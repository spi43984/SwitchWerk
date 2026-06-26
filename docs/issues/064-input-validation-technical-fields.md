# Issue 064: Input Validation For Technical Fields

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: Security / Validation / UX
- Bereich: Eingabeformulare / Geräte / WLAN-Profile / Import-Export

## Ziel

Technische Eingabefelder sollen eingegebene Werte validieren, bevor sie gespeichert oder für Netzwerkaufrufe verwendet werden.

Ungültige IP-Adressen, ungültige DNS-Namen, fehlerhafte Hostnamen, unsichere Pfade oder potenziell gefährliche Eingaben sollen erkannt und mit verständlichen Fehlermeldungen abgelehnt werden.

## Hintergrund

Die App enthält mehrere Eingabefelder für technische Daten, z. B. Hostname/IP, DNS-Namen, API-Pfade, URLs, HTTP-Methoden, Request Body, Content-Type, WLAN-Namen und weitere Konfigurationswerte.

Diese Eingaben können aktuell potenziell ungültige Werte enthalten. Dadurch können Fehlfunktionen, schwer verständliche Netzwerkfehler oder sicherheitsrelevante Risiken entstehen.

Die App soll keine beliebigen oder offensichtlich gefährlichen Eingaben ungeprüft übernehmen.

## Scope

### Validierung technischer Eingabefelder

- Bestehende Eingabefelder für Hostname, IP-Adresse, DNS-Name, URL, API-Pfad, HTTP-Methode, Content-Type und Request Body prüfen.
- Zentrale oder wiederverwendbare Validierungslogik einführen, soweit sinnvoll.
- IP-Adressen syntaktisch validieren.
- DNS-/Hostnamen syntaktisch validieren.
- API-Pfade und URL-Bestandteile validieren.
- HTTP-Methoden auf erlaubte Werte begrenzen.
- Content-Type auf plausible Werte prüfen.
- Offensichtlich gefährliche oder unerwartete Eingaben ablehnen.
- Validierung vor dem Speichern ausführen.
- Validierung vor Netzwerkaufrufen berücksichtigen, falls gespeicherte Altdaten ungültig sind.
- Verständliche Fehlermeldungen in Deutsch und Englisch anzeigen.
- Hilfe-, Info- und Tooltip-Texte prüfen und bei Bedarf ergänzen.

### Sicherheitsanforderungen

- Keine sensiblen Eingaben loggen.
- Keine realen IP-Adressen, Hostnamen, SSIDs, Tokens oder Passwörter in Tests, Dokumentation oder Fehlermeldungsbeispiele aufnehmen.
- Für Beispiele ausschließlich neutrale Platzhalter verwenden, z. B. `192.0.2.10`, `device.local`, `server.domain.com` oder `Example WiFi`.
- Validierung darf keine Cloud-Abhängigkeit und keine externe Sicherheitsbibliothek erzwingen.
- Request Body nicht ungewollt verändern.
- Eingaben nicht automatisch ausführen oder interpretieren.

### Bestehende Daten

- Verhalten bei bereits gespeicherten ungültigen Altdaten festlegen.
- Ungültige Altdaten dürfen nicht zu einem App-Absturz führen.
- Nutzer sollen ungültige Altdaten korrigieren können.
- Importierte Konfigurationen sollen dieselbe Validierung oder eine klare Import-Fehlermeldung erhalten.

## Nicht im Scope

- Vollständiger Sandbox-Interpreter für Request Body oder Befehle
- Neue Geräteprotokolle
- Änderung der bestehenden HTTP-/RPC-Ausführungslogik außerhalb notwendiger Validierungsprüfungen
- Neue externe Abhängigkeiten
- Cloud-basierte Sicherheitsprüfung
- Automatische Migration oder Reparatur beliebiger ungültiger Altdaten
- GitHub-Issue, Branch, Pull Request oder Merge

## Architekturhinweise

- Validierungslogik nicht direkt in Composables duplizieren.
- Wiederverwendbare Validatoren bevorzugt in einer geeigneten Domain-, Data- oder Utility-Schicht ablegen.
- ViewModels sollen Validierungsergebnisse in UI-State übersetzen.
- Compose-Felder zeigen Fehler über vorhandene Material-3-Mechanismen an.
- Bestehende MVVM-Struktur beibehalten.
- Keine Netzwerklogik in Compose.
- Keine unnötigen Framework-Wechsel.
- Unit-Tests für Validatoren ergänzen.

## Akzeptanzkriterien

- [x] Ungültige IP-Adressen werden erkannt und nicht gespeichert.
- [x] Gültige Beispiel-IP-Adressen wie `192.0.2.10` werden akzeptiert.
- [x] Ungültige DNS-/Hostnamen werden erkannt und nicht gespeichert.
- [x] Gültige Beispiel-Hostnamen wie `device.local` und `server.domain.com` werden akzeptiert.
- [x] API-Pfade werden auf plausible und sichere Eingaben geprüft.
- [x] HTTP-Methoden werden auf erlaubte Werte begrenzt.
- [x] Content-Type-Werte werden auf plausible Werte geprüft.
- [x] Potenziell gefährliche oder unerwartete Eingaben werden nicht ungeprüft gespeichert oder ausgeführt.
- [x] Fehler werden direkt am betroffenen Eingabefeld verständlich angezeigt.
- [x] Fehlertexte sind auf Deutsch und Englisch vorhanden.
- [x] Bereits gespeicherte ungültige Altdaten führen nicht zu einem App-Absturz.
- [x] Importierte Konfigurationen umgehen die Validierung nicht.
- [x] Es werden keine sensiblen Daten geloggt.
- [x] Unit-Tests decken zentrale Validatoren ab.
- [x] Bestehende gültige Konfigurationen funktionieren weiterhin.

## Testhinweise

- Gerätebearbeitung öffnen und Hostname/IP bearbeiten.
- WLAN-Profil- und Geräte-Zuordnungsdialoge prüfen.
- Gültige IP-Adresse testen, z. B. `192.0.2.10`.
- Ungültige IP-Adresse testen, z. B. `999.999.999.999`.
- Gültigen Hostnamen testen, z. B. `device.local`.
- Ungültigen Hostnamen testen, z. B. `bad host name`.
- API-Pfad testen, z. B. `/rpc/Switch.Toggle?id=0`.
- Ungültige oder unerwartete Pfade testen.
- Import mit ungültigen technischen Feldern testen.
- Prüfen, dass Fehlermeldungen verständlich sind.
- Prüfen, dass Speichern mit gültigen Eingaben weiterhin funktioniert.
- Prüfen, dass Abbrechen keine Validierungsfehler dauerhaft speichert.
