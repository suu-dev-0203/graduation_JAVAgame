package com.example.graduation_java_game;

import java.util.List;

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
		Long nextStageQuestionId,
		List<ChoiceResult> choices,
		String shortExplanation,
		String detailedExplanation) {

	/** 回答後に画面へ表示する選択肢です。回答前の問題APIには含めません。 */
	public record ChoiceResult(
			Long choiceId,
			String choiceText,
			int choiceOrder,
			boolean selected,
			boolean correct) {
	}
}
