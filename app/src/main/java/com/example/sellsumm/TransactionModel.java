package com.example.sellsumm;

public class TransactionModel
{

    private int transactionNumber;
    private int totalUnits;
    private double totalPrice;

    public TransactionModel() {}

    public TransactionModel(int transactionNumber, int totalUnits, double totalPrice) {
        this.transactionNumber = transactionNumber;
        this.totalUnits = totalUnits;
        this.totalPrice = totalPrice;
    }

    public int getTransactionNumber()
    {
        return transactionNumber;
    }
    public int getTotalUnits()
    {
        return totalUnits;
    }
    public double getTotalPrice()
    {
        return totalPrice;
    }
}

