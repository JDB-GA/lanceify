package com.ga.lanceify.contract.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Document(collection = "contracts")
public class Contract {

	@Id
	private String id;

	@Indexed(unique = true)
	private String proposalId;

	private String projectId;

	private String projectTitle;

	@Indexed
	private String clientId;

	@Indexed
	private String freelancerId;

	@Field(targetType = FieldType.DECIMAL128)
	private BigDecimal amount;

	private Integer deliveryDays;

	private ContractStatus status = ContractStatus.ACTIVE;

	private Instant createdAt;

	private Instant closedAt;

	public String getId() { return id; }

	public void setId(String id) { this.id = id; }

	public String getProposalId() { return proposalId; }

	public void setProposalId(String proposalId) { this.proposalId = proposalId; }

	public String getProjectId() { return projectId; }

	public void setProjectId(String projectId) { this.projectId = projectId; }

	public String getProjectTitle() { return projectTitle; }

	public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

	public String getClientId() { return clientId; }

	public void setClientId(String clientId) { this.clientId = clientId; }

	public String getFreelancerId() { return freelancerId; }

	public void setFreelancerId(String freelancerId) { this.freelancerId = freelancerId; }

	public BigDecimal getAmount() { return amount; }

	public void setAmount(BigDecimal amount) { this.amount = amount; }

	public Integer getDeliveryDays() { return deliveryDays; }

	public void setDeliveryDays(Integer deliveryDays) { this.deliveryDays = deliveryDays; }

	public ContractStatus getStatus() { return status; }

	public void setStatus(ContractStatus status) { this.status = status; }

	public Instant getCreatedAt() { return createdAt; }

	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

	public Instant getClosedAt() { return closedAt; }

	public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

}
