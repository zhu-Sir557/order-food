package com.restaurant.controller.admin;

import com.restaurant.common.Result;
import com.restaurant.dto.TableSaveDTO;
import com.restaurant.service.DiningTableService;
import com.restaurant.vo.TableVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
public class DiningTableController {

    private final DiningTableService diningTableService;

    @GetMapping
    public Result<List<TableVO>> getTableList() {
        return Result.success(diningTableService.getTableList());
    }

    @PostMapping
    public Result<Void> addTable(@Valid @RequestBody TableSaveDTO saveDTO) {
        diningTableService.addTable(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateTable(@PathVariable Long id, @Valid @RequestBody TableSaveDTO saveDTO) {
        diningTableService.updateTable(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTable(@PathVariable Long id) {
        diningTableService.deleteTable(id);
        return Result.success();
    }
}
