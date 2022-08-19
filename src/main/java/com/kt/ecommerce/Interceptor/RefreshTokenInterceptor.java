package com.kt.ecommerce.Interceptor;

import com.kt.ecommerce.dto.UserDTO;
import com.kt.ecommerce.utils.RedisConstants;
import com.kt.ecommerce.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.kt.ecommerce.utils.RedisConstants.LOGIN_USER_TTL;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. get res header token
        String token = request.getHeader("authorization");
        if (token == null || token.isEmpty()) {
            return true;
        }

        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        // 2. get token from redis
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);

        // 3. check user is existing
        if (userMap.isEmpty()) {
            return true;
        }

        UserDTO user = new UserDTO();
        user.setId(Long.parseLong(String.valueOf(userMap.get("Id"))));
        user.setNickName(String.valueOf(userMap.get("NickName")));
        user.setIcon(String.valueOf(userMap.get("Icon")));

        // 5. existing, save user to ThreadLocal
        UserHolder.saveUser(user);

        // 6. update the redis TTL for the user token
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 6. release
        return true;
    }
}
