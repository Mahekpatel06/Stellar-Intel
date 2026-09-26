package com.stellarintel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encapsulates metadata and excerpt content for a knowledge document chunk
 * retrieved from the Spring AI VectorStore to ground GenAI diagnostics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSourceDto {

    /**
     * File name or identifier of the source knowledge document (e.g., "battery-thermal.md").
     */
    private String documentName;

    /**
     * Topic or section heading extracted from the knowledge document.
     */
    private String relevantTopic;

    /**
     * Brief excerpt or summary preview of the retrieved text.
     */
    private String retrievedContentPreview;

    /**
     * Similarity score or relevance ranking metric (0.0 to 1.0).
     */
    private double relevanceScore;

    /**
     * Complete classpath or resource path identifier (e.g., "knowledge/battery-thermal.md").
     */
    private String sourceIdentifier;

    public KnowledgeSourceDto() {}

    public KnowledgeSourceDto(String documentName, String relevantTopic, String retrievedContentPreview,
                              double relevanceScore, String sourceIdentifier) {
        this.documentName = documentName;
        this.relevantTopic = relevantTopic;
        this.retrievedContentPreview = retrievedContentPreview;
        this.relevanceScore = relevanceScore;
        this.sourceIdentifier = sourceIdentifier;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String documentName;
        private String relevantTopic;
        private String retrievedContentPreview;
        private double relevanceScore;
        private String sourceIdentifier;

        public Builder documentName(String documentName) {
            this.documentName = documentName;
            return this;
        }

        public Builder relevantTopic(String relevantTopic) {
            this.relevantTopic = relevantTopic;
            return this;
        }

        public Builder retrievedContentPreview(String retrievedContentPreview) {
            this.retrievedContentPreview = retrievedContentPreview;
            return this;
        }

        public Builder relevanceScore(double relevanceScore) {
            this.relevanceScore = relevanceScore;
            return this;
        }

        public Builder sourceIdentifier(String sourceIdentifier) {
            this.sourceIdentifier = sourceIdentifier;
            return this;
        }

        public KnowledgeSourceDto build() {
            return new KnowledgeSourceDto(documentName, relevantTopic, retrievedContentPreview, relevanceScore, sourceIdentifier);
        }
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getRelevantTopic() {
        return relevantTopic;
    }

    public void setRelevantTopic(String relevantTopic) {
        this.relevantTopic = relevantTopic;
    }

    public String getRetrievedContentPreview() {
        return retrievedContentPreview;
    }

    public void setRetrievedContentPreview(String retrievedContentPreview) {
        this.retrievedContentPreview = retrievedContentPreview;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }
}
