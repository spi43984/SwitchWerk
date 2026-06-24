# Issue 051 iOS: WLAN Join Flow

## Metadaten

- Status: Backlog
- Priorität: P4
- Plattform: iOS
- iOS-Phase: 6 von 6
- Typ: WLAN / UX / Risiko

## Ziel

Den iOS-spezifischen Ablauf zum Verbinden mit Geräte-APs planen. Dieser Ablauf ist das größte technische Risiko der iOS-Version und darf keine nicht von iOS garantierte Automatisierung versprechen.

## Geplanter Ablauf

1. Der Benutzer startet eine Geräteaktion und erhält eine klare Erklärung des bevorstehenden WLAN-Wechsels.
2. Die App fordert über den technisch zulässigen iOS-Mechanismus die Verbindung zu SSID und gegebenenfalls Passwort an.
3. System-Prompts und Ablehnungen werden eindeutig in der UI behandelt.
4. Die Verbindung und die Erreichbarkeit des Geräts werden geprüft.
5. Der HTTP- oder RPC-Befehl wird nur nach der bestätigten, machbarkeitskonformen Verbindung gesendet.
6. Der Rückwechsel ins Heim-WLAN wird im Rahmen der iOS-Möglichkeiten unterstützt und dem Benutzer verständlich erklärt.

## Fehlerfälle

- WLAN-Passwort falsch oder AP nicht erreichbar.
- Benutzer lehnt System-Prompt oder Berechtigung ab.
- iOS bleibt auf Heim-WLAN oder bewertet den Geräte-AP ohne Internet als nicht nutzbar.
- Verbindung besteht, aber DNS, mDNS, Hostname oder HTTP/RPC-Aufruf schlägt fehl.
- Rückwechsel ins Heim-WLAN ist nicht automatisierbar oder schlägt fehl.
- Aktion wird abgebrochen oder die App wechselt in den Hintergrund.

## Sicherheits- und UX-Regeln

- SSID und Passwort werden nur für den erforderlichen Verbindungsablauf verwendet und nicht protokolliert.
- Der Nutzer bleibt über jeden erforderlichen Systemdialog und manuellen Schritt informiert.
- Bei Bestätigungs- oder Sicherheitsdialogen steht die sichere Abbruchaktion rechts.
- Keine stillen Wiederholungen potenziell nicht-idempotenter Gerätebefehle.

## Abhängigkeiten

- Issue 047 muss die technische Machbarkeit und die iOS-Einschränkungen festlegen.
- Issue 050 definiert die anschließende lokale HTTP/RPC-Ausführung.

## Nicht Bestandteil

- Zusage eines vollständig automatischen WLAN-Rückwechsels.
- Android-WLAN-Logik oder Android-Codeänderungen.
- Hintergrundüberwachung, Cloud-Dienste oder Standort-Tracking.

## Akzeptanzkriterien

- [ ] Der vollständige Nutzer- und Systemablauf ist einschließlich Prompts dokumentiert.
- [ ] Erfolg, Abbruch und alle relevanten Fehlerfälle haben verständliche Zustände und nächste Schritte.
- [ ] Die Grenzen für APs ohne Internet und den WLAN-Rückwechsel sind klar benannt.
- [ ] Keine nicht garantierte iOS-Automatisierung wird als Produktversprechen festgeschrieben.
- [ ] Das Risiko ist explizit als abhängig von Issue 047 festgehalten.
