package com.example.graduation_java_game;

import java.time.Instant;

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
 * 1つのStageで出題する問題と、その出題順を記録します。
 */
@Entity
@Table(name = "stage_questions", uniqueConstraints = {
		@UniqueConstraint(
				name = "uk_stage_question_order",
				columnNames = { "stage_session_id", "question_order" }),
		@UniqueConstraint(
				name = "uk_stage_question_question",
				columnNames = { "stage_session_id", "question_id" })
})
public class StageQuestion {

	/** DBが自動で割り当てるStage問題のIDです。 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long stageQuestionId;

	/** この出題が含まれるStageの進行記録です。 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stage_session_id", nullable = false)
	private StageSession stageSession;

	/** Stageで実際に出題するQuestionです。 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	/** Stage内での出題順です。1問目から5問目を1～5で表します。 */
	@Column(nullable = false)
	private int questionOrder;

	/** プレイヤーがこの問題を開始した時刻です。開始前はnullです。 */
	private Instant startedAt;

	/** プレイヤーがこの問題に回答した時刻です。未回答ならnullです。 */
	private Instant answeredAt;

	/** この問題を一時停止した時刻です。停止中でなければnullです。 */
	private Instant pausedAt;

	/** この問題で一時停止していた合計時間をミリ秒で記録します。 */
	@Column(nullable = false)
	private long totalPausedMs;

	protected StageQuestion() {
		// JPAがDBからStageQuestionを読み込むときに使います。
	}

	/**
	 * Stageに出題する問題と順番を登録します。
	 * ゲーム開始時点ではまだ問題を開始していないため、時刻は未設定にします。
	 */
	public StageQuestion(StageSession stageSession, Question question, int questionOrder) {
		this.stageSession = stageSession;
		this.question = question;
		this.questionOrder = questionOrder;
		this.totalPausedMs = 0;
	}

	/** このStage問題を識別するIDを返します。 */
	public Long getStageQuestionId() {
		return stageQuestionId;
	}

	/** この問題を出題するStage記録を返します。 */
	public StageSession getStageSession() {
		return stageSession;
	}

	/** 実際に出題するQuestionを返します。 */
	public Question getQuestion() {
		return question;
	}

	/** Stage内での出題順を返します。 */
	public int getQuestionOrder() {
		return questionOrder;
	}

	/** 問題を開始した日時を返します。まだ開始していなければnullです。 */
	public Instant getStartedAt() {
		return startedAt;
	}

	/**
	 * この問題の開始時刻を記録します。
	 * 開始済み、または回答済みの問題を開始し直すことはできません。
	 *
	 * @param startTime サーバーが決めた問題開始時刻
	 * @return 初回の記録に成功した場合はtrue、すでに開始・回答済みの場合はfalse
	 */
	public boolean start(Instant startTime) {
		if (startTime == null) {
			throw new IllegalArgumentException("問題の開始時刻は必須です。");
		}
		if (startedAt != null || answeredAt != null) {
			return false;
		}

		this.startedAt = startTime;
		return true;
	}

	/**
	 * この問題の回答時刻を記録します。
	 *
	 * @return 初回回答の場合はtrue、回答できない状態ならfalse
	 */
	public boolean recordAnswer(Instant answerTime) {
		if (answerTime == null) {
			throw new IllegalArgumentException("問題の回答時刻は必須です。");
		}
		if (startedAt == null || answeredAt != null || pausedAt != null) {
			return false;
		}

		answeredAt = answerTime;
		return true;
	}

	/** 問題に回答した日時を返します。まだ回答していなければnullです。 */
	public Instant getAnsweredAt() {
		return answeredAt;
	}

	/** 問題を一時停止した日時を返します。一時停止中でなければnullです。 */
	public Instant getPausedAt() {
		return pausedAt;
	}

	/** 問題の一時停止時間の合計をミリ秒で返します。 */
	public long getTotalPausedMs() {
		return totalPausedMs;
	}
}
