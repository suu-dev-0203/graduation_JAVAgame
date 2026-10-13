package com.example.graduation_java_game;

/**
 * 回答判定と、画面に反映するゲーム状態です。
 */
public record AnswerResponse(
		boolean correct,
		boolean timeout,
		int enemyHp,
		int combo,
		int score,
		String effectType,
		int stageCorrectCount,
		int stageScore,
		int maxCombo,
		boolean stageClear,
		boolean perfect,
		Long nextStageQuestionId) {
}
