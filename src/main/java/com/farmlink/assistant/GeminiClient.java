package com.farmlink.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Gemini generateContent API(v1beta)를 직접 호출하는 저수준 클라이언트.
// 별도 SDK 의존성 추가 없이 spring-boot-starter-web에 이미 포함된 RestClient로 처리.
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String model;

    public GeminiClient(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    /**
     * @param contents         지금까지의 대화 히스토리(user/model/function 턴 배열)
     * @param toolDeclarations 사용 가능한 도구(function) 스펙 배열
     * @param systemInstruction 시스템 프롬프트
     * @return Gemini 응답 전체(JsonNode) - candidates[0].content.parts에서 text 또는 functionCall을 꺼내 씀
     */
    public JsonNode generateContent(ArrayNode contents, ArrayNode toolDeclarations, String systemInstruction) {
        ObjectNode requestBody = objectMapper.createObjectNode();

        ObjectNode systemInstructionNode = objectMapper.createObjectNode();
        ArrayNode systemParts = objectMapper.createArrayNode();
        systemParts.add(objectMapper.createObjectNode().put("text", systemInstruction));
        systemInstructionNode.set("parts", systemParts);
        requestBody.set("system_instruction", systemInstructionNode);

        requestBody.set("contents", contents);

        ObjectNode tool = objectMapper.createObjectNode();
        tool.set("functionDeclarations", toolDeclarations);
        ArrayNode tools = objectMapper.createArrayNode();
        tools.add(tool);
        requestBody.set("tools", tools);

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY가 설정되지 않았습니다. 환경변수를 확인하세요.");
        }

        String responseBody = restClient.post()
                .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody.toString())
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new IllegalStateException("Gemini 응답 파싱 실패", e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
