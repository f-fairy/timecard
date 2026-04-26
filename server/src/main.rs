use axum::{routing::post, Router, Json};
use serde::Deserialize;

#[derive(Deserialize)]
struct Stamp {
    name: String,
    status: String,
}

#[tokio::main]
async fn main() {
    let app = Router::new().route("/stamp", post(|Json(payload): Json<Stamp>| async move {
        println!("打刻受信: {} さんが {} しました！", payload.name, payload.status);
        format!("{}さん、お疲れ様です！", payload.name)
    }));

    let listener = tokio::net::TcpListener::bind("0.0.0.0:3000").await.unwrap();
    println!("Server running on port 3000...");
    axum::serve(listener, app).await.unwrap();
}
