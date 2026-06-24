# Issue 047 iOS: WLAN/AP/HTTP Feasibility

## Metadaten

- Status: Backlog
- Priorität: P4
- Plattform: iOS
- iOS-Phase: 2 von 6
- Typ: Machbarkeitsanalyse / Risiko

## Ziel

Vor einer iOS-Implementierung soll belastbar geklärt werden, ob SwitchWerk unter iOS eine Geräte-AP-Verbindung herstellen und darüber lokale HTTP- oder RPC-Aufrufe zuverlässig ausführen kann.

## Zu prüfen

- `NEHotspotConfiguration` für Geräte-APs, einschließlich SSID und Passwort.
- Local-Network-Permission und die für lokale Gerätekommunikation nötigen `Info.plist`-Angaben.
- Verhalten bei Geräte-APs ohne Internetzugang sowie bei Captive- oder No-Internet-Bewertungen durch iOS.
- Wechsel zwischen Heim-WLAN und Geräte-AP, einschließlich Rückwechsel und der Grenzen einer App-gesteuerten Netzwerkauswahl.
- Erreichbarkeit über IP-Adresse, DNS, mDNS und Hostnamen auf dem jeweils ausgewählten Netz.
- Auswirkung von URLSession, Netzwerkpfad und möglichen iOS-Einschränkungen auf HTTP/RPC-Aufrufe.
- App-Store- und Review-Risiken der benötigten Berechtigungen und des Verbindungsablaufs.

## Vorgehen und Ergebnis

Die Analyse soll mit einem kleinen, isolierten iOS-Prototyp und realen Shelly-Geräte-APs erfolgen. Sie dokumentiert die getesteten iOS-Versionen, Geräte, Netzwerkszenarien, beobachteten Systemdialoge und verbleibenden Risiken.

Die dafür benötigte Mac-/Xcode-Umgebung, ein reales iPhone und die Testumgebung
werden zuvor durch Issue 052 festgelegt.

Das Ergebnis lautet eindeutig genau eines von:

- machbar
- machbar mit Einschränkungen
- nicht machbar

Bei Einschränkungen müssen der notwendige Nutzerablauf und die betroffenen Gerätekonfigurationen benannt sein. Eine spätere iOS-App-Foundation oder ein iOS-WLAN-Join-Flow darf erst auf dieser Entscheidung aufbauen.

## Nicht Bestandteil

- Umsetzung einer produktiven iOS-App.
- Migration oder Umbau der Android-App.
- Cloud-, Tracking-, Konto- oder Analytics-Dienste.
- Zusage einer automatischen Rückverbindung, falls iOS diese nicht zuverlässig zulässt.

## Sicherheits- und Datenschutzhinweise

- SSIDs, Passwörter, lokale IP-Adressen und Befehle dürfen weder geloggt noch in Analyseberichte übernommen werden.
- Es werden nur lokale Geräteverbindungen untersucht.
- Benötigte Berechtigungen werden auf das technisch erforderliche Minimum begrenzt.

## Akzeptanzkriterien

- [ ] Alle genannten iOS-Netzwerkaspekte sind anhand eines realen Prototyps bewertet.
- [ ] Erfolgs- und Fehlerfälle für Heim-WLAN, Geräte-AP und AP ohne Internet sind dokumentiert.
- [ ] App-Store-Risiken und erforderliche Nutzerinteraktionen sind benannt.
- [ ] Das Ergebnis ist eindeutig als machbar, machbar mit Einschränkungen oder nicht machbar festgehalten.
- [ ] Es werden keine Android-Produktivdateien geändert.
