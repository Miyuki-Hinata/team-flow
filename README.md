# TeamFlow

<!-- コマ18で追記：CI バッジ（front / back） -->

多職種が同時に動く病棟で、「誰が・どの患者に・何をするか」が口頭とメモに散っている問題を解決する、**チーム内タスク共有アプリ**です。
**前職で看護師として病棟に勤務した実体験**をもとに、医師・看護師・薬剤師・リハビリ職など 11 職種が担当・期限・進捗を**患者単位**で共有できるように設計しました。
Spring Boot 4 + React 19 / TypeScript の SPA。JWT + リフレッシュトークン認証、全テーブル共通の論理削除による監査証跡、Flyway によるスキーマ管理までを個人開発で実装しています。

| | |
|---|---|
| バックエンド（本リポジトリ） | API / DB 設計 / 認証・認可。**プロジェクトの入口はこちら** |
| フロントエンド | [`team-flow-front`](https://github.com/Miyuki-Hinata/team-flow-front) |
| 全コンポーネントの設計意図 | [`docs/implementation/`（46 本）](https://github.com/Miyuki-Hinata/team-flow-front/tree/main/docs/implementation) |

---

## なぜ作ったか

病棟で一番時間を奪われていたのは医療行為ではなく、**情報の同期**でした。「あの患者さんの点滴指示、もう先生に確認の依頼しましたか？」を確認するために人を探す。申し送りまで状況が揃わない。誰かのメモにしか無い情報がある。

現場で「こういう仕組みがあれば」と思っていたことを、エンジニアへの転職を機に自分の手で形にしたのが TeamFlow です。作る機能・作らない機能の判断は、一般論だけではなく病棟での実体験に基づいて行いました。

また、特定の職種に向けたアプリにはせず、**多くの職種で使える構成**にしました。現場での情報の滞りは同職種間だけでなく、職種と職種の**境界**でも多く発生していたため、職種は [`Role`](src/main/java/com/example/teamflow/enums/Role.java) として 11 種を列挙し、画面や権限を特定の職種に固定しない設計にしています。

| 現場で起きていること | TeamFlow での解決 |
|---|---|
| 情報が人に紐づき、申し送りまで同期されない | タスク・お知らせを DB に集約し、状態と履歴を常時参照 |
| 担当が曖昧で、二重実施と抜け落ちが起きる | 担当者の明示（複数可 / 全員宛）＋ `未対応 / 対応中 / 完了` の状態管理 |
| 「この患者に今日何が残っているか」を横串で見られない | 受け持ち患者の **24 時間タイムライン**で当日の残タスクを時系列表示 |
| 記録は消してはいけない（退職者の実施記録も監査対象） | 物理削除を使わない**論理削除**＋変更履歴テーブル |

---

## デモ

<!-- コマ8-9で追記：デモGIF（nurse アカウントで撮影。ログイン→ダッシュボード→タスク→タイムライン→管理画面） -->

パスワードはいずれも `admin1234`。一般ユーザーと管理者で、表示されるメニューと操作できる範囲が変わります。

| ログイン ID | 職種 / 権限 | ログイン後の状態 |
|---|---|---|
| `nurse` | 看護師（一般） | 受け持ち患者・本日のタスク・要対応患者が入っている。「管理」メニューは表示されない |
| `doctor` | 医師（一般） | 担当患者を持つ医師のアカウント |
| `admin` | 管理者 | `/admin`（ユーザー・部署・カテゴリ・プロジェクトの管理）が使える |

デモデータはタスク期限を `CURDATE()` 相対で投入しているため、**起動した日がいつでも「本日のタスク」が入った状態**になります（[data-demo.sql](src/main/resources/data-demo.sql)）。

---

## 技術スタック

| 層 | 技術 |
|---|---|
| バックエンド | Java 17 / Spring Boot 4.0 / Spring Security / Spring Data JPA (Hibernate) / JJWT |
| DB | MySQL 8 / **Flyway**（スキーマ管理） |
| フロントエンド | React 19 / TypeScript / Vite / React Router 7 / styled-components |
| テスト | JUnit + MockMvc（バックエンド）/ Vitest + React Testing Library（フロントエンド） |

<!-- コマ6で追記：技術選定の理由（Spring Boot / Flyway / styled-components / JWT / Context API＝Redux不採用） -->

---

## アーキテクチャ・設計のポイント

<!-- コマ5で追記：構成図＋ER図（V1__init.sql から作図） -->

### 1. 監査証跡を前提にした論理削除

医療現場では「担当者が退職したら記録が消える」ことは許されません。全エンティティが継承する [`BaseEntity`](src/main/java/com/example/teamflow/entity/BaseEntity.java) に `deleted_at` / `updated_by` を持たせ、**物理削除をどこにも使っていません**。加えてタスク・お知らせは「いつ・誰が・どの項目を・何から何に変えたか」を履歴テーブル（`task_histories` / `announcement_histories`）に、パスワード変更も `password_change_logs` に記録します。

### 2. リフレッシュトークンはローテーション＋DB 失効管理

アクセストークン 15 分 / リフレッシュトークン 7 日（HttpOnly Cookie）。リフレッシュのたびにトークンを発行し直し、旧トークンは [`refresh_tokens`](src/main/java/com/example/teamflow/entity/RefreshToken.java) テーブルで失効させます。JWT に `typ` クレームを持たせ、**リフレッシュトークンを API 認証に流用できない**ようにし（[JwtUtil](src/main/java/com/example/teamflow/security/JwtUtil.java)）、論理削除済みユーザーの認証は [JwtAuthFilter](src/main/java/com/example/teamflow/security/JwtAuthFilter.java) で拒否します。

### 3. 権限は「職種」と分離し、API と UI で二重にガード

`users.level`（権限）と `Role`（職種）を分けています。職種はあくまで属性で、**職種から権限を導出しません**（現場では「看護師だが管理者」が普通に存在するため）。防御の本体は [`SecurityConfig`](src/main/java/com/example/teamflow/config/SecurityConfig.java) の `hasRole("ADMIN")` で、フロントの `AdminRoute`・メニュー出し分けは**体験のための二重ガード**です。認可の判定はサーバー側だけを信頼し、UI の出し分けには防御の役割を持たせていません。

### 4. スキーマの正解を Flyway に一本化

テーブル定義の唯一の正解は [`V1__init.sql`](src/main/resources/db/migration/V1__init.sql)（16 テーブル）です。**手書きのスキーマ資料はあえて作りません**。Markdown に写した瞬間に「第 2 の正解」が生まれ、必ず古くなるからです。デモデータは Spring プロファイルで分離し（[application-demo.properties](src/main/resources/application-demo.properties)）、本番想定起動ではマスタのみ投入されます。

### 5. 設定ミスは起動時に落とす

`JWT_SECRET` に既定値を持たせず、未設定なら**起動に失敗**させます。DB 資格情報も [`RequiredConfigValidator`](src/main/java/com/example/teamflow/config/RequiredConfigValidator.java) で起動時に検証します。一方、接続先（ホスト・ポート）や CORS には安全側の既定値を置きました。**「気づかず動いてしまう」ことが危険な値は必須に、「動かないだけ」の値は既定値あり**、と設定漏れの危険の向きで分けています。

---

## 開発の進め方と学び

### 設計判断を文書で残す

全コンポーネントについて「何を作ったか・**なぜこの設計にしたか**・何と一貫させたか」を [`docs/implementation/`（46 本）](https://github.com/Miyuki-Hinata/team-flow-front/tree/main/docs/implementation) に残しています。コミット本文にも判断理由を書く運用です（`git log` 参照）。

### 詰まった問題から学んだこと（抜粋）

- **`Access denied for user '${DB_USERNAME}'` の謎** — 「環境変数にすれば設定漏れは起動失敗になる」は思い込みでした。Spring の Binder は未解決のプレースホルダを**文字列のまま握りつぶして通す**ため、MySQL のエラーに化けます。さらに検証を `@PostConstruct` に書くと **Flyway の DB 接続の方が先に走る**ことも分かり、`BeanFactoryPostProcessor` での検証に落ち着きました。
- **CORS の矛盾はリクエスト時まで発覚しない** — `allowCredentials(true)` と `*` は併用できませんが、旧実装では起動が成功し、ブラウザから叩いて初めて（しかも 401 という無関係な症状で）壊れました。起動時に設定を組み立てて `validateAllowCredentials()` を明示的に呼ぶ形に直しました。
- **jjwt は鍵長で署名アルゴリズムが変わる** — 鍵を長くしたら HS256 が HS512 に**自動で**切り替わっていました。アルゴリズムを明示して固定しています。

### AI 支援について

実装には AI コーディング支援を全面的に使っています。一方で**設計判断（何を作るか・どの方式を選ぶか・何をやらないか）は自分で行い**、その根拠を `docs/implementation/` とコミット本文に残しています。上の「詰まった問題」はいずれも、AI の出力を実測で検証する過程で見つかったものです。

---

## セットアップ

<!-- コマ12-13で docker compose up 一発に統合予定。完成したらこの節を差し替える -->

```bash
# 1) 2つのリポジトリを並べて clone
git clone https://github.com/Miyuki-Hinata/team-flow.git
git clone https://github.com/Miyuki-Hinata/team-flow-front.git

# 2) バックエンド：環境変数を設定して起動
cd team-flow
cp .env.example .env        # JWT_SECRET 等を設定（生成方法は .env.example のコメント参照）
docker compose up -d        # MySQL 8 が立ち上がる
./mvnw spring-boot:run      # http://localhost:8080

# 3) フロントエンド
cd ../team-flow-front
cp .env.example .env        # VITE_API_BASE_URL=http://localhost:8080
npm install
npm run dev                 # http://localhost:5173

# 4) ログイン：nurse / admin1234（管理者は admin / admin1234）
```

---

## 今後の課題

現在も継続して開発しています（Issue はこのリポジトリに集約）。

- [#22](https://github.com/Miyuki-Hinata/team-flow/issues/22) `docker compose up` 一発起動への統合と CI（フロント / バックエンド）
- [#24](https://github.com/Miyuki-Hinata/team-flow/issues/24) タスク一覧のページネーションと N+1 クエリの点検
- [#26](https://github.com/Miyuki-Hinata/team-flow/issues/26) AI サマリの本実装 — 現状は `LlmService` インターフェースとモック実装まで。**LLM 接続は未実装**で、差し替えられる形にしてあります
- [#27](https://github.com/Miyuki-Hinata/team-flow/issues/27) E2E テスト（Selenium）
- [#25](https://github.com/Miyuki-Hinata/team-flow/issues/25) 公開デモ環境のデプロイ

---

<details>
<summary><b>機能・画面の一覧（クリックで展開）</b></summary>

### 機能

| 機能 | 内容 |
|---|---|
| 認証 | ログイン ID / パスワード。アクセストークン 15 分＋リフレッシュトークン 7 日（HttpOnly Cookie・ローテーション・DB 失効） |
| 認可 | 管理者のみ：マスタ管理・ユーザー管理・患者の登録／削除。API（Spring Security）と UI（AdminRoute）の二重ガード |
| タスク管理 | 患者 / プロジェクト / カテゴリ紐づけ、優先度・期限・状態、複数担当、タスク間の関連付け（自己参照多対多） |
| 変更履歴 | タスク・お知らせの項目単位の変更履歴（いつ・誰が・何から何に） |
| 患者管理 | 患者情報・担当医の紐づけ・患者単位のタスクカンバン |
| 受け持ち患者 | 自分の受け持ちを選択し、24 時間縦タイムライン（午前/午後/夜・7 日分切替・期限超過警告）で表示 |
| お知らせ | 部署 / カテゴリ / 優先度つき掲示、既読管理と未読バッジ |
| 管理画面 | ユーザー・部署・カテゴリ・プロジェクトの CRUD（タブ切替） |

### 画面

| 画面 | パス | 備考 |
|---|---|---|
| ログイン | `/login` | 未認証で唯一到達できる画面 |
| ダッシュボード | `/dashboard` | サマリカード×3＋未読お知らせ＋本日の要対応患者 |
| 受け持ち患者 | `/my-patients` | 一覧タブ／24 時間タイムラインタブ |
| 患者 | `/patients`, `/patients/:id`, `/patients/create` | 登録は**管理者のみ** |
| タスク | `/tasks`, `/tasks/my-tasks`, `/tasks/:id`, `/tasks/create` | 詳細で状態遷移と変更履歴 |
| お知らせ | `/announcements`, `/announcements/:id`, `/announcements/create` | 未読/既読タブ |
| 管理 | `/admin` | **管理者のみ** |

### 規模

| 項目 | 数 |
|---|---|
| テーブル | 16 |
| API コントローラ | 11 |
| 画面ルート | 16 |
| 共通 UI コンポーネント | 27 |
| 設計意図ドキュメント | 46 |
| テスト | バックエンド 22 件・フロントエンド 26 件（いずれも緑） |

</details>

---

Personal portfolio. No license granted for reuse.

<!--
=== 追記予定（docs/schedule.local.md のコマ番号）===
コマ5   : 構成図＋ER図 →「アーキテクチャ・設計のポイント」冒頭へ
コマ6   : 技術選定の理由 →「技術スタック」直下へ
コマ7   : 設計判断の深掘り・あえてやらなかったこと（Redux不採用 / Testcontainers不採用 /
          物理削除・パスワードリセット未実装）→「設計のポイント」に追記 or 新節
コマ8-9 : デモGIF →「デモ」冒頭へ（nurse で撮影。admin はダッシュボードが空で見栄えしない）
コマ12-13: セットアップ節を docker compose up 一発に差し替え
コマ18  : CI バッジ
-->
