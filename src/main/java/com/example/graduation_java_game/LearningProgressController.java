package com.example.graduation_java_game;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 学習状況と苦手特訓の記録を扱うAPIです。 */
@RestController
@RequestMapping("/api/learning-progress")
public class LearningProgressController {

	private final LearningProgressService learningProgressService;

	public LearningProgressController(LearningProgressService learningProgressService) {
		this.learningProgressService = learningProgressService;
	}

	/** Chapterごとの分類数と苦手問題を返します。 */
	@GetMapping("/chapters")
	public ChapterProgressResponse findChapterProgress() {
		return learningProgressService.findChapterProgress();
	}

	/** 完了した苦手特訓の記録を返します。 */
	@GetMapping("/training-records")
	public TrainingRecordResponse findTrainingRecords() {
		return learningProgressService.findTrainingRecords();
	}

	/** 回答済み問題の分類を更新します。 */
	@PutMapping("/questions/{questionId}")
	public MasteryStatusUpdateResponse updateStatus(
			@PathVariable Long questionId,
			@RequestBody MasteryStatusUpdateRequest request) {
		return learningProgressService.updateStatus(
				questionId,
				request == null ? null : request.status());
	}
}
