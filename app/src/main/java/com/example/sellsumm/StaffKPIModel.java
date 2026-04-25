package com.example.sellsumm;

public class StaffKPIModel {

    private String name;
    private String description;
    private double targetValue;
    private double actualValue;
    private String status;

    private String frequency;

    public StaffKPIModel() {}

    public StaffKPIModel(String name, String description, double targetValue, double actualValue, String status, String frequency)
    {
        this.name = name;
        this.description = description;
        this.targetValue = targetValue;
        this.actualValue = actualValue;
        this.status = status;
        this.frequency = frequency;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getTargetValue() { return targetValue; }
    public double getActualValue() { return actualValue; }
    public String getStatus() { return status; }

    public String getFrequency() { return frequency; }
}
