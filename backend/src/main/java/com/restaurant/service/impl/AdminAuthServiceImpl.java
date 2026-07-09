package com.restaurant.service.impl;

import com.restaurant.common.BizException;
import com.restaurant.dto.AdminLoginDTO;
import com.restaurant.entity.AdminUser;
import com.restaurant.mapper.AdminUserMapper;
import com.restaurant.service.AdminAuthService;
import com.restaurant.util.JwtUtil;
import com.restaurant.vo.AdminLoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AdminLoginVO login(AdminLoginDTO loginDTO) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, loginDTO.getUsername());
        AdminUser admin = adminUserMapper.selectOne(wrapper);

        if (admin == null || !passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        String token = jwtUtil.generateToken(admin.getId().toString(), claims, 72);

        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(token);
        AdminLoginVO.AdminInfo info = new AdminLoginVO.AdminInfo();
        info.setId(admin.getId());
        info.setName(admin.getName());
        info.setAvatar(admin.getAvatar());
        vo.setAdminInfo(info);

        return vo;
    }
}
