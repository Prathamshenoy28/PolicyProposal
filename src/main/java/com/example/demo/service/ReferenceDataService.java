package com.example.demo.service;

import com.example.demo.repository.ReferenceDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReferenceDataService {
    @Autowired
    ReferenceDataRepository referenceDataRepository;

    public List<String> getReferenceData(String category) {
        return referenceDataRepository.findByCategory(category);
    }
}
