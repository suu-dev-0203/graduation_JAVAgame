package com.example.graduation_java_game;

/**
 * 1回のゲーム全体が、現在どの状態かを表します。
 */
public enum GameSessionStatus {
	/** ゲームを進めている状態です。 */
	PLAYING,
	/** プレイヤーがゲームを一時停止している状態です。 */
	PAUSED,
	/** 選んだ問題数を最後まで遊び終えた状態です。 */
	COMPLETED,
	/** プレイヤーが途中でゲームをやめた状態です。 */
	ABANDONED
}
