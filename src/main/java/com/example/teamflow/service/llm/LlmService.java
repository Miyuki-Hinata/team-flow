package com.example.teamflow.service.llm;

/**
 * LLM（大規模言語モデル）サービスのインターフェース。
 *
 * 実装クラス：
 * - MockLlmService: 開発・テスト用の固定レスポンス
 * - ClaudeLlmService: 本番用、Anthropic API を呼び出す（後で実装）
 */
public interface LlmService {

    /**
     * プロンプトを渡してサマリを生成する。
     *
     * @param prompt LLMに渡すプロンプト
     * @return 生成されたサマリ文字列
     */
    String generateSummary(String prompt);
}