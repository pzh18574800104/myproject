package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.StudyFile;
import com.example.demo.service.StudyFileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private StudyFileService studyFileService;

    @PostMapping("/upload")
    public Result<StudyFile> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", defaultValue = "0") Long parentId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            StudyFile studyFile = studyFileService.uploadFile(file, userId, parentId);
            return Result.success(studyFile);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<StudyFile>> list(
            @RequestParam(value = "parentId", defaultValue = "0") Long parentId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<StudyFile> files = studyFileService.listFiles(userId, parentId);
        return Result.success(files);
    }

    @PostMapping("/folder")
    public Result<Void> createFolder(
            @RequestBody CreateFolderRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        boolean success = studyFileService.createFolder(request.getFolderName(), userId, request.getParentId());
        if (success) {
            return Result.success();
        }
        return Result.error("创建失败");
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> download(
            @PathVariable Long fileId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            StudyFile file = studyFileService.getById(fileId);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] data = studyFileService.downloadFile(fileId, userId);
            
            String encodedFilename = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{fileId}")
    public Result<Void> delete(@PathVariable Long fileId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = studyFileService.deleteFile(fileId, userId);
        if (success) {
            return Result.success();
        }
        return Result.error("删除失败");
    }

    @PostMapping("/text")
    public Result<StudyFile> createTextFile(@RequestBody TextFileRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        StudyFile file = studyFileService.createTextFile(
            request.getFileName(),
            request.getContent(),
            userId,
            request.getParentId()
        );
        return Result.success(file);
    }

    @PutMapping("/{fileId}/content")
    public Result<Void> updateContent(
            @PathVariable Long fileId,
            @RequestBody ContentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        boolean success = studyFileService.updateFileContent(fileId, request.getContent(), userId);
        if (success) {
            return Result.success();
        }
        return Result.error("保存失败");
    }

    @GetMapping("/{fileId}/content")
    public Result<String> getContent(@PathVariable Long fileId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String content = studyFileService.getFileContent(fileId, userId);
        return Result.success(content);
    }

    @PostMapping("/{fileId}/move")
    public Result<Void> moveFile(
            @PathVariable Long fileId,
            @RequestBody MoveRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        boolean success = studyFileService.moveFile(fileId, request.getTargetParentId(), userId);
        if (success) {
            return Result.success();
        }
        return Result.error("移动失败");
    }

    public static class CreateFolderRequest {
        private String folderName;
        private Long parentId;

        public String getFolderName() {
            return folderName;
        }

        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }
    }

    public static class TextFileRequest {
        private String fileName;
        private String content;
        private Long parentId;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }
    }

    public static class ContentRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class MoveRequest {
        private Long targetParentId;

        public Long getTargetParentId() {
            return targetParentId;
        }

        public void setTargetParentId(Long targetParentId) {
            this.targetParentId = targetParentId;
        }
    }
}
