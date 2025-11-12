package com.bcm.shared.node_roles;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("backup_manager")
@ComponentScan("com.bcm.backup_manager")
public class BackupManagerConfig {}
