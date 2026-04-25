package com.example.sellsumm;

public class KPIModel
{
    public String name;
    public double target;
    public double actual;
    public String status;

    public KPIModel() {}

    public KPIModel(String name, double target, double actual, String status)
    {
        this.name = name;
        this.target = target;
        this.actual = actual;
        this.status = status;
    }

}
