package com.example.graduation_java_game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("demo")
class OriginalQuestionBankInitializerTest {

	@Autowired
	private ChapterRepository chapterRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private StageDefinitionRepository stageDefinitionRepository;

	@Test
	void demoProfileSeedsAllChaptersQuestionsAndStages() {
		assertEquals(17, chapterRepository.count());
		assertEquals(935, questionRepository.count());
		assertEquals(68, stageDefinitionRepository.count());

		for (Chapter chapter : chapterRepository.findAll()) {
			Map<QuestionDifficulty, Long> counts = questionRepository
					.findByChapter_ChapterId(chapter.getChapterId())
					.stream()
					.map(Question::getDifficulty)
					.collect(Collectors.groupingBy(
							Function.identity(),
							Collectors.counting()));
			assertEquals(15L, counts.get(QuestionDifficulty.EASY), chapter.getName());
			assertEquals(20L, counts.get(QuestionDifficulty.ADVANCED), chapter.getName());
			assertEquals(20L, counts.get(QuestionDifficulty.BRONZE_STYLE), chapter.getName());
		}
	}
}
