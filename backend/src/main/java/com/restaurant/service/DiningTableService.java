package com.restaurant.service;

import com.restaurant.dto.TableSaveDTO;
import com.restaurant.vo.TableVO;
import java.util.List;

public interface DiningTableService {
    List<TableVO> getTableList();
    void addTable(TableSaveDTO saveDTO);
    void updateTable(Long id, TableSaveDTO saveDTO);
    void deleteTable(Long id);
    List<TableVO> getAvailableTables();
}
