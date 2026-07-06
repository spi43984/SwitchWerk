# Issue 084: Gruppenmitglied-Swipe dialogweit schließen

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: Bugfix
- Bereich: Schaltgruppen / Einstellungen / Bedienung

## Ziel

Im Dialog `Gruppe bearbeiten` soll ein geöffneter Lösch-Swipe eines
Gruppenmitglieds durch Antippen eines beliebigen Bereichs im Gruppenformular
zurückgesetzt werden. Das Verhalten soll dem Dialog `Gerät bearbeiten` mit
seinen WLAN-Zuordnungen entsprechen.

## Hintergrund

Der Swipe-Zustand der Gruppenmitglieder wird derzeit innerhalb der
Mitgliederliste verwaltet. Deshalb schließen nur Berührungen innerhalb dieser
Liste einen geöffneten Swipe. Berührungen oberhalb der Liste, beispielsweise im
Namensfeld oder bei den Gruppeneinstellungen, lassen ihn geöffnet.

## Scope

- Zustand des geöffneten Mitglieder-Swipes auf Formularebene verwalten.
- Einen kurzen Tap im gesamten Gruppenformular zum Schließen verwenden.
- Scroll- und Swipe-Gesten nicht als Tap behandeln.
- Löschen, Sortieren und Pausenkonfiguration der Gruppenmitglieder unverändert
  lassen.
- Das bestehende Verhalten im Dialog `Gerät bearbeiten` als Referenz nutzen.

## Nicht im Scope

- Keine Änderung gespeicherter Gruppendaten.
- Keine Änderung an Konfigurationsimport oder -export.
- Keine neuen Einstellungen.
- Keine Änderung der Gruppenausführung.
- Keine neuen Hilfe-, Info- oder Tooltip-Texte, da nur bestehendes
  Interaktionsverhalten vereinheitlicht wird.

## Akzeptanzkriterien

- [ ] Ein geöffneter Mitglieder-Swipe wird durch Antippen oberhalb der
      Mitgliederliste geschlossen.
- [ ] Ein geöffneter Mitglieder-Swipe wird weiterhin durch Antippen innerhalb
      der Mitgliederliste geschlossen.
- [ ] Vertikales Scrollen und horizontales Swipen werden nicht als Tap zum
      Schließen fehlinterpretiert.
- [ ] Die Löschaktion bleibt von links und rechts erreichbar.
- [ ] Das Verhalten entspricht den WLAN-Zuordnungen in `Gerät bearbeiten`.

## Testhinweise

- Eine Gruppe mit mindestens zwei Mitgliedern bearbeiten.
- Ein Mitglied nach links und nach rechts swipen und jeweils oberhalb der Liste
  tippen.
- Einen Swipe durch Tippen auf einen anderen Eintrag schließen.
- Mitgliederliste vertikal scrollen und prüfen, dass keine unbeabsichtigte
  Aktion ausgelöst wird.
- Mitglied löschen, verschieben und seine Pause ändern.

