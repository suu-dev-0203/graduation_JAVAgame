package com.example.graduation_java_game;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * プレイヤーの問題回答を受け付けます。
 */
@RestController
@RequestMapping("/api/answers")
public class AnswerController {

	private final AnswerService answerService;

	public AnswerController(AnswerService answerService) {
		this.answerService = answerService;
	}

	@PostMapping
	public AnswerResponse submit(@RequestBody AnswerRequest request) {
		return answerService.submit(request);
	}
}
