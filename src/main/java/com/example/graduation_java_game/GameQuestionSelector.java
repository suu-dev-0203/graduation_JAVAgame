package com.example.graduation_java_game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * ゲームで出す問題を集め、出題する順番を決めます。
 * ゲームの開始やStageの進行はGameSessionServiceが担当します。
 */
@Service
public class GameQuestionSelector {

	private static final int QUESTIONS_PER_STAGE = 5;

	private final QuestionRepository questionRepository;
	private final AnswerHistoryRepository answerHistoryRepository;
	private final QuestionMasteryRepository questionMasteryRepository;

	public GameQuestionSelector(
			QuestionRepository questionRepository,
			AnswerHistoryRepository answerHistoryRepository,
			QuestionMasteryRepository questionMasteryRepository) {
		this.questionRepository = questionRepository;
		this.answerHistoryRepository = answerHistoryRepository;
		this.questionMasteryRepository = questionMasteryRepository;
	}

	/** 選択された難易度に合う問題だけを集めます。 */
	List<Question> findQuestionsForMode(Long chapterId, QuestionSelectionMode mode) {
		List<Question> questions = questionRepository.findByChapter_ChapterId(chapterId);
		QuestionDifficulty selectedDifficulty = mode.difficultyOrNull();
		if (selectedDifficulty == null) {
			return questions;
		}

		return questions.stream()
				.filter(question -> question.getDifficulty() == selectedDifficulty)
				.toList();
	}

	/** 未出題や苦手の問題を先にし、今回すでに使った問題を除いて並べます。 */
	List<Question> prioritizeQuestions(
			Long chapterId,
			List<Question> questions,
			Set<Long> excludedQuestionIds) {
		Map<Long, AnswerHistory> latestHistoryByQuestion = findLatestHistoryByQuestion(chapterId);
		Map<Long, QuestionMastery> savedMasteryByQuestion = findSavedMasteryByQuestion(chapterId);

		List<Question> unanswered = new ArrayList<>();
		List<Question> veryWeak = new ArrayList<>();
		List<Question> weak = new ArrayList<>();
		List<Question> almost = new ArrayList<>();
		List<Question> cleared = new ArrayList<>();

		for (Question question : questions) {
			if (excludedQuestionIds.contains(question.getQuestionId())) {
				continue;
			}

			MasteryStatus status = findStatus(
					question, latestHistoryByQuestion, savedMasteryByQuestion);
			switch (status) {
				case UNSEEN -> unanswered.add(question);
				case VERY_WEAK -> veryWeak.add(question);
				case WEAK -> weak.add(question);
				case ALMOST -> almost.add(question);
				case OK -> cleared.add(question);
			}
		}

		// 同じ分類の中では順番をランダムにし、分類ごとの優先順を保ちます。
		Collections.shuffle(unanswered);
		Collections.shuffle(veryWeak);
		Collections.shuffle(weak);
		Collections.shuffle(almost);
		Collections.shuffle(cleared);

		List<Question> prioritized = new ArrayList<>(questions.size());
		prioritized.addAll(unanswered);
		prioritized.addAll(veryWeak);
		prioritized.addAll(weak);
		prioritized.addAll(almost);
		prioritized.addAll(cleared);
		return prioritized;
	}

	/** 苦手問題を先に選び、5問に足りない場合は特訓中に出した問題を再利用します。 */
	List<Question> selectWeakTrainingQuestionsForStage(
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
		// 候補を最後まで使ったら先頭へ戻り、5問になるまで問題を追加します。
		while (selectedQuestions.size() < QUESTIONS_PER_STAGE) {
			selectedQuestions.add(repeatCandidates.get(repeatIndex % repeatCandidates.size()));
			repeatIndex++;
		}
		return selectedQuestions;
	}

	/** 今回のゲームで出した問題を、同じ問題が重ならない一覧にします。 */
	List<Question> findDistinctQuestions(List<StageQuestion> stageQuestions) {
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

	private Map<Long, AnswerHistory> findLatestHistoryByQuestion(Long chapterId) {
		Map<Long, AnswerHistory> latestHistoryByQuestion = new HashMap<>();
		for (AnswerHistory history : answerHistoryRepository
				.findByStageQuestion_Question_Chapter_ChapterId(chapterId)) {
			Long questionId = history.getStageQuestion().getQuestion().getQuestionId();
			AnswerHistory currentLatest = latestHistoryByQuestion.get(questionId);
			// 同じ問題の回答が複数ある場合、いちばん新しい回答だけを残します。
			if (currentLatest == null || isLaterHistory(history, currentLatest)) {
				latestHistoryByQuestion.put(questionId, history);
			}
		}
		return latestHistoryByQuestion;
	}

	private Map<Long, QuestionMastery> findSavedMasteryByQuestion(Long chapterId) {
		Map<Long, QuestionMastery> savedMasteryByQuestion = new HashMap<>();
		for (QuestionMastery mastery : questionMasteryRepository
				.findByQuestion_Chapter_ChapterId(chapterId)) {
			savedMasteryByQuestion.put(mastery.getQuestion().getQuestionId(), mastery);
		}
		return savedMasteryByQuestion;
	}

	private MasteryStatus findStatus(
			Question question,
			Map<Long, AnswerHistory> latestHistoryByQuestion,
			Map<Long, QuestionMastery> savedMasteryByQuestion) {
		QuestionMastery savedMastery = savedMasteryByQuestion.get(question.getQuestionId());
		// 保存済みの分類があれば、回答履歴から計算した分類より優先します。
		if (savedMastery != null) {
			return savedMastery.getStatus();
		}

		AnswerHistory latestHistory = latestHistoryByQuestion.get(question.getQuestionId());
		if (latestHistory == null) {
			return MasteryStatus.UNSEEN;
		}
		return latestHistory.isCorrect() ? MasteryStatus.OK : MasteryStatus.WEAK;
	}

	private boolean isLaterHistory(AnswerHistory candidate, AnswerHistory current) {
		// 回答時刻が同じ場合は、後から作られた履歴IDを新しいものとして扱います。
		int answerTimeComparison = candidate.getStageQuestion().getAnsweredAt()
				.compareTo(current.getStageQuestion().getAnsweredAt());
		return answerTimeComparison > 0
				|| (answerTimeComparison == 0
						&& candidate.getAnswerHistoryId() > current.getAnswerHistoryId());
	}
}
