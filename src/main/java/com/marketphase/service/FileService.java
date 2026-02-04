package com.marketphase.service;

import com.marketphase.model.MarketSignal;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileService {

    // Pattern for simple CSV: Pair,Sentiment,Date,"Details"
    private static final Pattern SIMPLE_CSV_PATTERN = Pattern.compile(
            "([^,]+),([^,]+),([^,]+),\"?([^\"]*)\"?");

    public File findLatestFile(File directory) throws IOException {
        List<File> files = getSortedFiles(directory);
        return files.isEmpty() ? null : files.get(0);
    }

    /**
     * Find the second-newest file in the directory (for previous signals)
     */
    public File findPreviousFile(File directory) throws IOException {
        List<File> files = getSortedFiles(directory);
        return files.size() < 2 ? null : files.get(1);
    }

    /**
     * Get all files sorted by modification time (newest first)
     */
    private List<File> getSortedFiles(File directory) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Invalid directory: " + directory.getAbsolutePath());
        }

        try (Stream<java.nio.file.Path> stream = Files.list(directory.toPath())) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(java.nio.file.Path::toFile)
                    .sorted(Comparator.comparingLong(File::lastModified).reversed())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Read signals from current file and populate Last Signal from previous file
     */
    public List<MarketSignal> readSignalsWithHistory(File currentFile, File previousFile) throws IOException {
        List<MarketSignal> currentSignals = readSignals(currentFile);

        if (previousFile == null || !previousFile.exists()) {
            // No previous file - Last Signal remains empty or same as current
            return currentSignals;
        }

        // Read previous signals and create a lookup map
        List<MarketSignal> previousSignals = readSignals(previousFile);
        Map<String, String> previousSignalMap = new HashMap<>();

        for (MarketSignal prev : previousSignals) {
            String pair = normalizePair(prev.getPair());
            String signal = getSignalString(prev);
            previousSignalMap.put(pair, signal);
        }

        // Update Last Signal for each current signal
        for (MarketSignal current : currentSignals) {
            String pair = normalizePair(current.getPair());
            String previousSig = previousSignalMap.get(pair);

            if (previousSig != null) {
                current.setLastSignal(previousSig);
            } else {
                // No previous signal found - set to "N/A" or keep current
                current.setLastSignal("N/A");
            }
        }

        return currentSignals;
    }

    /**
     * Normalize pair names for comparison (remove spaces, handle different formats)
     */
    private String normalizePair(String pair) {
        if (pair == null)
            return "";
        return pair.trim().toUpperCase().replace(" ", "");
    }

    /**
     * Get the signal string (Buy Signal / Sell Signal / Neutral Signal) from a
     * MarketSignal
     */
    private String getSignalString(MarketSignal signal) {
        String phase = signal.getMarketPhase();
        if (phase != null) {
            if (phase.equalsIgnoreCase("Bullish")) {
                return "Buy Signal";
            } else if (phase.equalsIgnoreCase("Bearish")) {
                return "Sell Signal";
            }
        }

        String lastSig = signal.getLastSignal();
        if (lastSig != null && !lastSig.isEmpty()) {
            return lastSig;
        }

        return "Neutral Signal";
    }

    public List<MarketSignal> readSignals(File file) throws IOException {
        // First, check if file has the expected CSV headers
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String firstLine = br.readLine();
            if (firstLine != null) {
                String lower = firstLine.toLowerCase();
                // Check for standard headers
                if (lower.contains("pair") && lower.contains("market_phase")) {
                    // Use OpenCSV for standard format
                    return new CsvToBeanBuilder<MarketSignal>(new FileReader(file))
                            .withType(MarketSignal.class)
                            .build()
                            .parse();
                }
            }
        }

        // Fallback: Parse simple CSV format (Pair,Sentiment,Date,"Details")
        return readSimpleFormat(file);
    }

    /**
     * Read simple CSV format: Pair,Sentiment,Date,"Details"
     * Maps: Bullish->Buy Signal, Bearish->Sell Signal, Neutral->Neutral Signal
     */
    private List<MarketSignal> readSimpleFormat(File file) throws IOException {
        List<MarketSignal> signals = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                // Skip header line if present
                if (lineNum == 1 && line.toLowerCase().contains("pair")) {
                    continue;
                }

                MarketSignal signal = parseSimpleLine(line);
                if (signal != null) {
                    signals.add(signal);
                }
            }
        }

        return signals;
    }

    private MarketSignal parseSimpleLine(String line) {
        Matcher m = SIMPLE_CSV_PATTERN.matcher(line);
        if (!m.matches()) {
            System.err.println("Could not parse line: " + line);
            return null;
        }

        String pair = m.group(1).trim();
        String sentiment = m.group(2).trim();
        String date = m.group(3).trim();
        String details = m.group(4).trim();

        MarketSignal signal = new MarketSignal();
        signal.setPair(pair);
        signal.setDate(date);
        signal.setSourceSummary(details);

        // Map sentiment to market phase
        signal.setMarketPhase(sentiment); // Keep Bullish/Bearish/Neutral

        // Initially set Last Signal to empty - will be filled from previous file
        signal.setLastSignal("");

        return signal;
    }

    public void writeLastSignals(List<MarketSignal> signals, File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, "last_known_signals.csv");

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("Waehrungspaar;Letztes_Signal\n");
            for (MarketSignal signal : signals) {
                String outputSignal;
                String phase = signal.getMarketPhase();
                String lastSig = signal.getLastSignal();

                // Check marketPhase first (Bullish/Bearish/Neutral)
                if (phase != null && phase.equalsIgnoreCase("Bullish")) {
                    outputSignal = "BUY";
                } else if (phase != null && phase.equalsIgnoreCase("Bearish")) {
                    outputSignal = "SELL";
                } else if (lastSig != null && lastSig.equalsIgnoreCase("Buy Signal")) {
                    outputSignal = "BUY";
                } else if (lastSig != null && lastSig.equalsIgnoreCase("Sell Signal")) {
                    outputSignal = "SELL";
                } else {
                    outputSignal = "NEUTRAL";
                }
                writer.write(signal.getPair() + ";" + outputSignal + "\n");
            }
        }
    }
}
