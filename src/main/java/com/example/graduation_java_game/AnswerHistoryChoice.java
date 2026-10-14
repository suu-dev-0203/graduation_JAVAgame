package com.example.graduation_java_game;

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
 * 回答履歴と、プレイヤーが選んだ選択肢を関連付けます。
 */
@Entity
@Table(name = "answer_history_choices", uniqueConstraints = {
		@UniqueConstraint(
				name = "uk_answer_history_choice",
				columnNames = { "answer_history_id", "choice_id" })
})
public class AnswerHistoryChoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "answer_history_id", nullable = false)
	private AnswerHistory answerHistory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "choice_id", nullable = false)
	private Choice choice;

	protected AnswerHistoryChoice() {
	}

	public AnswerHistoryChoice(AnswerHistory answerHistory, Choice choice) {
		this.answerHistory = answerHistory;
		this.choice = choice;
	}

	public Choice getChoice() {
		return choice;
	}
}
