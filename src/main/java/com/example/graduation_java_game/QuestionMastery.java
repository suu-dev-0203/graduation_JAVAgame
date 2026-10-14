package com.example.graduation_java_game;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** 1人利用版で、各問題の現在の分類を1件ずつ保存します。 */
@Entity
@Table(name = "question_mastery", uniqueConstraints = {
		@UniqueConstraint(name = "uk_question_mastery_question", columnNames = "question_id")
})
public class QuestionMastery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long questionMasteryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MasteryStatus status;

	@Column(nullable = false)
	private Instant updatedAt;

	protected QuestionMastery() {
	}

	public QuestionMastery(Question question, MasteryStatus status, Instant updatedAt) {
		this.question = question;
		update(status, updatedAt);
	}

	public void update(MasteryStatus status, Instant updatedAt) {
		if (status == null || !status.isEditable()) {
			throw new IllegalArgumentException("回答済みの問題には編集可能な分類を指定してください。");
		}
		if (updatedAt == null) {
			throw new IllegalArgumentException("分類を更新した日時は必須です。");
		}
		this.status = status;
		this.updatedAt = updatedAt;
	}

	public Long getQuestionMasteryId() {
		return questionMasteryId;
	}

	public Question getQuestion() {
		return question;
	}

	public MasteryStatus getStatus() {
		return status;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
