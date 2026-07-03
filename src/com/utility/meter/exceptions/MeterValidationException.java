package com.utility.meter.exceptions;

/**
 * Base checked exception for all meter reading validation failures.
 * Every specific validation problem (backward reading, implausible jump,
 * suspected fault) is a subclass, so callers can catch this one type to
 * route ANY invalid reading to the inspection queue.
 */
public class MeterValidationException extends Exception {

    private final String meterId;

    public MeterValidationException(String meterId, String message) {
        super(message);
        this.meterId = meterId;
    }

    public String getMeterId() {
        return meterId;
    }

    /**
     * Short, human-readable category used in logs and the inspection queue.
     * Overridden by each subclass.
     */
    public String category() {
        return "VALIDATION";
    }
}
