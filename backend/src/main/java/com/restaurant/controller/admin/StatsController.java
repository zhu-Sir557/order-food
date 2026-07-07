package com.restaurant.controller.admin;

import com.restaurant.common.Result;
import com.restaurant.service.StatsService;
import com.restaurant.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.success(statsService.getDashboard());
    }
}
