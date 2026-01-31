package com.marketphase.model;

import com.opencsv.bean.CsvBindByName;

public class MarketSignal {

    @CsvBindByName(column = "Pair")
    private String pair;

    @CsvBindByName(column = "Market_Phase")
    private String marketPhase;

    @CsvBindByName(column = "Last_Signal")
    private String lastSignal;

    @CsvBindByName(column = "Date")
    private String date;

    @CsvBindByName(column = "Source_Summary")
    private String sourceSummary;

    // Getters and Setters

    public String getSourceSummary() {
        return sourceSummary;
    }

    public void setSourceSummary(String sourceSummary) {
        this.sourceSummary = sourceSummary;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getMarketPhase() {
        return marketPhase;
    }

    public void setMarketPhase(String marketPhase) {
        this.marketPhase = marketPhase;
    }

    public String getLastSignal() {
        return lastSignal;
    }

    public void setLastSignal(String lastSignal) {
        this.lastSignal = lastSignal;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
