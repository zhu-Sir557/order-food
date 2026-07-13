package com.restaurant.controller.h5;

import com.restaurant.common.Result;
import com.restaurant.dto.BindPhoneDTO;
import com.restaurant.dto.SendLoginCodeDTO;
import com.restaurant.dto.SetPasswordDTO;
import com.restaurant.dto.UpdateNicknameDTO;
import com.restaurant.service.MemberProfileService;
import com.restaurant.vo.AvatarVO;
import com.restaurant.vo.AvatarUpdateVO;
import com.restaurant.vo.ChangeLimitVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * H5 会员资料接口（F1 绑定手机/设密码、F3 昵称/头像）
 */
@RestController
@RequestMapping("/api/h5/member")
@RequiredArgsConstructor
public class MemberProfileController {

    private final MemberProfileService memberProfileService;

    /**
     * 绑定手机（需登录态 + 滑块 + 短信码，仅首次）
     */
    @PostMapping("/bind-phone")
    public Result<Void> bindPhone(@Valid @RequestBody BindPhoneDTO dto, HttpServletRequest request) {
        memberProfileService.bindPhone((Long) request.getAttribute("memberId"), dto);
        return Result.success();
    }

    /**
     * 设置密码（需登录态 + 滑块，仅未设密码可设）
     */
    @PostMapping("/set-password")
    public Result<Void> setPassword(@Valid @RequestBody SetPasswordDTO dto, HttpServletRequest request) {
        memberProfileService.setPassword((Long) request.getAttribute("memberId"), dto);
        return Result.success();
    }

    /**
     * 修改昵称（每日次数受限）
     */
    @PostMapping("/nickname")
    public Result<ChangeLimitVO> updateNickname(@Valid @RequestBody UpdateNicknameDTO dto,
                                                HttpServletRequest request) {
        return Result.success(memberProfileService.updateNickname((Long) request.getAttribute("memberId"), dto));
    }

    /**
     * 上传头像（multipart/form-data，字段名 file；每日次数受限）
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AvatarUpdateVO> updateAvatar(@RequestParam("file") MultipartFile file,
                                               HttpServletRequest request) {
        return Result.success(memberProfileService.updateAvatar((Long) request.getAttribute("memberId"), file));
    }

    /**
     * 账号名 + 验证码登录的发码
     */
    @PostMapping("/send-login-code")
    public Result<Void> sendLoginCode(@Valid @RequestBody SendLoginCodeDTO dto, HttpServletRequest request) {
        memberProfileService.sendLoginCode(dto, resolveClientIp(request));
        return Result.success();
    }

    /**
     * 头像列表
     */
    @GetMapping("/avatars")
    public Result<List<AvatarVO>> listAvatars() {
        return Result.success(memberProfileService.listAvatars());
    }

    /**
     * 解析客户端真实 IP
     */
    private static String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            for (String part : xff.split(",")) {
                String ip = part.trim();
                if (!ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    return ip;
                }
            }
        }
        return request.getRemoteAddr();
    }
}
