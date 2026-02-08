package com.marketphase;

import com.marketphase.model.MarketSignal;
import com.marketphase.service.FileService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

public class AnalyzerController {

    @FXML
    private TextField inputDirField;
    @FXML
    private TextField outputDirField;
    @FXML
    private TableView<MarketSignal> signalTable;
    @FXML
    private TableColumn<MarketSignal, String> pairColumn;
    @FXML
    private TableColumn<MarketSignal, String> phaseColumn;
    @FXML
    private TableColumn<MarketSignal, String> signalColumn;
    @FXML
    private TableColumn<MarketSignal, String> summaryColumn;
    @FXML
    private Label statusLabel;

    private final FileService fileService = new FileService();
    private final Preferences prefs = Preferences.userNodeForPackage(AnalyzerController.class);

    @FXML
    public void initialize() {
        inputDirField.setText(prefs.get("inputDir", ""));
        outputDirField.setText(prefs.get("outputDir", ""));

        pairColumn.setCellValueFactory(new PropertyValueFactory<>("pair"));

        // ... (previous cell factories) ...

        summaryColumn.setCellValueFactory(new PropertyValueFactory<>("sourceSummary"));
        summaryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true);
                }
            }
        });

        phaseColumn.setCellValueFactory(new PropertyValueFactory<>("marketPhase"));
        phaseColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    if (item.equalsIgnoreCase("Bullish")) {
                        setText("BUY");
                        setStyle("-fx-text-fill: #00E676; -fx-font-weight: bold;"); // Green
                    } else if (item.equalsIgnoreCase("Bearish")) {
                        setText("SELL");
                        setStyle("-fx-text-fill: #FF5252; -fx-font-weight: bold;"); // Red
                    } else {
                        setText("NEUTRAL");
                        setStyle("-fx-text-fill: #B0BEC5;"); // Grey
                    }
                }
            }
        });

        signalColumn.setCellValueFactory(new PropertyValueFactory<>("lastSignal"));
        signalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    if (item.equalsIgnoreCase("Buy Signal")) {
                        setText("▲ BUY");
                        setStyle("-fx-text-fill: #00E676; -fx-font-weight: bold;");
                    } else if (item.equalsIgnoreCase("Sell Signal")) {
                        setText("▼ SELL");
                        setStyle("-fx-text-fill: #FF5252; -fx-font-weight: bold;");
                    } else {
                        setText("▶ " + item.toUpperCase().replace(" SIGNAL", ""));
                        setStyle("-fx-text-fill: #B0BEC5;");
                    }
                }
            }
        });
    }

    @FXML
    private void onBrowseInput() {
        File dir = chooseDirectory("Select Input Directory");
        if (dir != null) {
            inputDirField.setText(dir.getAbsolutePath());
            prefs.put("inputDir", dir.getAbsolutePath());
        }
    }

    @FXML
    private void onBrowseOutput() {
        File dir = chooseDirectory("Select Output Directory");
        if (dir != null) {
            outputDirField.setText(dir.getAbsolutePath());
            prefs.put("outputDir", dir.getAbsolutePath());
        }
    }

    @FXML
    private void onLoadData() {
        String inputPath = inputDirField.getText();
        if (inputPath.isEmpty()) {
            showError("Please select an input directory.");
            return;
        }

        try {
            File inputDir = new File(inputPath);
            File latestFile = fileService.findLatestFile(inputDir);
            if (latestFile == null) {
                showError("No files found in input directory.");
                return;
            }

            // Find the previous file to get historical signals
            File previousFile = fileService.findPreviousFile(inputDir);

            statusLabel.setText("Loaded: " + latestFile.getName() +
                    (previousFile != null ? " (Previous: " + previousFile.getName() + ")" : " (No previous file)"));

            // Read signals with history lookup
            List<MarketSignal> signals = fileService.readSignalsWithHistory(latestFile, previousFile);
            signalTable.setItems(FXCollections.observableArrayList(signals));

        } catch (IOException e) {
            showError("Error loading data: " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) { // OpenCSV can throw runtime exceptions
            showError("Error parsing CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onShowInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Über 50Marktphasen Analysator");
        alert.setContentText(
                "Der 50Marktphasen Analysator wertet ein input file aus was mit Grok erstellt wurde um daraus die Marktphasen zu generieren. Es wird ein last_known_signals.csv generiert und in einem bestimmten Verzeichnis abgelegt.");
        alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    @FXML
    private void onExport() {
        String outputPath = outputDirField.getText();
        if (outputPath.isEmpty()) {
            showError("Please select an output directory.");
            return;
        }

        if (signalTable.getItems().isEmpty()) {
            showError("No data to export. Please load data first.");
            return;
        }

        try {
            fileService.writeLastSignals(signalTable.getItems(), new File(outputPath));
            statusLabel.setText("Exported successfully to " + outputPath);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Export Successful!", ButtonType.OK);
            alert.showAndWait();
        } catch (IOException e) {
            showError("Error exporting data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private File chooseDirectory(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        return chooser.showDialog(new Stage());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}
