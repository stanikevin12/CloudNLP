package com.example.demo.validation;

import com.example.demo.exception.PayloadTooLargeException;

public final class NlpInputValidator {

    private NlpInputValidator() {}

    public static void validateGrammar(String text) {
        validate(text, 20_000, "Grammar correction");
    }

    public static void validateKeywords(String text) {
        validate(text, 3_000, "Keyword extraction");
    }

    public static void validateSummarization(String text) {
        validate(text, 3_000, "Summarization");
    }

    public static void validateEntities(String text) {
        validate(text, 1_000, "Entity extraction");
    }

    private static void validate(String text, int maxChars, String operation) {
        if (text == null || text.isBlank()) {
            throw new PayloadTooLargeException(
                operation + " input is empty."
            );
        }

        if (text.length() > maxChars) {
            throw new PayloadTooLargeException(
                operation + " input exceeds maximum supported size (" +
                maxChars + " characters). Chunking is required."
            );
        }
    }
}
