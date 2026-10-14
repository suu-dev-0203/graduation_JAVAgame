package com.example.graduation_java_game;

/**
 * ゲーム全体を完了したときに表示する結果です。
 */
public record GameResultResponse(
		Long gameSessionId,
		int questionCount,
		int totalStageCount,
		int completedStageCount,
		int totalCorrectCount,
		int totalScore,
		int perfectStageCount,
		GameSessionStatus status) {
}
