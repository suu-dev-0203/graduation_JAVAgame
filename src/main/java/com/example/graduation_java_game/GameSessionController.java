package com.example.graduation_java_game;

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
}
