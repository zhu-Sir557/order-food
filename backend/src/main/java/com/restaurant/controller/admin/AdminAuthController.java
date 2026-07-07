package com.restaurant.controller.admin;

import com.restaurant.common.Result;
import com.restaurant.dto.AdminLoginDTO;
import com.restaurant.service.AdminAuthService;
import com.restaurant.vo.AdminLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        return Result.success(adminAuthService.login(loginDTO));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
