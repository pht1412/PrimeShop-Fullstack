package com.primeshop.chatc2c;

import java.time.LocalDateTime;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private Long otherUserId;
    private String otherUsername;
    private String otherAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount; // <--- Thêm dòng này
}
