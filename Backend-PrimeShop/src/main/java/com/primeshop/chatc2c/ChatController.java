package com.primeshop.chatc2c;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.primeshop.user.User;
import com.primeshop.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {
    private final ChatService chatService;
    private final SecurityUtils securityUtils;
    private final ChatMapper chatMapper;

    

    @PostMapping("/send")
    public Message send(@RequestParam Long sellerId,
                        @RequestParam Long customerId,
                        @RequestParam Long senderId,
                        @RequestParam String content) {
        return chatService.sendMessage(sellerId, customerId, senderId, content);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        // 1. Lấy User đang đăng nhập
        User currentUser = securityUtils.getCurrentUser();
        
        // 2. Gọi Service
        List<ConversationResponse> list = chatService.getConversationsByUserId(currentUser.getId());
        
        // 3. Trả về
        return ResponseEntity.ok(list);
    }

    @GetMapping("/messages")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<MessageResponse> messages = chatService.getMessagesByConversationId(
                conversationId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(messages);
    }

    // Endpoint tạo cuộc hội thoại mới khi bấm "Chat Ngay"
    @PostMapping("/create")
    public ResponseEntity<ConversationResponse> createConversation(@RequestParam Long sellerId) {
        // 1. Lấy người dùng hiện tại (người mua)
        User currentUser = securityUtils.getCurrentUser();
        
        // 2. Gọi Service tạo hoặc lấy hội thoại (logic bạn đã có sẵn)
        Conversation conversation = chatService.getOrCreateConversation(sellerId, currentUser.getId());
        
        // 3. Map sang Response (cần truyền thêm currentUser.getId() để Mapper biết ai là người "kia")
        return ResponseEntity.ok(chatMapper.toConversationResponse(conversation, currentUser.getId()));
    }

    
}
