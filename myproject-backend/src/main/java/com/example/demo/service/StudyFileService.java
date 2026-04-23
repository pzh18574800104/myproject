package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.StudyFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StudyFileService extends IService<StudyFile> {

    StudyFile uploadFile(MultipartFile file, Long userId, Long parentId) throws IOException;

    List<StudyFile> listFiles(Long userId, Long parentId);

    boolean createFolder(String folderName, Long userId, Long parentId);

    StudyFile createTextFile(String fileName, String content, Long userId, Long parentId);

    boolean updateFileContent(Long fileId, String content, Long userId);

    String getFileContent(Long fileId, Long userId);

    boolean moveFile(Long fileId, Long targetParentId, Long userId);

    byte[] downloadFile(Long fileId, Long userId) throws IOException;

    boolean deleteFile(Long fileId, Long userId);
}
