package com.revshop.controller;

import com.revshop.dto.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/secure")
    public ResponseEntity<ApiResponse<String>> secureEndpoint() {
        return ResponseEntity.ok(ApiResponse.success("JWT verification successful", "JWT WORKING SUCCESSFULLY"));
    }
}
