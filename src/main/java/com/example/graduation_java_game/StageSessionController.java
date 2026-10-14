package com.example.graduation_java_game;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * StageSessionのPauseとResumeを受け付けるAPIです。
 */
@RestController
@RequestMapping("/api/stage-sessions")
public class StageSessionController {

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
