package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.ModerationActionRequest;
import com.storefinds.uniquefindsbackend.entity.Report;
import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.ModerationLogMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.ReportMapper;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Verify moderation flows emit the expected structured notification events.
 * Params: None
 * Returns: None
 * Throws: None
 */
class AdminModerationServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private ModerationLogMapper moderationLogMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InteractionEventService interactionEventService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AdminModerationServiceImpl adminModerationService;

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Verify rejecting a post produces one post moderation notification for the author.
     * Params: None
     * Returns: None
     * Throws: None
     */
    void rejectPostCreatesModerationNotification() {
        Post post = new Post();
        post.setId(5L);
        post.setUserId(2L);
        post.setStatus("PENDING_REVIEW");
        when(postMapper.selectById(5L)).thenReturn(post);
        when(postMapper.rejectById(5L, "spam")).thenReturn(1);

        ModerationActionRequest request = new ModerationActionRequest();
        request.setReason("spam");

        adminModerationService.rejectPost(1L, 5L, request);

        verify(notificationService).createNotification(2L, 1L, "POST_MODERATED", "POST", 5L, 5L);
    }

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Verify hiding a comment produces one comment moderation notification for the author.
     * Params: None
     * Returns: None
     * Throws: None
     */
    void hideCommentCreatesModerationNotification() {
        Comment comment = new Comment();
        comment.setId(8L);
        comment.setPostId(5L);
        comment.setUserId(3L);
        comment.setStatus("VISIBLE");
        when(commentMapper.selectById(8L)).thenReturn(comment);
        when(commentMapper.updateStatusById(8L, "HIDDEN")).thenReturn(1);

        ModerationActionRequest request = new ModerationActionRequest();
        request.setReason("abuse");

        adminModerationService.hideComment(1L, 8L, request);

        verify(notificationService).createNotification(3L, 1L, "COMMENT_MODERATED", "COMMENT", 8L, 5L);
    }

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Verify report listing responses expose target summary text for post and comment targets.
     * Params: None
     * Returns: None
     * Throws: None
     */
    void getReportsIncludesResolvedPostAndCommentTargetSummary() {
        Report postReport = new Report();
        postReport.setId(6L);
        postReport.setTargetType("POST");
        postReport.setTargetId(5L);
        postReport.setStatus("PENDING");

        Report commentReport = new Report();
        commentReport.setId(7L);
        commentReport.setTargetType("COMMENT");
        commentReport.setTargetId(8L);
        commentReport.setStatus("PENDING");

        Post post = new Post();
        post.setId(5L);
        post.setStatus("PUBLISHED");
        post.setTitle("Vintage Lamp");

        Comment comment = new Comment();
        comment.setId(8L);
        comment.setStatus("VISIBLE");
        comment.setContent("looks good");

        when(reportMapper.selectByFilter(null, null, 0, 20)).thenReturn(java.util.List.of(postReport, commentReport));
        when(reportMapper.countByFilter(null, null)).thenReturn(2L);
        when(postMapper.selectById(5L)).thenReturn(post);
        when(commentMapper.selectById(8L)).thenReturn(comment);

        var result = adminModerationService.getReports(null, null, 1, 20);

        assertEquals("Vintage Lamp", result.data().getItems().get(0).getTargetSummary());
        assertEquals("looks good", result.data().getItems().get(1).getTargetSummary());
    }

    @Test
    void resolveReportFailsWhenConcurrentUpdateAlreadyClosedIt() {
        Report report = new Report();
        report.setId(6L);
        report.setTargetType("POST");
        report.setTargetId(5L);
        report.setStatus("PENDING");
        when(reportMapper.selectById(6L)).thenReturn(report);
        when(reportMapper.updateStatus(6L, "RESOLVED", 1L, "APPROVE", null)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminModerationService.resolveReport(1L, 6L, null));

        assertEquals("report status does not allow resolve", ex.getMessage());
    }
}
