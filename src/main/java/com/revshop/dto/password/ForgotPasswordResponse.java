package com.revshop.dto.password;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForgotPasswordResponse {

    private String deliveryChannel;
    private Integer expiresInMinutes;
    private String note;
}
