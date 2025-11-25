package com.bcm.shared.repository;

import com.bcm.shared.filter.Filter;
import com.bcm.shared.model.database.User;
import com.bcm.shared.sort.SortOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt
            FROM users WHERE id = #{id}
            """)
    User findById(@Param("id") Long id);

    @Select("""
            SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt FROM users WHERE name = #{name}
            """)
    User findByName(String name);

    @Select("""
            SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt FROM users WHERE name ILIKE CONCAT('%', #{name}, '%')
            """)
    List<User> findByNameSubtext(String name);

    @Select("""
            SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt FROM users
            """)
    List<User> findAll();

    @Select("""
            <script>
            SELECT COUNT(*) FROM users
            <where>
                <if test="search != null and search != ''">
                    (name ILIKE CONCAT('%', #{search}, '%') OR CAST(id AS TEXT) ILIKE CONCAT('%', #{search}, '%'))
                </if>
                <if test="isUserEnabled != null">
                    AND enabled = #{isUserEnabled}
                </if>
            </where>
            </script>
            """)
    long getTotalCount(@Param("search") String search, @Param("isUserEnabled") Boolean isUserEnabled);

    @Select("""
            <script>
            SELECT id, name, password_hash AS passwordHash, enabled, created_at AS createdAt, updated_at AS updatedAt
            FROM users
            <where>
                <if test="search != null and search != ''">
                    (name ILIKE CONCAT('%', #{search}, '%') OR CAST(id AS TEXT) ILIKE CONCAT('%', #{search}, '%'))
                </if>
                <if test="isUserEnabled != null">
                    AND enabled = #{isUserEnabled}
                </if>
            </where>
            <choose>
                <when test="sortBy != null and sortBy != ''">
                    ORDER BY ${sortBy}
                    <choose>
                        <when test="sortOrder != null and sortOrder.name() == 'DESC'">
                            DESC
                        </when>
                        <otherwise>
                            ASC
                        </otherwise>
                    </choose>
                </when>
                <otherwise>
                    ORDER BY created_at DESC
                </otherwise>
            </choose>
            LIMIT #{itemsPerPage} OFFSET ${(page - 1) * itemsPerPage}
            </script>
            """)
    List<User> getPaginatedAndFilteredUsers(@Param("page") long page, @Param("itemsPerPage") long itemsPerPage,
                                            @Param("search") String search, @Param("sortBy") String sortBy,
                                            @Param("sortOrder") SortOrder sortOrder, @Param("isUserEnabled") Boolean isUserEnabled);

    @Insert("""
            INSERT INTO users (name, password_hash, enabled, created_at, updated_at)
            VALUES (#{name}, #{passwordHash}, #{enabled}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("""
            <script>
            UPDATE users
            <set>
                <if test="name != null">name = #{name},</if>
                <if test="passwordHash != null">password_hash = #{passwordHash},</if>
                <if test="enabled != null">enabled = #{enabled},</if>
                updated_at = #{updatedAt}
            </set>
            WHERE id = #{id}
            </script>
            """)
    int update(User user);

    @Delete("""
           DELETE FROM users
           WHERE id = #{id}
           """)
    int delete(Long id);
}
