package com.campushub.model;

// Owner: Database and Data
// TODO: implement AlgorithmRun
public class AlgorithmRun {
    private int runId;
    private String algorithmName;
    private int inputSize;
    private int timeNs;
    private int memoryKb;
    private String dateRun;

    public AlgorithmRun(int runId, String algorithmName, int inputSize,
                        int timeNs, int memoryKb, String dateRun) {
        this.runId = runId;
        this.algorithmName = algorithmName;
        this.inputSize = inputSize;
        this.timeNs = timeNs;
        this.memoryKb = memoryKb;
        this.dateRun = dateRun;
    }

    public int getRunId() {
        return runId;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getTimeNs() {
        return timeNs;
    }

    public int getMemoryKb() {
        return memoryKb;
    }

    public String getDateRun() {
        return dateRun;
    }

    @Override
    public String toString() {
        return "AlgorithmRun{" +
                "runId=" + runId +
                ", algorithmName='" + algorithmName + '\'' +
                ", inputSize=" + inputSize +
                ", timeNs=" + timeNs +
                ", memoryKb=" + memoryKb +
                ", dateRun='" + dateRun + '\'' +
                '}';
    }
}
