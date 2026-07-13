package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改昵称请求 DTO
 */
@Data
public class UpdateNicknameDTO {

    /** 新昵称（≤20 字） */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称不能超过20个字符")
    private String nickname;
}
