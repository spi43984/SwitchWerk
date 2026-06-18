# Issue #21: HTTP/HTTPS Device Actions

## Ziel

Geräteaktionen sollen unabhängig vom verwendeten Transportprotokoll
funktionieren.

Jedes Gerät kann individuell festlegen, ob API-Aufrufe über HTTP oder HTTPS
ausgeführt werden.

## Scope

- Gerät speichert explizit das verwendete Protokoll
- unterstützte Protokolle:
  - HTTP
  - HTTPS
- URL-Erzeugung verwendet das konfigurierte Protokoll
- bestehende Geräte erhalten einen sinnvollen Standardwert
- Gerätebearbeitung ermöglicht Auswahl des Protokolls
- API-Aufrufe verwenden das konfigurierte Protokoll
- Fehler bei TLS- oder Zertifikatsproblemen werden verständlich angezeigt

## Nicht im Scope

- Zertifikat-Pinning
- Trust-On-First-Use
- Import von Zertifikaten
- Verwaltung eigener Trust Stores
- Umgehung von Zertifikatsprüfungen
- automatische Umschaltung zwischen HTTP und HTTPS

## Akzeptanzkriterien

- [ ] Geräte speichern ein Protokoll
- [ ] HTTP-Geräte funktionieren weiterhin
- [ ] HTTPS-Geräte können konfiguriert werden
- [ ] API-Aufrufe verwenden das konfigurierte Protokoll
- [ ] TLS-Fehler werden verständlich angezeigt
- [ ] Keine sensiblen Zertifikatsdaten werden geloggt

## Testhinweise

- HTTP-Gerät erfolgreich aufrufen
- HTTPS-Gerät erfolgreich aufrufen
- HTTPS-Gerät mit ungültigem Zertifikat
- HTTPS-Gerät nicht erreichbar
- bestehendes Gerät nach Update weiterhin funktionsfähig
