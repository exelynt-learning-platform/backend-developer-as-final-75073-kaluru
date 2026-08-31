package com.exelynt.booking.security;

import com.exelynt.booking.service.AppUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;
    private final SecurityErrorWriter errorWriter;

    public JwtAuthenticationFilter(JwtService jwtService, AppUserDetailsService userDetailsService,
                                   SecurityErrorWriter errorWriter) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.errorWriter = errorWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (!jwtService.valid(token)) {
                errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, "Invalid or expired JWT");
                return;
            }
            String username = jwtService.username(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var principal = userDetailsService.loadUserByUsername(username);
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
