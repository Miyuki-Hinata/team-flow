# TeamFlow プロジェクト概要

## 技術スタック
- Java 17 / Spring Boot 4.0.3
- MySQL（Docker）
- JWT認証

## 起動方法
./mvnw spring-boot:run

## DB接続
docker exec -it team-flow-db-1 mysql -uuser -ppassword

## 現在の状況
Week 10: 認可設定（管理者・一般ユーザー）の実装中

## 現在の問題
/api/auth/login に POST すると403が返ってくる
SecurityConfigでpermitAll()を設定済みのはずだが機能していない