package com.primeshop.chatc2c;

import java.time.LocalDateTime;
import java.util.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conversations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"seller_id", "customer_id"}))
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Conversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long customerId;

    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX) DEFAULT ''")
    private String lastMessage;

    private LocalDateTime lastMessageAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();
}