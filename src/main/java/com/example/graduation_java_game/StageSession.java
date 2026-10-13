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

/**
 * 1回のゲームに含まれる、個々のStageの進行記録です。
 * 現在の問題番号、正解数、敵HP、コンボなどを保存します。
 */
@Entity
@Table(name = "stage_sessions", uniqueConstraints = {
		@UniqueConstraint(
				name = "uk_stage_session_game_stage",
				columnNames = { "game_session_id", "stage_id" })
})
public class StageSession {

	/** DBが自動で割り当てるStage記録のIDです。 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long stageSessionId;

	/** このStage記録が属する、ゲーム全体の記録です。 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_session_id", nullable = false)
	private GameSession gameSession;

	/** Stage番号やキャラクターなど、このStageで使う設定です。 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stage_id", nullable = false)
	private StageDefinition stageDefinition;

	/** 次に出す問題の番号です。1問目から5問目までを1～5で表します。 */
	@Column(nullable = false)
	private int currentQuestionIndex;

	/** このStageで正解した問題数です。 */
	@Column(nullable = false)
	private int correctCount;

	/** このStageの敵に残っているHPです。 */
	@Column(nullable = false)
	private int enemyHp;

	/** 連続正解数です。不正解や時間切れで0に戻ります。 */
	@Column(nullable = false)
	private int combo;

	/** このStageがプレイ中・一時停止・クリア済みなどのどの状態かを表します。 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StageSessionStatus status;

	/** このStageを開始した日時です。 */
	@Column(nullable = false)
	private Instant startedAt;

	/** このStageを終えた日時です。プレイ中はまだ値がありません。 */
	private Instant endedAt;

	/** 一時停止中にその一時停止を開始した日時です。 */
	private Instant pausedAt;

	/** このStageで一時停止していた時間の合計（ミリ秒）です。 */
	@Column(nullable = false)
	private long totalPausedMs;

	protected StageSession() {
		// JPAがDBからStageSessionを読み込むときに使います。
	}

	public StageSession(
			GameSession gameSession,
			StageDefinition stageDefinition,
			int currentQuestionIndex,
			int correctCount,
			int enemyHp,
			int combo,
			StageSessionStatus status,
			Instant startedAt,
			Instant endedAt,
			Instant pausedAt,
			long totalPausedMs) {
		this.gameSession = gameSession;
		this.stageDefinition = stageDefinition;
		this.currentQuestionIndex = currentQuestionIndex;
		this.correctCount = correctCount;
		this.enemyHp = enemyHp;
		this.combo = combo;
		this.status = status;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
		this.pausedAt = pausedAt;
		this.totalPausedMs = totalPausedMs;
	}

	/** このStage記録を識別するIDを返します。 */
	public Long getStageSessionId() {
		return stageSessionId;
	}

	/** このStage記録が属するゲーム全体の記録を返します。 */
	public GameSession getGameSession() {
		return gameSession;
	}

	/** このStageで使うキャラクターなどの設定を返します。 */
	public StageDefinition getStageDefinition() {
		return stageDefinition;
	}

	/** 現在の問題番号を1～5の数値で返します。 */
	public int getCurrentQuestionIndex() {
		return currentQuestionIndex;
	}

	/** このStageで正解した問題数を返します。 */
	public int getCorrectCount() {
		return correctCount;
	}

	/** 敵に残っているHPを返します。 */
	public int getEnemyHp() {
		return enemyHp;
	}

	/** 連続正解数を返します。 */
	public int getCombo() {
		return combo;
	}

	/** Stageの現在の状態を返します。 */
	public StageSessionStatus getStatus() {
		return status;
	}

	/** Stageを開始した日時を返します。 */
	public Instant getStartedAt() {
		return startedAt;
	}

	/** Stageを終了した日時を返します。プレイ中はnullです。 */
	public Instant getEndedAt() {
		return endedAt;
	}

	/** 一時停止を始めた日時を返します。一時停止中でなければnullです。 */
	public Instant getPausedAt() {
		return pausedAt;
	}

	/** Stageで一時停止していた時間の合計をミリ秒で返します。 */
	public long getTotalPausedMs() {
		return totalPausedMs;
	}

	/**
	 * 回答結果を反映し、次の問題またはStage Clearへ進めます。
	 */
	public void recordAnswer(boolean correct, boolean stageClear) {
		if (status != StageSessionStatus.PLAYING) {
			throw new IllegalStateException("プレイ中のStageにだけ回答を反映できます。");
		}

		if (correct) {
			correctCount++;
			enemyHp = Math.max(0, enemyHp - 1);
			combo++;
		} else {
			combo = 0;
		}

		if (stageClear) {
			status = StageSessionStatus.CLEARED;
		} else {
			currentQuestionIndex++;
		}
	}
}
