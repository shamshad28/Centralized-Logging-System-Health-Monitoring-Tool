package com.pulse.monitoring.controller;

import com.pulse.monitoring.service.TrafficSimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/simulator")
@CrossOrigin(origins = "*")
public class SimulatorController {

    @Autowired
    private TrafficSimulatorService simulatorService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getStatus() {
        return ResponseEntity.ok(Map.of("enabled", simulatorService.isSimulatorEnabled()));
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Boolean>> toggleSimulator(@RequestParam boolean enabled) {
        simulatorService.setSimulatorEnabled(enabled);
        return ResponseEntity.ok(Map.of("enabled", simulatorService.isSimulatorEnabled()));
    }

    @PostMapping("/fault")
    public ResponseEntity<Map<String, String>> injectFault(@RequestParam String serviceName, @RequestParam String faultType) {
        simulatorService.injectFault(serviceName, faultType);
        return ResponseEntity.ok(Map.of("status", "Fault injected into " + serviceName));
    }
}
