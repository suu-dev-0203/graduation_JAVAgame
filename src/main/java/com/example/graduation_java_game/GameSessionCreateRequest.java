package com.example.graduation_java_game;

/**
 * ゲーム開始APIでブラウザから受け取る値です。
 *
 * @param chapterId 遊びたい学習テーマのID
 * @param questionCount 遊ぶ問題数（5、10、15、20のいずれか）
 * @param selectionMode 難易度を選ぶか、全難易度からランダムに出題するか
 * @param sessionType 通常ゲームか苦手特訓か
 */
public record GameSessionCreateRequest(
		Long chapterId,
		Integer questionCount,
		QuestionSelectionMode selectionMode,
		GameSessionType sessionType) {

	public GameSessionCreateRequest(
			Long chapterId,
			Integer questionCount,
			QuestionSelectionMode selectionMode) {
		this(chapterId, questionCount, selectionMode, GameSessionType.STANDARD);
	}

	public GameSessionCreateRequest(Long chapterId, Integer questionCount) {
		this(chapterId, questionCount, QuestionSelectionMode.RANDOM, GameSessionType.STANDARD);
	}
}
