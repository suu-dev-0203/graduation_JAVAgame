package com.example.graduation_java_game;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ブラウザからChapter一覧を取得するAPIの窓口です。
 */
@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

	private final ChapterService chapterService;

	public ChapterController(ChapterService chapterService) {
		this.chapterService = chapterService;
	}

	@GetMapping
	public List<ChapterResponse> findAll() {
		return chapterService.findAll();
	}
}
