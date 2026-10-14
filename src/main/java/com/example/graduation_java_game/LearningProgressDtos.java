package com.example.graduation_java_game;

import java.time.Instant;
import java.util.List;

/**
 * 学習状況APIで受け渡すデータ型をまとめます。
 * これらは画面に返す項目、または画面から受け取る項目を定義します。
 */
public final class LearningProgressDtos {

	private LearningProgressDtos() {
		// このクラス自体を使うのではなく、内側のデータ型を使います。
	}

	/** Chapterごとの問題数と、苦手・超苦手の問題を返します。 */
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

	/** 完了した苦手特訓の結果を返します。 */
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

	/** 問題の分類を画面から受け取ります。 */
	public record MasteryStatusUpdateRequest(MasteryStatus status) {
	}

	/** 保存した問題の分類と更新日時を返します。 */
	public record MasteryStatusUpdateResponse(
			Long questionId,
			MasteryStatus status,
			Instant updatedAt) {
	}
}
