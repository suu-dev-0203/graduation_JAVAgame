package com.example.graduation_java_game;

import java.util.List;

/** 回答APIが受け取るデータと返すデータをまとめます。 */
public final class AnswerDtos {

	private AnswerDtos() {
		// データ型をまとめるクラスなので、インスタンスは作りません。
	}

	/** ブラウザーから受け取る1問分の回答です。 */
	public record AnswerRequest(Long stageQuestionId, List<Long> selectedChoiceIds) {
	}

	/** 回答判定の結果と、画面に反映するゲーム状態です。 */
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

		/** 回答後に画面へ表示する選択肢です。 */
		public record ChoiceResult(
				Long choiceId,
				String choiceText,
				int choiceOrder,
				boolean selected,
				boolean correct) {
		}
	}
}
