package com.example.graduation_java_game;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stage内の問題を開始するAPIを受け付けます。
 */
@RestController
@RequestMapping("/api/stage-questions")
public class StageQuestionController {

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
