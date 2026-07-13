package com.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改头像请求 DTO
 */
@Data
public class UpdateAvatarDTO {

    /** 头像ID（取自 avatar 表） */
    @NotNull(message = "头像ID不能为空")
    private Long avatarId;
}
