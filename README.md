# 50Marktphasen - Projekt-Dokumentation

## 1. Projektübersicht
Das Projekt **50Marktphasen** ist eine JavaFX-Desktop-Anwendung zur Analyse und Visualisierung von Marktphasen für 50 spezifische Währungspaare (Forex).
Das Ziel der Anwendung ist es, Marktphasen-Berichte (CSV) einzulesen, visuell aufzubereiten (Signale, Trends) und eine vereinfachte Signalliste für die Weiterverarbeitung zu exportieren.

## 2. Features
*   **Dynamischer Datei-Import**: Liest automatisch die *neueste* CSV-Datei aus einem konfigurierbaren Input-Verzeichnis.
*   **Visuelle Aufbereitung**:
    *   Klare Darstellung von Währungspaar, Marktphase, letztem Signal und Erklärungstext.
    *   Farbliche Kodierung: **Grün** für BUY/Bullish, **Rot** für SELL/Bearish, **Grau** für Neutral.
    *   Verwendung von Symbolen (▲ / ▼) zur schnellen Erfassung der Marktrichtung.
*   **Daten-Normalisierung**: Vereinheitlichte Darstellung von "Bullish" -> "BUY" und "Bearish" -> "SELL".
*   **Export-Funktion**: Generiert eine standardisierte Datei `last_known_signals.csv` im Format `Waehrungspaar;Letztes_Signal` auf Knopfdruck.
*   **Dark Mode**: Modernes, augenschonendes User Interface.
*   **Persistenz**: Speichert die zuletzt genutzten Input- und Output-Verzeichnisse automatisch.

## 3. Technische Architektur
Das Projekt basiert auf folgenden Technologien:

*   **Sprache**: Java 21
*   **UI-Framework**: JavaFX 21 (FXML für Layouts, CSS für Styling)
*   **Build-System**: Maven
*   **Bibliotheken**:
    *   `opencsv` (v5.9): Zum robusten Einlesen und Parsen der CSV-Dateien.
    *   `gson`: (Optional, vorbereitet für komplexe Konfigurationen).

### Projektstruktur
*   `src/main/java/com/marketphase`:
    *   `AppLauncher.java`: Einstiegspunkt (Workaround für Eclipse/JavaFX Runtime Checks).
    *   `Main.java`: Lädt die JavaFX-Applikation und CSS.
    *   `AnalyzerController.java`: Enthält die Logik für UI-Interaktionen, Datenbindung und File-Handling.
    *   `model/MarketSignal.java`: Datenmodell, das die CSV-Spalten (`Pair`, `Market_Phase`, `Last_Signal`, `Date`, `Source_Summary`) repräsentiert.
    *   `service/FileService.java`: Hilfsklasse zum Finden der neuesten Datei und zum Lesen/Schreiben von CSVs.
*   `src/main/resources/com/marketphase`:
    *   `ui.fxml`: Definition der Benutzeroberfläche.
    *   `styles.css`: Dark-Theme Styling.

## 4. Installation & Einrichtung (Eclipse)

Da JavaFX in neueren Java-Versionen nicht mehr teil des JDK ist, muss das Projekt als Maven-Projekt importiert werden, damit die Abhängigkeiten korrekt geladen werden.

1.  **Eclipse öffnen**.
2.  `File` -> `Import...` -> `Maven` -> `Existing Maven Projects`.
3.  Root Directory: `d:\AntiGravitySoftware\GitWorkspace\50Marktphasen` auswählen.
4.  Sicherstellen, dass `pom.xml` angehakt ist -> `Finish`.
5.  Rechtsklick auf das Projekt -> `Maven` -> `Update Project...`, um alle Libraries herunterzuladen.

## 5. Anwendung Starten

**WICHTIG**: Aufgrund einer Besonderheit, wie Eclipse JavaFX-Applikationen startet, darf **nicht** die `Main.java` direkt gestartet werden (dies führt zum Fehler "JavaFX Runtime Components missing").

**Korrekter Start:**
1.  Navigiere zu `src/main/java` -> `com.marketphase`.
2.  Rechtsklick auf **`AppLauncher.java`**.
3.  Wähle `Run As` -> `Java Application`.

## 6. Bedienungsanleitung

1.  **Konfiguration**:
    *   Wähle oben das **Input Dir** aus (Ordner mit den Analyse-CSVs).
    *   Wähle das **Output Dir** aus (Zielordner für den Export).
2.  **Daten laden**:
    *   Klicke auf **"Load Latest Data"**.
    *   Die Anwendung sucht die Datei mit dem neuesten Änderungsdatum im Input-Ordner und zeigt sie in der Tabelle an.
3.  **Analyse**:
    *   Die Tabelle zeigt nun alle 50 Währungspaare.
    *   Mousover oder Scrollen in der "Explanation"-Spalte zeigt Details zur Analysequelle an.
4.  **Export**:
    *   Klicke unten auf **"Export last_known_signals.csv"**.
    *   Eine Datei wird im Output-Ordner erstellt (oder überschrieben).

## 7. Dateiformate

### Input (Erwartetes CSV Format)
Die Input-Datei muss kommagetrennt sein und folgende Header beinhalten:
`Pair,Market_Phase,Last_Signal,Date,Source_Summary`

### Output (Erstelltes Format)
Die Datei `last_known_signals.csv` ist semikolongetrennt:
`Waehrungspaar;Letztes_Signal`
Beispiel:
```csv
EUR/USD;BUY
USD/JPY;SELL
...
```
