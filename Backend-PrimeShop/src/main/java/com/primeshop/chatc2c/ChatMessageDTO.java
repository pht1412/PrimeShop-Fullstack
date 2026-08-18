package com.primeshop.chatc2c;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long conversationId;
    private Long receiverId;
    private String content;
}
