package com.estore.library.service.impl;

import com.estore.library.model.log.LoginLog;
import com.estore.library.repository.log.LoginLogRepository;
import com.estore.library.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogRepository loginLogRepository;

    @Override
    @Transactional
    public void logLogin(UUID userId, String source) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setSource(source);
        log.setLoggedAt(LocalDateTime.now());
        loginLogRepository.save(log);
    }

    @Override
    public List<Object[]> countByHourSince(LocalDateTime from) {
        return loginLogRepository.countByHourSince(from);
    }
}

