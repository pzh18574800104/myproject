package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Menu;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.RedisService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User login(String username, String password) {
        // 硬编码测试账号
        if ("admin".equals(username) && "admin123".equals(password)) {
            User admin = new User();
            admin.setId(1L);
            admin.setUsername("admin");
            admin.setNickname("管理员");
            admin.setStatus(1);
            return admin;
        }
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getStatus, 1)
               .eq(User::getDeleted, 0);
        
        User user = userMapper.selectOne(wrapper);
        
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        
        return user;
    }

    @Autowired
    private RedisService redisService;

    @Override
    @Cacheable(value = "user:menus", key = "#userId")
    public List<Menu> getUserMenus(Long userId) {
        // 先尝试从Redis获取
        String cacheKey = "user:menus:" + userId;
        @SuppressWarnings("unchecked")
        List<Menu> cachedMenus = (List<Menu>) redisService.get(cacheKey);
        if (cachedMenus != null) {
            return cachedMenus;
        }
        
        // 从数据库查询
        List<Menu> menus = userMapper.selectMenusByUserId(userId);
        
        // 存入Redis，设置30分钟过期
        redisService.set(cacheKey, menus, 30, TimeUnit.MINUTES);
        
        return menus;
    }

    @Override
    public boolean saveUser(User user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            return false;
        }
        
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean register(String username, String password, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setStatus(1);
        
        boolean success = saveUser(user);
        if (success) {
            // 给新用户分配默认角色 (ROLE_USER)
            // 这里简化处理，实际需要查询角色ID并插入关联表
        }
        return success;
    }
}
