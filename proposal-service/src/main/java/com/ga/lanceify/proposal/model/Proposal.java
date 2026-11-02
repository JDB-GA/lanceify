package com.ga.lanceify.proposal.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import lombok.Getter;
import lombok.Setter;

@Data
@Document(collection = "proposals")
@CompoundIndex(name = "one_proposal_per_project", def = "{'projectId': 1, 'freelancerId': 1}", unique = true)
public class Proposal {

    @Id
    private String id;

    private String projectId;

    private String clientId;

    private String freelancerId;

    private String coverLetter;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal bidAmount;

    private Integer deliveryDays;

    private ProposalStatus status = ProposalStatus.PENDING;
    public enum ProposalStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        WITHDRAWN
    }

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;


}
