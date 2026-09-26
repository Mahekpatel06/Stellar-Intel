package com.stellarintel.controller;

import com.stellarintel.dto.KnowledgeSourceDto;
import com.stellarintel.service.KnowledgeRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API Controller for searching and inspecting the domain-specific
 * satellite subsystem technical knowledge base via Spring AI RAG.
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeApiController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeApiController.class);

    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeApiController(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    /**
     * Executes similarity search against the Spring AI VectorStore.
     *
     * @param query search text (e.g., "reaction wheel saturation", "solar flare radiation")
     * @param limit maximum number of relevant knowledge chunks to return (default 4)
     * @return list of matched knowledge source snippets and metadata
     */
    @GetMapping("/search")
    public ResponseEntity<List<KnowledgeSourceDto>> searchKnowledge(
            @RequestParam(name = "q", required = false, defaultValue = "") String query,
            @RequestParam(name = "limit", required = false, defaultValue = "4") int limit) {
        log.info("REST knowledge search request received: query='{}', limit={}", query, limit);
        List<KnowledgeSourceDto> results = retrievalService.search(query, limit);
        return ResponseEntity.ok(results);
    }
}
