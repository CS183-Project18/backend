package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Insert one new report record.
     * Params:
     * - report: report entity to persist
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int insert(Report report);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Count all open reports submitted by one user for one target.
     * Params:
     * - reporterId: reporter user id
     * - targetType: report target type
     * - targetId: report target id
     * Returns:
     * - long: matched report count
     * Throws: None
     */
    long countOpenByReporterAndTarget(@Param("reporterId") Long reporterId,
                                      @Param("targetType") String targetType,
                                      @Param("targetId") Long targetId);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Count all reports matching admin filter conditions.
     * Params:
     * - targetType: optional target type
     * - status: optional report status
     * Returns:
     * - long: matched report count
     * Throws: None
     */
    long countByFilter(@Param("targetType") String targetType, @Param("status") String status);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Query one page of reports matching admin filter conditions.
     * Params:
     * - targetType: optional target type
     * - status: optional report status
     * - offset: row offset
     * - pageSize: target page size
     * Returns:
     * - List<Report>: matched report page list
     * Throws: None
     */
    List<Report> selectByFilter(@Param("targetType") String targetType,
                                @Param("status") String status,
                                @Param("offset") int offset,
                                @Param("pageSize") int pageSize);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Query one report by primary key with reporter and handler display info.
     * Params:
     * - id: report id
     * Returns:
     * - Report: matched report or null
     * Throws: None
     */
    Report selectById(@Param("id") Long id);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Update one report's status and handler information.
     * Params:
     * - id: report id
     * - status: new report status
     * - handledBy: admin user id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("handledBy") Long handledBy);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Resolve all pending reports for one moderation target.
     * Params:
     * - targetType: report target type
     * - targetId: report target id
     * - handledBy: admin user id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int resolvePendingByTarget(@Param("targetType") String targetType,
                               @Param("targetId") Long targetId,
                               @Param("handledBy") Long handledBy);
}
