package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.ModerationActionRequest;
import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.Post;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
