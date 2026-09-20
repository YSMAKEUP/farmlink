package com.farmlink.assistant;

import com.farmlink.assistant.dto.AssistantAskRequest;
import com.farmlink.assistant.dto.AssistantAskResponse;
import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 대화 오케스트레이션: Gemini에게 질문 전달 -> functionCall이 오면 실제 데이터 조회 -> 결과를 다시 넣어서
// 최종 자연어 답변을 받아옴. 최대 MAX_TOOL_ROUNDS번까지 이 왕복을 허용(도구 여러 개 연쇄 호출 대비).
@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final String SYSTEM_INSTRUCTION =
            "당신은 낙농 젖소 농장 관리 시스템 FarmLink의 AI 어시스턴트입니다. " +
                    "농장주(50~60대일 수 있음)의 질문에 대해, 반드시 제공된 도구(tool)를 사용해 실제 데이터를 조회한 뒤 " +
                    "그 결과를 바탕으로만 답변하세요. 도구로 확인할 수 없는 내용은 추측하지 말고 모른다고 답하세요. " +
                    "답변은 한국어로, 간결하고 친절하게 작성하세요.";

    private static final int MAX_TOOL_ROUNDS = 3;

    private final GeminiClient geminiClient;
    private final AssistantToolExecutor toolExecutor;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AssistantAskResponse ask(AssistantAskRequest request, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));
        String farmCode = user.getFarmCode();

        ArrayNode contents = objectMapper.createArrayNode();
        contents.add(userTurn(request.getQuestion()));

        ArrayNode tools = AssistantToolSpecs.declarations(objectMapper);

        for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
            JsonNode response = geminiClient.generateContent(contents, tools, SYSTEM_INSTRUCTION);
            JsonNode candidateContent = response.at("/candidates/0/content");
            JsonNode parts = candidateContent.get("parts");

            JsonNode functionCallPart = findFunctionCall(parts);

            if (functionCallPart == null) {
                return new AssistantAskResponse(extractText(parts));
            }

            // 모델이 함수 호출을 요청한 턴을 대화 히스토리에 그대로 추가해야 다음 요청에서 문맥이 이어짐
            contents.add(candidateContent);

            String toolName = functionCallPart.get("functionCall").get("name").asText();
            JsonNode args = functionCallPart.get("functionCall").get("args");

            ObjectNode toolResult = toolExecutor.execute(farmCode, toolName, args);

            contents.add(functionResponseTurn(toolName, toolResult));
        }

        return new AssistantAskResponse("죄송해요, 지금은 답변을 만드는 데 실패했어요. 다시 시도해주세요.");
    }

    private ObjectNode userTurn(String question) {
        ObjectNode turn = objectMapper.createObjectNode();
        turn.put("role", "user");
        ArrayNode parts = objectMapper.createArrayNode();
        parts.add(objectMapper.createObjectNode().put("text", question));
        turn.set("parts", parts);
        return turn;
    }

    private ObjectNode functionResponseTurn(String toolName, ObjectNode result) {
        ObjectNode turn = objectMapper.createObjectNode();
        turn.put("role", "function");
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode functionResponsePart = objectMapper.createObjectNode();
        ObjectNode functionResponse = objectMapper.createObjectNode();
        functionResponse.put("name", toolName);
        functionResponse.set("response", result);
        functionResponsePart.set("functionResponse", functionResponse);
        parts.add(functionResponsePart);
        turn.set("parts", parts);
        return turn;
    }

    private JsonNode findFunctionCall(JsonNode parts) {
        if (parts == null) return null;
        for (JsonNode part : parts) {
            if (part.has("functionCall")) {
                return part;
            }
        }
        return null;
    }

    private String extractText(JsonNode parts) {
        if (parts == null) return "";
        StringBuilder sb = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.has("text")) {
                sb.append(part.get("text").asText());
            }
        }
        return sb.toString();
    }
}
