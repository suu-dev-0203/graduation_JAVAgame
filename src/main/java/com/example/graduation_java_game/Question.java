package com.example.graduation_java_game;

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

/**
 * Chapterに属するJava学習問題をDBに保存するEntityです。
 */
@Entity
@Table(name = "questions")
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long questionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chapter_id", nullable = false)
	private Chapter chapter;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private QuestionType questionType;

	@Column(nullable = false)
	private String questionText;

	private String codeBlock;

	@Column(nullable = false)
	private int timeLimitSeconds;

	@Column(nullable = false)
	private String shortExplanation;

	@Column(nullable = false)
	private String detailedExplanation;

	protected Question() {
		// JPAがDBからQuestionを読み込むときに使います。
	}

	public Question(
			Chapter chapter,
			QuestionType questionType,
			String questionText,
			String codeBlock,
			int timeLimitSeconds,
			String shortExplanation,
			String detailedExplanation) {
		this.chapter = chapter;
		this.questionType = questionType;
		this.questionText = questionText;
		this.codeBlock = codeBlock;
		this.timeLimitSeconds = timeLimitSeconds;
		this.shortExplanation = shortExplanation;
		this.detailedExplanation = detailedExplanation;
	}

	public Long getQuestionId() {
		return questionId;
	}

	public Chapter getChapter() {
		return chapter;
	}

	public QuestionType getQuestionType() {
		return questionType;
	}

	public String getQuestionText() {
		return questionText;
	}

	public String getCodeBlock() {
		return codeBlock;
	}

	public int getTimeLimitSeconds() {
		return timeLimitSeconds;
	}

	public String getShortExplanation() {
		return shortExplanation;
	}

	public String getDetailedExplanation() {
		return detailedExplanation;
	}
}
