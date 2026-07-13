package com.restaurant.service;

import com.restaurant.dto.BindPhoneDTO;
import com.restaurant.dto.SendLoginCodeDTO;
import com.restaurant.dto.SetPasswordDTO;
import com.restaurant.dto.UpdateAvatarDTO;
import com.restaurant.dto.UpdateNicknameDTO;
import com.restaurant.vo.AvatarVO;
import com.restaurant.vo.ChangeLimitVO;
import java.util.List;

/**
 * 会员资料业务服务接口（F1 绑定手机/设密码、F3 昵称/头像）
 */
public interface MemberProfileService {

    /**
     * 绑定手机（仅首次，需登录态 + 滑块 + 短信码）
     *
     * @param memberId 会员ID
     * @param dto      绑定请求
     */
    void bindPhone(Long memberId, BindPhoneDTO dto);

    /**
     * 设置密码（仅 password 为 null 可设，需滑块）
     *
     * @param memberId 会员ID
     * @param dto      设密码请求
     */
    void setPassword(Long memberId, SetPasswordDTO dto);

    /**
     * 修改昵称（每日次数上限，返回剩余次数）
     *
     * @param memberId 会员ID
     * @param dto      修改请求
     * @return 修改后剩余次数
     */
    ChangeLimitVO updateNickname(Long memberId, UpdateNicknameDTO dto);

    /**
     * 修改头像（每日次数上限，返回剩余次数）
     *
     * @param memberId 会员ID
     * @param dto      修改请求
     * @return 修改后剩余次数
     */
    ChangeLimitVO updateAvatar(Long memberId, UpdateAvatarDTO dto);

    /**
     * 账号名 + 验证码登录的发码（按 username 定位绑定手机号后发送）
     *
     * @param dto      发码请求（含账号名与滑块 captchaToken）
     * @param clientIp 客户端真实 IP
     */
    void sendLoginCode(SendLoginCodeDTO dto, String clientIp);

    /**
     * 头像列表
     *
     * @return 头像选项
     */
    List<AvatarVO> listAvatars();

    /**
     * 生成默认昵称：美食家 + 8 位大小写英文
     *
     * @return 默认昵称
     */
    String generateDefaultNickname();

    /**
     * 从 avatar 表随机取一条 oss_url（表为空返回 null）
     *
     * @return 头像 OSS 地址
     */
    String randomAvatarUrl();
}
