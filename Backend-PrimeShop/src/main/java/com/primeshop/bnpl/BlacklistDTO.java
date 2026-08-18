package com.primeshop.bnpl;
import java.time.LocalDateTime;

public record BlacklistDTO(Long id, String username, String reason, LocalDateTime createdAt) {
    public static BlacklistDTO from(BNPLBlacklist b) {
        return new BlacklistDTO(b.getId(), b.getUser().getUsername(), b.getReason(), b.getCreatedAt());
    }
}
