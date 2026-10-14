package com.example.graduation_java_game;

import java.time.Instant;
import java.util.List;

import com.example.graduation_java_game.AnswerDtos.AnswerResponse;

/** Chapter・GameSession・Stage・Review APIで受け渡すデータ型をまとめます。 */
public final class GameApiDtos {

	private GameApiDtos() {
		// データ型をまとめるクラスなので、インスタンスは作りません。
	}

	/** Chapter一覧APIでブラウザーに返すデータです。 */
	public record ChapterResponse(Long chapterId, String name, String description) {
	}

	/** ゲーム開始APIでブラウザーから受け取る値です。 */
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

	/** ゲーム開始APIが成功したときにブラウザーへ返す値です。 */
	public record GameSessionCreateResponse(
			Long gameSessionId,
			Long chapterId,
			int questionCount,
			int totalStageCount,
			int currentStageNumber,
			GameSessionStatus status,
			GameSessionType sessionType) {
	}

	/** 次のStageを作成した結果です。 */
	public record AdvanceStageResponse(
			Long gameSessionId,
			Long stageSessionId,
			int currentStageNumber,
			int totalStageCount,
			GameSessionStatus gameSessionStatus) {
	}

	/** ゲーム画面を表示・復元するために必要な現在状態です。 */
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
			MiniGameType miniGameType,
			AnswerResponse answerResult) {

		/** 画面に表示する問題です。正解情報は含めません。 */
		public record QuestionResponse(
				Long questionId,
				QuestionType questionType,
				String questionText,
				int timeLimitSeconds,
				String shortExplanation,
				String codeBlock,
				List<ChoiceResponse> choices) {
		}

		/** 画面に表示する選択肢です。正解情報は含めません。 */
		public record ChoiceResponse(Long choiceId, String choiceText, int choiceOrder) {
		}
	}

	/** ゲーム全体を完了したときに表示する結果です。 */
	public record GameResultResponse(
			Long gameSessionId,
			int questionCount,
			int totalStageCount,
			int completedStageCount,
			int totalCorrectCount,
			int totalScore,
			int perfectStageCount,
			GameSessionStatus status,
			GameSessionType sessionType) {
	}

	/** ゲームの状態変更結果です。 */
	public record GameSessionStatusResponse(GameSessionStatus status) {
	}

	/** Stageをクリアしたときに表示する結果です。 */
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

	/** 問題の開始APIが成功したときに返す情報です。 */
	public record StartStageQuestionResponse(
			Long stageQuestionId,
			Instant startedAt,
			long remainingTimeMs) {
	}

	/** 完了したゲームの回答を、復習画面に表示する形式です。 */
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

	/** APIエラーのコードと説明をブラウザーへ返します。 */
	public record ErrorResponse(String code, String message) {
	}
}
