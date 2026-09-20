package com.farmlink.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

// Gemini에게 알려줄 도구(function) 목록 정의.
// 여기 있는 이름/파라미터는 AssistantToolExecutor의 switch문과 반드시 맞아야 함.
final class AssistantToolSpecs {

    private AssistantToolSpecs() {
    }

    static ArrayNode declarations(ObjectMapper mapper) {
        ArrayNode declarations = mapper.createArrayNode();
        declarations.add(getMilkAverageSpec(mapper));
        declarations.add(getPendingBreedingCountSpec(mapper));
        declarations.add(getRecentWorklogsSpec(mapper));
        return declarations;
    }

    private static ObjectNode getMilkAverageSpec(ObjectMapper mapper) {
        ObjectNode spec = mapper.createObjectNode();
        spec.put("name", "get_milk_average");
        spec.put("description", "지정한 기간(startDate~endDate) 동안 우리 농장의 착유량 평균(리터)과 기록 건수를 조회합니다.");

        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "OBJECT");
        ObjectNode properties = mapper.createObjectNode();
        properties.set("startDate", stringProp(mapper, "조회 시작일 (YYYY-MM-DD)"));
        properties.set("endDate", stringProp(mapper, "조회 종료일 (YYYY-MM-DD)"));
        parameters.set("properties", properties);
        ArrayNode required = mapper.createArrayNode();
        required.add("startDate");
        required.add("endDate");
        parameters.set("required", required);
        spec.set("parameters", parameters);

        return spec;
    }

    private static ObjectNode getPendingBreedingCountSpec(ObjectMapper mapper) {
        ObjectNode spec = mapper.createObjectNode();
        spec.put("name", "get_pending_breeding_count");
        spec.put("description", "우리 농장에서 임신감정 대기 중(WAITING)인 소의 마릿수를 조회합니다.");

        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "OBJECT");
        parameters.set("properties", mapper.createObjectNode());
        spec.set("parameters", parameters);

        return spec;
    }

    private static ObjectNode getRecentWorklogsSpec(ObjectMapper mapper) {
        ObjectNode spec = mapper.createObjectNode();
        spec.put("name", "get_recent_worklogs");
        spec.put("description", "최근 N일 동안 우리 농장에서 작성된 작업일지 목록(날짜, 작업종류, 내용)을 조회합니다.");

        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "OBJECT");
        ObjectNode properties = mapper.createObjectNode();
        ObjectNode daysProp = mapper.createObjectNode();
        daysProp.put("type", "INTEGER");
        daysProp.put("description", "조회할 최근 일수 (예: 7이면 최근 7일간의 작업일지)");
        properties.set("days", daysProp);
        parameters.set("properties", properties);
        ArrayNode required = mapper.createArrayNode();
        required.add("days");
        parameters.set("required", required);
        spec.set("parameters", parameters);

        return spec;
    }

    private static ObjectNode stringProp(ObjectMapper mapper, String description) {
        ObjectNode prop = mapper.createObjectNode();
        prop.put("type", "STRING");
        prop.put("description", description);
        return prop;
    }
}
