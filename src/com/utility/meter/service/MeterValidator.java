package com.utility.meter.service;

import com.utility.meter.audit.AuditTrail;
import com.utility.meter.exceptions.BackwardReadingException;
import com.utility.meter.exceptions.ImplausibleConsumptionException;
import com.utility.meter.exceptions.SuspectedFaultException;
import com.utility.meter.model.Meter;
import com.utility.meter.model.ValidationResult;

/**
 * Core validation service. Given a meter and a freshly entered reading it either
 * returns a {@link ValidationResult} (the reading is acceptable) or throws a
 * specific {@link com.utility.meter.exceptions.MeterValidationException}.
 *
 * Control flow, in order:
 *   1. Physical range check          -> SuspectedFaultException (OUT_OF_RANGE)
 *   2. Backward vs. odometer rollover -> BackwardReadingException (if not a plausible rollover)
 *   3. Plausibility threshold         -> ImplausibleConsumptionException
 *   4. Stuck-meter detection          -> SuspectedFaultException (STUCK_METER)
 */
public class MeterValidator {

    /** How many consecutive zero-consumption cycles before we suspect a stuck meter. */
    public static final int STUCK_METER_LIMIT = 3;

    /**
     * Validate a new reading WITHOUT committing it.
     * Callers commit with {@link #accept} once they are happy with the result.
     */
    public ValidationResult validate(Meter meter, long newReading)
            throws SuspectedFaultException, BackwardReadingException, ImplausibleConsumptionException {

        final String id = meter.getMeterId();

        // 1. Physical range -- a reading cannot be negative or exceed the dial capacity.
        if (newReading < 0) {
            throw new SuspectedFaultException(id,
                    SuspectedFaultException.FaultKind.OUT_OF_RANGE,
                    "reading is negative (" + newReading + ")");
        }
        if (newReading > meter.getDialCapacity()) {
            throw new SuspectedFaultException(id,
                    SuspectedFaultException.FaultKind.OUT_OF_RANGE,
                    "reading " + newReading + " exceeds dial capacity " + meter.getDialCapacity());
        }

        final long previous = meter.getLastReading();
        final long threshold = meter.getProfile().getPlausibilityThreshold();

        long consumption;
        boolean rolloverApplied = false;

        // 2. Backward reading vs. legitimate odometer rollover.
        if (newReading >= previous) {
            consumption = newReading - previous;
        } else {
            // The dial appears to have gone backwards. It MIGHT be an odometer
            // wrap (e.g. 99990 -> 00010). Compute what the consumption would be
            // if it wrapped exactly once and accept only if that is plausible.
            long rolloverConsumption = (meter.getDialCapacity() - previous) + 1 + newReading;
            if (rolloverConsumption <= threshold) {
                consumption = rolloverConsumption;
                rolloverApplied = true;
            } else {
                // Too big to be a believable single wrap -> genuine backward error.
                throw new BackwardReadingException(id, previous, newReading);
            }
        }

        // 3. Plausibility threshold for this consumer profile.
        if (consumption > threshold) {
            throw new ImplausibleConsumptionException(id, consumption, threshold);
        }

        // 4. Stuck meter: repeated zero consumption suggests the dial is jammed.
        if (consumption == 0 && meter.getUnchangedCycles() + 1 >= STUCK_METER_LIMIT) {
            throw new SuspectedFaultException(id,
                    SuspectedFaultException.FaultKind.STUCK_METER,
                    "no change for " + (meter.getUnchangedCycles() + 1) + " consecutive cycles");
        }

        return new ValidationResult(consumption, rolloverApplied);
    }

    /** Commit a validated reading to the meter. */
    public void accept(Meter meter, long newReading, ValidationResult result) {
        meter.commitReading(newReading, result.getConsumption());
    }

    /**
     * Authorised override: force-accept a reading that failed validation.
     * The reason and actor are written to the audit trail. Consumption is
     * computed leniently (raw forward difference, floored at zero) purely so the
     * meter's stored state stays consistent.
     */
    public void override(Meter meter, long newReading, String actor, String reason, AuditTrail audit) {
        long previous = meter.getLastReading();
        long consumption = Math.max(0, newReading - previous);
        meter.commitReading(newReading, consumption);
        audit.record(meter.getMeterId(), "OVERRIDE_ACCEPT reading=" + newReading, actor, reason);
    }
}
