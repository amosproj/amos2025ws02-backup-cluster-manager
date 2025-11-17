package com.bcm.shared;


import com.bcm.shared.model.api.ClusterTablesDTO;
import com.bcm.shared.service.LocalTablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class NodeController {

    @Autowired
    private LocalTablesService tables;

    @GetMapping("/example")
    public String test(){
        return "Here is a string";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping("/sync")
    public void sync(@RequestBody ClusterTablesDTO dto) {
        tables.replaceAll(dto.getActive(), dto.getInactive());
    }
}