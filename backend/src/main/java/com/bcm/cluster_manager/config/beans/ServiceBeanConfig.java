package com.bcm.cluster_manager.config.beans;

import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.service.GroupService;
import com.bcm.shared.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceBeanConfig {

    @Bean
    public UserService userServiceCM (UserGroupRelationMapper userGroupRelationMapper,
                                      UserMapper userMapper,
                                      GroupMapper groupMapper) {
        return new UserService(userGroupRelationMapper, userMapper, groupMapper);
    }

    @Bean
    public GroupService groupServiceCM ( GroupMapper groupMapper) {
        return new GroupService(groupMapper);
    }

}
