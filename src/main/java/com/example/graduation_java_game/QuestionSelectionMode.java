package com.example.graduation_java_game;

/**
 * ゲーム開始時に選ぶ出題方法です。
 */
public enum QuestionSelectionMode {
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
