package com.example.sellsumm;

public class KPITemplateModel
{

    private String id;
    private String name;
    private String description;
    private double targetValue;
    private String direction;
    private String frequency;

    public KPITemplateModel() {}

    public KPITemplateModel(String id, String name, String description, double targetValue, String direction, String frequency)
    {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.targetValue = targetValue;
        this.direction   = direction;
        this.frequency   = frequency;
    }

    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public double getTargetValue() { return targetValue; }
    public String getDirection()   { return direction; }
    public String getFrequency()   { return frequency; }

    public void setId(String id)                   { this.id = id; }
    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTargetValue(double targetValue)  { this.targetValue = targetValue; }
    public void setDirection(String direction)      { this.direction = direction; }
    public void setFrequency(String frequency)      { this.frequency = frequency; }
}