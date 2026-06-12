package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class AuthCheckRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String topic;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}
