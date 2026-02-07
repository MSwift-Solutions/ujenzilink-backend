package com.ujenzilink.ujenzilink_backend.chats.dtos;

import java.util.List;

/**
 * Paginated messages response
 */
public record MessagePageDTO(
        List<MessageDTO> content,
        long totalElements,
        int totalPages,
        int currentPage,
        boolean hasNext) {
}
