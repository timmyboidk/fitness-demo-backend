package com.example.fitness.api.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AuthRequest {
    private String type; // login_phone | login_wechat
    private Map<String, Object> payload;
}
