package com.primeshop.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import com.primeshop.security.JwtUtil;
import com.primeshop.user.User;
import com.primeshop.user.UserRepo;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtService;
    private final UserRepo userRepo;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            logger.info("--- INTERCEPTOR: Đang xử lý CONNECT command ---");
            try {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    logger.warn("Lỗi Interceptor: Missing or invalid Authorization header");
                    throw new RuntimeException("Missing or invalid Authorization");
                }
                String token = authHeader.substring(7);
                logger.info("Đã lấy token: {}...", token.substring(0, 10));
                String username = jwtService.extractUsername(token);
                logger.info("Extract được username: {}", username);
                User user = userRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found in DB: " + username));
                logger.info("ĐÃ TÌM THẤY USER: {}, ID CỦA USER LÀ: {}", user.getUsername(), user.getId());
                if (!jwtService.isTokenValid(token, username)) {
                    logger.warn("Lỗi Interceptor: Token không hợp lệ (isTokenValid = false)");
                    throw new RuntimeException("Invalid Token");
                }                
                logger.info("Token HỢP LỆ. Đang tạo Authentication...");
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );
                accessor.setUser(authentication);
                logger.info(">>> ĐÃ SET USER VÀO ACCESSOR THÀNH CÔNG: {} <<<", username);
            } catch (Exception e) {
                logger.error("!!! LỖI NGHIÊM TRỌNG TRONG INTERCEPTOR: {}", e.getMessage());
                throw e; 
            }
        }
        return message;
    }
}
