package com.stellarintel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service responsible for ingesting, chunking, and embedding domain-specific
 * technical knowledge documents into the Spring AI VectorStore at startup.
 */
@Service
public class KnowledgeBaseIngestionService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseIngestionService.class);

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourcePatternResolver;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicInteger indexedDocumentCount = new AtomicInteger(0);
    private final AtomicInteger indexedChunkCount = new AtomicInteger(0);

    public KnowledgeBaseIngestionService(VectorStore vectorStore, ResourcePatternResolver resourcePatternResolver) {
        this.vectorStore = vectorStore;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * Initializes the knowledge base once the application context is fully started.
     */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void initializeKnowledgeBase() {
        if (initialized.get()) {
            log.debug("Knowledge base already initialized. Skipping redundant ingestion.");
            return;
        }

        log.info("Starting StellarIntel Knowledge Base ingestion into Spring AI VectorStore...");
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:knowledge/*.md");
            if (resources == null || resources.length == 0) {
                log.warn("No knowledge documents found under classpath:knowledge/*.md");
                return;
            }

            List<Document> allChunks = new ArrayList<>();

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                String content = readResourceContent(resource);

                if (content == null || content.trim().isEmpty()) {
                    continue;
                }

                String title = extractTitle(content, filename);
                String topic = extractTopic(content, filename);

                List<Document> chunks = chunkMarkdownDocument(content, filename, title, topic);
                allChunks.addAll(chunks);
                indexedDocumentCount.incrementAndGet();
            }

            if (!allChunks.isEmpty()) {
                vectorStore.add(allChunks);
                indexedChunkCount.set(allChunks.size());
                log.info("Knowledge Base Ingestion complete: successfully indexed {} semantic chunks from {} technical documents.",
                        allChunks.size(), indexedDocumentCount.get());
            }

            initialized.set(true);

        } catch (Exception ex) {
            log.error("Failed to ingest knowledge base into VectorStore: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Reads the entire text content of a resource.
     */
    private String readResourceContent(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception ex) {
            log.error("Error reading resource {}: {}", resource.getFilename(), ex.getMessage());
            return null;
        }
    }

    /**
     * Splits a markdown knowledge document into cohesive semantic sections based on level-2 headers ('## ').
     * This preserves semantic context for each aerospace subsystem topic much better than arbitrary character token slices.
     */
    public List<Document> chunkMarkdownDocument(String content, String filename, String title, String topic) {
        List<Document> chunks = new ArrayList<>();
        String[] sections = content.split("(?m)^##\\s+");

        int sectionIndex = 0;
        for (String section : sections) {
            String trimmed = section.trim();
            if (trimmed.isEmpty()) continue;

            // If it's the very first part with # H1 title, preserve title context
            String chunkText;
            String sectionHeading;

            if (sectionIndex == 0 && trimmed.startsWith("#")) {
                sectionHeading = "Overview & Educational Notice";
                chunkText = trimmed;
            } else {
                int firstNewline = trimmed.indexOf('\n');
                if (firstNewline > 0) {
                    sectionHeading = trimmed.substring(0, firstNewline).trim();
                } else {
                    sectionHeading = "Section " + sectionIndex;
                }
                chunkText = "Document: " + title + "\nTopic: " + topic + "\nSection: " + sectionHeading + "\n\n" + trimmed;
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentName", filename);
            metadata.put("title", title);
            metadata.put("topic", topic);
            metadata.put("section", sectionHeading);
            metadata.put("source", "knowledge/" + filename);
            metadata.put("chunkIndex", sectionIndex);

            chunks.add(new Document(chunkText, metadata));
            sectionIndex++;
        }

        return chunks;
    }

    private String extractTitle(String content, String defaultTitle) {
        for (String line : content.split("\n")) {
            String t = line.trim();
            if (t.startsWith("# ")) {
                return t.substring(2).trim();
            }
        }
        return defaultTitle;
    }

    private String extractTopic(String content, String defaultTopic) {
        String marker = "## Topic";
        int idx = content.indexOf(marker);
        if (idx != -1) {
            int start = idx + marker.length();
            int end = content.indexOf("\n## ", start);
            if (end == -1) end = content.length();
            return content.substring(start, end).trim();
        }
        return defaultTopic;
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public int getIndexedDocumentCount() {
        return indexedDocumentCount.get();
    }

    public int getIndexedChunkCount() {
        return indexedChunkCount.get();
    }
}
