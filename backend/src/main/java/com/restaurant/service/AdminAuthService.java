package com.restaurant.service;

import com.restaurant.dto.AdminLoginDTO;
import com.restaurant.vo.AdminLoginVO;

public interface AdminAuthService {
    AdminLoginVO login(AdminLoginDTO loginDTO);
}
