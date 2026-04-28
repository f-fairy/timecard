use axum::{routing::post, Router, Json, extract::State};
use serde::Deserialize;
use sqlx::MySqlPool;
use std::sync::Arc;

// Javaから届くJSONのカタチ
#[derive(Deserialize)]
struct Stamp {
    name: String,
    status: String,
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
        .route("/stamp", post(handle_stamp))
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
