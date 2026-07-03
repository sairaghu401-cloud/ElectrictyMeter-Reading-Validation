package com.utility.meter.exceptions;

/**
 * Thrown when the computed consumption for a billing cycle exceeds the
 * plausibility threshold configured for the consumer's profile.
 * A huge but forward jump often indicates a misread digit or a data-entry slip.
 */
public class ImplausibleConsumptionException extends MeterValidationException {

    private final long consumption;
    private final long threshold;

    public ImplausibleConsumptionException(String meterId, long consumption, long threshold) {
        super(meterId, String.format(
                "Implausible consumption on meter %s: %d units exceeds threshold of %d units.",
                meterId, consumption, threshold));
        this.consumption = consumption;
        this.threshold = threshold;
    }

    public long getConsumption() {
        return consumption;
    }

    public long getThreshold() {
        return threshold;
    }

    @Override
    public String category() {
        return "IMPLAUSIBLE_CONSUMPTION";
    }
}
