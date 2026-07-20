package com.aiexpense.aiservice.controller;

import com.aiexpense.aiservice.dto.ExpenseAnalysisRequest;
import com.aiexpense.aiservice.dto.ExpenseAnalysisResponse;
import com.aiexpense.aiservice.service.AiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI", description = "Endpoints for AI-powered expense analysis and insights")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @Operation(
            summary = "Generate AI-powered monthly expense analysis",
            description = "Uses Gemini to generate a natural-language summary of the user's " +
                    "spending for the given month, based on report data already computed by ReportService"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analysis generated successfully"),
            @ApiResponse(responseCode = "503", description = "AI service unavailable"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @PostMapping("/expense-analysis")
    public ResponseEntity<ExpenseAnalysisResponse> analyzeExpenses(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody ExpenseAnalysisRequest request) {
        log.info("REST request for AI expense analysis - month: {} year: {}", request.month(), request.year());
        return ResponseEntity.ok(
                aiAnalysisService.analyzeExpenses(request.month(), request.year(), authorizationHeader));
    }
}
