package com.example.graduation_java_game;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private ChoiceRepository choiceRepository;

	@Autowired
	private StageDefinitionRepository stageDefinitionRepository;

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

	@Test
	@Transactional
	void choicesAreSavedForAQuestionAndReturnedInDisplayOrder() {
		// 問題はChapterに属し、選択肢は問題に属する関係で保存します。
		Chapter chapter = chapterRepository.save(
				new Chapter("Java Bronze 基礎", "基本文法を学びます。"));
		Question question = questionRepository.save(new Question(
				chapter,
				QuestionType.SINGLE_A_E,
				"正しい選択肢を選んでください。",
				null,
				20,
				"短い解説です。",
				"詳しい解説です。"));

		// 保存順とは異なる順番で登録し、Repositoryが表示順で検索することを確かめます。
		choiceRepository.save(new Choice(question, "選択肢B", 2, false));
		choiceRepository.save(new Choice(question, "選択肢A", 1, true));

		var choices = choiceRepository.findByQuestion_QuestionIdOrderByChoiceOrder(
				question.getQuestionId());

		assertEquals(2, choices.size());
		assertEquals("選択肢A", choices.get(0).getChoiceText());
		assertEquals("選択肢B", choices.get(1).getChoiceText());
		assertTrue(choices.get(0).isCorrect());
	}

	@Test
	@Transactional
	void stageDefinitionIsFoundByChapterAndStageNumber() {
		// StageはChapterに属し、使うキャラクターなどの設定を持ちます。
		Chapter chapter = chapterRepository.save(
				new Chapter("Java Bronze 基礎", "基本文法を学びます。"));
		StageDefinition stage = stageDefinitionRepository.save(new StageDefinition(
				chapter,
				1,
				"SWORDSMAN",
				"JAVA_GUARD",
				"GRASSLAND",
				MiniGameType.TYPING_SAMURAI,
				5));

		// GameSession開始時に必要となるChapter内のStage番号で設定を検索します。
		StageDefinition foundStage = stageDefinitionRepository
				.findByChapter_ChapterIdAndStageNumber(chapter.getChapterId(), 1)
				.orElseThrow();

		assertEquals(stage.getStageId(), foundStage.getStageId());
		assertEquals("SWORDSMAN", foundStage.getPlayerCharacterKey());
		assertEquals("JAVA_GUARD", foundStage.getEnemyKey());
		assertEquals("GRASSLAND", foundStage.getFieldKey());
		assertEquals(MiniGameType.TYPING_SAMURAI, foundStage.getMiniGameType());
		assertEquals(5, foundStage.getMaxEnemyHp());
	}
}
