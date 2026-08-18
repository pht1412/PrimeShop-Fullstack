package com.primeshop.chatc2c;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "messages")
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String content;

    private LocalDateTime createdAt;

    private boolean isRead; // false = chưa xem, true = đã xem
}
