import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StampClient {
    public static void main(String[] args) {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) {
	    e.printStackTrace();
	}

	// ★ 共通の日本語フォントを設定（Windowsなら"MS Gothic", Macなら"Hiragino Sans"など）
	Font jpFont = new Font("MS Gothic", Font.PLAIN, 16); 
	// もしMacなら Font jpFont = new Font("SansSerif", Font.PLAIN, 16); でもOK

        // 1. ウィンドウ(Frame)の作成
	JFrame frame = new JFrame("Toshi Timecard Client");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setSize(350, 250); // 少し大きくしましょう
	frame.setLayout(new FlowLayout());

	JLabel label = new JLabel("名前: Toshi");
	label.setFont(jpFont); // フォントを適用

	JButton btnIn = new JButton("出勤");
	btnIn.setFont(jpFont); // フォントを適用

	JButton btnOut = new JButton("退勤");
	btnOut.setFont(jpFont); // フォントを適用

	JTextArea logArea = new JTextArea(5, 20);
	logArea.setFont(jpFont); // フォントを適用
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
//                    .uri(URI.create("http://localhost:3000/stamp")) // RustサーバーのURL
                    .uri(URI.create("https://upgraded-train-7vq94x79q5vgfpp9-3000.app.github.dev/stamp")) // RustサーバーのURL
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

//            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                  .thenAccept(res -> {
//                      logArea.append(status + "成功！: " + res.body() + "\n");
//                  });
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                  .thenAccept(res -> {
                      // ステータスコード（200なら成功）も一緒に表示
                      logArea.append(status + "送信完了: HTTP " + res.statusCode() + "\n");
                      if (res.statusCode() == 200) {
                          logArea.append("サーバー返答: " + res.body() + "\n");
                      }
                  });

        } catch (Exception e) {
            logArea.append("エラー発生: " + e.getMessage() + "\n");
        }
    }
}
