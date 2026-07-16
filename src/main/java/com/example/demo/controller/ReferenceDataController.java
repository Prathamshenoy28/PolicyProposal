package com.example.demo.controller;

import com.example.demo.service.ReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reference-master")
public class ReferenceDataController {
    @Autowired
    private ReferenceDataService referenceDataService;

    @GetMapping("/{category}")
    public ResponseEntity<List<String>> getReferenceData(@PathVariable String category) {
        List<String> data = referenceDataService.getReferenceData(category);
        return ResponseEntity.ok(data);
    }
}
