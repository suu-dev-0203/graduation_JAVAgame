package com.example.graduation_java_game;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ゲーム開始時に必要なデータを確認し、最初のGameSessionとStageSessionを作ります。
 */
@Service
public class GameSessionService {

	private static final int QUESTIONS_PER_STAGE = 5;
	private static final List<Integer> ALLOWED_QUESTION_COUNTS = List.of(5, 10, 20);

	private final ChapterRepository chapterRepository;
	private final QuestionRepository questionRepository;
	private final ChoiceRepository choiceRepository;
	private final StageDefinitionRepository stageDefinitionRepository;
	private final GameSessionRepository gameSessionRepository;
	private final StageSessionRepository stageSessionRepository;
	private final StageQuestionRepository stageQuestionRepository;

	public GameSessionService(
			ChapterRepository chapterRepository,
			QuestionRepository questionRepository,
			ChoiceRepository choiceRepository,
			StageDefinitionRepository stageDefinitionRepository,
			GameSessionRepository gameSessionRepository,
			StageSessionRepository stageSessionRepository,
			StageQuestionRepository stageQuestionRepository) {
		this.chapterRepository = chapterRepository;
		this.questionRepository = questionRepository;
		this.choiceRepository = choiceRepository;
		this.stageDefinitionRepository = stageDefinitionRepository;
		this.gameSessionRepository = gameSessionRepository;
		this.stageSessionRepository = stageSessionRepository;
		this.stageQuestionRepository = stageQuestionRepository;
	}

	/**
	 * 入力内容と必要な問題・Stage設定を確認したうえでゲームを開始します。
	 * 途中で保存に失敗した場合に一部の記録だけが残らないよう、全処理を1つの取引にします。
	 */
	@Transactional
	public GameSessionCreateResponse create(GameSessionCreateRequest request) {
		validateRequest(request);

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

		List<Question> chapterQuestions = questionRepository.findByChapter_ChapterId(chapter.getChapterId());
		if (chapterQuestions.size() < questionCount) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INSUFFICIENT_QUESTIONS",
					"選択した問題数を遊ぶための問題がChapterに足りません。");
		}

		Instant startedAt = Instant.now();
		GameSession gameSession = gameSessionRepository.save(new GameSession(
				chapter,
				questionCount,
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
		Collections.shuffle(chapterQuestions);
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
					"questionCountは5、10、20のいずれかを指定してください。");
		}
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
