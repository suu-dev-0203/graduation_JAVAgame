package com.example.graduation_java_game;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ブラウザからゲームを開始するHTTPリクエストを受け付けます。
 */
@RestController
@RequestMapping("/api/game-sessions")
public class GameSessionController {

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
