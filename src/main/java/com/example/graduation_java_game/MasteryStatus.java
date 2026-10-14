package com.example.graduation_java_game;

/** 問題ごとの現在の理解度です。 */
public enum MasteryStatus {
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
