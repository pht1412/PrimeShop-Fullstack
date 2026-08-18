package com.primeshop.chatc2c;

import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder
public class MessageResponse {
    private Long conversationId;
    private Long messageId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime createdAt;
}
