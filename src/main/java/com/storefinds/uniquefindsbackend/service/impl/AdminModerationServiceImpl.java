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
import com.storefinds.uniquefindsbackend.dto.ModerationLogResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostImageResponse;
import com.storefinds.uniquefindsbackend.dto.ReportResponse;
import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.ModerationLog;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.PostImage;
import com.storefinds.uniquefindsbackend.entity.Report;
import com.storefinds.uniquefindsbackend.event.PostStatusChangedEvent;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.ModerationLogMapper;
import com.storefinds.uniquefindsbackend.mapper.PostImageMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.ReportMapper;
import com.storefinds.uniquefindsbackend.service.AdminModerationService;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * Author: Enqi Guo
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
    private final PostImageMapper postImageMapper;
    private final CommentMapper commentMapper;
    private final ModerationLogMapper moderationLogMapper;
    private final NotificationService notificationService;
    private final InteractionEventService interactionEventService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AdminModerationServiceImpl(ReportMapper reportMapper,
                                      PostMapper postMapper,
                                      PostImageMapper postImageMapper,
                                      CommentMapper commentMapper,
                                      ModerationLogMapper moderationLogMapper,
                                      NotificationService notificationService,
                                      InteractionEventService interactionEventService,
                                      ApplicationEventPublisher applicationEventPublisher) {
        this.reportMapper = reportMapper;
        this.postMapper = postMapper;
        this.postImageMapper = postImageMapper;
        this.commentMapper = commentMapper;
        this.moderationLogMapper = moderationLogMapper;
        this.notificationService = notificationService;
        this.interactionEventService = interactionEventService;
        this.applicationEventPublisher = applicationEventPublisher;
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
    /**
     * Author: Enqi Guo
     * Date: 2026-05-22
     * Purpose: Query moderation audit logs with optional target, moderator, action, and time filters.
     * Params:
     * - targetType: optional target type
     * - targetId: optional target id
     * - moderatorId: optional moderator id
     * - action: optional moderation action
     * - startTime: optional created-at lower bound
     * - endTime: optional created-at upper bound
     * - page: target page number starting from 1
     * - pageSize: target page size
     * Returns:
     * - Result<PageResponse<ModerationLogResponse>>: matched moderation log page
     * Throws: None
     */
    public Result<PageResponse<ModerationLogResponse>> getModerationLogs(String targetType,
                                                                         Long targetId,
                                                                         Long moderatorId,
                                                                         String action,
                                                                         LocalDateTime startTime,
                                                                         LocalDateTime endTime,
                                                                         int page,
                                                                         int pageSize) {
        String normalizedTargetType = normalizeTargetType(targetType);
        String normalizedAction = normalizeOptionalUpper(action);
        List<ModerationLog> logs = moderationLogMapper.selectByFilter(
                normalizedTargetType,
                targetId,
                moderatorId,
                normalizedAction,
                startTime,
                endTime,
                toOffset(page, pageSize),
                pageSize
        );
        PageResponse<ModerationLogResponse> response = new PageResponse<>();
        response.setTotal(moderationLogMapper.countByFilter(
                normalizedTargetType,
                targetId,
                moderatorId,
                normalizedAction,
                startTime,
                endTime
        ));
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(logs.stream().map(this::toModerationLogResponse).toList());
        return Result.success(response);
    }

    @Override
    public Result<PageResponse<AdminPostModerationResponse>> getPendingPosts(int page, int pageSize) {
        List<Post> posts = postMapper.selectPendingReviewPosts(toOffset(page, pageSize), pageSize);
        Map<Long, List<PostImageResponse>> imagesByPostId = groupPostImages(posts);
        PageResponse<AdminPostModerationResponse> response = new PageResponse<>();
        response.setTotal(postMapper.countPendingReviewPosts());
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(posts.stream()
                .map(post -> toAdminPostModerationResponse(post, imagesByPostId.getOrDefault(post.getId(), List.of())))
                .toList());
        return Result.success(response);
    }

    @Override
    @Transactional
    public Result<Void> approvePost(Long adminUserId, Long postId) {
        Post post = requireExistingPost(postId);
        if (postMapper.approveById(postId) == 0) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "post status does not allow approval");
        }
        applicationEventPublisher.publishEvent(new PostStatusChangedEvent(postId, post.getStatus(), PostStatus.PUBLISHED));
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
     * Author: Enqi Guo
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
        applicationEventPublisher.publishEvent(new PostStatusChangedEvent(postId, post.getStatus(), PostStatus.REJECTED));
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
        applicationEventPublisher.publishEvent(new PostStatusChangedEvent(postId, post.getStatus(), PostStatus.HIDDEN));
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
     * Author: Enqi Guo
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
     * Author: Enqi Guo
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
        if (reportMapper.updateStatus(reportId, ReportStatus.RESOLVED, adminUserId, ModerationActionType.APPROVE, reason) == 0) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "report status does not allow resolve");
        }
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
        notifyReporter(report, adminUserId, NotificationEventType.REPORT_RESOLVED);
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
        if (reportMapper.updateStatus(reportId, ReportStatus.REJECTED, adminUserId, ModerationActionType.UNHIDE, reason) == 0) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "report status does not allow reject");
        }
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
        notifyReporter(report, adminUserId, NotificationEventType.REPORT_REJECTED);
        log.info("report rejected: reportId={}, adminUserId={}", reportId, adminUserId);
        return Result.success("report rejected", null);
    }

    /**
     * Author: Enqi Guo
     * Date: 2026-05-22
     * Purpose: Notify the original reporter when a moderation report is closed.
     * Params:
     * - report: closed report entity
     * - adminUserId: moderator user id
     * - eventType: report closure notification event type
     * Returns: None
     * Throws: None
     */
    private void notifyReporter(Report report, Long adminUserId, String eventType) {
        notificationService.createNotification(
                report.getReporterId(),
                adminUserId,
                eventType,
                report.getTargetType(),
                report.getTargetId(),
                resolveRelatedPostId(report)
        );
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

    private String normalizeOptionalUpper(String value) {
        String normalized = normalizeOptionalText(value);
        return normalized == null ? null : normalized.toUpperCase();
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
        response.setTargetSummary(resolveTargetSummary(report));
        return response;
    }

    private AdminPostModerationResponse toAdminPostModerationResponse(Post post, List<PostImageResponse> images) {
        AdminPostModerationResponse response = new AdminPostModerationResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setAuthorUsername(post.getAuthorUsername());
        response.setStoreId(post.getStoreId());
        response.setCategoryId(post.getCategoryId());
        response.setTitle(post.getTitle());
        response.setDescription(post.getDescription());
        response.setImages(images);
        response.setStatus(post.getStatus());
        response.setModerationReason(post.getModerationReason());
        response.setPublishedAt(post.getPublishedAt());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        return response;
    }

    private Map<Long, List<PostImageResponse>> groupPostImages(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Map.of();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, List<PostImageResponse>> imagesByPostId = new LinkedHashMap<>();
        for (PostImage image : postImageMapper.selectByPostIds(postIds)) {
            imagesByPostId.computeIfAbsent(image.getPostId(), ignored -> new ArrayList<>())
                    .add(toPostImageResponse(image));
        }
        return imagesByPostId;
    }

    private PostImageResponse toPostImageResponse(PostImage image) {
        PostImageResponse response = new PostImageResponse();
        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setImageKey(image.getImageKey());
        response.setThumbnailUrl(image.getThumbnailUrl() == null ? image.getImageUrl() : image.getThumbnailUrl());
        response.setWidth(image.getWidth());
        response.setHeight(image.getHeight());
        response.setFileSize(image.getFileSize());
        response.setMimeType(image.getMimeType());
        response.setSortOrder(image.getSortOrder());
        response.setIsCover(image.getIsCover());
        return response;
    }

    private ModerationLogResponse toModerationLogResponse(ModerationLog log) {
        ModerationLogResponse response = new ModerationLogResponse();
        response.setId(log.getId());
        response.setTargetType(log.getTargetType());
        response.setTargetId(log.getTargetId());
        response.setModeratorId(log.getModeratorId());
        response.setModeratorUsername(log.getModeratorUsername());
        response.setAction(log.getAction());
        response.setReason(log.getReason());
        response.setTargetSummary(resolveTargetSummary(log.getTargetType(), log.getTargetId()));
        response.setCreatedAt(log.getCreatedAt());
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

    /**
     * Author: Enqi Guo
     * Date: 2026-05-18
     * Purpose: Resolve one short target summary string for admin report lists to reduce extra frontend lookups.
     * Params:
     * - report: source report entity
     * Returns:
     * - String: target summary text or null
     * Throws: None
     */
    private String resolveTargetSummary(Report report) {
        return resolveTargetSummary(report.getTargetType(), report.getTargetId());
    }

    /**
     * Author: Enqi Guo
     * Date: 2026-05-22
     * Purpose: Resolve one short target summary string for report and audit log responses.
     * Params:
     * - targetType: target type
     * - targetId: target id
     * Returns:
     * - String: target summary text or null
     * Throws: None
     */
    private String resolveTargetSummary(String targetType, Long targetId) {
        if (ReportTargetType.POST.equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            return post == null ? null : post.getTitle();
        }
        if (ReportTargetType.COMMENT.equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            return comment == null ? null : comment.getContent();
        }
        return null;
    }
}
