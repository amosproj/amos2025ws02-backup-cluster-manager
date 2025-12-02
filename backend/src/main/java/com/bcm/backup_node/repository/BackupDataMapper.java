package com.bcm.backup_node.repository;

import com.bcm.backup_node.model.database.BackupData;
import org.apache.ibatis.annotations.*;

import java.util.List;


/*

 * Store actual backup data, not metadata. Used on backup manager and backupnode.
 */
@Mapper
public interface BackupDataMapper {

    @Select("""
            SELECT
                id,
                backup_data,
                created_at AS createdAt
            FROM backups_data
            WHERE id = #{id}
            """)
    BackupData findById(Long id);

    //get All
    @Select("""
        SELECT
            id,
            backup_data,
            created_at AS createdAt
        FROM backups_data
        ORDER BY start_time DESC
        """)
    List<BackupData> findAll();

    @Insert("""
        INSERT INTO backups_data (
            id,                            
            backup_data,
            created_at
        ) VALUES (
            #{id},
            #{backup_data},
            #{createdAt}
        )
        """)
    int insert(BackupData b);

    @Update("""
        UPDATE backups_data SET
            id = #{id},
            backup_data = #{backup_data}
        WHERE id = #{id}
        """)
    int update(BackupData b);

    @Delete("""
            DELETE FROM backups_data
            WHERE id = #{id}
            """)
    int delete(Long id);
}
