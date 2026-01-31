package com.marketphase.service;

import com.marketphase.model.MarketSignal;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FileService {

    public File findLatestFile(File directory) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Invalid directory: " + directory.getAbsolutePath());
        }

        try (Stream<java.nio.file.Path> stream = Files.list(directory.toPath())) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(java.nio.file.Path::toFile)
                    .max(Comparator.comparingLong(File::lastModified))
                    .orElse(null);
        }
    }

    public List<MarketSignal> readSignals(File file) throws IOException {
        return new CsvToBeanBuilder<MarketSignal>(new FileReader(file))
                .withType(MarketSignal.class)
                .build()
                .parse();
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
                if (signal.getLastSignal().equalsIgnoreCase("Buy Signal")) {
                    outputSignal = "BUY";
                } else if (signal.getLastSignal().equalsIgnoreCase("Sell Signal")) {
                    outputSignal = "SELL";
                } else {
                    outputSignal = "NEUTRAL";
                }
                writer.write(signal.getPair() + ";" + outputSignal + "\n");
            }
        }
    }
}
