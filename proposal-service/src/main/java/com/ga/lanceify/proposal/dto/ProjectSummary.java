package com.ga.lanceify.proposal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The parts of a project (from marketplace-service) that proposal-service needs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSummary {

    private String id;

    private String clientId;

    private String title;

    private String status;

    @JsonIgnore
    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status);
    }

}
