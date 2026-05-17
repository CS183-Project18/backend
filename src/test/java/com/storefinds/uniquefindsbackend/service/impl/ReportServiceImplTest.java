package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.CreateReportRequest;
import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.ReportMapper;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private InteractionEventService interactionEventService;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void cannotReportOwnPost() {
        Post post = new Post();
        post.setId(5L);
        post.setUserId(3L);
        post.setStatus("PUBLISHED");
        when(postMapper.selectById(5L)).thenReturn(post);

        CreateReportRequest request = new CreateReportRequest();
        request.setReasonType("SPAM");

        BusinessException ex = assertThrows(BusinessException.class, () -> reportService.reportPost(3L, 5L, request));
        assertEquals("you cannot report your own post", ex.getMessage());
        verify(reportMapper, never()).insert(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void cannotReportOwnComment() {
        Comment comment = new Comment();
        comment.setId(8L);
        comment.setUserId(3L);
        comment.setPostId(5L);
        comment.setStatus("VISIBLE");
        when(commentMapper.selectById(8L)).thenReturn(comment);

        CreateReportRequest request = new CreateReportRequest();
        request.setReasonType("ABUSE");

        BusinessException ex = assertThrows(BusinessException.class, () -> reportService.reportComment(3L, 8L, request));
        assertEquals("you cannot report your own comment", ex.getMessage());
    }
}
