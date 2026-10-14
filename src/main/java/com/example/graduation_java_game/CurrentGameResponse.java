package com.example.graduation_java_game;

/**
 * ゲーム画面を表示・復元するために必要な現在状態です。
 *
 * @param gameSessionId ゲーム全体のID
 * @param stageSessionId 現在のStage進行記録のID
 * @param currentStageNumber 現在のStage番号
 * @param totalStageCount ゲーム全体のStage数
 * @param difficulty 現在の問題の難易度
 * @param stageQuestionId 現在出題する問題記録のID
 * @param questionState 現在の問題状態
 * @param questionNumber Stage内での問題番号
 * @param totalQuestionsInStage Stageの問題数
 * @param remainingTimeMs サーバーが計算した残り時間。開始前はnull
 * @param enemyHp 敵の残りHP
 * @param combo 現在の連続正解数
 * @param score ゲーム全体の得点
 * @param gameSessionStatus ゲーム全体の状態
 * @param stageSessionStatus 現在のStageの状態
 * @param question 画面に表示する問題と選択肢
 * @param playerCharacterKey プレイヤー画像を選ぶKey
 * @param enemyKey 敵画像を選ぶKey
 * @param fieldKey 背景画像を選ぶKey
 * @param miniGameType ミニゲームの種類
 */
public record CurrentGameResponse(
		Long gameSessionId,
		Long stageSessionId,
		int currentStageNumber,
		int totalStageCount,
		QuestionDifficulty difficulty,
		Long stageQuestionId,
		String questionState,
		int questionNumber,
		int totalQuestionsInStage,
		Long remainingTimeMs,
		int enemyHp,
		int combo,
		int score,
		GameSessionStatus gameSessionStatus,
		StageSessionStatus stageSessionStatus,
		QuestionResponse question,
		String playerCharacterKey,
		String enemyKey,
		String fieldKey,
		MiniGameType miniGameType) {

	/**
	 * 画面表示に必要な問題データです。正解情報は意図的に含めません。
	 */
	public record QuestionResponse(
			Long questionId,
			QuestionType questionType,
			String questionText,
			int timeLimitSeconds,
			String shortExplanation,
			String codeBlock,
			java.util.List<ChoiceResponse> choices) {
	}

	/**
	 * 画面に表示する選択肢です。正解かどうかはサーバー内に留めます。
	 */
	public record ChoiceResponse(Long choiceId, String choiceText, int choiceOrder) {
	}
}
