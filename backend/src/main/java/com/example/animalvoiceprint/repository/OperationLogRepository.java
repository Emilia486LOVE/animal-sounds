package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Integer> {
    List<OperationLog> findByUserId(Integer userId);
    List<OperationLog> findByOperationType(String operationType);
}