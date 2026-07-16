package com.restaurant.controller.admin;

import com.restaurant.common.GlobalExceptionHandler;
import com.restaurant.common.PageResult;
import com.restaurant.service.MessageService;
import com.restaurant.vo.MessageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MessageAdminController Web 层测试（standalone MockMvc + mock MessageService）。
 *
 * <p>覆盖 send / list / detail / revoke，以及发送参数校验失败返回 400。</p>
 */
@ExtendWith(MockitoExtension.class)
class MessageAdminControllerTest {

    @Mock MessageService messageService;
    @InjectMocks MessageAdminController controller;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /send 合法广播消息 → 200，data.id=100")
    void send_ok() throws Exception {
        MessageVO vo = new MessageVO();
        vo.setId(100L);
        vo.setType("BROADCAST");
        vo.setReceiverScope("ALL");
        vo.setStatus("SENT");
        when(messageService.send(any(), anyLong())).thenReturn(vo);
        mockMvc.perform(post("/api/admin/message/send").requestAttr("adminId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"BROADCAST\",\"title\":\"午市特惠\",\"content\":\"全场8折\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(100));
    }

    @Test
    @DisplayName("POST /send 缺标题 → 400 参数校验")
    void send_invalid() throws Exception {
        mockMvc.perform(post("/api/admin/message/send").requestAttr("adminId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"BROADCAST\",\"content\":\"全场8折\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /list 发送记录 → 200")
    void list_ok() throws Exception {
        when(messageService.adminList(anyInt(), anyInt(), any(), any(), any(), any()))
                .thenReturn(new PageResult<>(java.util.List.of(), 0, 1, 10));
        mockMvc.perform(get("/api/admin/message/list").requestAttr("adminId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /{id} 详情 → 200")
    void detail_ok() throws Exception {
        MessageVO vo = new MessageVO();
        vo.setId(1L);
        vo.setTitle("t");
        when(messageService.adminDetail(1L)).thenReturn(vo);
        mockMvc.perform(get("/api/admin/message/1").requestAttr("adminId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST /{id}/revoke 撤回 → 200")
    void revoke_ok() throws Exception {
        doNothing().when(messageService).revoke(anyLong(), anyLong());
        mockMvc.perform(post("/api/admin/message/1/revoke").requestAttr("adminId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
