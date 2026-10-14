package com.example.graduation_java_game;

import java.time.Instant;
import java.util.List;

/** Chapterごとの分類数と、苦手特訓の対象になる問題を返します。 */
public record ChapterProgressResponse(List<ChapterProgress> chapters) {

	public record ChapterProgress(
			Long chapterId,
			String chapterName,
			int questionCount,
			int unseenCount,
			int okCount,
			int almostCount,
			int weakCount,
			int veryWeakCount,
			List<WeakQuestion> weakQuestions) {
	}

	public record WeakQuestion(
			Long questionId,
			String questionText,
			MasteryStatus status,
			int attemptCount,
			Instant updatedAt) {
	}
}
