package com.restaurant.vo;

import lombok.Data;

@Data
public class AdminLoginVO {

    private String token;
    private AdminInfo adminInfo;

    @Data
    public static class AdminInfo {
        private Long id;
        private String name;
        private String avatar;
    }
}
