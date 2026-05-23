package com.example.scheduler.model;

import java.util.List;
import java.util.Map;

public class ScheduleResult {
    private String algorithm;
    private Map<String, Integer> schedule; // Course ID -> Slot
    private int numSlots;
    private long executionTimeMs;
    private double fitnessScore;
    private List<String> logs; // For step-by-step animation
    private String complexity;
    private String spaceComplexity;
    private String comparativeAnalysis;
    private String efficiency;
    private Object extraData;

    public ScheduleResult() {}

    public ScheduleResult(String algorithm, Map<String, Integer> schedule, int numSlots, long executionTimeMs, double fitnessScore, List<String> logs, Object extraData) {
        this.algorithm = algorithm;
        this.schedule = schedule;
        this.numSlots = numSlots;
        this.executionTimeMs = executionTimeMs;
        this.fitnessScore = fitnessScore;
        this.logs = logs;
        this.extraData = extraData;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Map<String, Integer> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<String, Integer> schedule) {
        this.schedule = schedule;
    }

    public int getNumSlots() {
        return numSlots;
    }

    public void setNumSlots(int numSlots) {
        this.numSlots = numSlots;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public double getFitnessScore() {
        return fitnessScore;
    }

    public void setFitnessScore(double fitnessScore) {
        this.fitnessScore = fitnessScore;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public Object getExtraData() {
        return extraData;
    }

    public void setExtraData(Object extraData) {
        this.extraData = extraData;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public void setSpaceComplexity(String spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
    }

    public String getComparativeAnalysis() {
        return comparativeAnalysis;
    }

    public void setComparativeAnalysis(String comparativeAnalysis) {
        this.comparativeAnalysis = comparativeAnalysis;
    }

    public String getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(String efficiency) {
        this.efficiency = efficiency;
    }
}
