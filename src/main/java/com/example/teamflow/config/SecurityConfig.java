package com.example.teamflow.config;

import com.example.teamflow.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // CORS で許可するオリジン。設定値（cors.allowed-origins）から読み込む。
    // ハードコードしていた http://localhost:5173 を外に出した理由：
    //  ・許可オリジンは「どこにデプロイしたか」で変わる環境依存の値であり、コードの決定事項ではない
    //  ・本番用の URL を許可するためにソースを書き換えてビルドし直す、という運用を避ける
    // List<String> で受けると、Spring がカンマ区切りを自動で分割してくれるので
    // 複数オリジン（例：ローカル + デモ環境）を1つの環境変数で渡せる。
    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF無効化（REST API用）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        // 未認証（トークンが無い・無効）の場合は401を返す
                        // ※ これを設定しないとSpring Securityのデフォルトである403が返り、
                        //    フロントの「401検知→リフレッシュ」のロジックが機能しない
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()

                        // マスタ（部署・カテゴリ・プロジェクト）の作成/更新/削除は管理者(ADMIN)だけに限定する。
                        // GET（参照）は誰でも可＝下の anyRequest().authenticated() に任せる。
                        .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/projects/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/projects/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasRole("ADMIN")

                        // ユーザー（職員）の作成/更新/削除は管理者(ADMIN)だけに限定する。
                        // ただし PUT /api/users/me/password（自分のパスワード変更）は全ユーザーが使うため、
                        // 先に authenticated として通しておく（この行を先に置くのが肝）。
                        // マッチャは "/api/users/*"（=1セグメント。/api/users/5 や /api/users/me）を使う。
                        // "/api/users/**"（2セグメント以上も含む）にすると /api/users/me/password まで
                        // ADMIN 限定になり、一般ユーザーが自分のパスワードを変更できなくなるため使わない。
                        // GET（一覧＝担当者選択などで使用／詳細）は下の anyRequest().authenticated() に任せる。
                        .requestMatchers(HttpMethod.PUT, "/api/users/me/password").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
                        .anyRequest().authenticated()  // それ以外は認証必要
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // CORS の中身は corsConfigurationSource() の Bean に持たせている。
                // withDefaults() を指定すると、Spring Security が CorsConfigurationSource 型の
                // Bean を探して使ってくれるので、ここには「CORS を有効にする」ことだけを書けばよい。
                .cors(Customizer.withDefaults());
        return http.build();
    }

    /**
     * CORS の設定を組み立てる。
     *
     * <p>以前はフィルタチェーンの中にラムダで書いていたが、そのラムダは
     * <b>リクエストのたびに呼ばれる</b>ものだった（リクエスト内容ごとに違うルールを返せる仕組みのため）。
     * このアプリは常に同じルールを返すので、起動時に一度だけ組み立てる形に変えた。
     *
     * <p>本当の狙いは実行速度ではなく、<b>設定ミスが分かるタイミング</b>にある。<br>
     * 許可オリジンを環境変数から読むようにしたことで、{@code .env} に {@code *} と書けてしまう
     * ようになった。ところが {@code allowCredentials(true)} と {@code *} は同時に使えない
     * （誰でも認証情報付きで API を叩けてしまうため、CORS の仕様で禁止されている）。
     *
     * <p>この矛盾は、リクエスト処理の中で初めて検査される。つまり以前の書き方では
     * <b>アプリは正常に起動し、ブラウザからのリクエストで初めて壊れる</b>。しかも
     * クライアントに返るのは 401 なので、「ログインできない」という原因と無関係な症状に見える。
     *
     * <p>そこで組み立てを起動時に移したうえで、Spring 自身が持つ検査
     * {@link CorsConfiguration#validateAllowCredentials()} を明示的に呼ぶ。
     * 設定ミスなら起動時点で落ちる。DB の資格情報を RequiredConfigValidator で
     * 起動時に検証しているのと同じ方針を、CORS にも揃えた。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        // フロントは認証情報（Authorization ヘッダ）を付けて呼ぶため true にする
        config.setAllowCredentials(true);

        // 組み立て直後に検査する。オブジェクトを作っただけでは検査されないため、
        // この一行が無いと「起動時に落ちる」効果は得られない。
        config.validateAllowCredentials();

        // どのパスにこのルールを適用するか。API 全体が対象なので "/**"。
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}