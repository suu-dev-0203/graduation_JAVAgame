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

	/** DBが自動で割り当てる問題のIDです。 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long questionId;

	/** この問題を学習するChapterです。 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chapter_id", nullable = false)
	private Chapter chapter;

	/** 単一選択・複数選択など、この問題の回答形式です。 */
	@Enumerated(EnumType.STRING)
	/** プレイヤーに出題する文章です。 */
	@Column(nullable = false)
	private QuestionType questionType;

	@Column(nullable = false)
	private String questionText;

	/** 問題文と一緒に表示するコードです。コード問題以外では空の場合があります。 */
	private String codeBlock;

	/** 回答に使える制限時間を秒で表します。 */
	@Column(nullable = false)
	private int timeLimitSeconds;

	/** 回答後に画面で見せる短い説明です。 */
	@Column(nullable = false)
	private String shortExplanation;

	/** 問題の詳しい解説文です。 */
	@Column(nullable = false)
	private String detailedExplanation;

	protected Question() {
		// JPAがDBからQuestionを読み込むときに使います。
	}

	/** 問題と、その問題の回答形式・説明を設定します。 */
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

	/** DB上でこの問題を識別するIDを返します。 */
	public Long getQuestionId() {
		return questionId;
	}

	/** この問題が属するChapterを返します。 */
	public Chapter getChapter() {
		return chapter;
	}

	/** この問題の回答形式を返します。 */
	public QuestionType getQuestionType() {
		return questionType;
	}

	/** 出題文を返します。 */
	public String getQuestionText() {
		return questionText;
	}

	/** 問題に表示するコードを返します。 */
	public String getCodeBlock() {
		return codeBlock;
	}

	/** 制限時間を秒で返します。 */
	public int getTimeLimitSeconds() {
		return timeLimitSeconds;
	}

	/** 短い解説を返します。 */
	public String getShortExplanation() {
		return shortExplanation;
	}

	/** 詳しい解説を返します。 */
	public String getDetailedExplanation() {
		return detailedExplanation;
	}
}
