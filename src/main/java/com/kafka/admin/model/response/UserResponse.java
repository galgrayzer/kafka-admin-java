package com.kafka.admin.model.response;

import java.util.List;

public class UserResponse {

    private String username;
    private List<String> mechanisms;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getMechanisms() {
        return mechanisms;
    }

    public void setMechanisms(List<String> mechanisms) {
        this.mechanisms = mechanisms;
    }
}
