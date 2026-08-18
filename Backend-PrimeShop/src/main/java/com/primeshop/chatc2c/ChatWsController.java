package com.primeshop.chatc2c;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.primeshop.user.User;
import com.primeshop.user.UserRepo;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ChatWsController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepo userRepo;
    private static final Logger logger = LoggerFactory.getLogger(ChatWsController.class);

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO request,
                            Principal principal) throws AccessDeniedException {
        // 1. Validate người gửi
        if (principal == null) {
            throw new AccessDeniedException("Principal is null. Interceptor failed.");
        }
        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
        User sender = (User) authToken.getPrincipal();

        // 2. Lưu tin nhắn
        MessageResponse saved = chatService.saveMessage(request, sender);
        
        // 3. Gửi Public vào Room Chat (Để cập nhật ngay lập tức cho 2 bên trong khung chat)
        messagingTemplate.convertAndSend(
            "/topic/conversation/" + saved.getConversationId(),
            saved
        );

        // 4. Logic gửi thông báo riêng (Sidebar update)
        Long receiverId = saved.getReceiverId();
        
        // FIX: Kiểm tra receiverId có null không
        if (receiverId == null) {
            logger.error("CRITICAL: Message saved but receiverId is NULL. Conversation ID: {}", saved.getConversationId());
            return;
        }

        userRepo.findById(receiverId).ifPresentOrElse(
            (receiver) -> {
                String receiverUsername = receiver.getUsername();
                // LOG QUAN TRỌNG: Xem server đang gửi cho ai
                logger.info(">>> SOCKET DEBUG: Đang gửi tin cho User [ID: {}] - [Username: {}]", receiver.getId(), receiverUsername);
                
                messagingTemplate.convertAndSendToUser(
                    receiverUsername, // <-- Spring dùng giá trị này để định tuyến
                    "/queue/messages",
                    saved
                );
            },
            () -> logger.warn("User ID {} not found...", receiverId)
        );
        
        // Gửi lại cho người gửi (để cập nhật UI sidebar của chính mình)
        messagingTemplate.convertAndSendToUser(
            sender.getUsername(),
            "/queue/messages",
            saved
        );
    }
}
