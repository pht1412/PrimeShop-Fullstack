package com.primeshop.chatc2c;

import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter @Builder
public class ConversationSummaryResponse {
    private Long conversationId;
    private Long sellerId;
    private Long customerId;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
