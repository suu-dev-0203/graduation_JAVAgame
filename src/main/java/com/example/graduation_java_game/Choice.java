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

	/** DBが自動で割り当てる選択肢のIDです。 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long choiceId;

	/** この選択肢が属する問題です。 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	/** 画面に表示する選択肢の文章です。 */
	@Column(nullable = false)
	private String choiceText;

	/** 選択肢を画面に並べる順番です。 */
	@Column(nullable = false)
	private int choiceOrder;

	/** 回答判定のためサーバー側で使う正解情報です。 */
	@Column(nullable = false)
	private boolean correct;

	protected Choice() {
		// JPAがDBからChoiceを読み込むときに使います。
	}

	/** 新しい選択肢を作り、所属する問題と正解情報を設定します。 */
	public Choice(Question question, String choiceText, int choiceOrder, boolean correct) {
		this.question = question;
		this.choiceText = choiceText;
		this.choiceOrder = choiceOrder;
		this.correct = correct;
	}

	/** DB上でこの選択肢を識別するIDを返します。 */
	public Long getChoiceId() {
		return choiceId;
	}

	/** この選択肢が属する問題を返します。 */
	public Question getQuestion() {
		return question;
	}

	/** 画面に表示する文章を返します。 */
	public String getChoiceText() {
		return choiceText;
	}

	/** 表示順を返します。 */
	public int getChoiceOrder() {
		return choiceOrder;
	}

	/** この選択肢が正解かどうかを返します。サーバー側の判定用です。 */
	public boolean isCorrect() {
		return correct;
	}
}
