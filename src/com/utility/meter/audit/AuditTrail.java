package com.utility.meter.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Append-only audit log. Every authorised override must be recorded here so the
 * decision can be traced back to the person who made it and why.
 */
public class AuditTrail {

    private final List<AuditEntry> entries = new ArrayList<>();

    public void record(String meterId, String action, String actor, String reason) {
        entries.add(new AuditEntry(meterId, action, actor, reason));
    }

    public List<AuditEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }
}
