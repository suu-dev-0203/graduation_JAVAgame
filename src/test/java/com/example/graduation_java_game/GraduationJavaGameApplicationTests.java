package com.example.graduation_java_game;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Bootの起動と、Chapter一覧APIの動作を確認するテストです。
 */
@SpringBootTest
@AutoConfigureMockMvc
class GraduationJavaGameApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ChapterRepository chapterRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void getChaptersReturnsSavedChapters() throws Exception {
		// テスト用のChapterをDBに保存してから、一覧APIを呼び出します。
		chapterRepository.save(new Chapter("Java Bronze 基礎", "基本文法を学びます。"));

		mockMvc.perform(get("/api/chapters"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].chapterId").isNumber())
				.andExpect(jsonPath("$[0].name").value("Java Bronze 基礎"))
				.andExpect(jsonPath("$[0].description").value("基本文法を学びます。"));
	}
}
