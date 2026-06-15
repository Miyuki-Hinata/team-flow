package com.example.teamflow.service.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 開発・テスト用のモック実装。
 *
 * 固定のサマリ文字列を返すだけ。
 * application.properties で llm.provider=mock の時に有効化される（デフォルト）。
 */
@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "mock", matchIfMissing = true)
public class MockLlmService implements LlmService {

    @Override
    public String generateSummary(String prompt) {
        // Claude.ai での検証済みサマリを固定で返す
        return """
                ## 朝の申し送り用タスクサマリー
                
                ### 1. ⚠️ 期限切れタスク（自分担当を優先・時系列順）
                - ★ 輸液交換（🟡MEDIUM・処置）2026-06-11期限 ※未着手
                - ★ リハビリ介助（🟡MEDIUM・リハビリ）2026-06-12期限 ※未着手
                
                ### 2. 📋 私の本日タスク
                **期限時刻があるもの（時系列順）**
                - ★ 家族説明（🔴HIGH・指導）本日08:00 ✓完了
                - ★ バイタル測定（🟢LOW・測定）本日09:00 ✓完了
                - ★ 創部処置（🔴HIGH・処置）本日10:00 ※進行中
                - ★ 服薬指導（🟡MEDIUM・指導）本日14:00 ※進行中
                - ★ 体位変換（🔴HIGH・処置）本日17:00
                
                ### 3. 👥 チーム全体の本日タスク
                - 経管栄養準備（🔴HIGH・処置）本日11:00
                - 退院指導（🟡MEDIUM・指導）本日16:00
                
                ### 4. 💡 業務上の注意事項・準備事項
                **朝イチ対応（期限切れ）**
                - ★ 輸液交換：輸液ボトル・ルートの準備、感染対策の確認
                - ★ リハビリ介助：リハビリスタッフとの連携確認
                
                **本日実施前準備**
                - ★ 創部処置（10:00）：滅菌物品とドレッシング材を事前準備
                - ★ 服薬指導（14:00）：服薬指導書類を準備・確認
                - ★ 体位変換（17:00）：応援要請の判断、褥瘡確認
                
                ### 5. 📝 全体所感
                期限切れの輸液交換とリハビリ介助を最優先で対応し、本日は朝から夕方にかけてHIGH優先度タスクが集中しているため、早めの準備と時間管理が必須です。
                
                ---
                ⚠️ これはモック実装による固定レスポンスです。本番環境では Anthropic Claude API を呼び出します。
                """;
    }
}