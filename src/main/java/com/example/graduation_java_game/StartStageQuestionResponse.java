package com.example.graduation_java_game;

import java.time.Instant;

/**
 * StageQuestionの開始APIが成功したときに返す情報です。
 *
 * @param stageQuestionId 開始したStageQuestionのID
 * @param startedAt サーバーが記録した開始時刻
 * @param remainingTimeMs サーバーが計算した残り時間（ミリ秒）
 */
public record StartStageQuestionResponse(
		Long stageQuestionId,
		Instant startedAt,
		long remainingTimeMs) {
}
