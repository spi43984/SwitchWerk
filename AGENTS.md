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

### Phase 1: Vorbereitung und Implementierung

Für jedes fachliche Issue gilt zunächst:

1. Auf `main` wechseln.
2. Aktuellen Stand holen.
3. Aus der passenden Datei unter `docs/issues/*.md` ein GitHub-Issue erzeugen.
4. Danach einen eigenen Branch mit fachlichem Namen anlegen, z. B. `wifi-connection-service`.
5. Ausschließlich den vereinbarten Issue-Scope implementieren.
6. Änderungen und Diff prüfen.
7. Verfügbare statische Prüfungen und Tests ausführen, die keine Android-SDK
   benötigen.
8. Vollständige Copy-&-Paste-Befehle für Build, Installation und manuelle Tests
   auf dem Host ausgeben.

Der Codex-Container enthält keine Android-SDK. Deshalb werden insbesondere
folgende Befehle vom Benutzer lokal auf dem Host ausgeführt:

    ./gradlew clean assembleDebug
    ./gradlew installDebug

Codex meldet den Build nicht als erfolgreich, solange der Benutzer kein
erfolgreiches Ergebnis vom Host zurückgemeldet hat.

### Phase 2: Veröffentlichung und Abschluss

Erst wenn die Implementierung geprüft wurde und der Benutzer ausdrücklich die
Veröffentlichung oder den Abschluss anfordert, dürfen folgende Schritte
ausgeführt werden:

1. Änderungen committen.
2. Feature-Branch pushen.
3. Pull Request erstellen.
4. Pull Request prüfen und nach ausdrücklicher Freigabe nach `main` mergen.
5. Lokale Issue-Datei abhaken.
6. Zugehöriges GitHub-Issue schließen.
7. Feature-Branch lokal und remote löschen.

Ohne ausdrückliche Nachfrage des Benutzers gilt:

- nicht committen
- nicht pushen
- keinen Pull Request erstellen
- nicht mergen
- kein GitHub-Issue schließen
- keinen Branch löschen

Beispiel:

    git switch main
    git pull
    gh issue create --title "WiFi Connection Service" --body-file docs/issues/009-wifi-connection-service.md
    git switch -c wifi-connection-service

Der Assistent gibt für alle vom Benutzer lokal auszuführenden Schritte immer die
vollständigen Copy-&-Paste-Befehle aus.

## AI-Handoff

Die Datei `AI_HANDOFF.md` wird immer direkt im Hauptverzeichnis des
Repositories abgelegt und aktualisiert.
