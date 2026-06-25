# Issue 056: Keyboard Options For Technical Device Inputs

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: UX / Eingabeformulare
- Bereich: Gerätebearbeitung / Geräte-WLAN-Zuordnung

## Ziel

Technische Eingaben im Gerätebereich sollen auf Android leichter und
fehlerärmer möglich sein.

Die Tastatur soll bei Hostnamen, IP-Adressen und API-Pfaden keine
Autokorrektur, keine automatische Großschreibung und keine erschwerende
Satzzeichenergänzung verwenden.

## Hintergrund

Beim Testen von HTTPS-Geräteaktionen fiel auf, dass die Android-Tastatur die
Eingabe von DNS-Namen, IP-Adressen und API-Pfaden erschwert.

Problematisch sind insbesondere:

- Autokorrektur bei DNS-Namen und Hostnamen
- automatische Eingabe- oder Satzzeichenfunktionen nach `.`
- automatische Großschreibung technischer Eingaben
- fehlender schneller Zugriff auf `/` beim API-Pfad

Issue 038 „Dialog Keyboard Handling“ behandelte Layout, Sichtbarkeit und Fokus
bei geöffneter Tastatur. Dieses Issue ergänzt gezielt die Tastaturkonfiguration
für technische Eingabefelder.

## Scope

### Hostname/IP-Eingaben

- Für Hostname/IP-Felder passende `KeyboardOptions` setzen.
- Autokorrektur deaktivieren.
- automatische Großschreibung deaktivieren.
- Eingabe von DNS-Namen und IP-Adressen darf nicht durch Tastaturvorschläge oder
  Satzzeichenautomatismen erschwert werden.

Betroffene Stellen:

- Geräte-WLAN-Zuordnung: `Hostname/IP`
- weitere vorhandene Host-/DNS-/IP-Felder, falls im Gerätebereich vorhanden

### API-Pfad-Eingaben

- Für API-Pfad-Felder passende `KeyboardOptions` setzen.
- Autokorrektur deaktivieren.
- automatische Großschreibung deaktivieren.
- Tastaturlayout so wählen, dass `/` gut erreichbar ist, insbesondere links
  unten, soweit Android-Tastaturen dies über den gewählten Keyboard-Typ
  unterstützen.

Betroffene Stellen:

- Gerätebearbeitung: `API-Aufruf`

## Nicht im Scope

- Änderung des Gerätemodells
- Änderung von HTTP/HTTPS-Logik
- Request Body oder Content-Type aus Issue 022
- neue Eingabefelder
- neue externe Abhängigkeiten
- eigene Tastatur oder eigenes Keyboard-Widget
- Workarounds für einzelne Hersteller-Tastaturen außerhalb der Android-/
  Compose-Standardoptionen

## Architekturhinweise

- Umsetzung in Compose über passende `KeyboardOptions`.
- Keine Netzwerklogik in Compose.
- Kein zusätzlicher ViewModel-State, wenn die Änderung rein die Tastaturoptionen
  betrifft.
- Bestehende Dialog- und Formularstruktur beibehalten.
- Möglichst gezielt an den betroffenen `OutlinedTextField`-Feldern ändern.

## Akzeptanzkriterien

- [ ] Hostname/IP-Felder deaktivieren Autokorrektur.
- [ ] Hostname/IP-Felder deaktivieren automatische Großschreibung.
- [ ] Hostname/IP-Eingaben wie `server.domain.con` und `192.0.2.10` werden
      nicht durch Tastaturautomatismen erschwert.
- [ ] API-Pfad-Feld deaktiviert Autokorrektur.
- [ ] API-Pfad-Feld deaktiviert automatische Großschreibung.
- [ ] API-Pfad-Feld verwendet ein für Pfade geeignetes Tastaturlayout mit gut
      erreichbarem `/`, soweit durch Android/Compose steuerbar.
- [ ] Bestehende Fokus- und Speichern-/Abbrechen-Bedienung aus Issue 038 bleibt
      unverändert.

## Testhinweise

- Gerätebearbeitung öffnen und API-Pfad bearbeiten.
- Geräte-WLAN-Zuordnung öffnen und Hostname/IP bearbeiten.
- DNS-Name eingeben, z. B. `server.domain.con`.
- IP-Adresse eingeben, z. B. `192.0.2.10`.
- API-Pfad eingeben, z. B. `/xyz` und `/rpc/Switch.Toggle?id=0`.
- Prüfen, dass keine Autokorrektur oder automatische Großschreibung eingreift.
- Prüfen, dass Speichern und Abbrechen weiterhin korrekt funktionieren.
