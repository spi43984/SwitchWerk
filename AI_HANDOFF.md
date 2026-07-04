# AI Handoff

Stand: 4. Juli 2026

## Abgeschlossen

- Issue 072 „SwitchWerk empfängt Intents“
- GitHub-Issue: #168
- Pull Request: #169
- Squash-Merge auf `main`: `4844d4e feat: add external device action intents (#169)`
- Externe Geräteaktionen verwenden eine strikt validierte lokale Geräte-ID und
  die bestehende MainViewModel-/DeviceActionService-Logik.
- Die globale Freigabe ist standardmäßig deaktiviert. URLs, Befehle,
  Request-Bodies und zusätzliche Extras werden nicht akzeptiert.
- Deutsche und englische Hilfe-, Info- und Fehlertexte sowie die README sind
  aktualisiert.
- Container-Prüfungen und GitHub-Prüfungen waren erfolgreich. Der manuelle
  ADB-Intent-Test wurde vom Benutzer bestätigt.

## Nächster Stand

- Auf dem lokalen Branch `fix-intent-error-display` ist die Anzeige von
  Intent-Fehlern korrigiert: bekannte Geräte verwenden den zeitlich begrenzten
  Widget-Fehler, nicht zuordenbare Fehler einen zeitlich begrenzten allgemeinen
  Hinweis; alle Fälle bleiben in den Aktionsdetails erhalten.
- Der Benutzer hat das korrigierte Verhalten auf dem Gerät bestätigt.
- Die Hilfe enthält deutsch/englisch konsistente, per Textauswahl kopierbare
  ADB-Beispiele für einen gültigen Intent und den Fehlertest ohne Geräte-ID.
- Der Benutzer hat auch die kopierbaren Beispiele in der Hilfe erfolgreich
  getestet.
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 074 „Gruppen“.
- Weitere Reihenfolge und Status ausschließlich aus `docs/issues/overview.txt`
  entnehmen.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
