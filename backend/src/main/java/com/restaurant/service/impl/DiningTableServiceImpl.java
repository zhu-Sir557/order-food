package com.restaurant.service.impl;

import com.restaurant.common.BizException;
import com.restaurant.dto.TableSaveDTO;
import com.restaurant.entity.DiningTable;
import com.restaurant.mapper.DiningTableMapper;
import com.restaurant.service.DiningTableService;
import com.restaurant.vo.TableVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiningTableServiceImpl implements DiningTableService {

    private final DiningTableMapper diningTableMapper;

    @Override
    public List<TableVO> getTableList() {
        List<DiningTable> tables = diningTableMapper.selectList(null);
        return tables.stream().map(this::toTableVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addTable(TableSaveDTO saveDTO) {
        DiningTable table = new DiningTable();
        table.setCode(saveDTO.getCode());
        table.setName(saveDTO.getName());
        table.setCapacity(saveDTO.getCapacity());
        table.setStatus(saveDTO.getStatus());
        diningTableMapper.insert(table);
    }

    @Override
    @Transactional
    public void updateTable(Long id, TableSaveDTO saveDTO) {
        DiningTable table = diningTableMapper.selectById(id);
        if (table == null) {
            throw new BizException("桌台不存在");
        }
        table.setCode(saveDTO.getCode());
        table.setName(saveDTO.getName());
        table.setCapacity(saveDTO.getCapacity());
        table.setStatus(saveDTO.getStatus());
        diningTableMapper.updateById(table);
    }

    @Override
    @Transactional
    public void deleteTable(Long id) {
        diningTableMapper.deleteById(id);
    }

    @Override
    public List<TableVO> getAvailableTables() {
        LambdaQueryWrapper<DiningTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiningTable::getStatus, 0);
        List<DiningTable> tables = diningTableMapper.selectList(wrapper);
        return tables.stream().map(this::toTableVO).collect(Collectors.toList());
    }

    private TableVO toTableVO(DiningTable table) {
        TableVO vo = new TableVO();
        vo.setId(table.getId());
        vo.setCode(table.getCode());
        vo.setName(table.getName());
        vo.setCapacity(table.getCapacity());
        vo.setStatus(table.getStatus());
        return vo;
    }
}
