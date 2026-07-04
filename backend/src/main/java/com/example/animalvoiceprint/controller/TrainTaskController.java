package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.dto.TrainTaskCreateRequest;
import com.example.animalvoiceprint.entity.TrainTask;
import com.example.animalvoiceprint.service.AuthService;
import com.example.animalvoiceprint.service.TrainTaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/train")
public class TrainTaskController {
    
    private final TrainTaskService trainTaskService;
    private final AuthService authService;
    
    public TrainTaskController(TrainTaskService trainTaskService, AuthService authService) {
        this.trainTaskService = trainTaskService;
        this.authService = authService;
    }
    
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<TrainTask>>> getAllTasks() {
        List<TrainTask> tasks = trainTaskService.getAllTasks();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<TrainTask>> getTaskById(@PathVariable("id") Integer taskId) {
        TrainTask task = trainTaskService.getTaskById(taskId);
        return ResponseEntity.ok(ApiResponse.success(task));
    }
    
    @GetMapping("/tasks/status/{status}")
    public ResponseEntity<ApiResponse<List<TrainTask>>> getTasksByStatus(@PathVariable("status") String status) {
        List<TrainTask> tasks = trainTaskService.getTasksByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    @GetMapping("/tasks/{id}/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaskProgress(@PathVariable("id") Integer taskId) {
        Map<String, Object> progress = trainTaskService.getTaskProgress(taskId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
    
    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<TrainTask>> createTask(@Valid @RequestBody TrainTaskCreateRequest request) {
        Integer userId = authService.getCurrentUser().getUserId();
        TrainTask task = trainTaskService.createTask(request, userId);
        return ResponseEntity.ok(ApiResponse.success("训练任务创建成功", task));
    }
    
    @PostMapping("/tasks/{id}/start")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<TrainTask>> startTask(@PathVariable("id") Integer taskId) {
        TrainTask task = trainTaskService.startTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("训练任务已启动", task));
    }
    
    @PutMapping("/tasks/{id}/progress")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<TrainTask>> updateTaskProgress(
            @PathVariable("id") Integer taskId,
            @RequestParam("epoch") Integer epoch,
            @RequestParam("valMetric") Double valMetric) {
        TrainTask task = trainTaskService.updateTaskProgress(taskId, epoch, valMetric);
        return ResponseEntity.ok(ApiResponse.success("进度已更新", task));
    }
    
    @PostMapping("/tasks/{id}/complete")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<TrainTask>> completeTask(
            @PathVariable("id") Integer taskId,
            @RequestParam("modelPath") String modelPath) {
        TrainTask task = trainTaskService.completeTask(taskId, modelPath);
        return ResponseEntity.ok(ApiResponse.success("训练任务已完成", task));
    }
    
    @PostMapping("/tasks/{id}/fail")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<TrainTask>> failTask(
            @PathVariable("id") Integer taskId,
            @RequestParam("errorMsg") String errorMsg) {
        TrainTask task = trainTaskService.failTask(taskId, errorMsg);
        return ResponseEntity.ok(ApiResponse.success("训练任务已失败", task));
    }
    
    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable("id") Integer taskId) {
        trainTaskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("训练任务已删除", null));
    }
}