package com.example.graduation_java_game;

/**
 * ゲーム開始APIが成功したときにブラウザへ返す値です。
 *
 * @param gameSessionId 作成されたゲーム全体のID
 * @param chapterId 選択されたChapterのID
 * @param questionCount 選択された問題数
 * @param totalStageCount このゲームで進むStageの合計数
 * @param currentStageNumber 最初に開始するStage番号
 * @param status ゲーム全体の状態
 * @param sessionType 通常ゲームか苦手特訓か
 */
public record GameSessionCreateResponse(
		Long gameSessionId,
		Long chapterId,
		int questionCount,
		int totalStageCount,
		int currentStageNumber,
		GameSessionStatus status,
		GameSessionType sessionType) {
}
