package com.example.graduation_java_game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * プレイヤーが1問に回答した結果を記録します。
 */
@Entity
@Table(name = "answer_histories", uniqueConstraints = {
		@UniqueConstraint(name = "uk_answer_history_stage_question", columnNames = "stage_question_id")
})
public class AnswerHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long answerHistoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_session_id", nullable = false)
	private GameSession gameSession;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stage_session_id", nullable = false)
	private StageSession stageSession;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stage_question_id", nullable = false)
	private StageQuestion stageQuestion;

	@Column(nullable = false)
	private boolean correct;

	@Column(nullable = false)
	private boolean timeout;

	@Column(nullable = false)
	private long answerTimeMs;

	protected AnswerHistory() {
	}

	public AnswerHistory(
			GameSession gameSession,
			StageSession stageSession,
			StageQuestion stageQuestion,
			boolean correct,
			boolean timeout,
			long answerTimeMs) {
		this.gameSession = gameSession;
		this.stageSession = stageSession;
		this.stageQuestion = stageQuestion;
		this.correct = correct;
		this.timeout = timeout;
		this.answerTimeMs = answerTimeMs;
	}

	public Long getAnswerHistoryId() {
		return answerHistoryId;
	}

	public StageQuestion getStageQuestion() {
		return stageQuestion;
	}

	public boolean isCorrect() {
		return correct;
	}

	public boolean isTimeout() {
		return timeout;
	}
}
