package com.example.fitness.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 微信登录 DTO
 */
@Data
public class WechatLoginRequest implements Serializable {

    /**
     * 微信授权code
     */
    @NotBlank(message = "微信授权code不能为空")
    private String code;
}
