package com.restaurant.vo;

import lombok.Data;

@Data
public class BannerVO {

    private Long id;
    private String title;
    private String image;
    private String link;
    private Integer sort;
    private Integer status;
    private String createTime;
}
