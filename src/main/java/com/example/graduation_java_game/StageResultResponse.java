package com.example.graduation_java_game;

/**
 * Stageをクリアしたときに表示する結果です。
 */
public record StageResultResponse(
		Long gameSessionId,
		Long stageSessionId,
		int stageNumber,
		int correctCount,
		int stageScore,
		int maxCombo,
		boolean perfect,
		boolean hasNextStage,
		boolean gameCompleted) {
}
