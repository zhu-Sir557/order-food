package com.restaurant.controller.h5;

import com.restaurant.common.Result;
import com.restaurant.entity.TempUser;
import com.restaurant.service.TempUserService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/h5/user")
@RequiredArgsConstructor
public class H5UserController {

    private final TempUserService tempUserService;

    @PostMapping("/temp")
    public Result<Map<String, Object>> createTempUser() {
        // service 返回包含 id 和 token 的 TempUser 对象
        TempUser tempUser = tempUserService.createTempUser();
        Map<String, Object> result = new HashMap<>();
        result.put("token", tempUser.getToken());
        result.put("tempUserId", tempUser.getId());
        return Result.success(result);
    }
}
