package com.restaurant.controller.h5;

import com.restaurant.common.GlobalExceptionHandler;
import com.restaurant.service.SmsAuthService;
import com.restaurant.vo.MemberLoginVO;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SmsController Web 层测试（standalone MockMvc + mock SmsAuthService，不加载 Spring 上下文）。
 *
 * <p>用 {@code MockMvcBuilders.standaloneSetup} 隔离容器，避免 @WebMvcTest 在缺 DataSource
 * 的切片里实例化 Mapper / 拦截器。重点验证 /send、/login 路由、@Valid DTO 校验，
 * 以及真实 IP 解析（X-Forwarded-For 第一非 unknown，降级 getRemoteAddr）。</p>
 */
@ExtendWith(MockitoExtension.class)
class SmsControllerTest {

    @Mock SmsAuthService smsAuthService;
    @InjectMocks SmsController smsController;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(smsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /send 合法请求 → 200，调用 sendCode")
    void send_ok() throws Exception {
        mockMvc.perform(post("/api/h5/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800001111\",\"captchaToken\":\"tok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(smsAuthService).sendCode(any(), any());
    }

    @Test
    @DisplayName("POST /send 手机号非法 → 400 参数校验，不进入业务")
    void send_invalidPhone() throws Exception {
        mockMvc.perform(post("/api/h5/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"123\",\"captchaToken\":\"tok\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
        verify(smsAuthService, never()).sendCode(any(), any());
    }

    @Test
    @DisplayName("POST /send 缺 captchaToken → 400 参数校验")
    void send_missingCaptcha() throws Exception {
        mockMvc.perform(post("/api/h5/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800001111\"}"))
                .andExpect(status().isBadRequest());
        verify(smsAuthService, never()).sendCode(any(), any());
    }

    @Test
    @DisplayName("POST /send X-Forwarded-For 第一项非 unknown → 作为 clientIp")
    void send_xff_first() throws Exception {
        mockMvc.perform(post("/api/h5/sms/send")
                        .header("X-Forwarded-For", "1.2.3.4, 9.9.9.9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800001111\",\"captchaToken\":\"tok\"}"))
                .andExpect(status().isOk());
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsAuthService).sendCode(any(), ipCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("1.2.3.4", ipCaptor.getValue());
    }

    @Test
    @DisplayName("POST /send XFF 首项 unknown → 取下一非 unknown IP")
    void send_xff_unknownThenValid() throws Exception {
        mockMvc.perform(post("/api/h5/sms/send")
                        .header("X-Forwarded-For", "unknown, 8.8.8.8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800001111\",\"captchaToken\":\"tok\"}"))
                .andExpect(status().isOk());
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsAuthService).sendCode(any(), ipCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("8.8.8.8", ipCaptor.getValue());
    }

    @Test
    @DisplayName("POST /send 无 XFF → 降级 getRemoteAddr 作为 clientIp")
    void send_remoteAddr() throws Exception {
        mockMvc.perform(post("/api/h5/sms/send")
                        .with(req -> {
                            req.setRemoteAddr("10.0.0.5");
                            return req;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800001111\",\"captchaToken\":\"tok\"}"))
                .andExpect(status().isOk());
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsAuthService).sendCode(any(), ipCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("10.0.0.5", ipCaptor.getValue());
    }

    @Test
    @DisplayName("POST /login 合法请求 → 200 返回 MemberLoginVO")
    void login_ok() throws Exception {
        MemberLoginVO vo = new MemberLoginVO();
        vo.setToken("jwt");
        vo.setMemberId(7L);
        vo.setUsername("u");
        vo.setBalance(BigDecimal.TEN);
        when(smsAuthService.login(any())).thenReturn(vo);

        mockMvc.perform(post("/api/h5/sms/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800001111\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt"))
                .andExpect(jsonPath("$.data.memberId").value(7));
    }

    @Test
    @DisplayName("POST /login 手机号非法 → 400，不进入业务")
    void login_invalidPhone() throws Exception {
        mockMvc.perform(post("/api/h5/sms/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"abc\",\"code\":\"123456\"}"))
                .andExpect(status().isBadRequest());
        verify(smsAuthService, never()).login(any());
    }
}
