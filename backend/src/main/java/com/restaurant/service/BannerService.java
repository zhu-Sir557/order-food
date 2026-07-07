package com.restaurant.service;

import com.restaurant.common.PageResult;
import com.restaurant.dto.BannerSaveDTO;
import com.restaurant.vo.BannerVO;
import java.util.List;

public interface BannerService {
    PageResult<BannerVO> getBannerPage(int page, int size);
    void addBanner(BannerSaveDTO saveDTO);
    void updateBanner(Long id, BannerSaveDTO saveDTO);
    void deleteBanner(Long id);
    void updateBannerStatus(Long id, Integer status);
    List<BannerVO> getH5BannerList();
}
