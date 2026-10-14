package com.example.graduation_java_game;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 回答の状態・時間・正誤を判定し、ゲーム進行と回答履歴を更新します。
 */
@Service
public class AnswerService {

	private static final int QUESTIONS_PER_STAGE = 5;
	private static final int POINTS_PER_CORRECT_ANSWER = 100;

	private final StageQuestionRepository stageQuestionRepository;
	private final ChoiceRepository choiceRepository;
	private final GameSessionRepository gameSessionRepository;
	private final AnswerHistoryRepository answerHistoryRepository;
	private final AnswerHistoryChoiceRepository answerHistoryChoiceRepository;
	private final LearningProgressService learningProgressService;

	public AnswerService(
			StageQuestionRepository stageQuestionRepository,
			ChoiceRepository choiceRepository,
			GameSessionRepository gameSessionRepository,
			AnswerHistoryRepository answerHistoryRepository,
			AnswerHistoryChoiceRepository answerHistoryChoiceRepository,
			LearningProgressService learningProgressService) {
		this.stageQuestionRepository = stageQuestionRepository;
		this.choiceRepository = choiceRepository;
		this.gameSessionRepository = gameSessionRepository;
		this.answerHistoryRepository = answerHistoryRepository;
		this.answerHistoryChoiceRepository = answerHistoryChoiceRepository;
		this.learningProgressService = learningProgressService;
	}

	/**
	 * 回答を検証し、問題の結果とゲーム状態を1つのトランザクションで記録します。
	 */
	@Transactional
	public AnswerResponse submit(AnswerRequest request) {
		if (request == null || request.stageQuestionId() == null
				|| request.selectedChoiceIds() == null) {
			throw new GameApiException(
					HttpStatus.BAD_REQUEST,
					"INVALID_REQUEST",
					"stageQuestionIdとselectedChoiceIdsを指定してください。");
		}

		StageQuestion stageQuestion = stageQuestionRepository.findById(request.stageQuestionId())
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定されたStageQuestionが見つかりません。"));
		StageSession stageSession = stageQuestion.getStageSession();
		GameSession gameSession = stageSession.getGameSession();

		if (answerHistoryRepository.existsByStageQuestion_StageQuestionId(
				stageQuestion.getStageQuestionId())
				|| stageQuestion.getAnsweredAt() != null) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"ALREADY_ANSWERED",
					"この問題はすでに回答されています。");
		}
		if (gameSession.getStatus() != GameSessionStatus.PLAYING
				|| stageSession.getStatus() != StageSessionStatus.PLAYING
				|| stageSession.getStageDefinition().getStageNumber()
						!= gameSession.getCurrentStageNumber()
				|| stageQuestion.getQuestionOrder() != stageSession.getCurrentQuestionIndex()) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"現在プレイ中の問題にだけ回答できます。");
		}
		if (stageQuestion.getStartedAt() == null || stageQuestion.getPausedAt() != null) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"INVALID_GAME_STATE",
					"開始済みで一時停止中でない問題にだけ回答できます。");
		}

		Instant answeredAt = Instant.now();
		Question question = stageQuestion.getQuestion();
		long remainingTimeMs = calculateRemainingTimeMs(stageQuestion, question, answeredAt);
		boolean timeout = remainingTimeMs <= 0;
		List<Choice> selectedChoices = timeout
				? List.of()
				: validateAndFindChoices(question, request.selectedChoiceIds());
		boolean correct = !timeout && hasExactCorrectSelection(question, selectedChoices);
		long answerTimeMs = Math.max(
				0,
				Duration.between(stageQuestion.getStartedAt(), answeredAt).toMillis()
						- stageQuestion.getTotalPausedMs());

		if (!stageQuestion.recordAnswer(answeredAt)) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"ALREADY_ANSWERED",
					"この問題はすでに回答されています。");
		}

		boolean stageClear = stageQuestion.getQuestionOrder() == QUESTIONS_PER_STAGE;
		boolean perfect = stageClear
				&& stageSession.getCorrectCount() + (correct ? 1 : 0) == QUESTIONS_PER_STAGE;
		stageSession.recordAnswer(correct, stageClear);
		if (correct) {
			gameSession.addScore(POINTS_PER_CORRECT_ANSWER);
		}
		if (stageClear && gameSession.getCurrentStageNumber() == gameSession.getTotalStageCount()) {
			gameSession.complete(answeredAt);
		}
		gameSessionRepository.save(gameSession);

		AnswerHistory answerHistory = answerHistoryRepository.save(new AnswerHistory(
				gameSession,
				stageSession,
				stageQuestion,
				correct,
				timeout,
				answerTimeMs));
		answerHistoryChoiceRepository.saveAll(selectedChoices.stream()
				.map(choice -> new AnswerHistoryChoice(answerHistory, choice))
				.toList());
		learningProgressService.recordAnswer(question, correct, answeredAt);

		int maxCombo = calculateMaxCombo(stageSession.getStageSessionId());
		int stageCorrectCount = stageSession.getCorrectCount();
		Long nextStageQuestionId = stageClear
				? null
				: stageQuestionRepository
						.findByStageSession_StageSessionIdAndQuestionOrder(
								stageSession.getStageSessionId(),
								stageQuestion.getQuestionOrder() + 1)
						.map(nextQuestion -> nextQuestion.getStageQuestionId())
						.orElseThrow(() -> new GameApiException(
								HttpStatus.CONFLICT,
								"INVALID_GAME_STATE",
								"次の問題が見つかりません。"));

		String effectType = perfect ? "PERFECT" : timeout ? "TIME_UP" : correct ? "HIT" : "MISS";
		Set<Long> selectedChoiceIds = selectedChoices.stream()
				.map(Choice::getChoiceId)
				.collect(java.util.stream.Collectors.toSet());
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
		return new AnswerResponse(
				correct,
				timeout,
				stageSession.getEnemyHp(),
				stageSession.getCombo(),
				gameSession.getScore(),
				effectType,
				stageCorrectCount,
				stageCorrectCount * POINTS_PER_CORRECT_ANSWER,
				maxCombo,
				stageClear,
				perfect,
				nextStageQuestionId,
				choiceResults,
				question.getShortExplanation(),
				question.getDetailedExplanation());
	}

	private List<Choice> validateAndFindChoices(Question question, List<Long> selectedChoiceIds) {
		int requiredCount = requiredChoiceCount(question.getQuestionType());
		Set<Long> requestedIds = new HashSet<>(selectedChoiceIds);
		if (selectedChoiceIds.size() != requiredCount || requestedIds.size() != requiredCount
				|| requestedIds.contains(null)) {
			throw new GameApiException(
					HttpStatus.BAD_REQUEST,
					"INVALID_ANSWER_COUNT",
					"この問題で選択できる選択肢の数と一致しません。");
		}

		List<Choice> choices = choiceRepository
				.findByQuestion_QuestionIdOrderByChoiceOrder(question.getQuestionId())
				.stream()
				.filter(choice -> requestedIds.contains(choice.getChoiceId()))
				.toList();
		int maximumChoiceOrder = maximumChoiceOrder(question.getQuestionType());
		if (choices.size() != requiredCount
				|| choices.stream().anyMatch(choice -> choice.getChoiceOrder() > maximumChoiceOrder)) {
			throw new GameApiException(
					HttpStatus.BAD_REQUEST,
					"INVALID_ANSWER",
					"選択肢がこの問題に属していないか、選択できる範囲外です。");
		}
		return choices;
	}

	private boolean hasExactCorrectSelection(Question question, List<Choice> selectedChoices) {
		Set<Long> selectedIds = selectedChoices.stream().map(choice -> choice.getChoiceId()).collect(
				java.util.stream.Collectors.toSet());
		Set<Long> correctIds = choiceRepository
				.findByQuestion_QuestionIdOrderByChoiceOrder(question.getQuestionId())
				.stream()
				.filter(choice -> choice.isCorrect())
				.map(choice -> choice.getChoiceId())
				.collect(java.util.stream.Collectors.toSet());
		return !correctIds.isEmpty() && selectedIds.equals(correctIds);
	}

	private int calculateMaxCombo(Long stageSessionId) {
		int maxCombo = 0;
		int currentCombo = 0;
		List<AnswerHistory> histories = answerHistoryRepository
				.findByStageSession_StageSessionIdOrderByStageQuestion_QuestionOrder(stageSessionId);
		for (AnswerHistory history : histories) {
			currentCombo = history.isCorrect() ? currentCombo + 1 : 0;
			maxCombo = Math.max(maxCombo, currentCombo);
		}
		return maxCombo;
	}

	private long calculateRemainingTimeMs(StageQuestion stageQuestion, Question question, Instant now) {
		Instant effectiveNow = stageQuestion.getPausedAt() == null ? now : stageQuestion.getPausedAt();
		long elapsedMs = Math.max(
				0,
				Duration.between(stageQuestion.getStartedAt(), effectiveNow).toMillis()
						- stageQuestion.getTotalPausedMs());
		return Math.max(0, question.getTimeLimitSeconds() * 1000L - elapsedMs);
	}

	private int requiredChoiceCount(QuestionType questionType) {
		return switch (questionType) {
			case SINGLE_A_E, SINGLE_A_G, CODE_SINGLE_A_E -> 1;
			case MULTIPLE_2_A_E, MULTIPLE_2_A_G, CODE_MULTIPLE_2_A_G -> 2;
			case MULTIPLE_3_A_G -> 3;
		};
	}

	private int maximumChoiceOrder(QuestionType questionType) {
		return switch (questionType) {
			case SINGLE_A_E, MULTIPLE_2_A_E, CODE_SINGLE_A_E -> 5;
			case SINGLE_A_G, MULTIPLE_2_A_G, MULTIPLE_3_A_G, CODE_MULTIPLE_2_A_G -> 7;
		};
	}
}
