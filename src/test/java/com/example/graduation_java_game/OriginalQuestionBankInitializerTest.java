package com.example.graduation_java_game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
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

	@Autowired
	private ChoiceRepository choiceRepository;

	@Autowired
	private GameSessionService gameSessionService;

	@Test
	void demoProfileSeedsEightChaptersWithTwoHundredSixtyQuestionsAndThirtyTwoStages() {
		assertEquals(8, chapterRepository.count());
		assertEquals(260, questionRepository.count());
		assertEquals(32, stageDefinitionRepository.count());

		Map<String, Integer> expectedCounts = Map.of(
				"第1章 Javaの基本とプログラムの流れ", 20,
				"第2章 変数・型・リテラル", 35,
				"第3章 演算子と条件分岐", 35,
				"第4章 繰り返し処理と配列", 35,
				"第5章 メソッドとクラスの基礎", 35,
				"第6章 オブジェクト指向の基礎", 35,
				"第7章 継承・ポリモフィズム・パッケージ", 30,
				"第8章 総合問題・実戦的な読解", 35);
		for (Chapter chapter : chapterRepository.findAll()) {
			var chapterQuestions = questionRepository.findByChapter_ChapterId(chapter.getChapterId());
			assertEquals(expectedCounts.get(chapter.getName()), chapterQuestions.size(), chapter.getName());
			Map<QuestionDifficulty, Long> counts = chapterQuestions.stream()
					.map(Question::getDifficulty)
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			assertTrue(counts.values().stream().allMatch(count -> count >= 5), chapter.getName());
			for (Question question : chapterQuestions) {
				QuestionType expectedType = question.getCodeBlock() == null
						? QuestionType.SINGLE_A_E
						: QuestionType.CODE_SINGLE_A_E;
				assertEquals(expectedType, question.getQuestionType(), question.getQuestionText());
				var choices = choiceRepository.findByQuestion_QuestionIdOrderByChoiceOrder(
						question.getQuestionId());
				assertEquals(5, choices.size(), question.getQuestionText());
				assertEquals(1, choices.stream().filter(Choice::isCorrect).count(),
						question.getQuestionText());
			}
		}
	}

	@Test
	@Transactional
	void allSeededChaptersCanStartGamesAtEveryDifficulty() {
		for (Chapter chapter : chapterRepository.findAll()) {
			for (QuestionSelectionMode mode : List.of(
					QuestionSelectionMode.EASY,
					QuestionSelectionMode.ADVANCED,
					QuestionSelectionMode.BRONZE_STYLE)) {
				var game = gameSessionService.create(
						new GameApiDtos.GameSessionCreateRequest(chapter.getChapterId(), 5, mode));
				assertEquals(5, game.questionCount(), chapter.getName() + " / " + mode);
			}

			var randomGame = gameSessionService.create(
					new GameApiDtos.GameSessionCreateRequest(
							chapter.getChapterId(), 20, QuestionSelectionMode.RANDOM));
			assertEquals(20, randomGame.questionCount(), chapter.getName());
		}
	}
}
