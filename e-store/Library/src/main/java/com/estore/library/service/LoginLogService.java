package com.estore.library.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LoginLogService {
    void logLogin(UUID userId, String source);

    List<Object[]> countByHourSince(LocalDateTime from);
}

