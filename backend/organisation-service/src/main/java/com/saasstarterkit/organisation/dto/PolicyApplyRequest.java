package com.saasstarterkit.organizationalmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PolicyApplyRequest {
    @NotBlank(message = "Policy name is required")
    private String name;

    private String description;

    @NotBlank(message = "Policy type is required")
    private String policyType;

    @NotBlank(message = "Policy data is required")
    private String policyData;

    private Integer priority = 0;

    // Constructors
    public PolicyApplyRequest() {}

    public PolicyApplyRequest(String name, String policyType, String policyData) {
        this.name = name;
        this.policyType = policyType;
        this.policyData = policyData;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }

    public String getPolicyData() { return policyData; }
    public void setPolicyData(String policyData) { this.policyData = policyData; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    @Override
    public String toString() {
        return "PolicyApplyRequest{" +
                "name='" + name + '\'' +
                ", policyType='" + policyType + '\'' +
                ", priority=" + priority +
                '}';
    }
}
