package com.scc.model;

public record Item(int id, String itemName, String category, String description, String location, String status, int reportedBy, String timestamp) {
    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s at %s [Status: %s]", id, itemName, category, description, location, status);
    }
}