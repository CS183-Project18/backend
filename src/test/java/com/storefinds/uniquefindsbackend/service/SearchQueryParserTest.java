package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchQueryParserTest {

    private final SearchQueryParser searchQueryParser = new SearchQueryParser();

    @Test
    void parsePostSearchQueryNormalizesKeywordAndSort() {
        PostSearchQuery query = searchQueryParser.parsePostSearchQuery("  lamp  ", 4L, "HOT", 2, 10);

        assertEquals("lamp", query.keyword());
        assertEquals("%lamp%", query.keywordLike());
        assertEquals("hot", query.sort());
        assertEquals(10, query.offset());
    }

    @Test
    void parsePostSearchQueryRejectsUnknownSort() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> searchQueryParser.parsePostSearchQuery("lamp", null, "random", 1, 20));

        assertEquals("sort must be one of: latest, hot", ex.getMessage());
    }
}
