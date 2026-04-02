package com.hag.core.parser;

import com.hag.core.model.Step;

import java.util.List;

/**
 * The result of parsing a single CSV test file.
 *
 * <p>Bundles the executable {@link Step} list together with the
 * {@link TestMetadata} extracted from {@code #!} directives, so that
 * the engine and report layer can access both without re-parsing.
 */
public record ParseResult(List<Step> steps, TestMetadata metadata) {

    /** Convenience factory for a result with no metadata directives. */
    public static ParseResult of(List<Step> steps) {
        return new ParseResult(steps, TestMetadata.empty());
    }
}
