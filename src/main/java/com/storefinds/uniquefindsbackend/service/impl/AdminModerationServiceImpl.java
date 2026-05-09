package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.AdminPostModerationResponse;
import com.storefinds.uniquefindsbackend.dto.ModerationActionRequest;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.ReportResponse;
import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.ModerationLog;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.Report;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.ModerationLogMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.ReportMapper;
import com.storefinds.uniquefindsbackend.service.AdminModerationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminModerationServiceImpl implements AdminModerationService {

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ModerationLogMapper moderationLogMapper;

    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Inject mapper dependencies for admin moderation business logic.
     * Params:
     * - reportMapper: report data access mapper
     * - postMapper: post data access mapper
     * - commentMapper: comment data access mapper
     * - moderationLogMapper: moderation log data access mapper
     * Returns: None
     * Throws: None
     */
    public AdminModerationServiceImpl(ReportMapper reportMapper,
                                      PostMapper postMapper,
                                      CommentMapper commentMapper,
                                      ModerationLogMapper moderationLogMapper) {
        this.reportMapper = reportMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.moderationLogMapper = moderationLogMapper;
    }

    @Override
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Query one page of reports matching admin filter conditions.
     * Params:
     * - targetType: optional report target type
     * - status: optional report status
     * - page: target page number starting from 1
     * - pageSize: target page size
     * Returns:
     * - Result<PageResponse<ReportResponse>>: matched report page
     * Throws: None
     */
    public Result<PageResponse<ReportResponse>> getReports(String targetType, String status, int page, int pageSize) {
        String normalizedTargetType = normalizeTargetType(targetType);
        String normalizedStatus = normalizeReportStatus(status);
        List<Report> reports = reportMapper.selectByFilter(normalizedTargetType, normalizedStatus, toOffset(page, pageSize), pageSize);
        PageResponse<ReportResponse> response = new PageResponse<>();
        response.setTotal(reportMapper.countByFilter(normalizedTargetType, normalizedStatus));
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(reports.stream().map(this::toReportResponse).toList());
        return Result.success(response);
    }

    @Override
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Query one page of posts currently pending review.
     * Params:
     * - page: target page number starting from 1
     * - pageSize: target page size
     * Returns:
     * - Result<PageResponse<AdminPostModerationResponse>>: pending post page
     * Throws: None
     */
    public Result<PageResponse<AdminPostModerationResponse>> getPendingPosts(int page, int pageSize) {
        List<Post> posts = postMapper.selectPendingReviewPosts(toOffset(page, pageSize), pageSize);
        PageResponse<AdminPostModerationResponse> response = new PageResponse<>();
        response.setTotal(postMapper.countPendingReviewPosts());
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(posts.stream().map(this::toAdminPostModerationResponse).toList());
        return Result.success(response);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Approve one post and make it published.
     * Params:
     * - adminUserId: current admin user id
     * - postId: target post id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when post is missing or deleted
     */
    public Result<Void> approvePost(Long adminUserId, Long postId) {
        requireExistingPost(postId);
        postMapper.approveById(postId);
        writeModerationLog("POST", postId, adminUserId, "APPROVE", null);
        return Result.success("post approved", null);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Reject one post and save moderation reason.
     * Params:
     * - adminUserId: current admin user id
     * - postId: target post id
     * - request: moderation action payload
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when post is missing or deleted
     */
    public Result<Void> rejectPost(Long adminUserId, Long postId, ModerationActionRequest request) {
        requireExistingPost(postId);
        String reason = normalizeRequiredReason(request);
        postMapper.rejectById(postId, reason);
        reportMapper.resolvePendingByTarget("POST", postId, adminUserId);
        writeModerationLog("POST", postId, adminUserId, "REJECT", reason);
        return Result.success("post rejected", null);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Hide one published post and save moderation reason.
     * Params:
     * - adminUserId: current admin user id
     * - postId: target post id
     * - request: moderation action payload
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when post is missing or deleted
     */
    public Result<Void> hidePost(Long adminUserId, Long postId, ModerationActionRequest request) {
        Post post = requireExistingPost(postId);
        if (!"PUBLISHED".equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException("post is not published");
        }
        String reason = normalizeRequiredReason(request);
        postMapper.hideById(postId, reason);
        reportMapper.resolvePendingByTarget("POST", postId, adminUserId);
        writeModerationLog("POST", postId, adminUserId, "HIDE", reason);
        return Result.success("post hidden", null);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Hide one visible comment by admin action.
     * Params:
     * - adminUserId: current admin user id
     * - commentId: target comment id
     * - request: moderation action payload
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when comment is missing or already deleted
     */
    public Result<Void> hideComment(Long adminUserId, Long commentId, ModerationActionRequest request) {
        Comment comment = requireModeratableComment(commentId);
        if (!"VISIBLE".equalsIgnoreCase(comment.getStatus())) {
            throw new BusinessException("comment is not visible");
        }
        String reason = normalizeRequiredReason(request);
        commentMapper.updateStatusById(commentId, "HIDDEN");
        reportMapper.resolvePendingByTarget("COMMENT", commentId, adminUserId);
        writeModerationLog("COMMENT", commentId, adminUserId, "HIDE", reason);
        return Result.success("comment hidden", null);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Delete one comment by admin action using the existing deleted placeholder strategy.
     * Params:
     * - adminUserId: current admin user id
     * - commentId: target comment id
     * - request: moderation action payload
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when comment is missing or already deleted
     */
    public Result<Void> deleteComment(Long adminUserId, Long commentId, ModerationActionRequest request) {
        requireModeratableComment(commentId);
        String reason = normalizeRequiredReason(request);
        commentMapper.updateStatusById(commentId, "DELETED");
        reportMapper.resolvePendingByTarget("COMMENT", commentId, adminUserId);
        writeModerationLog("COMMENT", commentId, adminUserId, "DELETE", reason);
        return Result.success("comment deleted", null);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Mark one report as resolved.
     * Params:
     * - adminUserId: current admin user id
     * - reportId: target report id
     * - request: moderation action payload
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when report does not exist
     */
    public Result<Void> resolveReport(Long adminUserId, Long reportId, ModerationActionRequest request) {
        Report report = requireReport(reportId);
        String reason = normalizeOptionalText(request == null ? null : request.getReason());
        reportMapper.updateStatus(reportId, "RESOLVED", adminUserId);
        writeModerationLog(report.getTargetType(), report.getTargetId(), adminUserId, "APPROVE", reason);
        return Result.success("report resolved", null);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Mark one report as rejected when admin determines the report is invalid.
     * Params:
     * - adminUserId: current admin user id
     * - reportId: target report id
     * - request: moderation action payload
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when report does not exist
     */
    public Result<Void> rejectReport(Long adminUserId, Long reportId, ModerationActionRequest request) {
        Report report = requireReport(reportId);
        String reason = normalizeOptionalText(request == null ? null : request.getReason());
        reportMapper.updateStatus(reportId, "REJECTED", adminUserId);
        writeModerationLog(report.getTargetType(), report.getTargetId(), adminUserId, "UNHIDE", reason);
        return Result.success("report rejected", null);
    }

    private Report requireReport(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("report not found");
        }
        return report;
    }

    private Post requireExistingPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || "DELETED".equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException("post not found");
        }
        return post;
    }

    private Comment requireModeratableComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || "DELETED".equalsIgnoreCase(comment.getStatus())) {
            throw new BusinessException("comment not found");
        }
        return comment;
    }

    private void writeModerationLog(String targetType, Long targetId, Long moderatorId, String action, String reason) {
        ModerationLog log = new ModerationLog();
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setModeratorId(moderatorId);
        log.setAction(action);
        log.setReason(reason);
        moderationLogMapper.insert(log);
    }

    private int toOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    private String normalizeRequiredReason(ModerationActionRequest request) {
        String normalized = normalizeOptionalText(request == null ? null : request.getReason());
        if (normalized == null) {
            throw new BusinessException("reason is required");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeTargetType(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toUpperCase();
        return switch (normalized) {
            case "POST", "COMMENT", "USER" -> normalized;
            default -> throw new BusinessException("targetType is invalid");
        };
    }

    private String normalizeReportStatus(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toUpperCase();
        return switch (normalized) {
            case "PENDING", "PROCESSING", "RESOLVED", "REJECTED" -> normalized;
            default -> throw new BusinessException("status is invalid");
        };
    }

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

    private AdminPostModerationResponse toAdminPostModerationResponse(Post post) {
        AdminPostModerationResponse response = new AdminPostModerationResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setAuthorUsername(post.getAuthorUsername());
        response.setStoreId(post.getStoreId());
        response.setCategoryId(post.getCategoryId());
        response.setTitle(post.getTitle());
        response.setDescription(post.getDescription());
        response.setStatus(post.getStatus());
        response.setModerationReason(post.getModerationReason());
        response.setPublishedAt(post.getPublishedAt());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        return response;
    }
}
