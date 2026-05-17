package com.storefinds.uniquefindsbackend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.InteractionEventType;
import com.storefinds.uniquefindsbackend.entity.InteractionEvent;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.InteractionEventMapper;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-15
 * Purpose: Persist normalized social and governance interaction events with JSON metadata support.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class InteractionEventServiceImpl implements InteractionEventService {

    private final InteractionEventMapper interactionEventMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-15
     * Purpose: Inject mapper dependency for interaction event persistence.
     * Params:
     * - interactionEventMapper: event data access mapper
     * Returns: None
     * Throws: None
     */
    public InteractionEventServiceImpl(InteractionEventMapper interactionEventMapper) {
        this.interactionEventMapper = interactionEventMapper;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-15
     * Purpose: Persist one supported interaction event with normalized target references and optional metadata.
     * Params:
     * - eventType: stable event type
     * - userId: actor user id
     * - postId: related post id
     * - commentId: related comment id
     * - targetType: target type value
     * - targetId: target entity id
     * - metadata: optional structured metadata
     * Returns: None
     * Throws:
     * - BusinessException: when event type is unsupported or metadata serialization fails
     */
    public void record(String eventType,
                       Long userId,
                       Long postId,
                       Long commentId,
                       String targetType,
                       Long targetId,
                       Map<String, Object> metadata) {
        if (!InteractionEventType.isSupported(eventType)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "interaction event type is invalid");
        }
        InteractionEvent event = new InteractionEvent();
        event.setEventType(eventType);
        event.setUserId(userId);
        event.setPostId(postId);
        event.setCommentId(commentId);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setEventValue(BigDecimal.ONE);
        event.setMetadata(toJson(metadata));
        interactionEventMapper.insert(event);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-15
     * Purpose: Serialize one metadata map to JSON for event persistence.
     * Params:
     * - metadata: optional metadata map
     * Returns:
     * - String: serialized JSON or null
     * Throws:
     * - BusinessException: when serialization fails
     */
    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to serialize interaction metadata");
        }
    }
}
