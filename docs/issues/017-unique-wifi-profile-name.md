# Issue #17: Unique WiFi Profile Name

Status: abgeschlossen

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
- [x] Jedes WLAN-Profil besitzt einen nicht leeren Namen
- [x] Profilnamen sind eindeutig
- [x] Mehrere Profile dürfen dieselbe SSID verwenden
- [x] Name, SSID und Passwort können bearbeitet werden
- [x] Bestehende Profile bleiben nach der Datenbankmigration erhalten
- [x] WLAN-Profile sind in Listen und Gerätezuordnungen eindeutig erkennbar
