package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class CreateQuotaRequest {

    @NotBlank(message = "Username is required")
    private String username;

    private Long bytesInQuota = 0L;

    private Long bytesOutQuota = 0L;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getBytesInQuota() {
        return bytesInQuota;
    }

    public void setBytesInQuota(Long bytesInQuota) {
        this.bytesInQuota = bytesInQuota;
    }

    public Long getBytesOutQuota() {
        return bytesOutQuota;
    }

    public void setBytesOutQuota(Long bytesOutQuota) {
        this.bytesOutQuota = bytesOutQuota;
    }
}
