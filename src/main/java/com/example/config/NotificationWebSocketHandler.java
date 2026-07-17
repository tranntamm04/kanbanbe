package com.example.config;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.NotificationWebSocketRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final String USER_ID_ATTRIBUTE = "userId";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final NotificationWebSocketRegistry registry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = authenticate(session);
        session.getAttributes().put(USER_ID_ATTRIBUTE, userId);
        registry.register(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object userId = session.getAttributes().get(USER_ID_ATTRIBUTE);
        if (userId instanceof Long id) {
            registry.unregister(id, session);
        }
    }

    private Long authenticate(WebSocketSession session) {
        String token = UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst("token");

        if (token == null || !jwtTokenUtil.validateJwtToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String username = jwtTokenUtil.getUsernameFromJwtToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return user.getId();
    }
}
