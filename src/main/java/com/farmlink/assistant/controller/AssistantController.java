package com.farmlink.assistant.controller;

import com.farmlink.assistant.AssistantService;
import com.farmlink.assistant.dto.AssistantAskRequest;
import com.farmlink.assistant.dto.AssistantAskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    // 자연어 질문 -> Gemini tool-calling으로 실제 데이터 조회 후 답변
    @PostMapping("ask")
    public AssistantAskResponse ask(@RequestBody AssistantAskRequest request,
                                     @AuthenticationPrincipal Long userId) {
        return assistantService.ask(request, userId);
    }
}
