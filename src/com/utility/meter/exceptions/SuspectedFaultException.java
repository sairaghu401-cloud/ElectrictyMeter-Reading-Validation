package com.utility.meter.exceptions;

/**
 * Thrown when a reading pattern suggests the meter itself is faulty or tampered,
 * rather than a simple keying error. Examples: identical reading repeated across
 * many cycles (stuck meter), or a reading that is negative / out of physical range.
 * These are flagged for a field inspection rather than silently corrected.
 */
public class SuspectedFaultException extends MeterValidationException {

    /** Distinguishes a hardware fault from deliberate interference. */
    public enum FaultKind {
        STUCK_METER,        // reading has not advanced for too many cycles
        OUT_OF_RANGE,       // reading below zero or above the meter's dial capacity
        SUSPECTED_TAMPER    // pattern consistent with interference
    }

    private final FaultKind kind;

    public SuspectedFaultException(String meterId, FaultKind kind, String detail) {
        super(meterId, String.format("Suspected fault on meter %s [%s]: %s",
                meterId, kind, detail));
        this.kind = kind;
    }

    public FaultKind getKind() {
        return kind;
    }

    @Override
    public String category() {
        return "SUSPECTED_FAULT/" + kind;
    }
}
