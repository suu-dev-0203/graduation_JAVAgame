package com.example.graduation_java_game;

/**
 * 問題の回答形式と、使える選択肢の範囲を表します。
 */
public enum QuestionType {
	/** A～Eの選択肢から1つ選ぶ問題です。 */
	SINGLE_A_E,
	/** A～Gの選択肢から1つ選ぶ問題です。 */
	SINGLE_A_G,
	/** A～Eの選択肢から2つ選ぶ問題です。 */
	MULTIPLE_2_A_E,
	/** A～Gの選択肢から2つ選ぶ問題です。 */
	MULTIPLE_2_A_G,
	/** A～Gの選択肢から3つ選ぶ問題です。 */
	MULTIPLE_3_A_G,
	/** コードを読み、A～Eから1つ選ぶ問題です。 */
	CODE_SINGLE_A_E,
	/** コードを読み、A～Gから2つ選ぶ問題です。 */
	CODE_MULTIPLE_2_A_G
}
