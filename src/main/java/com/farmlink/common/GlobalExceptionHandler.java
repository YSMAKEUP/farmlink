package com.farmlink.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// 컨트롤러/서비스에서 던지는 IllegalArgumentException(존재하지 않는 리소스 참조,
// 잘못된 입력값 등)을 그냥 두면 스프링 기본 동작으로 500이 나가버림.
// 이건 서버 잘못이 아니라 클라이언트가 잘못 보낸 요청이라 400으로 내려주는 게 맞아서 추가함.
// 프론트에서 400/500을 구분해서 다른 메시지를 보여줄 수 있게 되는 것도 이거 덕분.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }
}
