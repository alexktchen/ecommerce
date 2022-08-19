package com.kt.ecommerce.controller;

import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.dto.LoginFormDTO;
import com.kt.ecommerce.dto.UserDTO;
import com.kt.ecommerce.service.IUserService;
import com.kt.ecommerce.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;


    @PostMapping("code")
    public KTHttpResponse sendCode(@RequestParam("phone") String phone, HttpSession session) {
        return userService.sendCode(phone, session);
    }

    @PostMapping("login")
    public KTHttpResponse login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        return userService.login(loginFormDTO, session);
    }

    @GetMapping("me")
    public KTHttpResponse me() {
        UserDTO user = UserHolder.getUser();
        return KTHttpResponse.ok(user);
    }
}
