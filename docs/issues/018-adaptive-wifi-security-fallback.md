# Issue #18: Adaptive WiFi Security Fallback

## Ziel
Der WLAN-Verbindungsdienst soll Geräte-WLANs mit WPA2 oder WPA3 verbinden,
ohne dass der Sicherheitstyp bei jeder Änderung manuell angepasst werden muss.

Da die meisten Zielgeräte derzeit WPA2 verwenden, wird WPA2 beim ersten
Verbindungsaufbau bevorzugt. Schlägt dieser Versuch fehl, wird einmalig WPA3
versucht. Der zuletzt erfolgreiche Sicherheitstyp wird für folgende
Verbindungen bevorzugt.

## Scope
- unterstützte Sicherheitstypen als klaren Modelltyp abbilden
- WPA2 beim ersten Verbindungsversuch bevorzugen
- bei `Unavailable` oder Timeout einmalig den jeweils anderen Typ versuchen
- zuletzt erfolgreichen Sicherheitstyp pro WLAN-Profil speichern
- zuletzt erfolgreichen Typ bei späteren Verbindungen zuerst verwenden
- Gesamt-Timeout über beide Verbindungsversuche begrenzen
- gespeicherten Typ nach einer erfolgreichen Verbindung aktualisieren
- bestehende WLAN-Profile per Room-Migration übernehmen
- strukturierte Fehler melden, wenn beide Versuche fehlschlagen
- nach Fehlschlag beider Sicherheitstypen ein eindeutiges Endergebnis liefern,
  damit die übergeordnete Geräteaktion die nächste dem Gerät zugewiesene SSID
  testen kann
- keine SSIDs, Passwörter oder anderen sensiblen Daten loggen

## Verbindungsstrategie
1. Ist ein zuletzt erfolgreicher Typ gespeichert, wird dieser zuerst versucht.
2. Ist kein Typ gespeichert, wird zuerst WPA2 versucht.
3. Nur bei `Unavailable` oder Timeout wird einmalig der andere Typ versucht.
4. Bei Erfolg wird der funktionierende Typ für das WLAN-Profil gespeichert.
5. Das gemeinsame Zeitlimit darf durch den zweiten Versuch nicht neu beginnen.
6. Scheitern WPA2 und WPA3 für dieselbe SSID, ist dieses WLAN-Profil
   abgeschlossen. Der Dienst liefert ein strukturiertes Endergebnis zurück.
7. Die übergeordnete Geräteaktion behandelt dieses Ergebnis als fehlgeschlagene
   Verbindung und versucht anschließend die nächste dem Gerät zugewiesene SSID.

Beispiel bei zwei einem Gerät zugewiesenen WLAN-Profilen:

```text
SSID 1: WPA2 -> fehlgeschlagen
SSID 1: WPA3 -> fehlgeschlagen
SSID 2: bevorzugter oder gespeicherter Sicherheitstyp -> versuchen
SSID 2: anderer Sicherheitstyp -> nur bei Unavailable oder Timeout versuchen
```

## Nicht im Scope
- HTTP- oder API-Aufrufe
- Geräte schalten
- Implementierung der Iteration über mehrere WLAN-Profile oder
  Geräteverbindungen; diese bleibt Bestandteil von Issue 011
- Wiederholung nach HTTP-, DNS- oder Gerätefehlern
- WPA3 Enterprise, WPA2 Enterprise oder andere Enterprise-Verfahren
- automatische Änderung oder Speicherung von WLAN-Passwörtern

## Akzeptanzkriterien
- [ ] Ohne gespeicherten Sicherheitstyp wird WPA2 zuerst versucht
- [ ] Nach WPA2-Timeout oder `Unavailable` wird einmalig WPA3 versucht
- [ ] Nach WPA3-Timeout oder `Unavailable` wird einmalig WPA2 versucht, wenn WPA3 zuerst verwendet wurde
- [ ] Der zuletzt erfolgreiche Sicherheitstyp wird pro WLAN-Profil gespeichert
- [ ] Bei späteren Verbindungen wird der zuletzt erfolgreiche Typ zuerst verwendet
- [ ] Eine Umstellung eines WLANs von WPA2 auf WPA3 oder umgekehrt wird durch den Fallback erkannt
- [ ] Beide Versuche teilen sich ein begrenztes Gesamt-Timeout
- [ ] Andere Fehler lösen keinen Security-Fallback aus
- [ ] Wenn beide Versuche scheitern, wird ein strukturiertes Fehlerergebnis geliefert
- [ ] Das Fehlerergebnis nach zwei fehlgeschlagenen Security-Versuchen erlaubt
  der übergeordneten Geräteaktion, mit der nächsten zugewiesenen SSID
  fortzufahren
- [ ] Ein fehlgeschlagener WPA2/WPA3-Durchlauf für eine SSID beendet nicht die
  gesamte Verbindungsfolge eines Geräts
- [ ] Bestehende WLAN-Profile bleiben nach der Room-Migration erhalten
- [ ] Es werden keine sensiblen WLAN-Daten geloggt

## Testhinweise
- WLAN-Profil ohne gespeicherten Typ verbindet sich direkt per WPA2
- WPA2 schlägt fehl und WPA3 ist erfolgreich
- gespeichertes WPA3 wird bei der nächsten Verbindung zuerst verwendet
- gespeichertes WPA3 schlägt nach Umstellung fehl und WPA2 ist erfolgreich
- beide Sicherheitstypen sind nicht verfügbar
- WPA2 und WPA3 der ersten SSID schlagen fehl; die übergeordnete Geräteaktion
  setzt mit der zweiten zugewiesenen SSID fort
- WPA2 und WPA3 aller zugewiesenen SSIDs schlagen fehl; erst danach wird die
  gesamte Geräteverbindung als fehlgeschlagen gemeldet
- Coroutine wird während des ersten und während des zweiten Versuchs abgebrochen
- Gesamt-Timeout wird auch beim zweiten Versuch nicht überschritten
- bestehende Room-Daten werden ohne Datenverlust migriert
