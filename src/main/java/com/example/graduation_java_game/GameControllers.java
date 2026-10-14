package com.example.graduation_java_game;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.graduation_java_game.AnswerDtos.AnswerRequest;
import com.example.graduation_java_game.AnswerDtos.AnswerResponse;
import com.example.graduation_java_game.GameApiDtos.AdvanceStageResponse;
import com.example.graduation_java_game.GameApiDtos.CurrentGameResponse;
import com.example.graduation_java_game.GameApiDtos.GameResultResponse;
import com.example.graduation_java_game.GameApiDtos.GameSessionCreateRequest;
import com.example.graduation_java_game.GameApiDtos.GameSessionCreateResponse;
import com.example.graduation_java_game.GameApiDtos.GameSessionStatusResponse;
import com.example.graduation_java_game.GameApiDtos.ReviewResponse;
import com.example.graduation_java_game.GameApiDtos.StageResultResponse;
import com.example.graduation_java_game.GameApiDtos.StartStageQuestionResponse;

/*
 * ゲームAPIの受付を担当するControllerをまとめます。
 * publicを付けていないControllerは、このpackage内で使います。
 * Springは@RestControllerを付けたクラスを検出してAPIを登録します。
 */

/**
 * プレイヤーの問題回答を受け付けます。
 */
@RestController
@RequestMapping("/api/answers")
class AnswerController {

	private final AnswerService answerService;

	public AnswerController(AnswerService answerService) {
		this.answerService = answerService;
	}

	@PostMapping
	public AnswerResponse submit(@RequestBody AnswerRequest request) {
		return answerService.submit(request);
	}
}

/**
 * ブラウザからゲームを開始するHTTPリクエストを受け付けます。
 */
@RestController
@RequestMapping("/api/game-sessions")
class GameSessionController {

	private final GameSessionService gameSessionService;

	public GameSessionController(GameSessionService gameSessionService) {
		this.gameSessionService = gameSessionService;
	}

	/**
	 * Chapter IDと問題数を受け取り、最初のStageを含むゲームを作成します。
	 */
	@PostMapping
	public GameSessionCreateResponse create(@RequestBody GameSessionCreateRequest request) {
		return gameSessionService.create(request);
	}

	/**
	 * GET /api/game-sessions/{gameSessionId}/currentで現在の問題を取得します。
	 */
	@GetMapping("/{gameSessionId}/current")
	public CurrentGameResponse findCurrent(@PathVariable Long gameSessionId) {
		return gameSessionService.findCurrent(gameSessionId);
	}

	/**
	 * Stageをクリアしたときに、Stage単位の結果を返します。
	 */
	@GetMapping("/{gameSessionId}/stages/{stageNumber}/result")
	public StageResultResponse findStageResult(
			@PathVariable Long gameSessionId,
			@PathVariable int stageNumber) {
		return gameSessionService.findStageResult(gameSessionId, stageNumber);
	}

	/**
	 * ゲーム全体を完了したときに、Game Resultを返します。
	 */
	@GetMapping("/{gameSessionId}/result")
	public GameResultResponse findGameResult(@PathVariable Long gameSessionId) {
		return gameSessionService.findGameResult(gameSessionId);
	}

	/**
	 * 完了したゲームに含まれる回答履歴を返します。
	 */
	@GetMapping("/{gameSessionId}/review")
	public ReviewResponse findReview(@PathVariable Long gameSessionId) {
		return gameSessionService.findReview(gameSessionId);
	}

	/**
	 * 一時停止中のゲームをABANDONEDとして終了します。
	 */
	@PostMapping("/{gameSessionId}/abandon")
	public GameSessionStatusResponse abandon(@PathVariable Long gameSessionId) {
		return gameSessionService.abandon(gameSessionId);
	}

	/**
	 * Stage Clear後、次のStageが残っているゲームを進めます。
	 */
	@PostMapping("/{gameSessionId}/advance-stage")
	public AdvanceStageResponse advanceStage(@PathVariable Long gameSessionId) {
		return gameSessionService.advanceStage(gameSessionId);
	}
}

/**
 * Stage内の問題を開始するAPIを受け付けます。
 */
@RestController
@RequestMapping("/api/stage-questions")
class StageQuestionController {

	private final GameSessionService gameSessionService;

	public StageQuestionController(GameSessionService gameSessionService) {
		this.gameSessionService = gameSessionService;
	}

	/**
	 * 問題の開始時刻を記録し、サーバー計算の残り時間を返します。
	 */
	@PostMapping("/{stageQuestionId}/start")
	public StartStageQuestionResponse start(@PathVariable Long stageQuestionId) {
		return gameSessionService.startQuestion(stageQuestionId);
	}
}

/**
 * StageSessionのPauseとResumeを受け付けるAPIです。
 */
@RestController
@RequestMapping("/api/stage-sessions")
class StageSessionController {

	private final GameSessionService gameSessionService;

	public StageSessionController(GameSessionService gameSessionService) {
		this.gameSessionService = gameSessionService;
	}

	@PostMapping("/{stageSessionId}/pause")
	public CurrentGameResponse pause(@PathVariable Long stageSessionId) {
		return gameSessionService.pause(stageSessionId);
	}

	@PostMapping("/{stageSessionId}/resume")
	public CurrentGameResponse resume(@PathVariable Long stageSessionId) {
		return gameSessionService.resume(stageSessionId);
	}
}
