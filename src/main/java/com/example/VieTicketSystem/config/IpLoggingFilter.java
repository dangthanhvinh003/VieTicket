package com.example.VieTicketSystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IpLoggingFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String remoteAddr = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-FORWARDED-FOR");
        String ip = forwardedFor != null ? forwardedFor : remoteAddr;

        // Log the IP address using your preferred logging framework
        logger.info("Request IP: " + ip);

        filterChain.doFilter(request, response);
    }
}

