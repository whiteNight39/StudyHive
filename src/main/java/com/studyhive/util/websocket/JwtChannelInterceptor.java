package com.studyhive.util.websocket;

import com.studyhive.util.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Read Authorization header from STOMP CONNECT headers
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String raw = authHeaders.get(0);
                if (raw.startsWith("Bearer ")) raw = raw.substring(7);
                try {
                    if (jwtUtil.isTokenValid(raw)) {
                        String userId = jwtUtil.getUserId(raw).toString(); // returns UUID as String
                        accessor.setUser(new StompPrincipal(userId));
                    } else {
                        // invalid token; throw or ignore (user remains unauthenticated)
                        throw new IllegalArgumentException("Invalid JWT");
                    }
                } catch (Exception ex) {
                    // fail the connect: setting user to null means no principal.
                    // you may want to throw an exception to stop the connect
                    throw new IllegalArgumentException("Invalid JWT: " + ex.getMessage());
                }
            } else {
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }

        return message;
    }
}
