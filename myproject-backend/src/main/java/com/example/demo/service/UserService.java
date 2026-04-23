package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.User;

import java.util.List;
import com.example.demo.entity.Menu;

public interface UserService extends IService<User> {
    
    User login(String username, String password);
    
    List<Menu> getUserMenus(Long userId);
    
    boolean saveUser(User user);
    
    boolean register(String username, String password, String nickname);
}
