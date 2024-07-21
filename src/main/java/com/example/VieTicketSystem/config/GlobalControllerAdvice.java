package com.example.VieTicketSystem.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {
    @ModelAttribute("baseUrl")
    public String addBaseUrl(HttpServletRequest request) {
        return request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
    }
}
