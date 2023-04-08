package com.frog.travelwithme.utils.reqeust;

import com.frog.travelwithme.global.security.auth.userdetails.CustomUserDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.MultiValueMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * ResultActionsUtils 설명: ResultActions 관리
 * 작성자: 이재혁
 * 수정자: 김찬빈
 * 버전 정보: 1.0.1
 * 작성일자: 2023/03/18
 **/
public class ResultActionsUtils {

    public static ResultActions postRequest(MockMvc mockMvc,
                                            String url,
                                            HttpHeaders headers) throws Exception {
        return mockMvc.perform(post(url)
                        .headers(headers)
                        .with(csrf()))
                .andDo(print());
    }

    public static ResultActions postRequest(MockMvc mockMvc,
                                            String url,
                                            String json) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andDo(print());
    }

    public static ResultActions postRequest(MockMvc mockMvc,
                                            String url,
                                            String json,
                                            HttpHeaders headers) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                        .with(csrf()))
                .andDo(print());
    }

    public static ResultActions patchRequest(MockMvc mockMvc,
                                             String url) throws Exception {
        return mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print());
    }

    public static ResultActions patchRequest(MockMvc mockMvc,
                                             String url,
                                             String json,
                                             CustomUserDetails userDetails) throws Exception {
        return mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf())
                        .with(user(userDetails)))
                .andDo(print());
    }

    public static ResultActions deleteRequest(MockMvc mockMvc,
                                              String url,
                                              CustomUserDetails userDetails) throws Exception {
        return mockMvc.perform(delete(url)
                        .with(csrf())
                        .with(user(userDetails)))
                .andDo(print());
    }

    public static ResultActions getRequest(MockMvc mockMvc,
                                           String url,
                                           String json) throws Exception {
        return mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print());
    }

    public static ResultActions getRequest(MockMvc mockMvc,
                                           String url,
                                           CustomUserDetails userDetails) throws Exception {
        return mockMvc.perform(get(url)
                        .with(user(userDetails)))
                .andDo(print());
    }

    public static ResultActions getRequest(MockMvc mockMvc, String url,
                                           HttpHeaders headers,
                                           MultiValueMap<String, String> params) throws Exception {
        return mockMvc.perform(get(url)
                        .params(params)
                        .headers(headers))
                .andDo(print());
    }
}
