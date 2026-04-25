package com.example.sellsumm;

public class AnalyticKPIModel
{
    public String title;
    public String value;
    public int progress;
    public int color;

    public AnalyticKPIModel(String title, String value, int progress, int color)
    {
        this.title = title;
        this.value = value;
        this.progress = progress;
        this.color = color;
    }
}
