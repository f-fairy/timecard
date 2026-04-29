//use axum::{routing::post, Router, Json, extract::State};
use axum::{routing::{get, post}, Router, Json, extract::State, response::Html};
use serde::Deserialize;
use sqlx::MySqlPool;
use std::sync::Arc;

// Javaから届くJSONのカタチ
#[derive(Deserialize)]
struct Stamp {          // 入口用
    name: String,
    status: String,
}
// DBから取得する用のデータ構造（deriveにFromRowを追加）
#[derive(serde::Serialize, sqlx::FromRow)]
struct StampResult {    // 出口用
    id: i32,
    name: String,
    status: String,
    created_at: chrono::DateTime<chrono::Utc>,
}

// 共有する状態（DB接続プール）
struct AppState {
    db: MySqlPool,
}

#[tokio::main]
async fn main() {
    // 1. データベースへの接続文字列（ユーザー:パスワード@ホスト/DB名）
    // let database_url = "mysql://root:@127.0.0.1/timecard_db";
    // root ではなく codespace を指定（パスワードなし）
    let database_url = "mysql://codespace@127.0.0.1/timecard_db";

    // 2. 接続プールの作成
    let pool = MySqlPool::connect(database_url)
        .await
        .expect("DBに接続できませんでした！");

    let shared_state = Arc::new(AppState { db: pool });

    // 3. ルーティングの設定（Stateを渡す）
    let app = Router::new()
        .route("/", get(index_page))       // ← ブラウザでアクセスした時にHTMLを返す
        .route("/stamp", post(handle_stamp))
        .route("/history", get(get_history)) // ← 履歴を取得するAPI
        .with_state(shared_state);

    let listener = tokio::net::TcpListener::bind("0.0.0.0:3000").await.unwrap();
    println!("Rust Server Running (DB Mode) on port 3000...");
    axum::serve(listener, app).await.unwrap();
}

// 打刻リクエストを処理する関数
async fn handle_stamp(
    State(state): State<Arc<AppState>>,
    Json(payload): Json<Stamp>,
) -> String {
    println!("打刻受信: {} さんが {} しました！", payload.name, payload.status);

    // 4. SQLの実行（MySQLにINSERT）
    let result = sqlx::query("INSERT INTO stamps (name, status) VALUES (?, ?)")
        .bind(&payload.name)
        .bind(&payload.status)
        .execute(&state.db)
        .await;

    match result {
        Ok(_) => {
            println!("DB保存成功！");
            format!("{}さん、DBに記録しました！", payload.name)
        }
        Err(e) => {
            println!("DB保存失敗...: {:?}", e);
            "エラーが発生しました".to_string()
        }
    }
}

// HTML画面を返す関数
async fn index_page() -> Html<&'static str> {
    Html(include_str!("index.html"))
}

// 履歴をDBから取得して返す関数
async fn get_history(State(state): State<Arc<AppState>>) -> Json<Vec<StampResult>> {
    let rows = sqlx::query_as::<_, StampResult>("SELECT id, name, status, created_at FROM stamps ORDER BY id DESC")
        .fetch_all(&state.db)
        .await
        .unwrap_or_default();
    
    Json(rows)
}
