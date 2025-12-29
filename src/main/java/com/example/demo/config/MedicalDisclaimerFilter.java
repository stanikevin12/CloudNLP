package com.example.demo.config;

import com.example.demo.dto.ApiResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class MedicalDisclaimerFilter extends OncePerRequestFilter {

    public static final String DISCLAIMER_HEADER = "X-Medical-Disclaimer";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader(DISCLAIMER_HEADER, ApiResult.MEDICAL_DISCLAIMER);
        filterChain.doFilter(request, response);
    }
}
