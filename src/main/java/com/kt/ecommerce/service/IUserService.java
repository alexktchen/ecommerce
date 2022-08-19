package com.kt.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.dto.LoginFormDTO;
import com.kt.ecommerce.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {

    KTHttpResponse sendCode(String phone, HttpSession session);

    KTHttpResponse login(LoginFormDTO loginFormDTO, HttpSession session);
}
