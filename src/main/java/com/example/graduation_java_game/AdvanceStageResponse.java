package com.example.graduation_java_game;

/**
 * 次のStageを作成した結果です。
 */
public record AdvanceStageResponse(
		Long gameSessionId,
		Long stageSessionId,
		int currentStageNumber,
		int totalStageCount,
		GameSessionStatus gameSessionStatus) {
}
