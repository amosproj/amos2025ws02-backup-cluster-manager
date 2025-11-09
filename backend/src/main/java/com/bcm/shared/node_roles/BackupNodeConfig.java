package com.bcm.shared.node_roles;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("backup_node")
@ComponentScan("com.bcm.backup_node")
public class BackupNodeConfig {}
