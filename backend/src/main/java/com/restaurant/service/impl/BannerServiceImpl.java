package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.BizException;
import com.restaurant.common.PageResult;
import com.restaurant.dto.BannerSaveDTO;
import com.restaurant.entity.Banner;
import com.restaurant.mapper.BannerMapper;
import com.restaurant.service.BannerService;
import com.restaurant.vo.BannerVO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<BannerVO> getBannerPage(int page, int size) {
        Page<Banner> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Banner::getSort);
        Page<Banner> result = bannerMapper.selectPage(pageObj, wrapper);
        List<BannerVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public void addBanner(BannerSaveDTO saveDTO) {
        Banner banner = new Banner();
        banner.setTitle(saveDTO.getTitle());
        banner.setImage(saveDTO.getImage());
        banner.setLink(saveDTO.getLink());
        banner.setSort(saveDTO.getSort() != null ? saveDTO.getSort() : 0);
        banner.setStatus(1);
        bannerMapper.insert(banner);
    }

    @Override
    @Transactional
    public void updateBanner(Long id, BannerSaveDTO saveDTO) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BizException("轮播图不存在");
        }
        banner.setTitle(saveDTO.getTitle());
        banner.setImage(saveDTO.getImage());
        banner.setLink(saveDTO.getLink());
        if (saveDTO.getSort() != null) {
            banner.setSort(saveDTO.getSort());
        }
        bannerMapper.updateById(banner);
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        bannerMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateBannerStatus(Long id, Integer status) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BizException("轮播图不存在");
        }
        banner.setStatus(status);
        bannerMapper.updateById(banner);
    }

    @Override
    public List<BannerVO> getH5BannerList() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1);
        wrapper.orderByAsc(Banner::getSort);
        List<Banner> banners = bannerMapper.selectList(wrapper);
        return banners.stream().map(this::toVO).collect(Collectors.toList());
    }

    private BannerVO toVO(Banner banner) {
        BannerVO vo = new BannerVO();
        vo.setId(banner.getId());
        vo.setTitle(banner.getTitle());
        vo.setImage(banner.getImage());
        vo.setLink(banner.getLink());
        vo.setSort(banner.getSort());
        vo.setStatus(banner.getStatus());
        if (banner.getCreateTime() != null) {
            vo.setCreateTime(banner.getCreateTime().format(FMT));
        }
        return vo;
    }
}
