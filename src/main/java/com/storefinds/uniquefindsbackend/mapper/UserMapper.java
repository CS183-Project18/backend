package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Query one user by username.
     * Params:
     * - username: username value
     * Returns:
     * - User: matched user or null
     * Throws: None
     */
    User selectByUsername(@Param("username") String username);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Query one user by email.
     * Params:
     * - email: email value
     * Returns:
     * - User: matched user or null
     * Throws: None
     */
    User selectByEmail(@Param("email") String email);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Query one user by primary key.
     * Params:
     * - id: user id
     * Returns:
     * - User: matched user or null
     * Throws: None
     */
    User selectById(@Param("id") Long id);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Insert new user record.
     * Params:
     * - user: user entity to persist
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int insert(User user);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Update user's last login timestamp to now.
     * Params:
     * - userId: user id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int updateLastLoginAt(@Param("userId") Long userId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Update editable profile fields of one user.
     * Params:
     * - user: user entity carrying latest profile values
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int updateProfile(User user);
}
