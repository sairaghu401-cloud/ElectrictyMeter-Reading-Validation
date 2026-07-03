package com.utility.meter.model;

/**
 * Outcome of a successful validation: the consumption billed for the cycle and
 * whether a legitimate odometer rollover was applied to reach it.
 */
public class ValidationResult {

    private final long consumption;
    private final boolean rolloverApplied;

    public ValidationResult(long consumption, boolean rolloverApplied) {
        this.consumption = consumption;
        this.rolloverApplied = rolloverApplied;
    }

    public long getConsumption() {
        return consumption;
    }

    public boolean isRolloverApplied() {
        return rolloverApplied;
    }
}
