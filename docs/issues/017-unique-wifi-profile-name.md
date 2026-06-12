# Issue #17: Unique WiFi Profile Name

## Ziel
WLAN-Profile erhalten zusätzlich zu SSID und Passwort einen eindeutigen, frei wählbaren Namen.

Dadurch können mehrere WLAN-Profile mit identischer SSID eindeutig unterschieden werden.

## Scope
- WLAN-Profil um ein Namensfeld erweitern
- Name beim Anlegen und Bearbeiten erfassen
- Namen eindeutig validieren
- Name statt SSID als primäre Bezeichnung anzeigen
- SSID weiterhin sichtbar anzeigen
- Bestehende Room-Daten migrieren
- Gerätezuordnungen anhand des Profilnamens verständlich darstellen

## Nicht im Scope
- Änderung der Passwortverschlüsselung
- Automatische Erkennung gleichnamiger WLANs
- Änderung des WLAN-Verbindungsdienstes

## Akzeptanzkriterien
- [ ] Jedes WLAN-Profil besitzt einen nicht leeren Namen
- [ ] Profilnamen sind eindeutig
- [ ] Mehrere Profile dürfen dieselbe SSID verwenden
- [ ] Name, SSID und Passwort können bearbeitet werden
- [ ] Bestehende Profile bleiben nach der Datenbankmigration erhalten
- [ ] WLAN-Profile sind in Listen und Gerätezuordnungen eindeutig erkennbar
