package com.example.teamflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ヘッダーからJWTを取り出す
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // "Bearer xxx" の形式かチェック
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                // トークンが無効または期限切れの場合はスキップ
            }
        }

        // ユーザー名が取れて、まだ認証されていない場合
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // JWTが有効で、かつ「アクセストークン」であれば認証情報をセットする。
            // 種別まで確認する理由：アクセストークンとリフレッシュトークンは同じ鍵で署名されており、
            // 署名と有効期限だけを見ると両者を区別できない。種別を見ないと、寿命7日の
            // リフレッシュトークンをそのまま API 呼び出しに使えてしまう。
            //
            // 一方 /api/auth/refresh 側で種別を見なくてよいのは、あちらが
            // 「DB の refresh_tokens に存在するか」を確認しており、アクセストークンは
            // そもそも DB に保存されないため構造的に弾かれるため。
            if (jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}