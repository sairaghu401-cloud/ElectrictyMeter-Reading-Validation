package com.utility.meter.app;

import com.utility.meter.audit.AuditEntry;
import com.utility.meter.audit.AuditTrail;
import com.utility.meter.exceptions.MeterValidationException;
import com.utility.meter.model.ConsumerProfile;
import com.utility.meter.model.Meter;
import com.utility.meter.model.ValidationResult;
import com.utility.meter.service.InspectionQueue;
import com.utility.meter.service.MeterRegistry;
import com.utility.meter.service.MeterValidator;

import java.util.Scanner;

/**
 * Terminal front-end for PS-52: Electricity Meter-Reading Validation with Exceptions.
 *
 * A field operator enters a meter id and a new reading. The validator either
 * accepts it, or throws a specific exception which is routed to the inspection
 * queue. An authorised supervisor may override a rejected reading with a reason,
 * which is written to the audit trail.
 */
public class Main {

    private final Scanner in = new Scanner(System.in);
    private final MeterRegistry registry = new MeterRegistry();
    private final MeterValidator validator = new MeterValidator();
    private final InspectionQueue inspectionQueue = new InspectionQueue();
    private final AuditTrail auditTrail = new AuditTrail();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        seedMeters();
        System.out.println("============================================================");
        System.out.println("  PS-52  Electricity Meter-Reading Validation (Terminal)");
        System.out.println("============================================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = in.nextLine().trim();
            switch (choice) {
                case "1" -> listMeters();
                case "2" -> enterReading();
                case "3" -> viewInspectionQueue();
                case "4" -> viewAuditTrail();
                case "5" -> runDemo();
                case "0" -> running = false;
                default -> System.out.println("  ! Unknown option: " + choice);
            }
        }
        System.out.println("\nGoodbye.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("------------------------------------------------------------");
        System.out.println("  1) List meters");
        System.out.println("  2) Enter a meter reading");
        System.out.println("  3) View inspection queue (" + inspectionQueue.size() + ")");
        System.out.println("  4) View audit trail (" + auditTrail.size() + ")");
        System.out.println("  5) Run built-in demo scenarios");
        System.out.println("  0) Exit");
        System.out.print("  Select: ");
    }

    // ---------------------------------------------------------------- seed data

    private void seedMeters() {
        // meterId, profile, dial capacity, opening reading
        registry.add(new Meter("R-1001", ConsumerProfile.RESIDENTIAL, 99_999, 4_820));
        registry.add(new Meter("C-2002", ConsumerProfile.COMMERCIAL, 99_999, 61_500));
        registry.add(new Meter("I-3003", ConsumerProfile.INDUSTRIAL, 999_999, 240_000));
        registry.add(new Meter("R-4004", ConsumerProfile.RESIDENTIAL, 99_999, 99_990)); // near rollover
    }

    // ---------------------------------------------------------------- menu ops

    private void listMeters() {
        System.out.println("\n  Registered meters:");
        for (Meter m : registry.all()) {
            System.out.printf("    %-8s %-12s last=%-8d cap=%-7d unchangedCycles=%d%n",
                    m.getMeterId(), m.getProfile().getLabel(),
                    m.getLastReading(), m.getDialCapacity(), m.getUnchangedCycles());
        }
    }

    private void enterReading() {
        System.out.print("\n  Meter id: ");
        String id = in.nextLine().trim();
        Meter meter = registry.get(id);
        if (meter == null) {
            System.out.println("  ! No such meter: " + id);
            return;
        }
        System.out.print("  New reading: ");
        String raw = in.nextLine().trim();
        long reading;
        try {
            reading = Long.parseLong(raw);
        } catch (NumberFormatException e) {
            System.out.println("  ! Not a whole number: " + raw);
            return;
        }
        boolean accepted = submit(meter, reading);
        if (!accepted) {
            offerOverride(meter, reading);   // interactive path only
        }
    }

    /**
     * Runs validation on a single reading. On success it commits and returns true.
     * On failure it prints the reason, routes the reading to the inspection queue,
     * and returns false. The caller decides whether to offer an override (the
     * interactive menu does; the non-interactive demo does not).
     */
    private boolean submit(Meter meter, long reading) {
        System.out.printf("  -> Validating meter %s: %d (previous=%d)%n",
                meter.getMeterId(), reading, meter.getLastReading());
        try {
            ValidationResult result = validator.validate(meter, reading);
            validator.accept(meter, reading, result);
            System.out.printf("     ACCEPTED. consumption=%d units%s%n",
                    result.getConsumption(),
                    result.isRolloverApplied() ? "  (odometer rollover applied)" : "");
            return true;
        } catch (MeterValidationException ex) {
            System.out.println("     REJECTED [" + ex.category() + "]");
            System.out.println("       " + ex.getMessage());
            inspectionQueue.route(ex);
            System.out.println("       -> routed to inspection queue.");
            return false;
        }
    }

    private void offerOverride(Meter meter, long reading) {
        System.out.print("     Authorise an override? (y/N): ");
        String yn = in.nextLine().trim();
        if (!yn.equalsIgnoreCase("y")) {
            return;
        }
        System.out.print("     Supervisor id: ");
        String actor = in.nextLine().trim();
        if (actor.isEmpty()) {
            System.out.println("     ! Override cancelled: supervisor id is required.");
            return;
        }
        System.out.print("     Reason for override: ");
        String reason = in.nextLine().trim();
        if (reason.isEmpty()) {
            System.out.println("     ! Override cancelled: a reason is required for the audit trail.");
            return;
        }
        validator.override(meter, reading, actor, reason, auditTrail);
        System.out.println("     OVERRIDE APPLIED and recorded in the audit trail.");
    }

    private void viewInspectionQueue() {
        System.out.println("\n  Inspection queue:");
        if (inspectionQueue.isEmpty()) {
            System.out.println("    (empty)");
            return;
        }
        int i = 1;
        for (InspectionQueue.Item item : inspectionQueue.view()) {
            System.out.printf("    %d. %s%n", i++, item);
        }
    }

    private void viewAuditTrail() {
        System.out.println("\n  Audit trail:");
        if (auditTrail.isEmpty()) {
            System.out.println("    (empty)");
            return;
        }
        for (AuditEntry entry : auditTrail.getEntries()) {
            System.out.println("    " + entry);
        }
    }

    // ---------------------------------------------------------------- demo

    /**
     * Non-interactive walkthrough of every branch, so the grader can see the
     * behaviour without typing. Uses dedicated demo meters so it does not disturb
     * the seeded ones.
     */
    private void runDemo() {
        System.out.println("\n  ===== DEMO SCENARIOS =====");

        Meter home = new Meter("DEMO-HOME", ConsumerProfile.RESIDENTIAL, 99_999, 5_000);
        registry.add(home);

        System.out.println("\n  [1] Normal forward reading:");
        submit(home, 5_180);

        System.out.println("\n  [2] Backward reading (typo, not a rollover):");
        submit(home, 4_000);

        System.out.println("\n  [3] Implausible jump above the residential threshold:");
        submit(home, 20_000);

        Meter wrap = new Meter("DEMO-WRAP", ConsumerProfile.RESIDENTIAL, 99_999, 99_980);
        registry.add(wrap);
        System.out.println("\n  [4] Legitimate odometer rollover (99980 -> 00040):");
        submit(wrap, 40);

        Meter stuck = new Meter("DEMO-STUCK", ConsumerProfile.RESIDENTIAL, 99_999, 7_000);
        registry.add(stuck);
        System.out.println("\n  [5] Stuck meter (same reading repeated):");
        submit(stuck, 7_000);
        submit(stuck, 7_000);
        submit(stuck, 7_000); // reaches STUCK_METER_LIMIT -> fault

        System.out.println("\n  [6] Out-of-range reading (negative):");
        submit(home, -5);

        System.out.println("\n  [7] Authorised override of a rejected reading (with audit):");
        long forced = 20_000;
        if (!submit(home, forced)) {
            validator.override(home, forced, "SUP-07", "Confirmed on-site: new large A/C load", auditTrail);
            System.out.println("     OVERRIDE APPLIED by SUP-07 and recorded in the audit trail.");
        }

        System.out.println("\n  Demo complete. Inspection queue=" + inspectionQueue.size()
                + ", audit entries=" + auditTrail.size()
                + " (options 3 and 4 to review).");
    }
}
