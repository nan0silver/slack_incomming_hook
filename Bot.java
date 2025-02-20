import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;

public class Bot {
    public static void main(String[] args) {
        String webhookUrl = System.getenv("SLACK_WEBHOOK_URL");
        String message = System.getenv("SLACK_WEBHOOK_MSG");

        // Java 11 -> fetch
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            //.POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"테스트 메시지\"}"))
            .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\" " + message + "\"}"))
            .build();

        try {
            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
            );
            System.out.println("요청 코드: " + response.statusCode());
            System.out.println("응답 결과: "+ response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}