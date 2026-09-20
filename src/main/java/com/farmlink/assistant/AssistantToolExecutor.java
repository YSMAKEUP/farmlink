package com.farmlink.assistant;

import com.farmlink.breedingRecord.Repository.BreedRecordRepository;
import com.farmlink.breedingRecord.entity.PregnancyResult;
import com.farmlink.milk.domain.MilkRecord;
import com.farmlink.milk.repository.MilkRepository;
import com.farmlink.worklog.domain.WorkLogEntity;
import com.farmlink.worklog.repository.WorkLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Gemini가 요청한 tool 이름에 맞춰 실제 Repository를 호출하는 실행기.
// 모든 조회는 반드시 farmCode로 스코프해서, 다른 농장 데이터가 답변에 섞이지 않게 함.
@Component
@RequiredArgsConstructor
public class AssistantToolExecutor {

    private static final int MAX_WORKLOG_ITEMS = 30;

    private final MilkRepository milkRepository;
    private final BreedRecordRepository breedRecordRepository;
    private final WorkLogRepository workLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public ObjectNode execute(String farmCode, String toolName, JsonNode args) {
        return switch (toolName) {
            case "get_milk_average" -> getMilkAverage(farmCode, args);
            case "get_pending_breeding_count" -> getPendingBreedingCount(farmCode);
            case "get_recent_worklogs" -> getRecentWorklogs(farmCode, args);
            default -> {
                ObjectNode error = objectMapper.createObjectNode();
                error.put("error", "알 수 없는 도구입니다: " + toolName);
                yield error;
            }
        };
    }

    private ObjectNode getMilkAverage(String farmCode, JsonNode args) {
        LocalDate start = LocalDate.parse(args.get("startDate").asText());
        LocalDate end = LocalDate.parse(args.get("endDate").asText());

        List<MilkRecord> records = milkRepository.findByCow_RegisteredBy_FarmCodeAndMilkedDateBetween(farmCode, start, end);

        double average = records.stream()
                .mapToDouble(MilkRecord::getAmount)
                .average()
                .orElse(0.0);

        ObjectNode result = objectMapper.createObjectNode();
        result.put("averageAmountLiters", Math.round(average * 100) / 100.0);
        result.put("recordCount", records.size());
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());
        return result;
    }

    private ObjectNode getPendingBreedingCount(String farmCode) {
        long count = breedRecordRepository.countByUserId_FarmCodeAndCheckResult(farmCode, PregnancyResult.WAITING);

        ObjectNode result = objectMapper.createObjectNode();
        result.put("pendingCount", count);
        return result;
    }

    private ObjectNode getRecentWorklogs(String farmCode, JsonNode args) {
        int days = args.has("days") ? args.get("days").asInt() : 7;
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);

        List<WorkLogEntity> logs = workLogRepository.findByUserId_FarmCodeAndWorkDateTimeBetween(farmCode, start, end);

        ArrayNode logsArray = objectMapper.createArrayNode();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        // 프롬프트 토큰 절약을 위해 최대 30건만 모델에 전달
        logs.stream().limit(MAX_WORKLOG_ITEMS).forEach(log -> {
            ObjectNode logNode = objectMapper.createObjectNode();
            logNode.put("date", log.getWorkDateTime().format(formatter));
            logNode.put("workType", log.getWorkType().name());
            logNode.put("content", log.getContent());
            logsArray.add(logNode);
        });

        ObjectNode result = objectMapper.createObjectNode();
        result.put("totalCount", logs.size());
        result.set("logs", logsArray);
        return result;
    }
}
