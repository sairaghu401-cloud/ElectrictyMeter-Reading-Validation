package com.utility.meter.service;

import com.utility.meter.exceptions.MeterValidationException;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;

/**
 * Holds readings that failed validation so a field crew can inspect them.
 * The validator routes every MeterValidationException here.
 */
public class InspectionQueue {

    /** One queued item: the meter, the category of problem, and the details. */
    public static class Item {
        private final String meterId;
        private final String category;
        private final String detail;

        Item(String meterId, String category, String detail) {
            this.meterId = meterId;
            this.category = category;
            this.detail = detail;
        }

        public String getMeterId() { return meterId; }
        public String getCategory() { return category; }
        public String getDetail() { return detail; }

        @Override
        public String toString() {
            return String.format("meter=%s  category=%s  -> %s", meterId, category, detail);
        }
    }

    private final Deque<Item> queue = new ArrayDeque<>();

    /** Route a failed validation to inspection. */
    public void route(MeterValidationException ex) {
        queue.addLast(new Item(ex.getMeterId(), ex.category(), ex.getMessage()));
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public List<Item> view() {
        return Collections.unmodifiableList(new ArrayList<>(queue));
    }

    /** Remove and return the next item to inspect, or null if empty. */
    public Item poll() {
        return queue.pollFirst();
    }
}
