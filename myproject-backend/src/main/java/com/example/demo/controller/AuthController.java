package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Menu;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        List<Menu> menus = userService.getUserMenus(user.getId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        data.put("menus", menus);
        
        return Result.success(data);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info(@RequestAttribute("userId") Long userId) {
        User user = userService.getById(userId);
        List<Menu> menus = userService.getUserMenus(userId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("menus", menus);
        
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        boolean success = userService.register(request.getUsername(), request.getPassword(), request.getNickname());
        if (success) {
            return Result.success();
        }
        return Result.error("用户名已存在");
    }

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Token is valid");
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String nickname;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}
