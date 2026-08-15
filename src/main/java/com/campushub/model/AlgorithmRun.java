package com.campushub.model;

// Owner: Database and Data
public class AlgorithmRun {
    private int runId;
    private String algorithmName;
    private int inputSize;
    private long timeNs;       // long to avoid overflow on large inputs
    private long memoryKb;
    private String dateRun;

    public AlgorithmRun(int runId, String algorithmName, int inputSize,
                        long timeNs, long memoryKb, String dateRun) {
        this.runId         = runId;
        this.algorithmName = algorithmName;
        this.inputSize     = inputSize;
        this.timeNs        = timeNs;
        this.memoryKb      = memoryKb;
        this.dateRun       = dateRun;
    }

    public int    getRunId()          { return runId; }
    public String getAlgorithmName()  { return algorithmName; }
    public int    getInputSize()      { return inputSize; }
    public long   getTimeNs()         { return timeNs; }
    public long   getMemoryKb()       { return memoryKb; }
    public String getDateRun()        { return dateRun; }

    @Override
    public String toString() {
        return "AlgorithmRun{runId=" + runId
                + ", algorithmName='" + algorithmName + '\''
                + ", inputSize=" + inputSize
                + ", timeNs=" + timeNs
                + ", memoryKb=" + memoryKb
                + ", dateRun='" + dateRun + '\'' + '}';
    }
}
