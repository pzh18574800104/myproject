package com.example.demo.service.impl;

import com.example.demo.entity.StudyFile;
import com.example.demo.mapper.StudyFileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyFileServiceImplTest {

    @Mock
    private StudyFileMapper studyFileMapper;

    @InjectMocks
    private StudyFileServiceImpl studyFileService;

    private StudyFile mockFile;

    @BeforeEach
    void setUp() {
        // Set baseMapper via reflection since ServiceImpl uses it internally
        ReflectionTestUtils.setField(studyFileService, "baseMapper", studyFileMapper);

        mockFile = new StudyFile();
        mockFile.setId(1L);
        mockFile.setUserId(1L);
        mockFile.setFileName("test.md");
        mockFile.setFileType("md");
        mockFile.setFileSize(100L);
        mockFile.setContent("# Hello");
        mockFile.setParentId(0L);
        mockFile.setIsFolder(0);
    }

    @Test
    void createTextFile_shouldCreateFileInDb() {
        when(studyFileMapper.insert(any(StudyFile.class))).thenReturn(1);

        StudyFile result = studyFileService.createTextFile("notes.md", "# Content", 1L, 0L);

        assertNotNull(result);
        assertEquals("notes.md", result.getFileName());
        assertEquals("md", result.getFileType());
        assertEquals(0, result.getIsFolder());
        verify(studyFileMapper).insert(any(StudyFile.class));
    }

    @Test
    void updateFileContent_shouldUpdateExistingFile() {
        when(studyFileMapper.selectById(1L)).thenReturn(mockFile);
        when(studyFileMapper.updateById(any(StudyFile.class))).thenReturn(1);

        boolean result = studyFileService.updateFileContent(1L, "# Updated", 1L);

        assertTrue(result);
        verify(studyFileMapper).updateById(any(StudyFile.class));
    }

    @Test
    void updateFileContent_shouldReturnFalse_whenFileNotFound() {
        when(studyFileMapper.selectById(1L)).thenReturn(null);

        boolean result = studyFileService.updateFileContent(1L, "# Updated", 1L);

        assertFalse(result);
    }

    @Test
    void updateFileContent_shouldReturnFalse_whenWrongUser() {
        StudyFile file = new StudyFile();
        file.setId(1L);
        file.setUserId(2L);
        when(studyFileMapper.selectById(1L)).thenReturn(file);

        boolean result = studyFileService.updateFileContent(1L, "# Updated", 1L);

        assertFalse(result);
    }

    @Test
    void getFileContent_shouldReturnContent_whenFileExists() {
        when(studyFileMapper.selectById(1L)).thenReturn(mockFile);

        String content = studyFileService.getFileContent(1L, 1L);

        assertEquals("# Hello", content);
    }

    @Test
    void getFileContent_shouldReturnNull_whenFileNotFound() {
        when(studyFileMapper.selectById(1L)).thenReturn(null);

        String content = studyFileService.getFileContent(1L, 1L);

        assertNull(content);
    }

    @Test
    void moveFile_shouldUpdateParentId() {
        when(studyFileMapper.selectById(1L)).thenReturn(mockFile);
        when(studyFileMapper.updateById(any(StudyFile.class))).thenReturn(1);

        boolean result = studyFileService.moveFile(1L, 2L, 1L);

        assertTrue(result);
        verify(studyFileMapper).updateById(argThat(file -> file.getParentId().equals(2L)));
    }

    @Test
    void createFolder_shouldCreateFolderEntry() {
        when(studyFileMapper.insert(any(StudyFile.class))).thenReturn(1);

        boolean result = studyFileService.createFolder("NewFolder", 1L, 0L);

        assertTrue(result);
        verify(studyFileMapper).insert(argThat(file ->
                file.getIsFolder().equals(1) &&
                        file.getFileName().equals("NewFolder") &&
                        file.getFileType().equals("folder")
        ));
    }

    @Test
    void listFiles_shouldReturnUserFiles() {
        List<StudyFile> files = Arrays.asList(mockFile);
        when(studyFileMapper.selectList(any())).thenReturn(files);

        List<StudyFile> result = studyFileService.listFiles(1L, 0L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test.md", result.get(0).getFileName());
    }

    @Test
    void deleteFile_shouldDeleteFile() {
        StudyFile fileWithPath = new StudyFile();
        fileWithPath.setId(1L);
        fileWithPath.setUserId(1L);
        fileWithPath.setFileName("test.txt");
        fileWithPath.setFilePath("D:/myproject-uploads/test.txt");
        fileWithPath.setIsFolder(0);

        when(studyFileMapper.selectById(1L)).thenReturn(fileWithPath);
        when(studyFileMapper.deleteById(1L)).thenReturn(1);

        boolean result = studyFileService.deleteFile(1L, 1L);

        assertTrue(result);
        verify(studyFileMapper).deleteById(1L);
    }
}
