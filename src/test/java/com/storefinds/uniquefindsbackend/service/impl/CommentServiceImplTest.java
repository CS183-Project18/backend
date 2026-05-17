package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentLikeMapper;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CommentLikeMapper commentLikeMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InteractionEventService interactionEventService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void pinCommentAllowsPostOwnerAndCreatesNotification() {
        Comment comment = new Comment();
        comment.setId(8L);
        comment.setPostId(5L);
        comment.setPostOwnerId(2L);
        comment.setUserId(9L);
        comment.setStatus("VISIBLE");

        when(commentMapper.selectById(8L)).thenReturn(comment);

        commentService.pinComment(2L, "USER", 8L);

        verify(commentMapper).clearPinnedByPostId(5L);
        verify(commentMapper).updatePinnedById(8L, 1);
        verify(notificationService).createNotification(9L, 2L, "COMMENT_PINNED", "COMMENT", 8L, 5L);
    }

    @Test
    void pinCommentRejectsUnauthorizedUser() {
        Comment comment = new Comment();
        comment.setId(8L);
        comment.setPostId(5L);
        comment.setPostOwnerId(2L);
        comment.setUserId(9L);
        comment.setStatus("VISIBLE");

        when(commentMapper.selectById(8L)).thenReturn(comment);

        BusinessException ex = assertThrows(BusinessException.class, () -> commentService.pinComment(3L, "USER", 8L));

        assertEquals("you cannot pin this comment", ex.getMessage());
        verify(commentMapper, never()).clearPinnedByPostId(5L);
    }

    @Test
    void createCommentReplySendsReplyNotification() {
        Post post = new Post();
        post.setId(5L);
        post.setStatus("PUBLISHED");

        Comment parentComment = new Comment();
        parentComment.setId(4L);
        parentComment.setPostId(5L);
        parentComment.setUserId(7L);
        parentComment.setStatus("VISIBLE");

        Comment createdComment = new Comment();
        createdComment.setId(10L);
        createdComment.setPostId(5L);
        createdComment.setUserId(2L);
        createdComment.setStatus("VISIBLE");

        when(postMapper.selectById(5L)).thenReturn(post);
        when(commentMapper.selectById(4L)).thenReturn(parentComment);
        when(commentMapper.selectById(10L)).thenReturn(createdComment);
        doAnswer(invocation -> {
            Comment inserted = invocation.getArgument(0);
            inserted.setId(10L);
            return 1;
        }).when(commentMapper).insert(any(Comment.class));

        var request = new com.storefinds.uniquefindsbackend.dto.CreateCommentRequest();
        request.setParentId(4L);
        request.setContent("reply");

        commentService.createComment(2L, 5L, request);

        verify(notificationService).createNotification(7L, 2L, "COMMENT_REPLIED", "COMMENT", 10L, 5L);
    }
}
