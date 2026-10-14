package com.example.graduation_java_game;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

	public GameSessionService(
			ChapterRepository chapterRepository,
			QuestionRepository questionRepository,
			ChoiceRepository choiceRepository,
			StageDefinitionRepository stageDefinitionRepository,
			GameSessionRepository gameSessionRepository,
			StageSessionRepository stageSessionRepository,
			StageQuestionRepository stageQuestionRepository,
			AnswerHistoryRepository answerHistoryRepository) {
		this.chapterRepository = chapterRepository;
		this.questionRepository = questionRepository;
		this.choiceRepository = choiceRepository;
		this.stageDefinitionRepository = stageDefinitionRepository;
		this.gameSessionRepository = gameSessionRepository;
		this.stageSessionRepository = stageSessionRepository;
		this.stageQuestionRepository = stageQuestionRepository;
		this.answerHistoryRepository = answerHistoryRepository;
	}

	/**
	 * 入力内容と必要な問題・Stage設定を確認したうえでゲームを開始します。
	 * 途中で保存に失敗した場合に一部の記録だけが残らないよう、全処理を1つの取引にします。
	 */
	@Transactional
	public GameSessionCreateResponse create(GameSessionCreateRequest request) {
		validateRequest(request);
		QuestionSelectionMode selectionMode = request.selectionMode() == null
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

		List<Question> availableQuestions = findQuestionsForMode(chapter.getChapterId(), selectionMode);
		if (availableQuestions.size() < questionCount) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INSUFFICIENT_QUESTIONS",
					"選択した難易度に必要な問題がありません。問題数または難易度を変更してください。");
		}
		List<Question> chapterQuestions = prioritizeQuestions(
				chapter.getChapterId(), availableQuestions, Set.of());

		Instant startedAt = Instant.now();
		GameSession gameSession = gameSessionRepository.save(new GameSession(
				chapter,
				questionCount,
				selectionMode,
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
				GameSessionStatus.PLAYING);
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

		Set<Long> usedQuestionIds = stageQuestionRepository
				.findByStageSession_GameSession_GameSessionId(gameSessionId)
				.stream()
				.map(stageQuestion -> stageQuestion.getQuestion().getQuestionId())
				.collect(Collectors.toSet());
		List<Question> availableQuestions = findQuestionsForMode(
				gameSession.getChapter().getChapterId(),
				gameSession.getSelectionMode());
		List<Question> remainingQuestions = prioritizeQuestions(
				gameSession.getChapter().getChapterId(),
				availableQuestions,
				usedQuestionIds);
		if (remainingQuestions.size() < QUESTIONS_PER_STAGE) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INSUFFICIENT_QUESTIONS",
					"次のStageで使える未出題問題が5問ありません。");
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
				stageSession.getStageDefinition().getMiniGameType());
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

		List<Question> unanswered = new ArrayList<>();
		List<Question> review = new ArrayList<>();
		List<Question> cleared = new ArrayList<>();
		for (Question question : questions) {
			if (excludedQuestionIds.contains(question.getQuestionId())) {
				continue;
			}
			AnswerHistory latestHistory = latestHistoryByQuestion.get(question.getQuestionId());
			if (latestHistory == null) {
				unanswered.add(question);
			} else if (!latestHistory.isCorrect() || latestHistory.isTimeout()) {
				review.add(question);
			} else {
				cleared.add(question);
			}
		}

		Collections.shuffle(unanswered);
		Collections.shuffle(review);
		Collections.shuffle(cleared);
		List<Question> prioritized = new ArrayList<>(unanswered.size() + review.size() + cleared.size());
		prioritized.addAll(unanswered);
		prioritized.addAll(review);
		prioritized.addAll(cleared);
		return prioritized;
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
