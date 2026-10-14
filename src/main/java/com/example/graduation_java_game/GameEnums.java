package com.example.graduation_java_game;

/*
 * 状態・種類・問題の分類を表すenumをまとめます。
 * publicを付けていないenumは、このpackage内のJavaファイルから使います。
 */

/** ゲーム全体が現在どの状態かを表します。 */
enum GameSessionStatus {
	/** ゲームを進めています。 */
	PLAYING,
	/** プレイヤーが一時停止しています。 */
	PAUSED,
	/** 選んだ問題数を最後まで遊び終えました。 */
	COMPLETED,
	/** プレイヤーが途中でゲームをやめました。 */
	ABANDONED
}

/** 通常学習か苦手特訓かを表します。 */
enum GameSessionType {
	STANDARD,
	WEAK_TRAINING
}

/** 問題ごとの現在の理解度を表します。 */
enum MasteryStatus {
	UNSEEN("未出題"),
	OK("OK"),
	ALMOST("あと一歩"),
	WEAK("苦手"),
	VERY_WEAK("超苦手");

	private final String displayName;

	MasteryStatus(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}

	public boolean isWeak() {
		return this == WEAK || this == VERY_WEAK;
	}

	public boolean isEditable() {
		return this != UNSEEN;
	}
}

/** Stageで使うミニゲームの種類を表します。 */
enum MiniGameType {
	/** キー入力で侍が攻撃するミニゲームです。 */
	TYPING_SAMURAI,
	/** コードを狙うゲーム画面を使うミニゲームです。 */
	CODE_TARGET
}

/** 問題バンクで使う難易度を表します。 */
enum QuestionDifficulty {
	EASY,
	ADVANCED,
	BRONZE_STYLE
}

/** ゲーム開始時に選ぶ出題方法を表します。 */
enum QuestionSelectionMode {
	RANDOM,
	EASY,
	ADVANCED,
	BRONZE_STYLE;

	public QuestionDifficulty difficultyOrNull() {
		return switch (this) {
			case RANDOM -> null;
			case EASY -> QuestionDifficulty.EASY;
			case ADVANCED -> QuestionDifficulty.ADVANCED;
			case BRONZE_STYLE -> QuestionDifficulty.BRONZE_STYLE;
		};
	}

	public String displayName() {
		return switch (this) {
			case RANDOM -> "難易度おまかせ（ランダム）";
			case EASY -> "入門（簡単）";
			case ADVANCED -> "応用（難しい）";
			case BRONZE_STYLE -> "Java Bronze風（オリジナル問題）";
		};
	}
}

/** 問題の回答形式と、使える選択肢の範囲を表します。 */
enum QuestionType {
	/** A～Eの選択肢から1つ選びます。 */
	SINGLE_A_E,
	/** A～Gの選択肢から1つ選びます。 */
	SINGLE_A_G,
	/** A～Eの選択肢から2つ選びます。 */
	MULTIPLE_2_A_E,
	/** A～Gの選択肢から2つ選びます。 */
	MULTIPLE_2_A_G,
	/** A～Gの選択肢から3つ選びます。 */
	MULTIPLE_3_A_G,
	/** コードを読み、A～Eから1つ選びます。 */
	CODE_SINGLE_A_E,
	/** コードを読み、A～Gから2つ選びます。 */
	CODE_MULTIPLE_2_A_G
}

/** 1つのStageが現在どの状態かを表します。 */
enum StageSessionStatus {
	/** Stageの問題を進めています。 */
	PLAYING,
	/** Stageを一時停止しています。 */
	PAUSED,
	/** Stageの5問に回答しました。 */
	CLEARED,
	/** プレイヤーがStageの途中でやめました。 */
	ABANDONED
}
