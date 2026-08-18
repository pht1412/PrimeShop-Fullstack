package com.primeshop.chatc2c;

import java.util.List;
import lombok.*;

@Getter @Setter @Builder
public class ConversationDetailResponse {
    private Long conversationId;
    private Long sellerId;
    private Long customerId;
    private List<MessageResponse> messages;
}
