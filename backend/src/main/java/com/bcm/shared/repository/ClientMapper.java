package com.bcm.shared.repository;

import com.bcm.shared.model.database.Client;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClientMapper {

    @Select("""
            SELECT id, name_or_ip AS nameOrIp, enabled, created_at AS createdAt, updated_at AS updatedAt
            FROM clients
            WHERE id = #{id}
            """)
    Client findById(Long id);

    @Select("""
            SELECT id, name_or_ip AS nameOrIp, enabled, created_at AS createdAt, updated_at AS updatedAt
            FROM clients
            WHERE name_or_ip = #{nameOrIp}
            """)
    Client findByNameOrIp(String nameOrIp);

    @Select("""
            SELECT id, name_or_ip AS nameOrIp, enabled, created_at AS createdAt, updated_at AS updatedAt
            FROM clients
            """)
    List<Client> findAll();

    @Insert("""
            INSERT INTO clients (name_or_ip, enabled, created_at, updated_at)
            VALUES (#{nameOrIp}, #{enabled}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Client c);

    @Update("""
            UPDATE clients
            SET name_or_ip = #{nameOrIp},
                enabled = #{enabled},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int update(Client c);

    @Delete("""
            DELETE FROM clients
            WHERE id = #{id}
            """)
    int delete(Long id);
}
