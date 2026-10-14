package com.example.graduation_java_game;

import java.time.Instant;
import java.util.List;

/** 完了した苦手特訓の記録を返します。 */
public record TrainingRecordResponse(List<Item> items) {

	public record Item(
			Long gameSessionId,
			String chapterName,
			Instant startedAt,
			Instant endedAt,
			int questionCount,
			int correctCount,
			int score) {
	}
}
