package com.example.graduation_java_game;

import java.util.List;

/**
 * 完了したゲームの回答を、復習画面に表示する形式です。
 */
public record ReviewResponse(List<Item> items) {

	public record Item(
			Long stageQuestionId,
			Long questionId,
			int stageNumber,
			int questionNumber,
			String questionText,
			MasteryStatus masteryStatus,
			List<Long> selectedChoiceIds,
			List<Long> correctChoiceIds,
			boolean correct,
			boolean timeout,
			long answerTimeMs,
			String detailedExplanation,
			List<ChoiceItem> choices) {
	}

	public record ChoiceItem(
			Long choiceId,
			String choiceText,
			int choiceOrder,
			boolean selected,
			boolean correct) {
	}
}
