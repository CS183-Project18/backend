package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchQueryParserTest {

    private final SearchQueryParser searchQueryParser = new SearchQueryParser();

    @Test
    void parsePostSearchQueryNormalizesKeywordAndSort() {
        PostSearchQuery query = searchQueryParser.parsePostSearchQuery("  lamp  ", 4L, 6L, List.of(2L, 3L), null, null, "HOT", 2, 10);

        assertEquals("lamp", query.keyword());
        assertEquals("%lamp%", query.keywordLike());
        assertEquals(6L, query.storeId());
        assertEquals(List.of(2L, 3L), query.tagIds());
        assertEquals("hot", query.sort());
        assertEquals(true, query.sortExplicitlySpecified());
        assertEquals(10, query.offset());
    }

    @Test
    void parsePostSearchQueryRejectsUnknownSort() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> searchQueryParser.parsePostSearchQuery("lamp", null, null, List.of(), null, null, "random", 1, 20));

        assertEquals("sort must be one of: latest, hot, favorites, comments", ex.getMessage());
    }

    @Test
    void parsePostSearchQueryDeduplicatesAndFiltersTagIds() {
        PostSearchQuery query = searchQueryParser.parsePostSearchQuery("desk", null, null, Arrays.asList(5L, 5L, null, -1L, 3L), null, null, "latest", 1, 20);

        assertEquals(List.of(5L, 3L), query.tagIds());
    }

    @Test
    void parsePostSearchQueryKeepsEmptyTagListWhenAbsent() {
        PostSearchQuery query = searchQueryParser.parsePostSearchQuery("desk", null, null, null, null, null, "latest", 1, 20);

        assertEquals(List.of(), query.tagIds());
    }

    @Test
    void parsePostSearchQueryMarksDefaultSortAsImplicit() {
        PostSearchQuery query = searchQueryParser.parsePostSearchQuery("desk", null, null, null, null, null, null, 1, 20);

        assertEquals("latest", query.sort());
        assertEquals(false, query.sortExplicitlySpecified());
    }

    @Test
    void parsePostSearchQueryRejectsReversedPriceRange() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> searchQueryParser.parsePostSearchQuery("lamp", null, null, List.of(), new BigDecimal("30"), new BigDecimal("20"), "latest", 1, 20));

        assertEquals("priceMin cannot be greater than priceMax", ex.getMessage());
    }
}
