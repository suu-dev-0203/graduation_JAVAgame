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

/**
 * 問題に表示する選択肢をDBに保存するEntityです。
 */
@Entity
@Table(name = "choices")
public class Choice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long choiceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@Column(nullable = false)
	private String choiceText;

	@Column(nullable = false)
	private int choiceOrder;

	@Column(nullable = false)
	private boolean correct;

	protected Choice() {
		// JPAがDBからChoiceを読み込むときに使います。
	}

	public Choice(Question question, String choiceText, int choiceOrder, boolean correct) {
		this.question = question;
		this.choiceText = choiceText;
		this.choiceOrder = choiceOrder;
		this.correct = correct;
	}

	public Long getChoiceId() {
		return choiceId;
	}

	public Question getQuestion() {
		return question;
	}

	public String getChoiceText() {
		return choiceText;
	}

	public int getChoiceOrder() {
		return choiceOrder;
	}

	public boolean isCorrect() {
		return correct;
	}
}
