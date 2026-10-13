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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long stageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chapter_id", nullable = false)
	private Chapter chapter;

	@Column(nullable = false)
	private int stageNumber;

	@Column(nullable = false)
	private String playerCharacterKey;

	@Column(nullable = false)
	private String enemyKey;

	@Column(nullable = false)
	private String fieldKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MiniGameType miniGameType;

	@Column(nullable = false)
	private int maxEnemyHp;

	protected StageDefinition() {
		// JPAがDBからStageDefinitionを読み込むときに使います。
	}

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

	public Long getStageId() {
		return stageId;
	}

	public Chapter getChapter() {
		return chapter;
	}

	public int getStageNumber() {
		return stageNumber;
	}

	public String getPlayerCharacterKey() {
		return playerCharacterKey;
	}

	public String getEnemyKey() {
		return enemyKey;
	}

	public String getFieldKey() {
		return fieldKey;
	}

	public MiniGameType getMiniGameType() {
		return miniGameType;
	}

	public int getMaxEnemyHp() {
		return maxEnemyHp;
	}
}
