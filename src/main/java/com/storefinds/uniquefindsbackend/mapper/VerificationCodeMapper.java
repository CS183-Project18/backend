package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VerificationCodeMapper {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Insert a newly generated verification code record.
     * Params:
     * - verificationCode: verification code entity
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int insert(VerificationCode verificationCode);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Query latest login verification code regardless of status.
     * Params:
     * - email: target email
     * Returns:
     * - VerificationCode: latest code record or null
     * Throws: None
     */
    VerificationCode selectLatestLoginCodeByEmail(@Param("email") String email);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Query latest pending login verification code.
     * Params:
     * - email: target email
     * Returns:
     * - VerificationCode: latest pending code record or null
     * Throws: None
     */
    VerificationCode selectLatestPendingLoginCodeByEmail(@Param("email") String email);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Mark specified code as used.
     * Params:
     * - id: verification code id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int markAsUsed(@Param("id") Long id);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Mark specified code as expired.
     * Params:
     * - id: verification code id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int markAsExpired(@Param("id") Long id);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Increase wrong-attempt counter and lock code when threshold reached.
     * Params:
     * - id: verification code id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int increaseAttemptAndLockIfNeeded(@Param("id") Long id);
}
