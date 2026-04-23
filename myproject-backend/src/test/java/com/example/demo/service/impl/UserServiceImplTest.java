package com.example.demo.service.impl;

import com.example.demo.entity.Menu;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void login_shouldReturnAdminUser_whenHardcodedCredentials() {
        User result = userService.login("admin", "admin123");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("管理员", result.getNickname());
        assertEquals(1L, result.getId());
    }

    @Test
    void login_shouldReturnNull_whenWrongAdminPassword() {
        User result = userService.login("admin", "wrongpassword");
        assertNull(result);
    }

    @Test
    void getUserMenus_shouldReturnMenusFromRedis_whenCacheHit() {
        List<Menu> cachedMenus = Arrays.asList(createMenu(1L, "Dashboard"));
        when(redisService.get(anyString())).thenReturn(cachedMenus);

        List<Menu> result = userService.getUserMenus(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dashboard", result.get(0).getMenuName());
        verify(redisService).get("user:menus:1");
        verify(userMapper, never()).selectMenusByUserId(anyLong());
    }

    @Test
    void getUserMenus_shouldReturnMenusFromDb_whenCacheMiss() {
        List<Menu> dbMenus = Arrays.asList(createMenu(1L, "Dashboard"), createMenu(2L, "Settings"));
        when(redisService.get(anyString())).thenReturn(null);
        when(userMapper.selectMenusByUserId(1L)).thenReturn(dbMenus);

        List<Menu> result = userService.getUserMenus(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userMapper).selectMenusByUserId(1L);
    }

    @Test
    void saveUser_shouldReturnFalse_whenUsernameExists() {
        when(userMapper.selectCount(any())).thenReturn(1L);

        User user = new User();
        user.setUsername("existing");

        boolean result = userService.saveUser(user);
        assertFalse(result);
    }

    @Test
    void register_shouldCreateUser_whenUsernameAvailable() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        boolean result = userService.register("newuser", "password", "New User");

        assertTrue(result);
    }

    private Menu createMenu(Long id, String name) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setMenuName(name);
        return menu;
    }
}
