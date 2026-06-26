# Issue 061: Import Replace Preserves Wizard State

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: Bugfix / Regression / UX
- Bereich: Import/Export / Setup Wizard / AppSettings

## Ziel

Beim Import einer Konfiguration mit **Alles ersetzen** darf der bestehende Zustand des Einrichtungs-Assistenten nicht verändert werden.

Der Import darf insbesondere nicht dazu führen, dass der Wizard erneut automatisch angezeigt wird, nur weil der Datenbestand während des Replace-Imports zunächst gelöscht wird.

## Hintergrund

Regression zu Issue 058 **Setup Wizard / Einrichtungs-Assistent**.

Beim Import einer Konfiguration mit aktivierter Option **Alles ersetzen** wird zuerst der komplette Datenbestand gelöscht. Dieses Löschen triggert aktuell die Anzeige des Wizards.

Das ist unerwünscht, weil ein Benutzer, der aktiv eine Konfiguration importiert, als bereits eingewiesen gelten muss.

Der bestehende Wizard-Zustand muss daher beim Import erhalten bleiben:

- **Überspringen** bleibt erhalten
- **Nicht mehr anzeigen** bleibt erhalten
- ein bereits deaktivierter Wizard bleibt deaktiviert
- ein nur für die laufende Sitzung übersprungener Wizard darf durch den Import nicht dauerhaft umgestellt werden

Etwas anderes gilt bei echter Neuinitialisierung der App, zum Beispiel:

- App wird deinstalliert und neu installiert
- App-Daten werden über die Android-Systemeinstellungen gelöscht

In diesen Fällen darf der Wizard wieder wie beim ersten Start angezeigt werden.

## Scope

### Import mit Alles ersetzen

- Importlogik für **Alles ersetzen** prüfen.
- Sicherstellen, dass das Löschen des Datenbestands innerhalb des Imports nicht als Konfigurationsreset oder Erststart interpretiert wird.
- Wizard-State vor dem Replace-Import erhalten und nach dem Import unverändert beibehalten.
- Bestehende Einstellungen für:
  - **Überspringen**
  - **Nicht mehr anzeigen**
  - erneute Anzeige über Einstellungen / Hilfe
  dürfen durch den Import nicht unbeabsichtigt geändert werden.
- Importierte Konfiguration darf den Wizard-State nicht überschreiben, sofern das aktuelle Importformat keinen ausdrücklich dafür vorgesehenen Mechanismus besitzt.

### Abgrenzung zu echter Neuinstallation

- Verhalten beim ersten Start der App unverändert beibehalten.
- Verhalten nach Löschen der App-Daten über Android-Systemeinstellungen unverändert beibehalten.
- Verhalten nach Deinstallation und Neuinstallation unverändert beibehalten.
- Nur der Importvorgang mit **Alles ersetzen** darf den Wizard-State nicht zurücksetzen.

### Regression zu Issue 058

- Bestehende Wizard-Logik aus Issue 058 prüfen.
- Sicherstellen, dass die Anzeige nach echtem Konfigurationsreset weiterhin funktioniert, falls dieser bewusst durch den Benutzer ausgelöst wird.
- Sicherstellen, dass der Wizard weiterhin über **Einrichtungs-Assistent erneut anzeigen** in Einstellungen / Hilfe manuell geöffnet werden kann.

## Nicht im Scope

- Neues Import-/Export-Dateiformat
- Änderung der Wizard-Inhalte
- Änderung der Wizard-Navigation
- Änderung der Backup-Dateistruktur
- Änderung der Geräte-, WLAN-Profil- oder Action-Datenmodelle
- Cloud-Synchronisation
- GitHub-Issue, Branch, Pull Request oder Merge

## Architekturhinweise

- Bestehende Import-/Export-Architektur beibehalten.
- Bestehende `AppSettings`- und `AppSettingsRepository`-Struktur beibehalten.
- Wizard-State ist App-/Onboarding-Zustand und darf nicht unbeabsichtigt an Datenbank-Löschoperationen gekoppelt sein.
- Falls Replace-Import intern dieselbe Löschlogik wie ein Konfigurationsreset nutzt, muss die Ursache bzw. der Kontext der Löschoperation unterschieden werden.
- Keine neue externe Abhängigkeit einführen.
- Hilfe-, Info- und Tooltip-Texte nur ändern, falls sich sichtbares Verhalten oder Beschreibungen tatsächlich ändern.

## Akzeptanzkriterien

- [ ] Beim Import mit **Alles ersetzen** wird der Wizard nicht automatisch angezeigt, nur weil der Datenbestand gelöscht wurde.
- [ ] Ein zuvor mit **Nicht mehr anzeigen** deaktivierter Wizard bleibt nach dem Replace-Import deaktiviert.
- [ ] Ein zuvor nur mit **Überspringen** geschlossener Wizard wird durch den Replace-Import nicht dauerhaft deaktiviert oder anderweitig verändert.
- [ ] Der Wizard-State bleibt beim Replace-Import vor und nach der Aktion identisch.
- [ ] Import ohne **Alles ersetzen** verändert den Wizard-State ebenfalls nicht.
- [ ] Manuelles Öffnen über **Einrichtungs-Assistent erneut anzeigen** funktioniert weiterhin.
- [ ] Echter erster App-Start zeigt den Wizard weiterhin an.
- [ ] Nach Löschen der App-Daten über Android-Systemeinstellungen darf der Wizard wieder wie beim ersten Start erscheinen.
- [ ] Nach Deinstallation und Neuinstallation darf der Wizard wieder wie beim ersten Start erscheinen.
- [ ] Bestehende Tests bzw. neue Regressionstests decken den Replace-Import ohne Wizard-State-Änderung ab.
