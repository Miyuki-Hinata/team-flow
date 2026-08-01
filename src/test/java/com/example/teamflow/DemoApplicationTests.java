package com.example.teamflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// demo … デモデータ(data-demo.sql)を投入する
// test … application-test.properties（テスト専用の jwt.secret）を読み込む
@ActiveProfiles({"demo", "test"})
@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
