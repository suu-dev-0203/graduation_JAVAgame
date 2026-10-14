package com.example.graduation_java_game;

import com.example.graduation_java_game.LearningProgressDtos.ChapterProgressResponse;
import com.example.graduation_java_game.LearningProgressDtos.MasteryStatusUpdateResponse;
import com.example.graduation_java_game.LearningProgressDtos.TrainingRecordResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 問題の分類、Chapterごとの学習状況、苦手特訓の記録を管理します。 */
@Service
public class LearningProgressService {

	private final ChapterRepository chapterRepository;
	private final QuestionRepository questionRepository;
	private final AnswerHistoryRepository answerHistoryRepository;
	private final QuestionMasteryRepository questionMasteryRepository;
	private final GameSessionRepository gameSessionRepository;

	public LearningProgressService(
			ChapterRepository chapterRepository,
			QuestionRepository questionRepository,
			AnswerHistoryRepository answerHistoryRepository,
			QuestionMasteryRepository questionMasteryRepository,
			GameSessionRepository gameSessionRepository) {
		this.chapterRepository = chapterRepository;
		this.questionRepository = questionRepository;
		this.answerHistoryRepository = answerHistoryRepository;
		this.questionMasteryRepository = questionMasteryRepository;
		this.gameSessionRepository = gameSessionRepository;
	}

	/** 回答のたびに、その問題の現在の分類を初期設定または更新します。 */
	@Transactional
	public void recordAnswer(Question question, boolean correct, Instant answeredAt) {
		MasteryStatus status = correct ? MasteryStatus.OK : MasteryStatus.WEAK;
		QuestionMastery mastery = questionMasteryRepository
				.findByQuestion_QuestionId(question.getQuestionId())
				.orElseGet(() -> new QuestionMastery(question, status, answeredAt));
		mastery.update(status, answeredAt);
		questionMasteryRepository.save(mastery);
	}

	/** Chapterごとの分類数と、苦手特訓の対象問題を返します。 */
	@Transactional(readOnly = true)
	public ChapterProgressResponse findChapterProgress() {
		List<Chapter> chapters = chapterRepository.findAll();
		chapters.sort(Comparator.comparing(Chapter::getChapterId));
		List<Question> questions = questionRepository.findAll();
		Map<Long, List<Question>> questionsByChapter = new HashMap<>();
		for (Question question : questions) {
			questionsByChapter.computeIfAbsent(
					question.getChapter().getChapterId(), ignored -> new ArrayList<>()).add(question);
		}

		Map<Long, QuestionMastery> savedMastery = masteryByQuestion(questionMasteryRepository.findAll());
		List<AnswerHistory> allHistories = answerHistoryRepository.findAll();
		Map<Long, AnswerHistory> latestHistory = latestHistoryByQuestion(allHistories);
		Map<Long, Integer> attemptCounts = attemptCountsByQuestion(allHistories);
		List<ChapterProgressResponse.ChapterProgress> chapterProgress = new ArrayList<>();
		for (Chapter chapter : chapters) {
			int unseenCount = 0;
			int okCount = 0;
			int almostCount = 0;
			int weakCount = 0;
			int veryWeakCount = 0;
			List<ChapterProgressResponse.WeakQuestion> weakQuestions = new ArrayList<>();
			List<Question> chapterQuestions = questionsByChapter.getOrDefault(
					chapter.getChapterId(), List.of());
			chapterQuestions.sort(Comparator.comparing(Question::getQuestionId));
			for (Question question : chapterQuestions) {
				Long questionId = question.getQuestionId();
				MasteryStatus status = resolveStatus(questionId, savedMastery, latestHistory);
				AnswerHistory latestAnswer = latestHistory.get(questionId);
				// 保存済みの分類を使い、未保存なら最新の回答から分類を決めます。
				switch (status) {
					case UNSEEN -> unseenCount++;
					case OK -> okCount++;
					case ALMOST -> almostCount++;
					case WEAK, VERY_WEAK -> {
						if (status == MasteryStatus.WEAK) {
							weakCount++;
						} else {
							veryWeakCount++;
						}
						weakQuestions.add(toWeakQuestion(
								question, status, savedMastery, latestAnswer, attemptCounts));
					}
				}
			}
			chapterProgress.add(new ChapterProgressResponse.ChapterProgress(
					chapter.getChapterId(),
					chapter.getName(),
					chapterQuestions.size(),
					unseenCount,
					okCount,
					almostCount,
					weakCount,
					veryWeakCount,
					weakQuestions));
		}
		return new ChapterProgressResponse(chapterProgress);
	}

	/** 回答済み問題の分類を学習者が変更します。 */
	@Transactional
	public MasteryStatusUpdateResponse updateStatus(Long questionId, MasteryStatus status) {
		if (status == null || !status.isEditable()) {
			throw new GameApiException(
					HttpStatus.BAD_REQUEST,
					"INVALID_MASTERY_STATUS",
					"分類はOK、あと一歩、苦手、超苦手から選んでください。");
		}
		Question question = questionRepository.findById(questionId)
				.orElseThrow(() -> new GameApiException(
						HttpStatus.NOT_FOUND,
						"NOT_FOUND",
						"指定された問題が見つかりません。"));
		if (!answerHistoryRepository.existsByStageQuestion_Question_QuestionId(questionId)) {
			throw new GameApiException(
					HttpStatus.CONFLICT,
					"QUESTION_NOT_ANSWERED",
					"回答した問題だけ分類を変更できます。");
		}

		Instant updatedAt = Instant.now();
		QuestionMastery mastery = questionMasteryRepository
				.findByQuestion_QuestionId(questionId)
				.orElseGet(() -> new QuestionMastery(question, status, updatedAt));
		mastery.update(status, updatedAt);
		questionMasteryRepository.save(mastery);
		return new MasteryStatusUpdateResponse(questionId, status, updatedAt);
	}

	/** 指定Chapterで「苦手」または「超苦手」に分類された問題を返します。 */
	@Transactional(readOnly = true)
	public List<Question> findWeakQuestionsForTraining(Long chapterId) {
		List<Question> chapterQuestions = questionRepository.findByChapter_ChapterId(chapterId);
		Map<Long, QuestionMastery> savedMastery = masteryByQuestion(
				questionMasteryRepository.findByQuestion_Chapter_ChapterId(chapterId));
		Map<Long, AnswerHistory> latestHistory = latestHistoryByQuestion(
				answerHistoryRepository.findByStageQuestion_Question_Chapter_ChapterId(chapterId));
		return chapterQuestions.stream()
				.filter(question -> resolveStatus(
						question.getQuestionId(), savedMastery, latestHistory).isWeak())
				.toList();
	}

	/** 完了した苦手特訓の回数、日付、正解数、得点を返します。 */
	@Transactional(readOnly = true)
	public TrainingRecordResponse findTrainingRecords() {
		List<TrainingRecordResponse.Item> items = gameSessionRepository
				.findBySessionTypeAndStatusOrderByEndedAtDesc(
						GameSessionType.WEAK_TRAINING, GameSessionStatus.COMPLETED)
				.stream()
				.map(gameSession -> {
					List<AnswerHistory> histories = answerHistoryRepository
							.findByGameSession_GameSessionId(gameSession.getGameSessionId());
					int correctCount = (int) histories.stream().filter(AnswerHistory::isCorrect).count();
					return new TrainingRecordResponse.Item(
							gameSession.getGameSessionId(),
							gameSession.getChapter().getName(),
							gameSession.getStartedAt(),
							gameSession.getEndedAt(),
							gameSession.getQuestionCount(),
							correctCount,
							gameSession.getScore());
				})
				.toList();
		return new TrainingRecordResponse(items);
	}

	private ChapterProgressResponse.WeakQuestion toWeakQuestion(
			Question question,
			MasteryStatus status,
			Map<Long, QuestionMastery> savedMastery,
			AnswerHistory latestAnswer,
			Map<Long, Integer> attemptCounts) {
		QuestionMastery mastery = savedMastery.get(question.getQuestionId());
		Instant updatedAt = mastery != null
				? mastery.getUpdatedAt()
				: latestAnswer.getStageQuestion().getAnsweredAt();
		return new ChapterProgressResponse.WeakQuestion(
				question.getQuestionId(),
				question.getQuestionText(),
				status,
				attemptCounts.getOrDefault(question.getQuestionId(), 0),
				updatedAt);
	}

	private MasteryStatus resolveStatus(
			Long questionId,
			Map<Long, QuestionMastery> savedMastery,
			Map<Long, AnswerHistory> latestHistory) {
		// 手動変更を含む現在の分類を最優先し、なければ回答履歴を使います。
		QuestionMastery mastery = savedMastery.get(questionId);
		if (mastery != null) {
			return mastery.getStatus();
		}
		AnswerHistory history = latestHistory.get(questionId);
		if (history == null) {
			return MasteryStatus.UNSEEN;
		}
		return history.isCorrect() ? MasteryStatus.OK : MasteryStatus.WEAK;
	}

	private Map<Long, QuestionMastery> masteryByQuestion(List<QuestionMastery> masteries) {
		Map<Long, QuestionMastery> byQuestion = new HashMap<>();
		for (QuestionMastery mastery : masteries) {
			byQuestion.put(mastery.getQuestion().getQuestionId(), mastery);
		}
		return byQuestion;
	}

	private Map<Long, AnswerHistory> latestHistoryByQuestion(List<AnswerHistory> histories) {
		Map<Long, AnswerHistory> latest = new HashMap<>();
		for (AnswerHistory history : histories) {
			Long questionId = history.getStageQuestion().getQuestion().getQuestionId();
			AnswerHistory current = latest.get(questionId);
			if (current == null || isLaterHistory(history, current)) {
				latest.put(questionId, history);
			}
		}
		return latest;
	}

	private Map<Long, Integer> attemptCountsByQuestion(List<AnswerHistory> histories) {
		Map<Long, Integer> counts = new HashMap<>();
		for (AnswerHistory history : histories) {
			Long questionId = history.getStageQuestion().getQuestion().getQuestionId();
			counts.merge(questionId, 1, Integer::sum);
		}
		return counts;
	}

	private boolean isLaterHistory(AnswerHistory candidate, AnswerHistory current) {
		// 同時刻に回答された場合も、後から保存された履歴を最新として扱います。
		int answerTimeComparison = candidate.getStageQuestion().getAnsweredAt()
				.compareTo(current.getStageQuestion().getAnsweredAt());
		return answerTimeComparison > 0
				|| (answerTimeComparison == 0
						&& candidate.getAnswerHistoryId() > current.getAnswerHistoryId());
	}
}
