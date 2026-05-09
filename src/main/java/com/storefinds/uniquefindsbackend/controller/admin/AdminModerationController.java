package com.storefinds.uniquefindsbackend.controller.admin;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.AdminPostModerationResponse;
import com.storefinds.uniquefindsbackend.dto.ModerationActionRequest;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.ReportResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.AdminModerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/moderation")
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Inject admin moderation service for governance endpoints.
     * Params:
     * - adminModerationService: admin moderation business service
     * Returns: None
     * Throws: None
     */
    public AdminModerationController(AdminModerationService adminModerationService) {
        this.adminModerationService = adminModerationService;
    }

    @GetMapping("/reports")
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
    public Result<PageResponse<ReportResponse>> getReports(@RequestParam(required = false) String targetType,
                                                           @RequestParam(required = false) String status,
                                                           @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                            @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize) {
        return adminModerationService.getReports(targetType, status, page, pageSize);
    }

    @GetMapping("/posts/pending")
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
    public Result<PageResponse<AdminPostModerationResponse>> getPendingPosts(@RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                                             @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize) {
        return adminModerationService.getPendingPosts(page, pageSize);
    }

    @PostMapping("/posts/{postId}/approve")
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Approve one post and make it published.
     * Params:
     * - postId: target post id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    public Result<Void> approvePost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                    Authentication authentication) {
        return adminModerationService.approvePost(requireCurrentUser(authentication).userId(), postId);
    }

    @PostMapping("/posts/{postId}/reject")
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Reject one post and save moderation reason.
     * Params:
     * - postId: target post id
     * - request: moderation action payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    public Result<Void> rejectPost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                   @RequestBody @Valid ModerationActionRequest request,
                                   Authentication authentication) {
        return adminModerationService.rejectPost(requireCurrentUser(authentication).userId(), postId, request);
    }

    @PostMapping("/posts/{postId}/hide")
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Hide one published post and save moderation reason.
     * Params:
     * - postId: target post id
     * - request: moderation action payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    public Result<Void> hidePost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                 @RequestBody @Valid ModerationActionRequest request,
                                 Authentication authentication) {
        return adminModerationService.hidePost(requireCurrentUser(authentication).userId(), postId, request);
    }

    @PostMapping("/comments/{commentId}/hide")
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Hide one visible comment by admin action.
     * Params:
     * - commentId: target comment id
     * - request: moderation action payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    public Result<Void> hideComment(@PathVariable @Min(value = 1, message = "commentId must be greater than 0") Long commentId,
                                    @RequestBody @Valid ModerationActionRequest request,
                                    Authentication authentication) {
        return adminModerationService.hideComment(requireCurrentUser(authentication).userId(), commentId, request);
    }

    @PostMapping("/comments/{commentId}/delete")
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Delete one comment by admin action using the existing deleted placeholder strategy.
     * Params:
     * - commentId: target comment id
     * - request: moderation action payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    public Result<Void> deleteComment(@PathVariable @Min(value = 1, message = "commentId must be greater than 0") Long commentId,
                                      @RequestBody @Valid ModerationActionRequest request,
                                      Authentication authentication) {
        return adminModerationService.deleteComment(requireCurrentUser(authentication).userId(), commentId, request);
    }

    @PostMapping("/reports/{reportId}/resolve")
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Mark one report as resolved.
     * Params:
     * - reportId: target report id
     * - request: optional moderation action payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    public Result<Void> resolveReport(@PathVariable @Min(value = 1, message = "reportId must be greater than 0") Long reportId,
                                      @RequestBody(required = false) ModerationActionRequest request,
                                      Authentication authentication) {
        return adminModerationService.resolveReport(requireCurrentUser(authentication).userId(), reportId, request);
    }

    @PostMapping("/reports/{reportId}/reject")
    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Mark one report as rejected when admin determines the report is invalid.
     * Params:
     * - reportId: target report id
     * - request: optional moderation action payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    public Result<Void> rejectReport(@PathVariable @Min(value = 1, message = "reportId must be greater than 0") Long reportId,
                                     @RequestBody(required = false) ModerationActionRequest request,
                                     Authentication authentication) {
        return adminModerationService.rejectReport(requireCurrentUser(authentication).userId(), reportId, request);
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-05-06
     * Purpose: Extract current authenticated admin user from spring security context.
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
