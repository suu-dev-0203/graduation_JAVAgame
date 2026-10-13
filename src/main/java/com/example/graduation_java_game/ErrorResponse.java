package com.example.graduation_java_game;

/**
 * APIがエラーになった理由をブラウザへ返すデータです。
 *
 * @param code プログラムから判別できるエラー名
 * @param message 利用者に伝えるエラーの説明
 */
public record ErrorResponse(String code, String message) {
}
