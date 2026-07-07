package com.restaurant.controller.h5;

import com.restaurant.common.Result;
import com.restaurant.service.DiningTableService;
import com.restaurant.vo.TableVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/h5/tables")
@RequiredArgsConstructor
public class H5TableController {

    private final DiningTableService diningTableService;

    @GetMapping("/available")
    public Result<List<TableVO>> getAvailableTables() {
        return Result.success(diningTableService.getAvailableTables());
    }
}
