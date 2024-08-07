package gist.pilldispenser.api.drugSummaryAPI.controller;

import gist.pilldispenser.api.drugSummaryAPI.domain.dto.response.DrugSummaryDTO;
import gist.pilldispenser.api.drugSummaryAPI.service.DrugSummarySearchService;
import gist.pilldispenser.api.drugSummaryAPI.service.DrugSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "약 용법 Controller", description = "약물 요약 정보를 검색하는 API를 제공합니다.")
@RestController
@RequiredArgsConstructor
public class DrugSummaryController {

    private final DrugSummarySearchService drugSummarySearchService;
    private final DrugSummaryService drugSummaryService;

    @GetMapping("/fetch-and-save-drugs")
    public ResponseEntity<String> fetchAndSaveDrugs() throws Exception {
        String result = drugSummaryService.fetchAndSaveDrugs();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Search drugs by name", description = "주어진 약 이름으로 약물 정보를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색된 약물 정보 리스트"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search-drugs")
    public ResponseEntity<List<DrugSummaryDTO>> searchDrugsByName(
            @Parameter(description = "검색할 약 이름") @RequestParam(name = "itemName") String itemName) {
        try {
            List<DrugSummaryDTO> drugSummaries = drugSummarySearchService.searchDrugsByName(itemName);
            return ResponseEntity.ok(drugSummaries);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/auto-complete")
    @Operation(summary = "자동완성 검색", description = "약 이름 자동완성 검색을 제공합니다.")
    public ResponseEntity<List<DrugSummaryDTO>> autoComplete(@RequestParam(name = "query") String query) {
        try {
            List<DrugSummaryDTO> suggestions = drugSummarySearchService.searchDrugsByName(query);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}