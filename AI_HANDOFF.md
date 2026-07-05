# AI Handoff

Stand: 5. Juli 2026

## Aktueller Stand

- Issue 081 "Homescreen Widgets" auf Branch `homescreen-widgets` implementiert,
  per Unit-Tests und Lint geprüft sowie iterativ als Release auf dem Gerät
  installiert und manuell getestet. Veröffentlichung und Merge stehen an.
- Architektur: klassische Android AppWidget/RemoteViews plus Compose-
  Konfigurations-Activity.
- Widget-Zuordnungen liegen launcher-lokal in SharedPreferences und sind
  bewusst nicht Teil von Konfigurationsimport/-export, weil AppWidget-IDs
  gerätelokal sind.
- Widget-Klicks öffnen SwitchWerk nicht sichtbar, sondern starten einen nicht
  exportierten lokalen Foreground-Service und delegieren anschließend an
  `DeviceActionService` beziehungsweise `SwitchGroupActionService`.
- Die Widget-Auswahl stellt eigene Varianten für `1x1`, `1x2` und `2x1` mit
  passenden Android-Zellmaßen bereit.
- Die Widget-Zuordnung kann bei kompatiblen Launchern über die native
  Bearbeiten-Aktion nach langem Drücken erneut geöffnet und erweitert werden.
- Die Render-Kapazität nutzt die aktuelle AppWidget-Größe, damit nach einer
  Vergrößerung zusätzlich ausgewählte Aktionen angezeigt werden.
- Widgets können einen optionalen freien Titel speichern. Bei leerem Feld wird
  kein Titel gerendert und die Aktionsfläche nutzt den Platz. Das Refresh-Symbol
  im Titelfeld stellt den Standardtitel `SwitchWerk` wieder her; bei neuen
  Widgets ist dieser Titel vorausgefüllt.
- Pro Widget kann die Button-Anordnung automatisch, einspaltig oder zweispaltig
  gewählt werden. Eine Spalte stellt mehrere Aktionen in ausreichend hohen
  Widgets als breite Buttons untereinander dar.
- Die Widget-Konfiguration zeigt ausgewählten Aktionen ihre laufende Nummer
  statt eines Hakens; sie entspricht der gespeicherten Widget-Reihenfolge.
- Alle Widget-Provider sind als `reconfigurable` markiert. Kompatible Launcher
  bieten dadurch nach langem Drücken eine native Bearbeiten-/Einstellungsaktion;
  im Widget selbst wird kein Zahnrad mehr angezeigt. Die Widget-Konfiguration
  verwendet die normale Android-Task-Zuordnung ohne Sonderflags. Speichern und
  Abbrechen beenden immer den Android-Konfigurationsvertrag per Activity-Ergebnis.
  Kein expliziter HOME-Wechsel: Dieser verhindert auf manchen Launchern die
  Fertigstellung und Skalierbarkeit des Widgets. Ein verbleibendes Launcher-
  Kontextmenü kann die App nicht zuverlässig schließen.
- Physische `1x1`-Widgets blenden den Titel aus und verwenden die frei werdende
  Höhe für den einzelnen Aktionsbutton. Halbe Launcher-Zellen sind nicht
  vorgesehen; das Rendering passt sich nur an die tatsächlich gemeldete Größe an.
- Ab Android 12 (`drawable-v31`) verwenden Außenrahmen und Aktionsbuttons
  `system_app_widget_background_radius` beziehungsweise
  `system_app_widget_inner_radius`; ältere Versionen nutzen feste Fallbacks.
- Aktionsbuttons verwenden je `2dp` oberen und unteren Rand statt ausschließlich
  eines unteren Randes. So bleiben `4dp` Abstand zwischen Buttons erhalten und
  der titellose Aktionsbereich sitzt vertikal symmetrisch im Widget-Rahmen.
- Widget-Aktionsflächen übernehmen die Dashboard-nahe WLAN-Statusfarbe
  grün/grau/rot. Erfolg oder Fehler einer Widget-Aktion wird vier Sekunden in
  einem helleren Grün beziehungsweise Rot hervorgehoben und anschließend wieder
  auf den aktuellen Status gesetzt.
- PendingIntents enthalten nur Widget-ID und Eintragsindex; Ziel-IDs, Labels,
  Hosts, SSIDs und Zugangsdaten werden nicht in PendingIntent-Extras abgelegt.
- Leere Schaltgruppen werden in der Widget-Konfiguration nicht angeboten;
  gelöschte oder später geleerte Ziele erscheinen im Widget als nicht verfügbar.

## Prüfungen

- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`

## Noch offen

- Nach Abschluss von Issue 081 ein separates Planungs-Issue für eine gemeinsame
  Aktionshistorie erstellen: Widget-Aktionen schreiben aktuell nicht in die
  Dashboard-Aktionsdetails; künftig sollen Dashboard, Widgets, Shortcuts und
  Intents denselben sicheren Detail-Store verwenden.
- Dem Benutzer künftig passende schnelle Prüfbefehle nennen, nicht automatisch
  immer den vollständigen Block mit `clean`; `clean` nur für finale
  Veröffentlichung oder unklare Build-Probleme empfehlen.
- Der Benutzer testet standardmäßig Release. Nach Code-, UI-, Android- oder
  Widget-Änderungen automatisch `./gradlew assembleRelease` und
  `./gradlew installRelease` als schnellen Host-Test ausgeben; Debug nur auf
  ausdrücklichen Wunsch oder wenn Release technisch ungeeignet ist.
- Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
  GitHub-Issue schließen oder Branch löschen.
