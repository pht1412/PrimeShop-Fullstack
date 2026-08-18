package com.primeshop.chatc2c;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.primeshop.user.User;
import com.primeshop.user.UserService;
import com.primeshop.utils.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SecurityUtils securityUtils;
    private final UserService userService;

    @Transactional
    public Conversation getOrCreateConversation(Long sellerId, Long customerId) {
        return conversationRepository.findBySellerIdAndCustomerId(sellerId, customerId)
                .orElseGet(() -> {
                    Conversation c = Conversation.builder()
                        .sellerId(sellerId)
                        .customerId(customerId)
                        .createdAt(LocalDateTime.now())
                        .lastMessageAt(LocalDateTime.now())
                        .build();
                    return conversationRepository.save(c);
                });
    }

    @Transactional
    public Message sendMessage(Long sellerId, Long customerId, Long senderId, String content) {
        Conversation conversation = getOrCreateConversation(sellerId, customerId);
        conversation.setLastMessage(content);
        conversation.setLastMessageAt(LocalDateTime.now());

        Message m = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        messageRepository.save(m);
        return m;
    }

    @Transactional
    public MessageResponse saveMessage(ChatMessageDTO payload, User user) throws AccessDeniedException {
        Long senderId = user.getId();
        if (payload.getContent() == null || payload.getContent().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Message content cannot be empty");
        }
        Conversation conversation;
        if (payload.getConversationId() != null) {
                conversation = conversationRepository.findById(payload.getConversationId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Conversation not found"));
        }
        else {
                if (payload.getReceiverId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ReceiverId is required for new conversation");
                }
                Long receiverId = payload.getReceiverId();
                boolean senderIsSeller = userService.isSeller(senderId);
                conversation = conversationRepository
                        .findBySellerIdAndCustomerId(
                                senderIsSeller ? senderId : receiverId,
                                senderIsSeller ? receiverId : senderId
                        ).orElseGet(() -> conversationRepository.save(
                                Conversation.builder()
                                        .sellerId(senderIsSeller ? senderId : receiverId)
                                        .customerId(senderIsSeller ? receiverId : senderId)
                                        .createdAt(LocalDateTime.now())
                                        .lastMessageAt(LocalDateTime.now())
                                        .build()
                        ));
        }
        if (!senderId.equals(conversation.getSellerId())
                && !senderId.equals(conversation.getCustomerId())) {
                throw new AccessDeniedException("You are not part of this conversation");
        }
        Long receiverId = senderId.equals(conversation.getSellerId())
                ? conversation.getCustomerId()
                : conversation.getSellerId();
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .content(payload.getContent().trim())
                .createdAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);
        conversation.setLastMessage(message.getContent());
        conversation.setLastMessageAt(message.getCreatedAt());
        conversationRepository.save(conversation);
        return MessageResponse.builder()
                .messageId(message.getId())
                .conversationId(conversation.getId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }


    public ConversationResponse mapToDto(Conversation conversation, Long currentUserId) {
        Long otherUserId = conversation.getSellerId().equals(currentUserId) ?
                conversation.getCustomerId() :
                conversation.getSellerId();
        User otherUser = securityUtils.getUserById(otherUserId);
        // compute unread messages: for each message, receiver = the other participant (not the sender)
        long unread = conversation.getMessages().stream()
            .filter(m -> {
                Long senderId = m.getSenderId();
                Long receiverId = senderId != null && senderId.equals(conversation.getSellerId())
                    ? conversation.getCustomerId()
                    : conversation.getSellerId();
                return receiverId != null && receiverId.equals(currentUserId) && !m.isRead();
            })
            .count();
        return ConversationResponse.builder()
            .id(conversation.getId())
            .otherUserId(otherUser.getId())
            .otherUsername(otherUser.getUsername())
            .otherAvatar(otherUser.getAvatar())
            .lastMessage(conversation.getLastMessage())
            .lastMessageAt(conversation.getLastMessageAt())
            .unreadCount((int) unread)
            .build();
    }

    public List<ConversationResponse> findAllByCustomerId(Long customerId) {
        List<Conversation> conversations = conversationRepository
                .findByCustomerId(customerId);
        return conversations.stream()
                .map(c -> mapToDto(c, customerId))
                .toList();
    }

    public List<ConversationResponse> findByUserId(Long userId) {
        List<Conversation> conversations = conversationRepository
                .findBySellerIdOrCustomerId(userId, userId);
        return conversations.stream()
                .map(c -> mapToDto(c, userId))
                .toList();
    }

    public Page<MessageResponse> getMessagesByConversationId(Long conversationId, Pageable pageable) {
        Page<Message> messages = messageRepository
                .findByConversationId(conversationId, pageable);
        return messages.map(m -> MessageResponse.builder()
                .messageId(m.getId())
                .senderId(m.getSenderId())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build());
    }
    public List<ConversationResponse> getConversationsByUserId(Long userId) {
        // 1. Gọi Repository lấy List Entity
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

        // 2. Duyệt qua từng cái và map sang DTO bằng method mapToDto có sẵn trong class
        return conversations.stream()
                .map(c -> mapToDto(c, userId))
                .toList();
    }


}
