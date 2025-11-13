package com.bcm.shared.repository;

import com.bcm.shared.model.database.Client;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClientMapper {


    Client findById(Long id);


    Client findByNameOrIp(String nameOrIp);


    List<Client> findAll();


    int insert(Client c);


    int update(Client c);


    int delete(Long id);


}
