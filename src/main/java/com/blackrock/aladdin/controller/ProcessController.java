package com.blackrock.aladdin.controller;

import com.blackrock.aladdin.model.ProcessRequest;
import com.blackrock.aladdin.model.ProcessResponse;
import com.blackrock.aladdin.service.InvestmentEngineService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API: single endpoint for the micro-investment engine.
 * Port 5477 (configured in application.properties).
 */
@RestController
@RequestMapping("/api/v1/engine")
public class ProcessController {

    private final InvestmentEngineService engineService;

    public ProcessController(InvestmentEngineService engineService) {
        this.engineService = engineService;
    }

    @PostMapping(value = "/process", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcessResponse> process(@Valid @RequestBody ProcessRequest request) {
        ProcessResponse response = engineService.process(request);
        return ResponseEntity.ok(response);
    }
}
