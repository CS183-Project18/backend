package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.common.CommentStatus;
import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.InteractionEventType;
import com.storefinds.uniquefindsbackend.common.PostStatus;
import com.storefinds.uniquefindsbackend.common.ReportStatus;
import com.storefinds.uniquefindsbackend.common.ReportReasonType;
import com.storefinds.uniquefindsbackend.common.ReportTargetType;
import com.storefinds.uniquefindsbackend.dto.CreateReportRequest;
import com.storefinds.uniquefindsbackend.dto.ReportResponse;
import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.Report;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.ReportMapper;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final InteractionEventService interactionEventService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-16
     * Purpose: Inject mapper and interaction event dependencies for report business logic.
     * Params:
     * - reportMapper: report data access mapper
     * - postMapper: post data access mapper
     * - commentMapper: comment data access mapper
     * - interactionEventService: interaction event recording service
     * Returns: None
     * Throws: None
     */
    public ReportServiceImpl(ReportMapper reportMapper,
                             PostMapper postMapper,
                             CommentMapper commentMapper,
                             InteractionEventService interactionEventService) {
        this.reportMapper = reportMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.interactionEventService = interactionEventService;
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-16
     * Purpose: Create one report against one published post while blocking self-reporting.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * - request: create report payload
     * Returns:
     * - Result<ReportResponse>: created report detail
     * Throws:
     * - BusinessException: when target is unavailable or duplicate pending report exists
     */
    public Result<ReportResponse> reportPost(Long userId, Long postId, CreateReportRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null || !PostStatus.PUBLISHED.equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "post is not available");
        }
        if (userId.equals(post.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "you cannot report your own post");
        }
        Report report = createReport(userId, ReportTargetType.POST, postId, request);
        return Result.success("report created", toReportResponse(reportMapper.selectById(report.getId())));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-16
     * Purpose: Create one report against one visible comment while blocking self-reporting.
     * Params:
     * - userId: current authenticated user id
     * - commentId: target comment id
     * - request: create report payload
     * Returns:
     * - Result<ReportResponse>: created report detail
     * Throws:
     * - BusinessException: when target is unavailable or duplicate pending report exists
     */
    public Result<ReportResponse> reportComment(Long userId, Long commentId, CreateReportRequest request) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !CommentStatus.VISIBLE.equalsIgnoreCase(comment.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "comment is not available");
        }
        if (userId.equals(comment.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "you cannot report your own comment");
        }
        Report report = createReport(userId, ReportTargetType.COMMENT, commentId, request);
        return Result.success("report created", toReportResponse(reportMapper.selectById(report.getId())));
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-16
     * Purpose: Build and persist one normalized report entity and record the submission event.
     * Params:
     * - userId: current authenticated user id
     * - targetType: report target type
     * - targetId: report target id
     * - request: create report payload
     * Returns:
     * - Report: created report entity
     * Throws:
     * - BusinessException: when duplicate pending report exists or reason type is invalid
     */
    private Report createReport(Long userId, String targetType, Long targetId, CreateReportRequest request) {
        if (reportMapper.countOpenByReporterAndTarget(userId, targetType, targetId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "you have already submitted a pending report for this target");
        }

        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReasonType(normalizeReasonType(request.getReasonType()));
        report.setReasonDetail(normalizeOptionalText(request.getReasonDetail()));
        report.setStatus(ReportStatus.PENDING);
        report.setResolutionAction(null);
        report.setResolutionNote(null);
        reportMapper.insert(report);
        interactionEventService.record(
                InteractionEventType.REPORT_SUBMIT,
                userId,
                resolvePostId(targetType, targetId),
                ReportTargetType.COMMENT.equals(targetType) ? targetId : null,
                targetType,
                targetId,
                buildReportMetadata(report)
        );
        return report;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-16
     * Purpose: Normalize one optional text field and convert blank to null.
     * Params:
     * - value: raw text value
     * Returns:
     * - String: normalized text value or null
     * Throws: None
     */
    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-16
     * Purpose: Normalize one report reason type and validate it against shared allowed values.
     * Params:
     * - reasonType: raw reason type value
     * Returns:
     * - String: normalized upper-cased reason type
     * Throws:
     * - BusinessException: when reason type is unsupported
     */
    private String normalizeReasonType(String reasonType) {
        String normalized = reasonType == null ? "" : reasonType.trim().toUpperCase();
        if (!ReportReasonType.isSupported(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "reasonType is invalid");
        }
        return normalized;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-17
     * Purpose: Convert report entity to report response object with moderation audit fields.
     * Params:
     * - report: source report entity
     * Returns:
     * - ReportResponse: response payload object
     * Throws: None
     */
    private ReportResponse toReportResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setReporterId(report.getReporterId());
        response.setReporterUsername(report.getReporterUsername());
        response.setTargetType(report.getTargetType());
        response.setTargetId(report.getTargetId());
        response.setReasonType(report.getReasonType());
        response.setReasonDetail(report.getReasonDetail());
        response.setStatus(report.getStatus());
        response.setResolutionAction(report.getResolutionAction());
        response.setResolutionNote(report.getResolutionNote());
        response.setHandledBy(report.getHandledBy());
        response.setHandledByUsername(report.getHandledByUsername());
        response.setHandledAt(report.getHandledAt());
        response.setCreatedAt(report.getCreatedAt());
        response.setTargetStatus(resolveTargetStatus(report));
        return response;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-16
     * Purpose: Resolve the related post id for one report target so the event ledger can keep post context.
     * Params:
     * - targetType: report target type
     * - targetId: report target id
     * Returns:
     * - Long: related post id or null
     * Throws: None
     */
    private Long resolvePostId(String targetType, Long targetId) {
        if (ReportTargetType.POST.equals(targetType)) {
            return targetId;
        }
        Comment comment = commentMapper.selectById(targetId);
        return comment == null ? null : comment.getPostId();
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-16
     * Purpose: Build structured metadata for one report submission event.
     * Params:
     * - report: created report entity
     * Returns:
     * - Map<String, Object>: structured metadata map
     * Throws: None
     */
    private Map<String, Object> buildReportMetadata(Report report) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("reasonType", report.getReasonType());
        metadata.put("status", report.getStatus());
        if (report.getReasonDetail() != null) {
            metadata.put("reasonDetail", report.getReasonDetail());
        }
        return metadata;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-17
     * Purpose: Resolve the latest moderation-visible status of the report target for admin response rendering.
     * Params:
     * - report: source report entity
     * Returns:
     * - String: target status or null
     * Throws: None
     */
    private String resolveTargetStatus(Report report) {
        if (ReportTargetType.POST.equals(report.getTargetType())) {
            Post post = postMapper.selectById(report.getTargetId());
            return post == null ? PostStatus.DELETED : post.getStatus();
        }
        if (ReportTargetType.COMMENT.equals(report.getTargetType())) {
            Comment comment = commentMapper.selectById(report.getTargetId());
            return comment == null ? CommentStatus.DELETED : comment.getStatus();
        }
        return null;
    }
}
