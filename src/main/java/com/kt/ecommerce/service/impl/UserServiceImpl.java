package com.kt.ecommerce.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kt.ecommerce.dto.KTHttpResponse;
import com.kt.ecommerce.dto.LoginFormDTO;
import com.kt.ecommerce.dto.UserDTO;
import com.kt.ecommerce.entity.User;
import com.kt.ecommerce.mapper.UserMapper;
import com.kt.ecommerce.service.IUserService;
import com.kt.ecommerce.utils.GenerateRandom;
import com.kt.ecommerce.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.kt.ecommerce.utils.RedisConstants.*;
import static com.kt.ecommerce.utils.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public KTHttpResponse sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return KTHttpResponse.fail("Invalid phone number");
        }

        String code = GenerateRandom.randomNumbers(6, 9);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // session.setAttribute("code", code);
        // log.info("Send the code successful, code {}", code);
        return KTHttpResponse.ok();
    }

    @Override
    public KTHttpResponse login(LoginFormDTO loginFormDTO, HttpSession session) {
        String phone = loginFormDTO.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return KTHttpResponse.fail("Invalid phone number");
        }

        Object cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginFormDTO.getCode();

        if (cacheCode == null || !cacheCode.toString().equals(code)) {
            return KTHttpResponse.fail("Invalid code");
        }

        User user = query().eq("phone", phone).one();

        if (user == null) {
            user = createUserWithPhone(phone);
        }


        String token = UUID.randomUUID().toString();
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        Map<String, String> userMap = new HashMap<>();
        userMap.put("Icon", userDTO.getIcon());
        userMap.put("NickName", userDTO.getNickName());
        userMap.put("Id", String.valueOf(userDTO.getId()));

        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return KTHttpResponse.ok(token);
    }

    private User createUserWithPhone(String phone) {
        LocalDateTime lt = LocalDateTime.now();
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + GenerateRandom.randomNumbers(10, 9));
        user.setCreateTime(lt);
        user.setUpdateTime(lt);
        save(user);
        return user;
    }
}
