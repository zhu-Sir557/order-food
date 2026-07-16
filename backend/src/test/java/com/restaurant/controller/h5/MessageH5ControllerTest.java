package com.restaurant.controller.h5;

import com.restaurant.common.GlobalExceptionHandler;
import com.restaurant.common.PageResult;
import com.restaurant.common.ResultCode;
import com.restaurant.service.MessageService;
import com.restaurant.vo.MessageUnreadVO;
import com.restaurant.vo.MessageVO;
import java.util.List;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MessageH5Controller Web 层测试（standalone MockMvc + mock MessageService）。
 *
 * <p>覆盖 list / detail / read / read-batch / unread-count，以及未携带身份时返回 401 码。</p>
 */
@ExtendWith(MockitoExtension.class)
class MessageH5ControllerTest {

    @Mock MessageService messageService;
    @InjectMocks MessageH5Controller controller;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /list 携带会员身份 → 200")
    void list_ok() throws Exception {
        when(messageService.listForUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0, 1, 10));
        mockMvc.perform(get("/api/h5/message/list").requestAttr("memberId", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /list 无身份 → 401 业务码")
    void list_noAuth() throws Exception {
        mockMvc.perform(get("/api/h5/message/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("GET /{id} 详情 → 200")
    void detail_ok() throws Exception {
        MessageVO vo = new MessageVO();
        vo.setId(1L);
        vo.setTitle("t");
        when(messageService.detailForUser(anyLong(), anyLong(), anyString())).thenReturn(vo);
        mockMvc.perform(get("/api/h5/message/1").requestAttr("memberId", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("PUT /{id}/read 标记已读 → 200")
    void read_ok() throws Exception {
        mockMvc.perform(put("/api/h5/message/1/read").requestAttr("memberId", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /read-batch 批量已读 → 200")
    void readBatch_ok() throws Exception {
        mockMvc.perform(put("/api/h5/message/read-batch").requestAttr("memberId", 5L)
                        .contentType(MediaType.APPLICATION_JSON).content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /unread-count 未读计数 → 200，data.unreadCount=3")
    void unreadCount_ok() throws Exception {
        when(messageService.unreadCount(anyLong(), anyString())).thenReturn(3L);
        mockMvc.perform(get("/api/h5/message/unread-count").requestAttr("memberId", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }
}
