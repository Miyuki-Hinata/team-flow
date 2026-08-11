# TeamFlow バックエンドのイメージを作るレシピ（マルチステージビルド）。
#
# ステージ1（build）：JDK + Maven Wrapper で jar を作る「作業場」。
# ステージ2（実行用）：JRE だけの土台に jar を載せる「出荷箱」。
# 作業場はイメージ完成後に使われないため、最終イメージにビルド道具とソースコードは残らない。

# ---- ステージ1：ビルド ----
# JDK 21 入りの公式イメージを土台にし、このステージに build という名前を付ける。
# Maven 本体入りのイメージは使わず、リポジトリ同梱の Maven Wrapper（mvnw）でビルドする。
# ローカル開発時とコンテナ内ビルドで Maven のバージョンが必ず一致するため。
FROM eclipse-temurin:21-jdk AS build

# 以降の命令が実行される作業ディレクトリ。無ければ作られる
WORKDIR /build

# ソースより先に「依存の定義」だけを持ち込み、依存ライブラリをダウンロードしておく。
# Docker は命令ごとの結果をレイヤとしてキャッシュし、コピー元のファイルが
# 変わらない限り再利用する。この順番にすることで、ソースコードだけ変更した
# 再ビルドでは依存の再ダウンロード（数分）がキャッシュで飛ばせる
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline

# ソースを持ち込んで jar を作る。
# テストは実 MySQL（localhost:3306）とデモデータ前提で、イメージビルド中は
# 接続先が存在しないためスキップする。テストの実行は CI が担当する（Issue #22 C）
COPY src/ src/
RUN ./mvnw -B package -DskipTests

# ---- ステージ2：実行 ----
# JRE（実行環境のみ）の土台。この FROM 以降だけが最終イメージになり、
# ステージ1の JDK・Maven・ソースコード・依存キャッシュは一切含まれない
FROM eclipse-temurin:21-jre

WORKDIR /app

# root のまま動かさないための実行専用ユーザー。
# 万一アプリが乗っ取られてもコンテナ内での権限を最小にする
RUN useradd --system --no-create-home appuser
USER appuser

# ステージ1（build）の成果物から jar だけを受け取る。
# バージョン番号をここに書かないよう *.jar で受け、名前を app.jar に固定する
COPY --from=build /build/target/*.jar app.jar

# このコンテナは 8080 番で待ち受けるという表明（ドキュメントの役割。公開自体は compose 側で行う）
EXPOSE 8080

# コンテナ起動時に実行するコマンド。Spring Boot の fat jar なので
# これ1つで内蔵 Tomcat ごとサーバーが立ち上がる
ENTRYPOINT ["java", "-jar", "app.jar"]
