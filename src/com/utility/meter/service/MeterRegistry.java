package com.utility.meter.service;

import com.utility.meter.model.Meter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory store of meters keyed by meter id. Keeps insertion order so the
 * terminal listing is stable.
 */
public class MeterRegistry {

    private final Map<String, Meter> meters = new LinkedHashMap<>();

    public void add(Meter meter) {
        meters.put(meter.getMeterId(), meter);
    }

    public Meter get(String meterId) {
        return meters.get(meterId);
    }

    public boolean contains(String meterId) {
        return meters.containsKey(meterId);
    }

    public Collection<Meter> all() {
        return meters.values();
    }
}
