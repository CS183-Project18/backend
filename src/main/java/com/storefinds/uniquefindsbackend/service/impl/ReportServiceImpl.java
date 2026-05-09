package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreateReportRequest;
import com.storefinds.uniquefindsbackend.dto.ReportResponse;
import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.Report;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.ReportMapper;
import com.storefinds.uniquefindsbackend.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Inject mapper dependencies for report business logic.
     * Params:
     * - reportMapper: report data access mapper
     * - postMapper: post data access mapper
     * - commentMapper: comment data access mapper
     * Returns: None
     * Throws: None
     */
    public ReportServiceImpl(ReportMapper reportMapper, PostMapper postMapper, CommentMapper commentMapper) {
        this.reportMapper = reportMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Create one report against one published post.
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
        if (post == null || !"PUBLISHED".equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException("post is not available");
        }
        Report report = createReport(userId, "POST", postId, request);
        return Result.success("report created", toReportResponse(reportMapper.selectById(report.getId())));
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Create one report against one visible comment.
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
        if (comment == null || !"VISIBLE".equalsIgnoreCase(comment.getStatus())) {
            throw new BusinessException("comment is not available");
        }
        Report report = createReport(userId, "COMMENT", commentId, request);
        return Result.success("report created", toReportResponse(reportMapper.selectById(report.getId())));
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Build and persist one normalized report entity.
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
            throw new BusinessException("you have already submitted a pending report for this target");
        }

        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReasonType(normalizeReasonType(request.getReasonType()));
        report.setReasonDetail(normalizeOptionalText(request.getReasonDetail()));
        report.setStatus("PENDING");
        reportMapper.insert(report);
        return report;
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
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
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Normalize one report reason type and validate it against allowed enum values.
     * Params:
     * - reasonType: raw reason type value
     * Returns:
     * - String: normalized upper-cased reason type
     * Throws:
     * - BusinessException: when reason type is unsupported
     */
    private String normalizeReasonType(String reasonType) {
        String normalized = reasonType == null ? "" : reasonType.trim().toUpperCase();
        return switch (normalized) {
            case "SPAM", "ILLEGAL", "ABUSE", "PORN", "MISLEADING", "OTHER" -> normalized;
            default -> throw new BusinessException("reasonType is invalid");
        };
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Convert report entity to report response object.
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
        response.setHandledBy(report.getHandledBy());
        response.setHandledByUsername(report.getHandledByUsername());
        response.setHandledAt(report.getHandledAt());
        response.setCreatedAt(report.getCreatedAt());
        return response;
    }
}
