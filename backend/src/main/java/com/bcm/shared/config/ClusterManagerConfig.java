package com.bcm.shared.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("cluster_manager")
@ComponentScan("com.bcm.cluster_manager")
public class ClusterManagerConfig {}
