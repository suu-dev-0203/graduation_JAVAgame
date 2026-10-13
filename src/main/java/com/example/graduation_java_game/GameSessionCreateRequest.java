package com.example.graduation_java_game;

/**
 * ゲーム開始APIでブラウザから受け取る値です。
 *
 * @param chapterId 遊びたい学習テーマのID
 * @param questionCount 遊ぶ問題数（5、10、20のいずれか）
 */
public record GameSessionCreateRequest(Long chapterId, Integer questionCount) {
}
