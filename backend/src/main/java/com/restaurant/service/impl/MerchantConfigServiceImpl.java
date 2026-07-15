package com.restaurant.service.impl;

import com.restaurant.common.HtmlSanitizer;
import com.restaurant.dto.MerchantConfigSaveDTO;
import com.restaurant.entity.MerchantConfig;
import com.restaurant.mapper.MerchantConfigMapper;
import com.restaurant.service.MerchantConfigService;
import com.restaurant.vo.MerchantConfigPublicVO;
import com.restaurant.vo.MerchantConfigVO;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商家配置服务实现。
 *
 * <p>本表为单行配置，固定使用主键 {@code 1}。所有写操作均为 upsert 语义，
 * 写入前对富文本做轻量级 XSS 净化（{@link HtmlSanitizer}）。</p>
 */
@Service
@RequiredArgsConstructor
public class MerchantConfigServiceImpl implements MerchantConfigService {

    /** 单行配置固定主键 */
    private static final Long CONFIG_ID = 1L;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MerchantConfigMapper merchantConfigMapper;

    @Override
    public MerchantConfigVO getConfig() {
        MerchantConfig entity = merchantConfigMapper.selectById(CONFIG_ID);
        if (entity == null) {
            return new MerchantConfigVO();
        }
        return toVO(entity);
    }

    @Override
    @Transactional
    public void upsert(MerchantConfigSaveDTO saveDTO) {
        // 1. 富文本轻量净化（XSS 防护）
        String safeContent = HtmlSanitizer.sanitize(saveDTO.getAboutUsContent());

        // 2. 单行 upsert：无记录则插入（id=1），有记录则更新
        MerchantConfig existing = merchantConfigMapper.selectById(CONFIG_ID);
        if (existing == null) {
            MerchantConfig entity = new MerchantConfig();
            entity.setId(CONFIG_ID);
            entity.setAboutUsContent(safeContent);
            entity.setContactPhone(saveDTO.getContactPhone());
            merchantConfigMapper.insert(entity);
        } else {
            existing.setAboutUsContent(safeContent);
            existing.setContactPhone(saveDTO.getContactPhone());
            merchantConfigMapper.updateById(existing);
        }
    }

    @Override
    public MerchantConfigPublicVO getPublic() {
        MerchantConfigPublicVO vo = new MerchantConfigPublicVO();
        MerchantConfig entity = merchantConfigMapper.selectById(CONFIG_ID);
        if (entity != null) {
            vo.setAboutUsContent(entity.getAboutUsContent());
            vo.setContactPhone(entity.getContactPhone());
        }
        return vo;
    }

    private MerchantConfigVO toVO(MerchantConfig entity) {
        MerchantConfigVO vo = new MerchantConfigVO();
        vo.setId(entity.getId());
        vo.setAboutUsContent(entity.getAboutUsContent());
        vo.setContactPhone(entity.getContactPhone());
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(FMT));
        }
        if (entity.getUpdateTime() != null) {
            vo.setUpdateTime(entity.getUpdateTime().format(FMT));
        }
        return vo;
    }
}
