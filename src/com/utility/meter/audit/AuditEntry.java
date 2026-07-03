package com.utility.meter.audit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One immutable line in the audit trail. Records overrides and other
 * decisions that a supervisor may later need to justify.
 */
public class AuditEntry {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final String meterId;
    private final String action;
    private final String actor;
    private final String reason;

    public AuditEntry(String meterId, String action, String actor, String reason) {
        this.timestamp = LocalDateTime.now();
        this.meterId = meterId;
        this.action = action;
        this.actor = actor;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return String.format("[%s] meter=%s action=%s by=%s reason=\"%s\"",
                timestamp.format(TS), meterId, action, actor, reason);
    }
}
