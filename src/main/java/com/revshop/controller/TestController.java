package com.revshop.controller;

import lombok.extern.log4j.Log4j2;
import com.revshop.dto.common.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Health/Test", description = "Secure test endpoint")
@SecurityRequirement(name = "bearerAuth")
@Log4j2
public class TestController {

    @GetMapping("/secure")
    public ResponseEntity<ApiResponse<String>> secureEndpoint() {
        return ResponseEntity.ok(ApiResponse.success("JWT verification successful", "JWT WORKING SUCCESSFULLY"));
    }
}
