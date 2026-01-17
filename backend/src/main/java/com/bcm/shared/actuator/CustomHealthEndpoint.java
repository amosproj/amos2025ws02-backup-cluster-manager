package com.bcm.shared.actuator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator/custom")
public class CustomHealthEndpoint {

    @GetMapping("/health")
    public Map<String, Object> customHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // CPU Information
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("availableProcessors", osBean.getAvailableProcessors());
        cpu.put("systemLoadAverage", osBean.getSystemLoadAverage());
        
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            cpu.put("processCpuLoad", sunOsBean.getProcessCpuLoad() * 100);
            cpu.put("systemCpuLoad", sunOsBean.getSystemCpuLoad() * 100);
        }
        health.put("cpu", cpu);
        
        // Memory Information
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memory = new HashMap<>();
        
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long heapCommitted = memoryBean.getHeapMemoryUsage().getCommitted();
        
        memory.put("heapUsedMB", heapUsed / (1024 * 1024));
        memory.put("heapMaxMB", heapMax / (1024 * 1024));
        memory.put("heapCommittedMB", heapCommitted / (1024 * 1024));
        memory.put("heapUsagePercent", (double) heapUsed / heapMax * 100);
        
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        memory.put("nonHeapUsedMB", nonHeapUsed / (1024 * 1024));
        
        health.put("memory", memory);
        
        // Storage Information
        File root = new File("/");
        Map<String, Object> storage = new HashMap<>();
        storage.put("totalSpaceGB", root.getTotalSpace() / (1024 * 1024 * 1024));
        storage.put("freeSpaceGB", root.getFreeSpace() / (1024 * 1024 * 1024));
        storage.put("usableSpaceGB", root.getUsableSpace() / (1024 * 1024 * 1024));
        storage.put("usedSpaceGB", (root.getTotalSpace() - root.getFreeSpace()) / (1024 * 1024 * 1024));
        storage.put("usagePercent", (double) (root.getTotalSpace() - root.getFreeSpace()) / root.getTotalSpace() * 100);
        
        health.put("storage", storage);
        
        return health;
    }
}
