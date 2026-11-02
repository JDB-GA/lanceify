package com.ga.lanceify.proposal.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
@Document(collection = "proposals")
@CompoundIndexes({
        @CompoundIndex(name = "one_proposal_per_project", def = "{'projectId': 1, 'freelancerId': 1}", unique = true),
        @CompoundIndex(name = "one_accepted_per_project", def = "{'projectId': 1}", unique = true,
                partialFilter = "{ 'status': 'ACCEPTED' }"),
        @CompoundIndex(name = "client_inbox", def = "{'clientId': 1, 'createdAt': -1}")
})
public class Proposal {

    @Id
    private String id;

    private String projectId;

    private String clientId;

    private String projectTitle;

    private String freelancerId;

    private String coverLetter;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal bidAmount;

    private Integer deliveryDays;

    private ProposalStatus status = ProposalStatus.PENDING;

    private Instant decidedAt;

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
