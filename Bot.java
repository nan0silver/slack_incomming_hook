import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;

public class Bot {
    public static void main(String[] args) {
        // 환경 변수 가져오기
        String webhookUrl = System.getenv("SLACK_WEBHOOK_URL");
        String message = System.getenv("SLACK_WEBHOOK_MSG");
        String llmUrl = System.getenv("LLM_URL");
        String llmKey = System.getenv("LLM_KEY");

        // 환경 변수 체크
        if (webhookUrl == null || webhookUrl.isBlank()) {
            System.err.println("❌ SLACK_WEBHOOK_URL이 설정되지 않았습니다.");
            return;
        }
        if (message == null || message.isBlank()) {
            System.err.println("❌ SLACK_WEBHOOK_MSG가 설정되지 않았습니다.");
            return;
        }
        if (llmUrl == null || llmUrl.isBlank()) {
            System.err.println("❌ LLM_URL이 설정되지 않았습니다.");
            return;
        }
        if (llmKey == null || llmKey.isBlank()) {
            System.err.println("❌ LLM_KEY가 설정되지 않았습니다.");
            return;
        }

        // LLM 요청 JSON 생성 (올바른 메시지 형식)
        Map<String, Object> llmPayload = new HashMap<>();
        llmPayload.put("model", "togethercomputer/Meta-Llama-3-8B-Instruct");


        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);

        llmPayload.put("messages", messages);

        Gson gson = new Gson();
        String llmJson = gson.toJson(llmPayload);

        HttpClient llmClient = HttpClient.newHttpClient();
        HttpRequest llmRequest = HttpRequest.newBuilder()
            .uri(URI.create(llmUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + llmKey)
            .POST(HttpRequest.BodyPublishers.ofString(llmJson, StandardCharsets.UTF_8))
            .build();

        String llmResponseBody = null;
        try {
            HttpResponse<String> llmResponse = llmClient.send(llmRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("✅ LLM 요청 코드: " + llmResponse.statusCode());
            System.out.println("✅ LLM 응답: " + llmResponse.body());

            if (llmResponse.statusCode() == 200) {
                llmResponseBody = llmResponse.body();
            } else {
                System.err.println("❌ LLM 요청 실패, 기본 메시지 사용");
                llmResponseBody = "LLM 요청 실패 (코드: " + llmResponse.statusCode() + ")";
            }
        } catch (Exception e) {
            System.err.println("❌ LLM 요청 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            llmResponseBody = "LLM 응답을 받을 수 없습니다.";
        }

        // Slack 메시지 전송
        Map<String, String> slackPayload = new HashMap<>();
        slackPayload.put("text", llmResponseBody);
        String slackJson = gson.toJson(slackPayload);

        HttpClient slackClient = HttpClient.newHttpClient();
        HttpRequest slackRequest = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(slackJson, StandardCharsets.UTF_8))
            .build();

        try {
            HttpResponse<String> slackResponse = slackClient.send(slackRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("✅ Slack 요청 코드: " + slackResponse.statusCode());
            System.out.println("✅ Slack 응답: " + slackResponse.body());

            if (slackResponse.statusCode() != 200) {
                System.err.println("❌ Slack Webhook 요청 실패!");
            }
        } catch (Exception e) {
            System.err.println("❌ Slack 메시지 전송 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
