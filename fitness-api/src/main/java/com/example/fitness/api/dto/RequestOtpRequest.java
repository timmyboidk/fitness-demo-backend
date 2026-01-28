package com.example.fitness.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 请求验证码 DTO
 */
@Data
public class RequestOtpRequest implements Serializable {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;
}
