package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.common.CommentStatus;
import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.InteractionEventType;
import com.storefinds.uniquefindsbackend.common.ModerationActionType;
import com.storefinds.uniquefindsbackend.common.NotificationEventType;
import com.storefinds.uniquefindsbackend.common.NotificationTargetType;
import com.storefinds.uniquefindsbackend.common.PostStatus;
import com.storefinds.uniquefindsbackend.common.ReportStatus;
import com.storefinds.uniquefindsbackend.common.ReportTargetType;
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
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Implement moderation workflows for posts, comments, reports, and related moderation notifications.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AdminModerationServiceImpl implements AdminModerationService {

    private static final Logger log = LoggerFactory.getLogger(AdminModerationServiceImpl.class);

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ModerationLogMapper moderationLogMapper;
    private final NotificationService notificationService;
    private final InteractionEventService interactionEventService;

    public AdminModerationServiceImpl(ReportMapper reportMapper,
                                      PostMapper postMapper,
                                      CommentMapper commentMapper,
                                      ModerationLogMapper moderationLogMapper,
                                      NotificationService notificationService,
                                      InteractionEventService interactionEventService) {
        this.reportMapper = reportMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.moderationLogMapper = moderationLogMapper;
        this.notificationService = notificationService;
        this.interactionEventService = interactionEventService;
    }

    @Override
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
    public Result<Void> approvePost(Long adminUserId, Long postId) {
        Post post = requireExistingPost(postId);
        if (postMapper.approveById(postId) == 0) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "post status does not allow approval");
        }
        writeModerationLog(ReportTargetType.POST, postId, adminUserId, ModerationActionType.APPROVE, null);
        notificationService.createNotification(post.getUserId(),
                adminUserId,
                NotificationEventType.POST_MODERATED,
                NotificationTargetType.POST,
                postId,
                postId);
        return Result.success("post approved", null);
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Reject one pending post and notify the author about the moderation outcome.
     * Params:
     * - adminUserId: moderator user id
     * - postId: target post id
     * - request: moderation action payload
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the post or reason is invalid
     */
    public Result<Void> rejectPost(Long adminUserId, Long postId, ModerationActionRequest request) {
        Post post = requireExistingPost(postId);
        String reason = normalizeRequiredReason(request);
        if (postMapper.rejectById(postId, reason) == 0) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "post status does not allow rejection");
        }
        reportMapper.resolvePendingByTarget(ReportTargetType.POST, postId, adminUserId, ModerationActionType.TARGET_MODERATED, reason);
        writeModerationLog(ReportTargetType.POST, postId, adminUserId, ModerationActionType.REJECT, reason);
        notificationService.createNotification(post.getUserId(),
                adminUserId,
                NotificationEventType.POST_MODERATED,
                NotificationTargetType.POST,
                postId,
                postId);
        return Result.success("post rejected", null);
    }

    @Override
    @Transactional
    public Result<Void> hidePost(Long adminUserId, Long postId, ModerationActionRequest request) {
        Post post = requireExistingPost(postId);
        if (!PostStatus.PUBLISHED.equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "post is not published");
        }
        String reason = normalizeRequiredReason(request);
        if (postMapper.hideById(postId, reason) == 0) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "post status does not allow hide");
        }
        reportMapper.resolvePendingByTarget(ReportTargetType.POST, postId, adminUserId, ModerationActionType.TARGET_MODERATED, reason);
        writeModerationLog(ReportTargetType.POST, postId, adminUserId, ModerationActionType.HIDE, reason);
        notificationService.createNotification(post.getUserId(),
                adminUserId,
                NotificationEventType.POST_MODERATED,
                NotificationTargetType.POST,
                postId,
                postId);
        return Result.success("post hidden", null);
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Hide one visible comment and notify the comment author about the moderation outcome.
     * Params:
     * - adminUserId: moderator user id
     * - commentId: target comment id
     * - request: moderation action payload
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the comment or reason is invalid
     */
    public Result<Void> hideComment(Long adminUserId, Long commentId, ModerationActionRequest request) {
        Comment comment = requireModeratableComment(commentId);
        if (!CommentStatus.VISIBLE.equalsIgnoreCase(comment.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "comment is not visible");
        }
        String reason = normalizeRequiredReason(request);
        if (commentMapper.updateStatusById(commentId, CommentStatus.HIDDEN) == 0) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "comment status does not allow hide");
        }
        reportMapper.resolvePendingByTarget(ReportTargetType.COMMENT, commentId, adminUserId, ModerationActionType.TARGET_MODERATED, reason);
        writeModerationLog(ReportTargetType.COMMENT, commentId, adminUserId, ModerationActionType.HIDE, reason);
        notificationService.createNotification(comment.getUserId(),
                adminUserId,
                NotificationEventType.COMMENT_MODERATED,
                NotificationTargetType.COMMENT,
                commentId,
                comment.getPostId());
        return Result.success("comment hidden", null);
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Delete one comment through moderation flow and notify the comment author.
     * Params:
     * - adminUserId: moderator user id
     * - commentId: target comment id
     * - request: moderation action payload
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the comment or reason is invalid
     */
    public Result<Void> deleteComment(Long adminUserId, Long commentId, ModerationActionRequest request) {
        Comment comment = requireModeratableComment(commentId);
        String reason = normalizeRequiredReason(request);
        if (commentMapper.updateStatusById(commentId, CommentStatus.DELETED) == 0) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "comment status does not allow delete");
        }
        reportMapper.resolvePendingByTarget(ReportTargetType.COMMENT, commentId, adminUserId, ModerationActionType.TARGET_MODERATED, reason);
        writeModerationLog(ReportTargetType.COMMENT, commentId, adminUserId, ModerationActionType.DELETE, reason);
        notificationService.createNotification(comment.getUserId(),
                adminUserId,
                NotificationEventType.COMMENT_MODERATED,
                NotificationTargetType.COMMENT,
                commentId,
                comment.getPostId());
        return Result.success("comment deleted", null);
    }

    @Override
    @Transactional
    public Result<Void> resolveReport(Long adminUserId, Long reportId, ModerationActionRequest request) {
        Report report = requireReport(reportId);
        String reason = normalizeOptionalText(request == null ? null : request.getReason());
        if (!isOpenReportStatus(report.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "report status does not allow resolve");
        }
        reportMapper.updateStatus(reportId, ReportStatus.RESOLVED, adminUserId, ModerationActionType.APPROVE, reason);
        writeModerationLog(report.getTargetType(), report.getTargetId(), adminUserId, ModerationActionType.APPROVE, reason);
        interactionEventService.record(
                InteractionEventType.REPORT_CLOSE,
                adminUserId,
                resolveRelatedPostId(report),
                ReportTargetType.COMMENT.equals(report.getTargetType()) ? report.getTargetId() : null,
                report.getTargetType(),
                report.getTargetId(),
                Map.of("reportId", reportId, "status", ReportStatus.RESOLVED, "resolutionAction", ModerationActionType.APPROVE)
        );
        log.info("report resolved: reportId={}, adminUserId={}", reportId, adminUserId);
        return Result.success("report resolved", null);
    }

    @Override
    @Transactional
    public Result<Void> rejectReport(Long adminUserId, Long reportId, ModerationActionRequest request) {
        Report report = requireReport(reportId);
        String reason = normalizeOptionalText(request == null ? null : request.getReason());
        if (!isOpenReportStatus(report.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "report status does not allow reject");
        }
        reportMapper.updateStatus(reportId, ReportStatus.REJECTED, adminUserId, ModerationActionType.UNHIDE, reason);
        writeModerationLog(report.getTargetType(), report.getTargetId(), adminUserId, ModerationActionType.UNHIDE, reason);
        interactionEventService.record(
                InteractionEventType.REPORT_CLOSE,
                adminUserId,
                resolveRelatedPostId(report),
                ReportTargetType.COMMENT.equals(report.getTargetType()) ? report.getTargetId() : null,
                report.getTargetType(),
                report.getTargetId(),
                Map.of("reportId", reportId, "status", ReportStatus.REJECTED, "resolutionAction", ModerationActionType.UNHIDE)
        );
        log.info("report rejected: reportId={}, adminUserId={}", reportId, adminUserId);
        return Result.success("report rejected", null);
    }

    private Report requireReport(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "report not found");
        }
        return report;
    }

    private Post requireExistingPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || PostStatus.DELETED.equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "post not found");
        }
        return post;
    }

    private Comment requireModeratableComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || CommentStatus.DELETED.equalsIgnoreCase(comment.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "comment not found");
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
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "reason is required");
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
            case ReportTargetType.POST, ReportTargetType.COMMENT, ReportTargetType.USER -> normalized;
            default -> throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "targetType is invalid");
        };
    }

    private String normalizeReportStatus(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toUpperCase();
        return switch (normalized) {
            case ReportStatus.PENDING, ReportStatus.PROCESSING, ReportStatus.RESOLVED, ReportStatus.REJECTED -> normalized;
            default -> throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "status is invalid");
        };
    }

    private boolean isOpenReportStatus(String status) {
        return ReportStatus.PENDING.equalsIgnoreCase(status) || ReportStatus.PROCESSING.equalsIgnoreCase(status);
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
        response.setResolutionAction(report.getResolutionAction());
        response.setResolutionNote(report.getResolutionNote());
        response.setHandledBy(report.getHandledBy());
        response.setHandledByUsername(report.getHandledByUsername());
        response.setHandledAt(report.getHandledAt());
        response.setCreatedAt(report.getCreatedAt());
        response.setTargetStatus(resolveTargetStatus(report));
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

    private Long resolveRelatedPostId(Report report) {
        if (ReportTargetType.POST.equals(report.getTargetType())) {
            return report.getTargetId();
        }
        Comment comment = commentMapper.selectById(report.getTargetId());
        return comment == null ? null : comment.getPostId();
    }

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
