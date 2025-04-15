package org.jboss.pnc.dingrogu.api.dto;

import java.util.UUID;

import lombok.Getter;

/**
 * Simple DTO to enforce that some methods return the Correlation id.
 *
 * The correlation id is the unique id that links the workflow, Rex, and the adapter endpoints together. When submitting
 * a workflow request, Dingrogu will generate and return back a unique correlation id to the caller. That same
 * correlation id is used to send the graph request of all the tasks to Rex. The caller can then use that correlation id
 * to query Dingrogu on the status of the workflow and even cancel it if needed.
 *
 * This DTO might seem silly, but I wanted to enforce the idea that a method is return a correlation id, and not just
 * any random string.
 *
 * @author Dustin Kut Moy Cheung dcheung@redhat.com
 */
@Getter
public class CorrelationId {

    /**
     * the specific id
     */
    private final String id;

    public CorrelationId(String id) {
        this.id = id;
    }

    public static CorrelationId generateUnique() {
        return new CorrelationId(UUID.randomUUID().toString());
    }
}
