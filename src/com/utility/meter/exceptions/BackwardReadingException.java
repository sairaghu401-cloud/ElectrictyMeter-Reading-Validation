package com.utility.meter.exceptions;

/**
 * Thrown when a new reading is LESS than the previous reading and the drop
 * cannot be explained by a legitimate odometer-style rollover.
 * This usually means a typo, a swapped meter, or tampering.
 */
public class BackwardReadingException extends MeterValidationException {

    private final long previous;
    private final long current;

    public BackwardReadingException(String meterId, long previous, long current) {
        super(meterId, String.format(
                "Reading went backwards on meter %s: previous=%d, new=%d (drop of %d).",
                meterId, previous, current, previous - current));
        this.previous = previous;
        this.current = current;
    }

    public long getPrevious() {
        return previous;
    }

    public long getCurrent() {
        return current;
    }

    @Override
    public String category() {
        return "BACKWARD_READING";
    }
}
