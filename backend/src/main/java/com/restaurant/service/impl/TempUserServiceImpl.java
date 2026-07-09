package com.restaurant.service.impl;

import com.restaurant.entity.TempUser;
import com.restaurant.mapper.TempUserMapper;
import com.restaurant.service.TempUserService;
import com.restaurant.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TempUserServiceImpl implements TempUserService {

    private final TempUserMapper tempUserMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public TempUser createTempUser() {
        TempUser tempUser = new TempUser();

        // 为避免数据库 token 列 NOT NULL 约束报错，先设置占位 token 再 insert
        tempUser.setToken("placeholder_" + System.currentTimeMillis());
        tempUserMapper.insert(tempUser);

        // 用自增 ID 生成 JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CUSTOMER");
        String token = jwtUtil.generateToken(tempUser.getId().toString(), claims, 720);

        // 更新为真实 token
        tempUser.setToken(token);
        tempUserMapper.updateById(tempUser);

        return tempUser;
    }

    @Override
    public TempUser getByToken(String token) {
        LambdaQueryWrapper<TempUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TempUser::getToken, token);
        return tempUserMapper.selectOne(wrapper);
    }
}
