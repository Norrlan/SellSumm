package com.example.sellsumm;

import java.util.ArrayList;
import java.util.List;

public class MakeSaleModel {

    public static class DraftTransaction
    {
        private String id;
        private int totalUnits;
        private double totalAmount;
        private boolean hasAddon;
        private List<String> items;

        public DraftTransaction() {}

        public DraftTransaction(String id, int totalUnits, double totalAmount, boolean hasAddon)
        {
            this.id = id;
            this.totalUnits = totalUnits;
            this.totalAmount = totalAmount;
            this.hasAddon = hasAddon;
            this.items = new ArrayList<>();
        }

        public String getId() { return id; }
        public int getTotalUnits() { return totalUnits; }
        public double getTotalAmount() { return totalAmount; }
        public boolean isHasAddon() { return hasAddon; }
        public List<String> getItems() {return items;}
    }
}
