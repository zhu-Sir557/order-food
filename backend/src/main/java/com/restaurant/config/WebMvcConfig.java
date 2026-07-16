package com.restaurant.config;

import com.restaurant.interceptor.AdminAuthInterceptor;
import com.restaurant.interceptor.CustomerAuthInterceptor;
import com.restaurant.interceptor.MemberAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final CustomerAuthInterceptor customerAuthInterceptor;
    private final MemberAuthInterceptor memberAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login", "/api/admin/auth/**");

        registry.addInterceptor(customerAuthInterceptor)
                .addPathPatterns("/api/h5/orders/**", "/api/h5/message/**");
        // register 不再需要临时用户认证，tempUserId 可选

        registry.addInterceptor(memberAuthInterceptor)
                .addPathPatterns("/api/h5/member/**", "/api/h5/orders/*/pay/balance")
                .excludePathPatterns("/api/h5/member/register", "/api/h5/member/login");
    }
}
