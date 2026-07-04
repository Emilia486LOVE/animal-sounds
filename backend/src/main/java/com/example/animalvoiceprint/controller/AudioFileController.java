package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.dto.AudioFileUpdateRequest;
import com.example.animalvoiceprint.entity.AudioFile;
import com.example.animalvoiceprint.service.AuthService;
import com.example.animalvoiceprint.service.AudioFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/audio")
public class AudioFileController {
    
    private final AudioFileService audioFileService;
    private final AuthService authService;
    
    public AudioFileController(AudioFileService audioFileService, AuthService authService) {
        this.audioFileService = audioFileService;
        this.authService = authService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<AudioFile>>> getAllAudioFiles() {
        List<AudioFile> files = audioFileService.getAllAudioFiles();
        return ResponseEntity.ok(ApiResponse.success(files));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AudioFile>> getAudioFileById(@PathVariable("id") Integer audioId) {
        AudioFile file = audioFileService.getAudioFileById(audioId);
        return ResponseEntity.ok(ApiResponse.success(file));
    }
    
    @GetMapping("/dataset/{datasetId}")
    public ResponseEntity<ApiResponse<List<AudioFile>>> getAudioFilesByDatasetId(@PathVariable("datasetId") Integer datasetId) {
        List<AudioFile> files = audioFileService.getAudioFilesByDatasetId(datasetId);
        return ResponseEntity.ok(ApiResponse.success(files));
    }
    
    
    
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('admin', 'annotator', 'algorithm')")
    public ResponseEntity<ApiResponse<List<AudioFile>>> uploadAudioFiles(
            @RequestParam("datasetId") Integer datasetId,
            @RequestParam("files") List<MultipartFile> files) {
        Integer userId = authService.getCurrentUser().getUserId();
        List<AudioFile> uploaded = audioFileService.uploadAudioFiles(datasetId, files, userId);
        return ResponseEntity.ok(ApiResponse.success("音频上传成功", uploaded));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'annotator')")
    public ResponseEntity<ApiResponse<AudioFile>> updateAudioFile(
            @PathVariable("id") Integer audioId,
            @RequestBody AudioFileUpdateRequest request) {
        AudioFile file = audioFileService.updateAudioFile(audioId, request.getNoiseLevel(), request.getLocation(), request.getRemark());
        return ResponseEntity.ok(ApiResponse.success("音频信息已更新", file));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Void>> deleteAudioFile(@PathVariable("id") Integer audioId) {
        audioFileService.deleteAudioFile(audioId);
        return ResponseEntity.ok(ApiResponse.success("音频文件已删除", null));
    }
    
    @GetMapping("/download/{filePath}")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<Resource> downloadAudioFile(@PathVariable("filePath") String filePath) {
        Resource resource = audioFileService.loadAudioFileAsResource(filePath);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        
        String fileName = Paths.get(filePath).getFileName().toString();
        String contentType = "audio/mpeg";
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}