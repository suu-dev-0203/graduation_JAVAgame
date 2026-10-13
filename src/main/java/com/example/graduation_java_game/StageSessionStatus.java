package com.example.graduation_java_game;

/**
 * 1つのStageが、現在どの状態かを表します。
 */
public enum StageSessionStatus {
	/** Stageの問題を進めている状態です。 */
	PLAYING,
	/** Stageを一時停止している状態です。 */
	PAUSED,
	/** Stageの5問を最後まで回答した状態です。 */
	CLEARED,
	/** プレイヤーがStageの途中でゲームをやめた状態です。 */
	ABANDONED
}
