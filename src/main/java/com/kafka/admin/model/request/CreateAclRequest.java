package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateAclRequest {

    @NotBlank(message = "Resource type is required")
    private String resourceType;

    @NotBlank(message = "Resource name is required")
    private String resourceName;

    @NotBlank(message = "Principal is required")
    private String principal;

    @NotBlank(message = "Operation is required")
    private String operation;

    @NotBlank(message = "Permission is required")
    private String permission;

    private String host;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
