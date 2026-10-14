package com.example.graduation_java_game;

import java.time.Instant;

/** 問題の分類を保存した結果です。 */
public record MasteryStatusUpdateResponse(
		Long questionId,
		MasteryStatus status,
		Instant updatedAt) {
}
