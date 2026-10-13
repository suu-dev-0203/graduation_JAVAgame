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
 * Chapter内のStage設定をDBに保存するEntityです。
 */
@Entity
@Table(name = "stage_definitions")
public class StageDefinition {

	/** DBが自動で割り当てるStage設定のIDです。 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long stageId;

	/** このStageが属するChapterです。 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chapter_id", nullable = false)
	private Chapter chapter;

	/** Chapter内でのStageの順番です。 */
	@Column(nullable = false)
	private int stageNumber;

	/** Stageで使うプレイヤー画像を選ぶための識別文字列です。 */
	@Column(nullable = false)
	private String playerCharacterKey;

	/** Stageで戦う敵画像を選ぶための識別文字列です。 */
	@Column(nullable = false)
	private String enemyKey;

	/** Stageの背景画像を選ぶための識別文字列です。 */
	@Column(nullable = false)
	private String fieldKey;

	/** Stageで使うミニゲームの種類です。 */
	@Enumerated(EnumType.STRING)
	/** Stage開始時の敵HPです。MVPでは5に設定します。 */
	@Column(nullable = false)
	private MiniGameType miniGameType;

	@Column(nullable = false)
	private int maxEnemyHp;

	protected StageDefinition() {
		// JPAがDBからStageDefinitionを読み込むときに使います。
	}

	/** ChapterとStage番号、ゲーム画面の設定をまとめて登録します。 */
	public StageDefinition(
			Chapter chapter,
			int stageNumber,
			String playerCharacterKey,
			String enemyKey,
			String fieldKey,
			MiniGameType miniGameType,
			int maxEnemyHp) {
		this.chapter = chapter;
		this.stageNumber = stageNumber;
		this.playerCharacterKey = playerCharacterKey;
		this.enemyKey = enemyKey;
		this.fieldKey = fieldKey;
		this.miniGameType = miniGameType;
		this.maxEnemyHp = maxEnemyHp;
	}

	/** DB上でこのStage設定を識別するIDを返します。 */
	public Long getStageId() {
		return stageId;
	}

	/** Stageが属するChapterを返します。 */
	public Chapter getChapter() {
		return chapter;
	}

	/** Chapter内でのStage番号を返します。 */
	public int getStageNumber() {
		return stageNumber;
	}

	/** プレイヤーの見た目を選ぶ識別文字列を返します。 */
	public String getPlayerCharacterKey() {
		return playerCharacterKey;
	}

	/** 敵の見た目を選ぶ識別文字列を返します。 */
	public String getEnemyKey() {
		return enemyKey;
	}

	/** 背景を選ぶ識別文字列を返します。 */
	public String getFieldKey() {
		return fieldKey;
	}

	/** Stageで使うミニゲームの種類を返します。 */
	public MiniGameType getMiniGameType() {
		return miniGameType;
	}

	/** Stage開始時の敵HPを返します。 */
	public int getMaxEnemyHp() {
		return maxEnemyHp;
	}
}
