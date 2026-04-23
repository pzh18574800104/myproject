package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.config.FileConfig;
import com.example.demo.entity.StudyFile;
import com.example.demo.mapper.StudyFileMapper;
import com.example.demo.service.StudyFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class StudyFileServiceImpl extends ServiceImpl<StudyFileMapper, StudyFile> implements StudyFileService {

    @Autowired
    private FileConfig fileConfig;

    @Override
    public StudyFile uploadFile(MultipartFile file, Long userId, Long parentId) throws IOException {
        // 创建上传目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String userDir = fileConfig.getUploadPath() + "/" + userId + "/" + dateDir;
        Path uploadDir = Paths.get(userDir);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        // 保存到数据库
        StudyFile studyFile = new StudyFile();
        studyFile.setUserId(userId);
        studyFile.setFileName(originalFilename);
        studyFile.setFilePath(filePath.toString());
        studyFile.setFileType(extension.toLowerCase().replace(".", ""));
        studyFile.setFileSize(file.getSize());
        studyFile.setParentId(parentId != null ? parentId : 0);
        studyFile.setIsFolder(0);

        baseMapper.insert(studyFile);
        return studyFile;
    }

    @Override
    public List<StudyFile> listFiles(Long userId, Long parentId) {
        LambdaQueryWrapper<StudyFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyFile::getUserId, userId)
               .eq(StudyFile::getParentId, parentId != null ? parentId : 0)
               .eq(StudyFile::getDeleted, 0)
               .orderByDesc(StudyFile::getIsFolder)
               .orderByDesc(StudyFile::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public boolean createFolder(String folderName, Long userId, Long parentId) {
        StudyFile folder = new StudyFile();
        folder.setUserId(userId);
        folder.setFileName(folderName);
        folder.setFilePath("");
        folder.setFileType("folder");
        folder.setFileSize(0L);
        folder.setParentId(parentId != null ? parentId : 0);
        folder.setIsFolder(1);
        return baseMapper.insert(folder) > 0;
    }

    @Override
    public StudyFile createTextFile(String fileName, String content, Long userId, Long parentId) {
        StudyFile file = new StudyFile();
        file.setUserId(userId);
        file.setFileName(fileName);
        file.setFilePath("");
        file.setFileType(getExtension(fileName));
        file.setFileSize(content != null ? (long) content.getBytes().length : 0);
        file.setContent(content);
        file.setParentId(parentId != null ? parentId : 0);
        file.setIsFolder(0);
        baseMapper.insert(file);
        return file;
    }

    @Override
    public boolean updateFileContent(Long fileId, String content, Long userId) {
        StudyFile file = baseMapper.selectById(fileId);
        if (file == null || !file.getUserId().equals(userId)) {
            return false;
        }
        file.setContent(content);
        file.setFileSize(content != null ? (long) content.getBytes().length : 0);
        return baseMapper.updateById(file) > 0;
    }

    @Override
    public String getFileContent(Long fileId, Long userId) {
        StudyFile file = baseMapper.selectById(fileId);
        if (file == null || !file.getUserId().equals(userId)) {
            return null;
        }
        // Return DB content if available
        if (file.getContent() != null) {
            return file.getContent();
        }
        // Otherwise read from disk
        if (file.getFilePath() != null && !file.getFilePath().isEmpty()) {
            try {
                return new String(Files.readAllBytes(Paths.get(file.getFilePath())));
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean moveFile(Long fileId, Long targetParentId, Long userId) {
        StudyFile file = baseMapper.selectById(fileId);
        if (file == null || !file.getUserId().equals(userId)) {
            return false;
        }
        file.setParentId(targetParentId != null ? targetParentId : 0);
        return baseMapper.updateById(file) > 0;
    }

    private String getExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public byte[] downloadFile(Long fileId, Long userId) throws IOException {
        StudyFile file = baseMapper.selectById(fileId);
        if (file == null || !file.getUserId().equals(userId)) {
            throw new RuntimeException("文件不存在或无权限");
        }
        if (file.getFilePath() != null && !file.getFilePath().isEmpty()) {
            return Files.readAllBytes(Paths.get(file.getFilePath()));
        }
        // For text files stored in DB
        return file.getContent() != null ? file.getContent().getBytes() : new byte[0];
    }

    @Override
    public boolean deleteFile(Long fileId, Long userId) {
        StudyFile file = baseMapper.selectById(fileId);
        if (file == null || !file.getUserId().equals(userId)) {
            return false;
        }
        
        // 如果是文件，删除物理文件
        if (file.getIsFolder() == 0) {
            try {
                Files.deleteIfExists(Paths.get(file.getFilePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return baseMapper.deleteById(fileId) > 0;
    }
}
