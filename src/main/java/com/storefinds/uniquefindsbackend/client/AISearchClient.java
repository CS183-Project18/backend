package com.storefinds.uniquefindsbackend.client;

import com.storefinds.uniquefindsbackend.dto.ai.BuildIndexResponse;
import com.storefinds.uniquefindsbackend.dto.ai.IndexPostData;
import com.storefinds.uniquefindsbackend.exception.AIServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Client interface for communicating with the AI Search Service.
 * <p>
 * This interface provides methods for health checking, semantic text search,
 * image-based visual search, and index building operations. All methods
 * communicate with the external Python FastAPI AI Search Service via HTTP.
 * </p>
 * <p>
 * The AI Search Service provides vector-based search capabilities using FAISS
 * indices for both text (semantic search) and images (visual similarity search).
 * </p>
 * <p>
 * <b>Error Handling:</b>
 * All search and index operations may throw AIServiceException or its subclasses:
 * <ul>
 *   <li>{@link com.storefinds.uniquefindsbackend.exception.AIServiceTimeoutException} - Request exceeded configured timeout</li>
 *   <li>{@link com.storefinds.uniquefindsbackend.exception.AIServiceUnavailableException} - Service is not reachable</li>
 *   <li>{@link com.storefinds.uniquefindsbackend.exception.AIServiceParseException} - Response JSON is malformed</li>
 *   <li>{@link AIServiceException} - General service error (HTTP error codes, validation failures)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Configuration:</b>
 * Implementations should read configuration from application properties including:
 * <ul>
 *   <li>Base URL of the AI Search Service</li>
 *   <li>Connection timeout (recommended: 5 seconds)</li>
 *   <li>Read timeout for search operations (recommended: 10 seconds)</li>
 *   <li>Read timeout for index build operations (recommended: 30 seconds)</li>
 *   <li>Retry configuration (max retries, backoff strategy)</li>
 *   <li>Circuit breaker configuration (failure threshold, wait duration)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Thread Safety:</b>
 * Implementations must be thread-safe as they will be used as Spring singleton beans.
 * </p>
 *
 * @author Kaijie Zhu
 * @version 1.0
 * @since 2026-05-15
 */
public interface AISearchClient {

    /**
     * Checks if the AI Search Service is available and healthy.
     * <p>
     * This method performs a health check by calling the AI Search Service's
     * health endpoint. It should be used before attempting search operations
     * to determine if the service is available, enabling graceful fallback
     * to SQL-based search when the AI service is down.
     * </p>
     * <p>
     * <b>Implementation Notes:</b>
     * <ul>
     *   <li>Should use a short timeout (e.g., 2-3 seconds) to fail fast</li>
     *   <li>Should cache the health status for a short period (e.g., 30 seconds) to avoid excessive health checks</li>
     *   <li>Should return false on any exception rather than propagating it</li>
     *   <li>Should log health check failures at DEBUG or WARN level</li>
     * </ul>
     * </p>
     * <p>
     * <b>Example Usage:</b>
     * <pre>{@code
     * if (aiSearchClient.isHealthy()) {
     *     // Use AI semantic search
     *     List<Long> postIds = aiSearchClient.semanticSearch(query, 20);
     * } else {
     *     // Fall back to SQL search
     *     List<Post> posts = postRepository.searchByKeyword(query);
     * }
     * }</pre>
     * </p>
     *
     * @return {@code true} if the AI Search Service is reachable and reports healthy status,
     *         {@code false} if the service is unavailable, unhealthy, or unreachable
     */
    boolean isHealthy();

    /**
     * Performs semantic text search using AI-powered natural language understanding.
     * <p>
     * This method sends a text query to the AI Search Service, which uses vector
     * embeddings and FAISS similarity search to find posts with semantically similar
     * content. Unlike traditional keyword search, semantic search understands the
     * meaning and context of the query, enabling better results even when exact
     * keywords don't match.
     * </p>
     * <p>
     * <b>Search Behavior:</b>
     * <ul>
     *   <li>Returns post IDs ranked by semantic similarity (most similar first)</li>
     *   <li>The AI service may cache results for identical queries to improve performance</li>
     *   <li>Results are based on the current state of the text index (last rebuild)</li>
     *   <li>Only published posts that were indexed are searchable</li>
     * </ul>
     * </p>
     * <p>
     * <b>Performance Considerations:</b>
     * <ul>
     *   <li>First query may be slower due to model loading (cold start)</li>
     *   <li>Subsequent queries are typically fast (50-200ms)</li>
     *   <li>Cached queries return almost instantly</li>
     *   <li>Timeout is configured separately for search operations (default: 10 seconds)</li>
     * </ul>
     * </p>
     * <p>
     * <b>Example Usage:</b>
     * <pre>{@code
     * try {
     *     List<Long> postIds = aiSearchClient.semanticSearch("vintage leather jacket", 20);
     *     List<Post> posts = postRepository.findAllById(postIds);
     *     // Process posts maintaining AI-returned order
     * } catch (AIServiceTimeoutException e) {
     *     // Fall back to SQL search
     *     logger.warn("AI search timed out, falling back to SQL search", e);
     * } catch (AIServiceException e) {
     *     // Fall back to SQL search
     *     logger.error("AI search failed, falling back to SQL search", e);
     * }
     * }</pre>
     * </p>
     *
     * @param query the search keyword or phrase (must not be null or empty)
     * @param topK the maximum number of results to return (must be positive, typically 20-100)
     * @return a list of post IDs ordered by semantic similarity (most similar first),
     *         may be empty if no similar posts are found, never null
     * @throws AIServiceException if the AI Search Service returns an error response
     *         (HTTP status >= 400 or response code != 200)
     * @throws com.storefinds.uniquefindsbackend.exception.AIServiceTimeoutException
     *         if the request exceeds the configured read timeout
     * @throws com.storefinds.uniquefindsbackend.exception.AIServiceUnavailableException
     *         if the AI Search Service is not reachable (connection refused, host unreachable)
     * @throws com.storefinds.uniquefindsbackend.exception.AIServiceParseException
     *         if the response JSON cannot be parsed or is missing required fields
     * @throws IllegalArgumentException if query is null/empty or topK is not positive
     */
    List<Long> semanticSearch(String query, int topK) throws AIServiceException;

    /**
     * Performs image-based visual similarity search using AI-powered computer vision.
     * <p>
     * This method uploads an image to the AI Search Service, which extracts visual
     * features using a deep learning model and searches the image index for visually
     * similar post images. This enables users to find products by uploading a photo
     * of something similar they've seen.
     * </p>
     * <p>
     * <b>Search Behavior:</b>
     * <ul>
     *   <li>Returns post IDs ranked by visual similarity (most similar first)</li>
     *   <li>Searches based on visual features (colors, shapes, textures, objects)</li>
     *   <li>Results are based on the current state of the image index (last rebuild)</li>
     *   <li>Only published posts with images that were indexed are searchable</li>
     *   <li>Image is processed on the server side and not stored permanently</li>
     * </ul>
     * </p>
     * <p>
     * <b>Supported Image Formats:</b>
     * <ul>
     *   <li>JPEG (.jpg, .jpeg)</li>
     *   <li>PNG (.png)</li>
     *   <li>WebP (.webp)</li>
     * </ul>
     * </p>
     * <p>
     * <b>Image Requirements:</b>
     * <ul>
     *   <li>Maximum file size: 10MB (enforced by controller layer)</li>
     *   <li>Minimum dimensions: 100x100 pixels (recommended)</li>
     *   <li>Maximum dimensions: 4096x4096 pixels (recommended)</li>
     *   <li>Image should be clear and well-lit for best results</li>
     * </ul>
     * </p>
     * <p>
     * <b>Performance Considerations:</b>
     * <ul>
     *   <li>Image upload and processing takes longer than text search (200-500ms)</li>
     *   <li>Larger images take longer to upload and process</li>
     *   <li>First request may be slower due to model loading (cold start)</li>
     *   <li>Timeout is configured separately for search operations (default: 10 seconds)</li>
     * </ul>
     * </p>
     * <p>
     * <b>Example Usage:</b>
     * <pre>{@code
     * try {
     *     MultipartFile uploadedImage = // ... from request
     *     List<Long> postIds = aiSearchClient.imageSearch(uploadedImage, 20);
     *     List<Post> posts = postRepository.findAllById(postIds);
     *     // Process posts maintaining AI-returned order
     * } catch (AIServiceTimeoutException e) {
     *     // Return empty results with message
     *     logger.warn("Image search timed out", e);
     *     return PageResponse.empty("Image search is temporarily unavailable");
     * } catch (AIServiceException e) {
     *     // Return empty results with message
     *     logger.error("Image search failed", e);
     *     return PageResponse.empty("Image search is temporarily unavailable");
     * }
     * }</pre>
     * </p>
     *
     * @param imageFile the uploaded image file (must not be null, must be a valid image format)
     * @param topK the maximum number of results to return (must be positive, typically 20-100)
     * @return a list of post IDs ordered by visual similarity (most similar first),
     *         may be empty if no similar posts are found, never null
     * @throws AIServiceException if the AI Search Service returns an error response
     *         (HTTP status >= 400 or response code != 200, including invalid image format)
     * @throws com.storefinds.uniquefindsbackend.exception.AIServiceTimeoutException
     *         if the request exceeds the configured read timeout
     * @throws com.storefinds.uniquefindsbackend.exception.AIServiceUnavailableException
     *         if the AI Search Service is not reachable (connection refused, host unreachable)
     * @throws com.storefinds.uniquefindsbackend.exception.AIServiceParseException
     *         if the response JSON cannot be parsed or is missing required fields
     * @throws IllegalArgumentException if imageFile is null or topK is not positive
     */
    List<Long> imageSearch(MultipartFile imageFile, int topK) throws AIServiceException;

    /**
     * Builds or rebuilds the AI search indices with the provided post data.
     * <p>
     * This method sends a complete list of published posts to the AI Search Service,
     * which rebuilds both the text index (for semantic search) and the image index
     * (for visual search). This is a potentially long-running operation that should
     * be called asynchronously.
     * </p>
     * <p>
     * <b>Index Building Process:</b>
     * <ol>
     *   <li>AI service receives the post data (IDs, titles, descriptions)</li>
     *   <li>Text embeddings are generated for each post's title and description</li>
     *   <li>Image embeddings are generated for each post's images (fetched from URLs)</li>
     *   <li>FAISS indices are built from the embeddings</li>
     *   <li>Indices are saved to disk and loaded into memory</li>
     *   <li>Old indices are replaced atomically</li>
     * </ol>
     * </p>
     * <p>
     * <b>When to Trigger Index Rebuild:</b>
     * <ul>
     *   <li>Application startup (initial index build)</li>
     *   <li>Post status changes to/from PUBLISHED</li>
     *   <li>Published post's searchable fields are modified (title, description, images, tags, category, store)</li>
     *   <li>Published post is soft-deleted</li>
     *   <li>Manual rebuild requested by administrator</li>
     * </ul>
     * </p>
     * <p>
     * <b>Performance Considerations:</b>
     * <ul>
     *   <li>Index building is CPU and memory intensive on the AI service</li>
     *   <li>Time scales with number of posts (roughly 1-2 seconds per 100 posts)</li>
     *   <li>Should be called asynchronously to avoid blocking the main thread</li>
     *   <li>Timeout is configured separately for index build (default: 30 seconds)</li>
     *   <li>Implement debouncing to prevent excessive rebuilds (minimum 5 seconds between rebuilds)</li>
     * </ul>
     * </p>
     * <p>
     * <b>Error Handling:</b>
     * <ul>
     *   <li>Index build failures should be logged but not propagated to users</li>
     *   <li>Old indices remain available if rebuild fails</li>
     *   <li>Search operations continue using the last successful index</li>
     *   <li>Retry logic should be implemented for transient failures</li>
     * </ul>
     * </p>
     * <p>
     * <b>Example Usage:</b>
     * <pre>{@code
     * @Async
     * public void rebuildIndex() {
     *     try {
     *         List<Post> publishedPosts = postRepository.findAllPublishedPosts();
     *         List<IndexPostData> indexData = publishedPosts.stream()
     *             .map(post -> new IndexPostData(post.getId(), post.getTitle(), post.getDescription()))
     *             .collect(Collectors.toList());
     *         
     *         BuildIndexResponse response = aiSearchClient.buildIndex(indexData);
     *         logger.info("Index rebuilt successfully: {} posts indexed", response.getData().getCount());
     *     } catch (AIServiceException e) {
     *         logger.error("Failed to rebuild index", e);
     *         // Do not propagate exception - index rebuild is non-blocking
     *     }
     * }
     * }</pre>
     * </p>
     *
     * @param posts the list of published posts to index (must not be null, may be empty)
     *              Each post must have a valid ID, title, and description
     * @return a response object containing the build result with success status,
     *         message, and count of indexed posts, never null
     * @throws AIServiceException if the AI Search Service returns an error response
     *         (HTTP status >= 400 or response code != 200, including validation errors)
     * @throws com.storefinds.uniquefindsbackend.exception.AIServiceTimeoutException
     *         if the request exceeds the configured index build timeout
     * @throws com.storefinds.uniquefindsbackend.exception.AIServiceUnavailableException
     *         if the AI Search Service is not reachable (connection refused, host unreachable)
     * @throws com.storefinds.uniquefindsbackend.exception.AIServiceParseException
     *         if the response JSON cannot be parsed or is missing required fields
     * @throws IllegalArgumentException if posts is null
     */
    BuildIndexResponse buildIndex(List<IndexPostData> posts) throws AIServiceException;
}
