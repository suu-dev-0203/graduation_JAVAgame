package com.example.graduation_java_game;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.graduation_java_game.GameApiDtos.ChapterResponse;
import com.example.graduation_java_game.LearningProgressDtos.ChapterProgressResponse;
import com.example.graduation_java_game.LearningProgressDtos.MasteryStatusUpdateRequest;
import com.example.graduation_java_game.LearningProgressDtos.MasteryStatusUpdateResponse;
import com.example.graduation_java_game.LearningProgressDtos.TrainingRecordResponse;

/*
 * Chapter一覧と学習状況APIの受付を担当するControllerをまとめます。
 * publicを付けていないControllerは、このpackage内で使います。
 * Springは@RestControllerを付けたクラスを検出してAPIを登録します。
 */

/**
 * ブラウザからChapter一覧を取得するAPIの窓口です。
 */
@RestController
@RequestMapping("/api/chapters")
class ChapterController {

	/** Chapter一覧を用意する処理をServiceに依頼します。 */
	private final ChapterService chapterService;

	public ChapterController(ChapterService chapterService) {
		this.chapterService = chapterService;
	}

	/**
	 * GET /api/chaptersへのリクエストを受け、Chapter一覧をJSONで返します。
	 */
	@GetMapping
	public List<ChapterResponse> findAll() {
		return chapterService.findAll();
	}
}

/** 学習状況と苦手特訓の記録を扱うAPIです。 */
@RestController
@RequestMapping("/api/learning-progress")
class LearningProgressController {

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
