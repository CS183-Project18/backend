package com.storefinds.uniquefindsbackend.client;

import com.storefinds.uniquefindsbackend.dto.ai.BuildIndexResponse;
import com.storefinds.uniquefindsbackend.dto.ai.IndexPostData;
import com.storefinds.uniquefindsbackend.exception.AIServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Author: Shuying Liang
 * Date: 2026-05-15
 * Purpose: Define backend calls to the Python AI search service.
 */
public interface AISearchClient {

    /**
     * Author: Shuying Liang
     * Purpose: Check AI search health.
     */
    boolean isHealthy();

    /**
     * Author: Shuying Liang
     * Purpose: Run semantic text search.
     *
     * @param query search keyword or phrase
     * @param topK maximum result count
     * @return ordered post IDs
     * @throws AIServiceException when the AI service call fails
     */
    List<Long> semanticSearch(String query, int topK) throws AIServiceException;

    /**
     * Author: Shuying Liang
     * Purpose: Run image similarity search.
     *
     * @param imageFile uploaded image
     * @param topK maximum result count
     * @return ordered post IDs
     * @throws AIServiceException when the AI service call fails
     */
    List<Long> imageSearch(MultipartFile imageFile, int topK) throws AIServiceException;

    /**
     * Author: Shuying Liang
     * Purpose: Rebuild AI search indexes.
     *
     * @param posts published posts to index
     * @return index build result
     * @throws AIServiceException when the AI service call fails
     */
    BuildIndexResponse buildIndex(List<IndexPostData> posts) throws AIServiceException;
}
