package com.example.teamflow.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 必須の設定値が揃っているかを、起動時にまとめて検証する。
 *
 * <p>なぜ必要か：<br>
 * application.properties は DB の資格情報を {@code ${DB_USERNAME}} のように環境変数から読む。
 * ところが Spring Boot が {@code spring.datasource.*} を束ねる仕組み（Binder）は、
 * <b>解決できなかったプレースホルダを例外にせず、文字列のまま通してしまう</b>。
 * その結果、環境変数を設定し忘れると
 *
 * <pre>Access denied for user '${DB_USERNAME}'@'...'</pre>
 *
 * という「MySQL 側の問題に見える」エラーで落ちる。原因が設定漏れだと気づきにくい。
 *
 * <p>（同じ application.properties でも {@code jwt.secret} は JwtUtil が {@code @Value} で
 * 受け取っており、そちらは未解決のプレースホルダで例外になる。仕組みによって挙動が違うため、
 * 「環境変数にすれば設定漏れは必ず起動失敗になる」とは言い切れない。ここはその差を埋める役割。）
 *
 * <p>そこで DB に接続しに行く前にこのクラスで検証し、
 * 「どの環境変数が足りないのか」を名指しで伝えてから起動を止める。
 * JwtUtil が {@code @PostConstruct} で鍵の長さを検証しているのと同じ考え方で、
 * 設定ミスは「起動した／しない」で判定できる方が運用上わかりやすい。
 *
 * <p>{@code @PostConstruct} ではなく {@link BeanFactoryPostProcessor} を使う理由：<br>
 * {@code @PostConstruct} は「この Bean が生成されたとき」に走るが、DataSource や Flyway は
 * 別の Bean であり、生成順は保証されない。実際に {@code @PostConstruct} で試したところ
 * <b>先に Flyway が DB へ接続しにいって上記のエラーで落ち、検証が走る前に終了した</b>。
 * BeanFactoryPostProcessor は「Bean の生成が始まる前」に呼ばれる拡張点なので、
 * DB 接続より確実に先回りできる。
 *
 * <p>環境変数名ではなく設定キー（{@code spring.datasource.username}）を検証しているのは、
 * テスト（application-test.properties）のように環境変数を経由せず値を直接指定する場合も
 * 正しく「設定済み」と判定するため。見るべきは「最終的に何が使われるか」であって、
 * 「どこから来たか」ではない。
 */
@Component
public class RequiredConfigValidator implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment environment;

    /**
     * Environment を受け取る。
     *
     * <p>このクラスだけコンストラクタ注入（他のクラスで使っている書き方）にしていないのは、
     * BeanFactoryPostProcessor が「DI の仕組みそのものより先」に生成されるため。
     * コンストラクタ注入を試すと、依存を解決する仕組みがまだ動いておらず
     * 「No default constructor found」で起動に失敗する。
     * EnvironmentAware は、その段階でも Spring が値を渡してくれる数少ない受け取り方。
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        requireResolved("spring.datasource.username", "DB_USERNAME");
        requireResolved("spring.datasource.password", "DB_PASSWORD");
    }

    /**
     * 設定キーが「解決済みかつ空でない」ことを確認する。
     *
     * @param propertyKey 検証する設定キー（例：spring.datasource.username）
     * @param envVarName  未設定だったときにユーザーに伝える環境変数名（例：DB_USERNAME）
     */
    private void requireResolved(String propertyKey, String envVarName) {
        String value;
        try {
            // Environment#getProperty は入れ子のプレースホルダを解決し、
            // 解決できなければ IllegalArgumentException を投げる（Binder と違って握り潰さない）。
            value = environment.getProperty(propertyKey);
        } catch (IllegalArgumentException e) {
            // 元の例外は原因として繋いでおく（どのキーで失敗したかの情報を捨てないため）
            throw new IllegalStateException(missingMessage(envVarName), e);
        }

        // 環境変数を「空文字で定義した」場合はプレースホルダの解決自体は成功してしまうため、
        // 中身が空でないことも確認する（空のユーザー名では結局 DB に接続できない）。
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(missingMessage(envVarName));
        }
    }

    private String missingMessage(String envVarName) {
        return "環境変数 " + envVarName + " が設定されていません。"
                + ".env.example を参考に .env を作成するか、環境変数として直接渡してください。";
    }
}
