package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.entity.OperationLog;
import com.example.animalvoiceprint.repository.OperationLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogService {
    
    private final OperationLogRepository logRepository;
    
    public OperationLogService(OperationLogRepository logRepository) {
        this.logRepository = logRepository;
    }
    
    public void log(Integer userId, String operationType, String targetType, Integer targetId, String detail) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType(operationType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        logRepository.save(log);
    }
    
    public List<OperationLog> getLogsByUserId(Integer userId) {
        return logRepository.findByUserId(userId);
    }
    
    public List<OperationLog> getLogsByOperationType(String operationType) {
        return logRepository.findByOperationType(operationType);
    }
    
    public List<OperationLog> getAllLogs() {
        return logRepository.findAll();
    }
}