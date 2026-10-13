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

/**
 * プレイヤーが1回のゲームを遊ぶ記録です。
 * Chapterと選んだ問題数、得点、全体の状態を保存します。
 */
@Entity
@Table(name = "game_sessions")
public class GameSession {

	/** DBが自動で割り当てるゲーム記録のIDです。 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long gameSessionId;

	/** このゲームで学習するChapterです。 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chapter_id", nullable = false)
	private Chapter chapter;

	/** プレイヤーが選んだ総問題数（5、10、20のいずれか）です。 */
	@Column(nullable = false)
	private int questionCount;

	/** 選んだ問題数を5問ずつに分けたStageの数です。 */
	@Column(nullable = false)
	private int totalStageCount;

	/** 今ゲームで進行しているStage番号です。Stage 1から開始します。 */
	@Column(nullable = false)
	private int currentStageNumber;

	/** ゲーム全体で獲得した得点です。 */
	@Column(nullable = false)
	private int score;

	/** ゲーム全体がプレイ中・一時停止・完了などのどの状態かを表します。 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GameSessionStatus status;

	/** ゲームを開始した日時です。 */
	@Column(nullable = false)
	private Instant startedAt;

	/** ゲームを終えた日時です。プレイ中はまだ値がありません。 */
	private Instant endedAt;

	protected GameSession() {
		// JPAがDBからGameSessionを読み込むときに使います。
	}

	public GameSession(
			Chapter chapter,
			int questionCount,
			int totalStageCount,
			int currentStageNumber,
			int score,
			GameSessionStatus status,
			Instant startedAt,
			Instant endedAt) {
		this.chapter = chapter;
		this.questionCount = questionCount;
		this.totalStageCount = totalStageCount;
		this.currentStageNumber = currentStageNumber;
		this.score = score;
		this.status = status;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
	}

	/** このゲーム記録を識別するIDを返します。 */
	public Long getGameSessionId() {
		return gameSessionId;
	}

	/** ゲームで選ばれたChapterを返します。 */
	public Chapter getChapter() {
		return chapter;
	}

	/** ゲーム全体で遊ぶ問題数を返します。 */
	public int getQuestionCount() {
		return questionCount;
	}

	/** ゲーム全体のStage数を返します。 */
	public int getTotalStageCount() {
		return totalStageCount;
	}

	/** 現在進行中のStage番号を返します。 */
	public int getCurrentStageNumber() {
		return currentStageNumber;
	}

	/** ここまでに獲得したゲーム全体の得点を返します。 */
	public int getScore() {
		return score;
	}

	/** ゲーム全体の状態を返します。 */
	public GameSessionStatus getStatus() {
		return status;
	}

	/** ゲームを開始した日時を返します。 */
	public Instant getStartedAt() {
		return startedAt;
	}

	/** ゲームを終了した日時を返します。プレイ中はnullです。 */
	public Instant getEndedAt() {
		return endedAt;
	}

	/** 正解時の得点を加算します。 */
	public void addScore(int points) {
		if (points < 0) {
			throw new IllegalArgumentException("加算する得点は0以上である必要があります。");
		}
		score += points;
	}

	/** 最終Stageを終えたゲームを完了状態にします。 */
	public void complete(Instant completedAt) {
		if (completedAt == null) {
			throw new IllegalArgumentException("ゲーム完了時刻は必須です。");
		}
		if (status != GameSessionStatus.PLAYING) {
			throw new IllegalStateException("プレイ中のゲームだけ完了できます。");
		}
		status = GameSessionStatus.COMPLETED;
		endedAt = completedAt;
	}
}
