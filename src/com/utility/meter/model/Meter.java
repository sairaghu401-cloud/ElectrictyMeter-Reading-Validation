package com.utility.meter.model;

/**
 * A physical meter installed for a consumer. Holds the last accepted reading,
 * the dial capacity (for odometer rollover), the consumer profile (which drives
 * the plausibility threshold), and a counter of how many consecutive cycles the
 * reading has not advanced (used to detect a stuck meter).
 */
public class Meter {

    private final String meterId;
    private final ConsumerProfile profile;
    private final long dialCapacity;   // e.g. 99_999 for a 5-digit mechanical dial
    private long lastReading;
    private int unchangedCycles;

    public Meter(String meterId, ConsumerProfile profile, long dialCapacity, long openingReading) {
        this.meterId = meterId;
        this.profile = profile;
        this.dialCapacity = dialCapacity;
        this.lastReading = openingReading;
        this.unchangedCycles = 0;
    }

    public String getMeterId() {
        return meterId;
    }

    public ConsumerProfile getProfile() {
        return profile;
    }

    public long getDialCapacity() {
        return dialCapacity;
    }

    public long getLastReading() {
        return lastReading;
    }

    public int getUnchangedCycles() {
        return unchangedCycles;
    }

    /** Called only after a reading has been validated (or overridden). */
    public void commitReading(long newReading, long consumption) {
        if (consumption == 0) {
            unchangedCycles++;
        } else {
            unchangedCycles = 0;
        }
        this.lastReading = newReading;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] last=%d cap=%d",
                meterId, profile.getLabel(), lastReading, dialCapacity);
    }
}
