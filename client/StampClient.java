import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StampClient {
    public static void main(String[] args) {
        // 1. ウィンドウ(Frame)の作成
        JFrame frame = new JFrame("Toshi Timecard Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new FlowLayout());

        // 2. 部品の作成
        JLabel label = new JLabel("名前: Toshi");
        JButton btnIn = new JButton("出勤");
        JButton btnOut = new JButton("退勤");
        JTextArea logArea = new JTextArea(5, 20);
        logArea.setEditable(false);

        // 3. 出勤ボタンが押された時の処理
        btnIn.addActionListener(e -> sendStamp("出勤", logArea));
        btnOut.addActionListener(e -> sendStamp("退勤", logArea));

        // 4. ウィンドウに配置
        frame.add(label);
        frame.add(btnIn);
        frame.add(btnOut);
        frame.add(new JScrollPane(logArea));

        // 5. 表示！
        frame.setVisible(true);
    }

    private static void sendStamp(String status, JTextArea logArea) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format("{\"name\":\"Toshi\", \"status\":\"%s\"}", status);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/stamp")) // RustサーバーのURL
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                  .thenAccept(res -> {
                      logArea.append(status + "成功！: " + res.body() + "\n");
                  });

        } catch (Exception e) {
            logArea.append("エラー発生: " + e.getMessage() + "\n");
        }
    }
}