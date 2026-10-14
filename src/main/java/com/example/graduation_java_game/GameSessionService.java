package com.example.graduation_java_game;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ゲーム開始時に必要なデータを確認し、最初のGameSessionとStageSessionを作ります。
 */
@Service
public class GameSessionService {

	private static final int QUESTIONS_PER_STAGE = 5;
	private static final List<Integer> ALLOWED_QUESTION_COUNTS = List.of(5, 10, 15, 20);

	private final ChapterRepository chapterRepository;
	private final QuestionRepository questionRepository;
	private final ChoiceRepository choiceRepository;
	private final StageDefinitionRepository stageDefinitionRepository;
	private final GameSessionRepository gameSessionRepository;
	private final StageSessionRepository stageSessionRepository;
	private final StageQuestionRepository stageQuestionRepository;
	private final AnswerHistoryRepository answerHistoryRepository;
	private final AnswerHistoryChoiceRepository answerHistoryChoiceRepository;
	private final QuestionMasteryRepository questionMasteryRepository;
	private final LearningProgressService learningProgressService;

	public GameSessionService(
			ChapterRepository chapterRepository,
			QuestionRepository questionRepository,
			ChoiceRepository choiceRepository,
			StageDefinitionRepository stageDefinitionRepository,
			GameSessionRepository gameSessionRepository,
			StageSessionRepository stageSessionRepository,
			StageQuestionRepository stageQuestionRepository,
			AnswerHistoryRepository answerHistoryRepository,
			AnswerHistoryChoiceRepository answerHistoryChoiceRepository,
			QuestionMasteryRepository questionMasteryRepository,
			LearningProgressService learningProgressService) {
		this.chapterRepository = chapterRepository;
		this.questionRepository = questionRepository;
		this.choiceRepository = choiceRepository;
		this.stageDefinitionRepository = stageDefinitionRepository;
		this.gameSessionRepository = gameSessionRepository;
		this.stageSessionRepository = stageSessionRepository;
		this.stageQuestionRepository = stageQuestionRepository;
		this.answerHistoryRepository = answerHistoryRepository;
		this.answerHistoryChoiceRepository = answerHistoryChoiceRepository;
		this.questionMasteryRepository = questionMasteryRepository;
		this.learningProgressService = learningProgressService;
	}

	/**
	 * 入力内容と必要な問題・Stage設定を確認したうえでゲームを開始します。
	 * 途中で保存に失敗した場合に一部の記録だけが残らないよう、全処理を1つの取引にします。
	 */
	@Transactional
	public GameSessionCreateResponse create(GameSessionCreateRequest request) {
		validateRequest(request);
		GameSessionType sessionType = request.sessionType() == null
				? GameSessionType.STANDARD
				: request.sessionType();
		QuestionSelectionMode selectionMode = sessionType == GameSessionType.WEAK_TRAINING
				? QuestionSelectionMode.RANDOM
				: request.selectionMode() == null
				? QuestionSelectionMode.RANDOM
				: request.selectionMode();

		Chapter chapter = chapterRepository.findById(request.chapterId())
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたChapterが見つかりません。"));

		int questionCount = request.questionCount();
		int totalStageCount = questionCount / QUESTIONS_PER_STAGE;
		List<StageDefinition> stageDefinitions = findRequiredStageDefinitions(
				chapter.getChapterId(), totalStageCount);
		if (stageDefinitions.stream().anyMatch(stage -> stage.getMaxEnemyHp() != 5)) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"MVPのStage設定では敵HPを5にしてください。");
		}

		List<Question> chapterQuestions;
		if (sessionType == GameSessionType.WEAK_TRAINING) {
			List<Question> weakQuestions = learningProgressService
					.findWeakQuestionsForTraining(chapter.getChapterId());
			if (weakQuestions.isEmpty()) {
				throw new GameApiException(
						HttpStatus.CONFLICT,
						"NO_WEAK_QUESTIONS",
						"この学習テーマには苦手・超苦手に分類された問題がありません。");
			}
			chapterQuestions = selectWeakTrainingQuestionsForStage(weakQuestions, List.of());
		} else {
			List<Question> availableQuestions = findQuestionsForMode(chapter.getChapterId(), selectionMode);
			if (availableQuestions.size() < questionCount) {
				throw new GameApiException(
						HttpStatus.CONFLICT,
						"INSUFFICIENT_QUESTIONS",
						"選択した難易度に必要な問題がありません。問題数または難易度を変更してください。");
			}
			chapterQuestions = prioritizeQuestions(
					chapter.getChapterId(), availableQuestions, Set.of());
		}

		Instant startedAt = Instant.now();
		GameSession gameSession = gameSessionRepository.save(new GameSession(
				chapter,
				questionCount,
				selectionMode,
				sessionType,
				totalStageCount,
				1,
				0,
				GameSessionStatus.PLAYING,
				startedAt,
				null));

		StageDefinition firstStage = stageDefinitions.get(0);
		StageSession stageSession = stageSessionRepository.save(new StageSession(
				gameSession,
				firstStage,
				1,
				0,
				firstStage.getMaxEnemyHp(),
				0,
				StageSessionStatus.PLAYING,
				startedAt,
				null,
				null,
				0));

		// 最初のStageで使う5問だけを選び、後のStageの問題はまだ作りません。
		List<StageQuestion> stageQuestions = new ArrayList<>(QUESTIONS_PER_STAGE);
		for (int index = 0; index < QUESTIONS_PER_STAGE; index++) {
			stageQuestions.add(new StageQuestion(stageSession, chapterQuestions.get(index), index + 1));
		}
		stageQuestionRepository.saveAll(stageQuestions);

		return new GameSessionCreateResponse(
				gameSession.getGameSessionId(),
				chapter.getChapterId(),
				questionCount,
				totalStageCount,
				1,
				GameSessionStatus.PLAYING,
				sessionType);
	}

	/**
	 * Stage Clear済みのゲームに、同じ難易度の未使用問題で次Stageを作成します。
	 */
	@Transactional
	public AdvanceStageResponse advanceStage(Long gameSessionId) {
		GameSession gameSession = gameSessionRepository.findById(gameSessionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたGameSessionが見つかりません。"));
		if (gameSession.getStatus() != GameSessionStatus.PLAYING
				|| gameSession.getCurrentStageNumber() >= gameSession.getTotalStageCount()) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"次のStageが残っているプレイ中のゲームだけ進行できます。");
		}

		List<StageSession> stageSessions = stageSessionRepository
				.findByGameSession_GameSessionId(gameSessionId);
		StageSession currentStage = stageSessions.stream()
				.filter(stage -> stage.getStageDefinition().getStageNumber()
						== gameSession.getCurrentStageNumber())
				.findFirst()
				.orElseThrow(() -> new GameApiException(
						HttpStatus.CONFLICT,
						"INVALID_GAME_STATE",
						"現在のStage記録が見つかりません。"));
		if (currentStage.getStatus() != StageSessionStatus.CLEARED) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"現在のStageをクリアしてから次へ進んでください。");
		}

		int nextStageNumber = gameSession.getCurrentStageNumber() + 1;
		StageDefinition nextStageDefinition = stageDefinitionRepository
				.findByChapter_ChapterIdAndStageNumber(
						gameSession.getChapter().getChapterId(), nextStageNumber)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.CONFLICT,
						"INVALID_GAME_STATE",
						"次のStage設定が見つかりません。"));
		if (stageSessionRepository.existsByGameSession_GameSessionIdAndStageDefinition_StageId(
				gameSessionId, nextStageDefinition.getStageId())) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"次のStageはすでに作成されています。");
		}

		List<StageQuestion> allSessionQuestions = stageQuestionRepository
				.findByStageSession_GameSession_GameSessionId(gameSessionId);
		Set<Long> usedQuestionIds = allSessionQuestions.stream()
				.map(stageQuestion -> stageQuestion.getQuestion().getQuestionId())
				.collect(Collectors.toSet());
		List<Question> remainingQuestions;
		if (gameSession.getSessionType() == GameSessionType.WEAK_TRAINING) {
			List<Question> unplayedWeakQuestions = learningProgressService
					.findWeakQuestionsForTraining(gameSession.getChapter().getChapterId())
					.stream()
					.filter(question -> !usedQuestionIds.contains(question.getQuestionId()))
					.toList();
			remainingQuestions = selectWeakTrainingQuestionsForStage(
					unplayedWeakQuestions,
					findDistinctQuestions(allSessionQuestions));
		} else {
			List<Question> availableQuestions = findQuestionsForMode(
					gameSession.getChapter().getChapterId(),
					gameSession.getSelectionMode());
			remainingQuestions = prioritizeQuestions(
					gameSession.getChapter().getChapterId(),
					availableQuestions,
					usedQuestionIds);
			if (remainingQuestions.size() < QUESTIONS_PER_STAGE) {
				throw new GameApiException(
						HttpStatus.CONFLICT,
						"INSUFFICIENT_QUESTIONS",
						"次のStageで使える未出題問題が5問ありません。");
			}
		}

		Instant startedAt = Instant.now();
		StageSession nextStage = stageSessionRepository.save(new StageSession(
				gameSession,
				nextStageDefinition,
				1,
				0,
				nextStageDefinition.getMaxEnemyHp(),
				0,
				StageSessionStatus.PLAYING,
				startedAt,
				null,
				null,
				0));
		List<StageQuestion> nextQuestions = new ArrayList<>(QUESTIONS_PER_STAGE);
		for (int index = 0; index < QUESTIONS_PER_STAGE; index++) {
			nextQuestions.add(new StageQuestion(
					nextStage, remainingQuestions.get(index), index + 1));
		}
		stageQuestionRepository.saveAll(nextQuestions);
		gameSession.advanceToStage(nextStageNumber);
		gameSessionRepository.save(gameSession);

		return new AdvanceStageResponse(
				gameSessionId,
				nextStage.getStageSessionId(),
				nextStageNumber,
				gameSession.getTotalStageCount(),
				gameSession.getStatus());
	}

	@Transactional
	public CurrentGameResponse pause(Long stageSessionId) {
		StageSession stageSession = stageSessionRepository.findById(stageSessionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたStageSessionが見つかりません。"));
		GameSession gameSession = stageSession.getGameSession();
		StageQuestion currentQuestion = stageQuestionRepository
				.findByStageSession_StageSessionIdAndQuestionOrder(
						stageSessionId, stageSession.getCurrentQuestionIndex())
				.orElseThrow(() -> new GameApiException(
						HttpStatus.CONFLICT,
						"INVALID_GAME_STATE",
						"現在の問題が見つかりません。"));
		if (gameSession.getStatus() != GameSessionStatus.PLAYING
				|| stageSession.getStatus() != StageSessionStatus.PLAYING
				|| currentQuestion.getStartedAt() == null
				|| currentQuestion.getAnsweredAt() != null
				|| currentQuestion.getPausedAt() != null) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"回答中の問題だけ一時停止できます。");
		}

		Instant pausedAt = Instant.now();
		gameSession.pause();
		stageSession.pause(pausedAt);
		currentQuestion.pause(pausedAt);
		return findCurrent(gameSession.getGameSessionId());
	}

	@Transactional
	public CurrentGameResponse resume(Long stageSessionId) {
		StageSession stageSession = stageSessionRepository.findById(stageSessionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたStageSessionが見つかりません。"));
		GameSession gameSession = stageSession.getGameSession();
		StageQuestion currentQuestion = stageQuestionRepository
				.findByStageSession_StageSessionIdAndQuestionOrder(
						stageSessionId, stageSession.getCurrentQuestionIndex())
				.orElseThrow(() -> new GameApiException(
						HttpStatus.CONFLICT,
						"INVALID_GAME_STATE",
						"現在の問題が見つかりません。"));
		if (gameSession.getStatus() != GameSessionStatus.PAUSED
				|| stageSession.getStatus() != StageSessionStatus.PAUSED
				|| stageSession.getPausedAt() == null
				|| !stageSession.getPausedAt().equals(currentQuestion.getPausedAt())) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"Pause中のゲームと問題だけ再開できます。");
		}

		Instant resumedAt = Instant.now();
		gameSession.resume();
		stageSession.resume(resumedAt);
		currentQuestion.resume(resumedAt);
		return findCurrent(gameSession.getGameSessionId());
	}

	@Transactional
	public GameSessionStatusResponse abandon(Long gameSessionId) {
		GameSession gameSession = gameSessionRepository.findById(gameSessionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたGameSessionが見つかりません。"));
		StageSession stageSession = stageSessionRepository
				.findByGameSession_GameSessionId(gameSessionId)
				.stream()
				.filter(candidate -> candidate.getStageDefinition().getStageNumber()
						== gameSession.getCurrentStageNumber())
				.findFirst()
				.orElseThrow(() -> new GameApiException(
						HttpStatus.CONFLICT,
						"INVALID_GAME_STATE",
						"現在のStage進行記録が見つかりません。"));
		if ((gameSession.getStatus() != GameSessionStatus.PLAYING
						&& gameSession.getStatus() != GameSessionStatus.PAUSED)
				|| (stageSession.getStatus() != StageSessionStatus.PLAYING
						&& stageSession.getStatus() != StageSessionStatus.PAUSED)
				|| (gameSession.getStatus() == GameSessionStatus.PAUSED)
						!= (stageSession.getStatus() == StageSessionStatus.PAUSED)) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"進行中または一時停止中のゲームだけ終了できます。");
		}

		Instant abandonedAt = Instant.now();
		gameSession.abandon(abandonedAt);
		stageSession.abandon(abandonedAt);
		return new GameSessionStatusResponse(gameSession.getStatus());
	}

	/**
	 * ゲームIDから現在のStageと問題を探し、画面向けのデータにして返します。
	 * Choice.correctは回答の正誤を決める情報なので、画面用DTOへ移しません。
	 */
	@Transactional(readOnly = true)
	public CurrentGameResponse findCurrent(Long gameSessionId) {
		GameSession gameSession = gameSessionRepository.findById(gameSessionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたGameSessionが見つかりません。"));

		StageSession stageSession = stageSessionRepository
				.findByGameSession_GameSessionId(gameSessionId)
				.stream()
				.filter(candidate -> candidate.getStageDefinition().getStageNumber()
						== gameSession.getCurrentStageNumber())
				.findFirst()
				.orElseThrow(() -> new GameApiException(
						HttpStatus.CONFLICT,
						"INVALID_GAME_STATE",
						"現在のStage進行記録が見つかりません。"));

		StageQuestion stageQuestion = stageQuestionRepository
				.findByStageSession_StageSessionIdAndQuestionOrder(
						stageSession.getStageSessionId(),
						stageSession.getCurrentQuestionIndex())
				.orElseThrow(() -> new GameApiException(
						HttpStatus.CONFLICT,
						"INVALID_GAME_STATE",
						"現在の問題が見つかりません。"));

		Question question = stageQuestion.getQuestion();
		AnswerResponse answerResult = stageQuestion.getAnsweredAt() == null
				? null
				: findAnswerResult(stageQuestion, stageSession, gameSession);
		List<CurrentGameResponse.ChoiceResponse> choices = choiceRepository
				.findByQuestion_QuestionIdOrderByChoiceOrder(question.getQuestionId())
				.stream()
				.map(choice -> new CurrentGameResponse.ChoiceResponse(
						choice.getChoiceId(),
						choice.getChoiceText(),
						choice.getChoiceOrder()))
				.toList();

		String questionState = stageQuestion.getAnsweredAt() != null
				? "RESULT"
				: stageQuestion.getStartedAt() == null ? "WAITING_TO_START" : "ANSWERING";
		Long remainingTimeMs = calculateRemainingTimeMs(stageQuestion, question);

		return new CurrentGameResponse(
				gameSession.getGameSessionId(),
				stageSession.getStageSessionId(),
				gameSession.getCurrentStageNumber(),
				gameSession.getTotalStageCount(),
				question.getDifficulty(),
				stageQuestion.getStageQuestionId(),
				questionState,
				stageQuestion.getQuestionOrder(),
				QUESTIONS_PER_STAGE,
				remainingTimeMs,
				stageSession.getEnemyHp(),
				stageSession.getCombo(),
				gameSession.getScore(),
				gameSession.getStatus(),
				stageSession.getStatus(),
				new CurrentGameResponse.QuestionResponse(
						question.getQuestionId(),
						question.getQuestionType(),
						question.getQuestionText(),
						question.getTimeLimitSeconds(),
						question.getShortExplanation(),
						question.getCodeBlock(),
						choices),
				stageSession.getStageDefinition().getPlayerCharacterKey(),
				stageSession.getStageDefinition().getEnemyKey(),
				stageSession.getStageDefinition().getFieldKey(),
				stageSession.getStageDefinition().getMiniGameType(),
				answerResult);
	}

	private AnswerResponse findAnswerResult(
			StageQuestion stageQuestion,
			StageSession stageSession,
			GameSession gameSession) {
		AnswerHistory history = answerHistoryRepository
				.findByStageQuestion_StageQuestionId(stageQuestion.getStageQuestionId())
				.orElseThrow(() -> new GameApiException(
						HttpStatus.CONFLICT,
						"INVALID_GAME_STATE",
						"回答済み問題の履歴が見つかりません。"));
		int maxCombo = 0;
		int currentCombo = 0;
		for (AnswerHistory stageHistory : answerHistoryRepository
				.findByStageSession_StageSessionIdOrderByStageQuestion_QuestionOrder(
						stageSession.getStageSessionId())) {
			currentCombo = stageHistory.isCorrect() ? currentCombo + 1 : 0;
			maxCombo = Math.max(maxCombo, currentCombo);
		}
		boolean stageClear = stageSession.getStatus() == StageSessionStatus.CLEARED;
		boolean perfect = stageClear && stageSession.getCorrectCount() == 5;
		String effectType = perfect
				? "PERFECT"
				: history.isTimeout() ? "TIME_UP" : history.isCorrect() ? "HIT" : "MISS";
		Set<Long> selectedChoiceIds = answerHistoryChoiceRepository
				.findByAnswerHistory_AnswerHistoryId(history.getAnswerHistoryId())
				.stream()
				.map(answerHistoryChoice -> answerHistoryChoice.getChoice().getChoiceId())
				.collect(Collectors.toSet());
		Question question = stageQuestion.getQuestion();
		List<AnswerResponse.ChoiceResult> choiceResults = choiceRepository
				.findByQuestion_QuestionIdOrderByChoiceOrder(question.getQuestionId())
				.stream()
				.map(choice -> new AnswerResponse.ChoiceResult(
						choice.getChoiceId(),
						choice.getChoiceText(),
						choice.getChoiceOrder(),
						selectedChoiceIds.contains(choice.getChoiceId()),
						choice.isCorrect()))
				.toList();
		Long nextStageQuestionId = stageClear
				? null
				: stageQuestionRepository
						.findByStageSession_StageSessionIdAndQuestionOrder(
								stageSession.getStageSessionId(),
								stageQuestion.getQuestionOrder() + 1)
						.map(StageQuestion::getStageQuestionId)
						.orElse(null);
		return new AnswerResponse(
				history.isCorrect(),
				history.isTimeout(),
				stageSession.getEnemyHp(),
				stageSession.getCombo(),
				gameSession.getScore(),
				effectType,
				stageSession.getCorrectCount(),
				stageSession.getCorrectCount() * 100,
				maxCombo,
				stageClear,
				perfect,
				nextStageQuestionId,
				choiceResults,
				question.getShortExplanation(),
				question.getDetailedExplanation());
	}

	/**
	 * クリア済みStageの回答履歴から正解数、得点、最大コンボを集計します。
	 */
	@Transactional(readOnly = true)
	public StageResultResponse findStageResult(Long gameSessionId, int stageNumber) {
		GameSession gameSession = gameSessionRepository.findById(gameSessionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたGameSessionが見つかりません。"));
		if (stageNumber < 1 || stageNumber > gameSession.getTotalStageCount()) {
			throw new GameApiException(
					HttpStatus.NOT_FOUND,
					"NOT_FOUND",
					"指定されたStageが見つかりません。");
		}

		StageSession stageSession = stageSessionRepository
				.findByGameSession_GameSessionId(gameSessionId)
				.stream()
				.filter(candidate -> candidate.getStageDefinition().getStageNumber() == stageNumber)
				.findFirst()
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたStageが見つかりません。"));
		if (stageSession.getStatus() != StageSessionStatus.CLEARED) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"Stageをクリアしたあとに結果を確認できます。");
		}

		List<AnswerHistory> histories = answerHistoryRepository
				.findByStageSession_StageSessionIdOrderByStageQuestion_QuestionOrder(
						stageSession.getStageSessionId());
		int currentCombo = 0;
		int maxCombo = 0;
		for (AnswerHistory history : histories) {
			currentCombo = history.isCorrect() ? currentCombo + 1 : 0;
			maxCombo = Math.max(maxCombo, currentCombo);
		}

		return new StageResultResponse(
				gameSessionId,
				stageSession.getStageSessionId(),
				stageNumber,
				stageSession.getCorrectCount(),
				stageSession.getCorrectCount() * 100,
				maxCombo,
				stageSession.getCorrectCount() == 5,
				stageNumber < gameSession.getTotalStageCount(),
				gameSession.getStatus() == GameSessionStatus.COMPLETED);
	}

	/**
	 * 完了済みのゲームから、全Stageの結果をまとめます。
	 */
	@Transactional(readOnly = true)
	public GameResultResponse findGameResult(Long gameSessionId) {
		GameSession gameSession = gameSessionRepository.findById(gameSessionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたGameSessionが見つかりません。"));
		if (gameSession.getStatus() != GameSessionStatus.COMPLETED) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"ゲーム全体を完了したあとに結果を確認できます。");
		}

		List<StageSession> stageSessions = stageSessionRepository
				.findByGameSession_GameSessionId(gameSessionId);
		int totalCorrectCount = stageSessions.stream()
				.mapToInt(StageSession::getCorrectCount)
				.sum();
		int perfectStageCount = (int) stageSessions.stream()
				.filter(stage -> stage.getStatus() == StageSessionStatus.CLEARED
						&& stage.getCorrectCount() == 5)
				.count();

		return new GameResultResponse(
				gameSessionId,
				gameSession.getQuestionCount(),
				gameSession.getTotalStageCount(),
				(int) stageSessions.stream()
						.filter(stage -> stage.getStatus() == StageSessionStatus.CLEARED)
						.count(),
				totalCorrectCount,
				gameSession.getScore(),
				perfectStageCount,
				gameSession.getStatus(),
				gameSession.getSessionType());
	}

	/**
	 * 完了済みゲームの回答と正解を、出題順に並べて返します。
	 */
	@Transactional(readOnly = true)
	public ReviewResponse findReview(Long gameSessionId) {
		GameSession gameSession = gameSessionRepository.findById(gameSessionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたGameSessionが見つかりません。"));
		if (gameSession.getStatus() != GameSessionStatus.COMPLETED) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"ゲーム全体を完了したあとに解答履歴を確認できます。");
		}

		List<AnswerHistory> histories = answerHistoryRepository
				.findByGameSession_GameSessionId(gameSessionId);
		if (histories.size() != gameSession.getQuestionCount()) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"ゲームの回答履歴が不足しているため、復習結果を表示できません。");
		}
		List<ReviewResponse.Item> items = histories.stream()
				.sorted(java.util.Comparator
						.comparingInt((AnswerHistory history) -> history.getStageQuestion()
								.getStageSession().getStageDefinition().getStageNumber())
						.thenComparingInt(history -> history.getStageQuestion().getQuestionOrder()))
				.map(this::toReviewItem)
				.toList();
		return new ReviewResponse(items);
	}

	private ReviewResponse.Item toReviewItem(AnswerHistory history) {
		StageQuestion stageQuestion = history.getStageQuestion();
		Question question = stageQuestion.getQuestion();
		Set<Long> selectedChoiceIds = answerHistoryChoiceRepository
				.findByAnswerHistory_AnswerHistoryId(history.getAnswerHistoryId())
				.stream()
				.map(answerHistoryChoice -> answerHistoryChoice.getChoice().getChoiceId())
				.collect(Collectors.toSet());
		List<ReviewResponse.ChoiceItem> choices = choiceRepository
				.findByQuestion_QuestionIdOrderByChoiceOrder(question.getQuestionId())
				.stream()
				.map(choice -> new ReviewResponse.ChoiceItem(
						choice.getChoiceId(),
						choice.getChoiceText(),
						choice.getChoiceOrder(),
						selectedChoiceIds.contains(choice.getChoiceId()),
						choice.isCorrect()))
				.toList();
		List<Long> selectedIds = choices.stream()
				.filter(ReviewResponse.ChoiceItem::selected)
				.map(ReviewResponse.ChoiceItem::choiceId)
				.toList();
		List<Long> correctIds = choices.stream()
				.filter(ReviewResponse.ChoiceItem::correct)
				.map(ReviewResponse.ChoiceItem::choiceId)
				.toList();

		return new ReviewResponse.Item(
				stageQuestion.getStageQuestionId(),
				question.getQuestionId(),
				stageQuestion.getStageSession().getStageDefinition().getStageNumber(),
				stageQuestion.getQuestionOrder(),
				question.getQuestionText(),
				questionMasteryRepository.findByQuestion_QuestionId(question.getQuestionId())
						.map(QuestionMastery::getStatus)
						.orElse(history.isCorrect() ? MasteryStatus.OK : MasteryStatus.WEAK),
				selectedIds,
				correctIds,
				history.isCorrect(),
				history.isTimeout(),
				history.getAnswerTimeMs(),
				question.getDetailedExplanation(),
				choices);
	}

	/**
	 * 現在の問題を初めて開始し、サーバーの開始時刻と残り時間を返します。
	 */
	@Transactional
	public StartStageQuestionResponse startQuestion(Long stageQuestionId) {
		StageQuestion stageQuestion = stageQuestionRepository.findById(stageQuestionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたStageQuestionが見つかりません。"));

		StageSession stageSession = stageQuestion.getStageSession();
		GameSession gameSession = stageSession.getGameSession();
		if (gameSession.getStatus() != GameSessionStatus.PLAYING
				|| stageSession.getStatus() != StageSessionStatus.PLAYING) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"プレイ中のゲームとStageの問題だけ開始できます。");
		}

		Instant startedAt = Instant.now();
		if (!stageQuestion.start(startedAt)) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"この問題はすでに開始済み、または回答済みです。");
		}

		StageQuestion savedStageQuestion = stageQuestionRepository.save(stageQuestion);
		long remainingTimeMs = calculateRemainingTimeMs(
				savedStageQuestion,
				savedStageQuestion.getQuestion());

		return new StartStageQuestionResponse(
				savedStageQuestion.getStageQuestionId(),
				savedStageQuestion.getStartedAt(),
				remainingTimeMs);
	}

	/** 問題開始前はnullを返し、開始後はサーバーの時刻から残り時間を計算します。 */
	private Long calculateRemainingTimeMs(StageQuestion stageQuestion, Question question) {
		if (stageQuestion.getStartedAt() == null) {
			return null;
		}

		Instant effectiveNow = stageQuestion.getPausedAt() == null
				? Instant.now()
				: stageQuestion.getPausedAt();
		long elapsedMs = Math.max(
				0,
				Duration.between(stageQuestion.getStartedAt(), effectiveNow).toMillis()
						- stageQuestion.getTotalPausedMs());
		long timeLimitMs = question.getTimeLimitSeconds() * 1000L;
		return Math.max(0, timeLimitMs - elapsedMs);
	}

	/** 必須項目とREADMEで許可された問題数だけを受け付けます。 */
	private void validateRequest(GameSessionCreateRequest request) {
		if (request == null || request.chapterId() == null || request.questionCount() == null) {
			throw new GameApiException(
					HttpStatus.BAD_REQUEST,
					"INVALID_REQUEST",
					"chapterIdとquestionCountを指定してください。");
		}
		if (!ALLOWED_QUESTION_COUNTS.contains(request.questionCount())) {
			throw new GameApiException(
					HttpStatus.BAD_REQUEST,
					"INVALID_REQUEST",
					"questionCountは5、10、15、20のいずれかを指定してください。");
		}
	}

	private List<Question> findQuestionsForMode(Long chapterId, QuestionSelectionMode mode) {
		List<Question> questions = questionRepository.findByChapter_ChapterId(chapterId);
		QuestionDifficulty selectedDifficulty = mode.difficultyOrNull();
		return selectedDifficulty == null
				? questions
				: questions.stream()
						.filter(question -> question.getDifficulty() == selectedDifficulty)
						.toList();
	}

	private List<Question> prioritizeQuestions(
			Long chapterId,
			List<Question> questions,
			Set<Long> excludedQuestionIds) {
		Map<Long, AnswerHistory> latestHistoryByQuestion = new HashMap<>();
		for (AnswerHistory history : answerHistoryRepository
				.findByStageQuestion_Question_Chapter_ChapterId(chapterId)) {
			Long questionId = history.getStageQuestion().getQuestion().getQuestionId();
			AnswerHistory currentLatest = latestHistoryByQuestion.get(questionId);
			if (currentLatest == null || isLaterHistory(history, currentLatest)) {
				latestHistoryByQuestion.put(questionId, history);
			}
		}

		Map<Long, QuestionMastery> savedMastery = new HashMap<>();
		for (QuestionMastery mastery : questionMasteryRepository
				.findByQuestion_Chapter_ChapterId(chapterId)) {
			savedMastery.put(mastery.getQuestion().getQuestionId(), mastery);
		}

		List<Question> unanswered = new ArrayList<>();
		List<Question> veryWeak = new ArrayList<>();
		List<Question> weak = new ArrayList<>();
		List<Question> almost = new ArrayList<>();
		List<Question> cleared = new ArrayList<>();
		for (Question question : questions) {
			if (excludedQuestionIds.contains(question.getQuestionId())) {
				continue;
			}
			AnswerHistory latestHistory = latestHistoryByQuestion.get(question.getQuestionId());
			QuestionMastery mastery = savedMastery.get(question.getQuestionId());
			MasteryStatus status = mastery != null
					? mastery.getStatus()
					: latestHistory == null
						? MasteryStatus.UNSEEN
						: latestHistory.isCorrect() ? MasteryStatus.OK : MasteryStatus.WEAK;
			switch (status) {
				case UNSEEN -> unanswered.add(question);
				case VERY_WEAK -> veryWeak.add(question);
				case WEAK -> weak.add(question);
				case ALMOST -> almost.add(question);
				case OK -> cleared.add(question);
			}
		}

		Collections.shuffle(unanswered);
		Collections.shuffle(veryWeak);
		Collections.shuffle(weak);
		Collections.shuffle(almost);
		Collections.shuffle(cleared);
		List<Question> prioritized = new ArrayList<>(
				unanswered.size() + veryWeak.size() + weak.size() + almost.size() + cleared.size());
		prioritized.addAll(unanswered);
		prioritized.addAll(veryWeak);
		prioritized.addAll(weak);
		prioritized.addAll(almost);
		prioritized.addAll(cleared);
		return prioritized;
	}

	/** 未出題の苦手問題を優先し、5問に足りない分は今回の特訓で出した問題から補います。 */
	private List<Question> selectWeakTrainingQuestionsForStage(
			List<Question> unplayedWeakQuestions,
			List<Question> previouslySelectedQuestions) {
		List<Question> shuffledWeakQuestions = new ArrayList<>(unplayedWeakQuestions);
		Collections.shuffle(shuffledWeakQuestions);
		int firstSelectionCount = Math.min(QUESTIONS_PER_STAGE, shuffledWeakQuestions.size());
		List<Question> selectedQuestions = new ArrayList<>(
				shuffledWeakQuestions.subList(0, firstSelectionCount));
		if (selectedQuestions.size() == QUESTIONS_PER_STAGE) {
			return selectedQuestions;
		}

		List<Question> repeatCandidates = new ArrayList<>(previouslySelectedQuestions.isEmpty()
				? unplayedWeakQuestions
				: previouslySelectedQuestions);
		if (repeatCandidates.isEmpty()) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"NO_WEAK_QUESTIONS",
					"次のStageで使える苦手問題がありません。");
		}
		Collections.shuffle(repeatCandidates);
		int repeatIndex = 0;
		while (selectedQuestions.size() < QUESTIONS_PER_STAGE) {
			selectedQuestions.add(repeatCandidates.get(repeatIndex % repeatCandidates.size()));
			repeatIndex++;
		}
		return selectedQuestions;
	}

	private List<Question> findDistinctQuestions(List<StageQuestion> stageQuestions) {
		Set<Long> questionIds = new HashSet<>();
		List<Question> distinctQuestions = new ArrayList<>();
		for (StageQuestion stageQuestion : stageQuestions) {
			Question question = stageQuestion.getQuestion();
			if (questionIds.add(question.getQuestionId())) {
				distinctQuestions.add(question);
			}
		}
		return distinctQuestions;
	}

	private boolean isLaterHistory(AnswerHistory candidate, AnswerHistory current) {
		int answerTimeComparison = candidate.getStageQuestion().getAnsweredAt()
				.compareTo(current.getStageQuestion().getAnsweredAt());
		return answerTimeComparison > 0
				|| (answerTimeComparison == 0
						&& candidate.getAnswerHistoryId() > current.getAnswerHistoryId());
	}

	/** 選んだ問題数を最後まで遊べるだけのStage設定がChapterにあるか確認します。 */
	private List<StageDefinition> findRequiredStageDefinitions(Long chapterId, int totalStageCount) {
		List<StageDefinition> stageDefinitions = new ArrayList<>(totalStageCount);
		for (int stageNumber = 1; stageNumber <= totalStageCount; stageNumber++) {
			StageDefinition stageDefinition = stageDefinitionRepository
					.findByChapter_ChapterIdAndStageNumber(chapterId, stageNumber)
					.orElseThrow(() -> new GameApiException(
							HttpStatus.CONFLICT,
							"INVALID_GAME_STATE",
							"選択した問題数を遊ぶために必要なStage設定が不足しています。"));
			stageDefinitions.add(stageDefinition);
		}
		return stageDefinitions;
	}
}
