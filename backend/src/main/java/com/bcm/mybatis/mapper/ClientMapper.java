package com.bcm.mybatis.mapper;

import com.bcm.mybatis.model.Client;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ClientMapper {
    Client findById(UUID id);
    Client findByNameOrIp(String nameOrIp);
    List<Client> findAll();
    int insert(Client c);
    int update(Client c);
    int delete(UUID id);
}
