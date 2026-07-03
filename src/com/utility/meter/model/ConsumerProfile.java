package com.utility.meter.model;

/**
 * Consumer category. Each profile carries its own plausibility threshold
 * (max believable units for one billing cycle) because a factory legitimately
 * draws far more than a household.
 */
public enum ConsumerProfile {

    RESIDENTIAL("Residential", 1_500),
    COMMERCIAL("Commercial", 20_000),
    INDUSTRIAL("Industrial", 500_000);

    private final String label;
    private final long plausibilityThreshold;

    ConsumerProfile(String label, long plausibilityThreshold) {
        this.label = label;
        this.plausibilityThreshold = plausibilityThreshold;
    }

    public String getLabel() {
        return label;
    }

    /** Maximum consumption (units) considered plausible for one cycle. */
    public long getPlausibilityThreshold() {
        return plausibilityThreshold;
    }
}
