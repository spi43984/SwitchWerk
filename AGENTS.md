# AGENTS.md

## Ziel des Projekts

Diese Android-App steuert Shelly und andere lokale Geräte über HTTP/API-Aufrufe.

Die App soll einfach, wartbar, sicher und datenschutzfreundlich bleiben.

## Grundregeln für KI-Agenten

Beim Erstellen oder Ändern von Code:

1. Kotlin verwenden.
2. Jetpack Compose verwenden.
3. MVVM verwenden.
4. Koin für Dependency Injection verwenden.
5. Coroutines und Flow verwenden.
6. Keine unnötige Komplexität einführen.
7. Keine Cloud-Abhängigkeit einbauen, außer ausdrücklich gewünscht.
8. Keine sensiblen Daten loggen.
9. Netzwerkfehler sauber behandeln.
10. Code einfach und verständlich halten.

## Bevorzugte Bibliotheken

- Jetpack Compose
- Material Design 3
- Koin
- Kotlin Coroutines
- StateFlow
- OkHttp oder Retrofit
- Room nur falls lokale Speicherung nötig ist

## Nicht verwenden ohne Rückfrage

- Firebase
- Analytics SDKs
- Tracking SDKs
- Werbung
- Cloud-Synchronisation
- Account-Systeme
- Hintergrund-Standortzugriff

## Entwicklungsziel

Lieber eine kleine, stabile App als eine überarchitektierte App.

## UI-Regel für Sicherheitsabfragen

Bei Sicherheitsabfragen steht die sichere Abbruchaktion immer rechts.

Beispiel:
- links: Ja / Löschen / Bestätigen
- rechts: Nein / Abbrechen

Damit ist „Nein“ bei Lösch- oder Sicherheitsabfragen immer auf der rechten Seite.


## Branch- und Issue-Workflow

Implementierungen und größere Änderungen werden nie direkt auf `main` begonnen.

Für jedes fachliche Issue gilt:

1. Auf `main` wechseln.
2. Aktuellen Stand holen.
3. Aus der passenden Datei unter `docs/issues/*.md` ein GitHub-Issue erzeugen.
4. Danach einen eigenen Branch mit fachlichem Namen anlegen, z. B. `wifi-connection-service`.
5. Implementieren, bauen und testen.
6. Branch pushen und Pull Request erstellen.
7. PR prüfen und nach `main` mergen.
8. Lokale Issue-Datei abhaken.
9. Zugehöriges GitHub-Issue schließen.
10. Feature-Branch lokal und remote löschen.

Beispiel:

    git switch main
    git pull
    gh issue create --title "WiFi Connection Service" --body-file docs/issues/009-wifi-connection-service.md
    git switch -c wifi-connection-service

Der Assistent gibt dafür immer die vollständigen Copy-&-Paste-Befehle aus.
