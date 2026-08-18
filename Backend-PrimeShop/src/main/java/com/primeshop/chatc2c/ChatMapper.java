// File: src/main/java/com/primeshop/chatc2c/ChatMapper.java

package com.primeshop.chatc2c;

import org.springframework.stereotype.Component;
import com.primeshop.user.User;
import com.primeshop.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMapper {

    private final SecurityUtils securityUtils;

    public ConversationSummaryResponse toSummary(Conversation c) {
        return ConversationSummaryResponse.builder()
                .conversationId(c.getId())
                .sellerId(c.getSellerId())
                .customerId(c.getCustomerId())
                .lastMessage(
                    c.getMessages().isEmpty() ? null :
                    c.getMessages().get(c.getMessages().size() - 1).getContent()
                )
                .lastMessageAt(c.getLastMessageAt())
                .build();
    }
    
    public MessageResponse toMessageResponse(Message m) {
        return MessageResponse.builder()
                .messageId(m.getId())
                .senderId(m.getSenderId())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }

    public ConversationDetailResponse toDetail(Conversation c) {
        return ConversationDetailResponse.builder()
                .conversationId(c.getId())
                .sellerId(c.getSellerId())
                .customerId(c.getCustomerId())
                .messages(c.getMessages()
                        .stream()
                        .map(this::toMessageResponse)
                        .toList())
                .build();
    }

    // --- CẬP NHẬT HÀM NÀY ---
    public ConversationResponse toConversationResponse(Conversation c, Long currentUserId) {
        ConversationResponse response = new ConversationResponse();
        response.setId(c.getId());
        
        Long otherUserId = currentUserId.equals(c.getCustomerId()) 
                           ? c.getSellerId() 
                           : c.getCustomerId();

        try {
            User otherUser = securityUtils.getUserById(otherUserId);
            response.setOtherUserId(otherUserId);
            
            // FIX 1: Ưu tiên FullName, nếu null thì lấy Username (đảm bảo không bao giờ null)
            String displayName = otherUser.getFullName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = otherUser.getUsername();
            }
            response.setOtherUsername(displayName);

            // FIX 2: Xử lý Avatar null ngay tại Backend
            String avatar = otherUser.getAvatar();
            if (avatar == null || avatar.trim().isEmpty()) {
                avatar = "https://cdn-icons-png.flaticon.com/512/1041/1041846.png"; // Ảnh mặc định
            }
            response.setOtherAvatar(avatar);

        } catch (RuntimeException e) {
            response.setOtherUserId(otherUserId);
            response.setOtherUsername("Người dùng không tồn tại");
            response.setOtherAvatar("https://cdn-icons-png.flaticon.com/512/1041/1041846.png");
        }

        response.setLastMessage(c.getLastMessage());
        response.setLastMessageAt(c.getLastMessageAt());
        
        return response;
    }
}