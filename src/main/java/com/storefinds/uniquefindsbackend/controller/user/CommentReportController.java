package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreateReportRequest;
import com.storefinds.uniquefindsbackend.dto.ReportResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.ReportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/comments/{commentId}/report")
public class CommentReportController {

    private final ReportService reportService;

    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Inject report service for comment report endpoint.
     * Params:
     * - reportService: report business service
     * Returns: None
     * Throws: None
     */
    public CommentReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Create one report against the specified comment.
     * Params:
     * - commentId: target comment id
     * - request: create report payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<ReportResponse>: created report detail
     * Throws:
     * - BusinessException: when current request is unauthenticated or target is unavailable
     */
    public Result<ReportResponse> reportComment(@PathVariable @Min(value = 1, message = "commentId must be greater than 0") Long commentId,
                                                @RequestBody @Valid CreateReportRequest request,
                                                Authentication authentication) {
        return reportService.reportComment(requireCurrentUser(authentication).userId(), commentId, request);
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Extract current authenticated user from spring security context.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - CurrentUser: authenticated principal wrapper
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    private CurrentUser requireCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new BusinessException("unauthorized");
        }
        return currentUser;
    }
}
