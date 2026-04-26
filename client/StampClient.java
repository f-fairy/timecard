import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StampClient {
    public static void main(String[] args) throws Exception {
        // RustサーバーのURL（Codespaces内なのでlocalhostの3000ポート）
        String url = "http://127.0.0.1:3000/stamp";

        // 送信するJSONデータ（トシさんの出勤信号！）
        String json = """
            {
                "name": "Toshi",
                "status": "出勤"
            }
            """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        System.out.println("Rustサーバーに打刻データを送信中...");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("サーバーからの返事: " + response.body());
    }
}
