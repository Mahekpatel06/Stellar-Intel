package com.stellarintel.config;

import com.stellarintel.service.StellarIntelEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI VectorStore Configuration for StellarIntel RAG Pipeline.
 *
 * <p>Phase 4 Contract:
 * Uses Spring AI's supported embedded SimpleVectorStore abstraction.
 * Provides in-memory vector indexing and cosine similarity search without requiring
 * external cloud vector databases or local Docker containers.
 */
@Configuration
public class RagVectorStoreConfig {

    @Bean
    public VectorStore vectorStore(StellarIntelEmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }
}
