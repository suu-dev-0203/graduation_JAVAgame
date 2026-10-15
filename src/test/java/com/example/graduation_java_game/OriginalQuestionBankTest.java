package com.example.graduation_java_game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class OriginalQuestionBankTest {

	@Test
	void createsEightChaptersAndTwoHundredSixtyDistinctFiveChoiceQuestions() {
		assertEquals(8, OriginalQuestionBank.chapters().size());

		Map<String, Integer> expectedQuestionCounts = Map.of(
				"第1章 Javaの基本とプログラムの流れ", 20,
				"第2章 変数・型・リテラル", 35,
				"第3章 演算子と条件分岐", 35,
				"第4章 繰り返し処理と配列", 35,
				"第5章 メソッドとクラスの基礎", 35,
				"第6章 オブジェクト指向の基礎", 35,
				"第7章 継承・ポリモフィズム・パッケージ", 30,
				"第8章 総合問題・実戦的な読解", 35);
		int totalQuestions = 0;
		Set<String> allQuestionTexts = new HashSet<>();
		Set<String> allQuestionIds = new HashSet<>();
		int[] correctAnswerCounts = new int[5];
		for (OriginalQuestionBank.ChapterSeed chapter : OriginalQuestionBank.chapters()) {
			assertFalse(chapter.name().isBlank());
			assertFalse(chapter.description().isBlank());
			assertEquals(expectedQuestionCounts.get(chapter.name()), chapter.questions().size(),
					chapter.name());
			totalQuestions += chapter.questions().size();
			Map<QuestionDifficulty, Integer> countByDifficulty =
					new EnumMap<>(QuestionDifficulty.class);
			for (QuestionDifficulty difficulty : QuestionDifficulty.values()) {
				countByDifficulty.put(difficulty, 0);
			}
			checkQuestions(
					chapter, countByDifficulty, allQuestionTexts, allQuestionIds, correctAnswerCounts);
			assertTrue(countByDifficulty.values().stream().allMatch(count -> count >= 5),
					chapter.name());
			Map<QuestionDifficulty, Integer> expectedDifficultyCounts =
					chapter.questions().size() == 20
							? Map.of(
									QuestionDifficulty.EASY, 7,
									QuestionDifficulty.ADVANCED, 7,
									QuestionDifficulty.BRONZE_STYLE, 6)
							: chapter.questions().size() == 30
									? Map.of(
											QuestionDifficulty.EASY, 10,
											QuestionDifficulty.ADVANCED, 10,
											QuestionDifficulty.BRONZE_STYLE, 10)
									: Map.of(
											QuestionDifficulty.EASY, 12,
											QuestionDifficulty.ADVANCED, 12,
											QuestionDifficulty.BRONZE_STYLE, 11);
			assertEquals(expectedDifficultyCounts, countByDifficulty, chapter.name());
		}
		assertEquals(260, totalQuestions);
		assertEquals(260, allQuestionTexts.size());
		assertEquals(260, allQuestionIds.size());
		for (int count : correctAnswerCounts) {
			assertTrue(count >= 40 && count <= 65, "正解位置の偏りが大きすぎます: " + count);
		}
	}

	private void checkQuestions(
			OriginalQuestionBank.ChapterSeed chapter,
			Map<QuestionDifficulty, Integer> countByDifficulty,
			Set<String> allQuestionTexts,
			Set<String> allQuestionIds,
			int[] correctAnswerCounts) {
		Set<String> prompts = new HashSet<>();
		for (OriginalQuestionBank.BankQuestion question : chapter.questions()) {
			assertFalse(question.questionText().isBlank());
			assertTrue(question.questionText().matches("(?s)\\[CH[1-8]-\\d{2,3}\\] .+"),
					question.questionText());
			String questionId = question.questionText().substring(1, question.questionText().indexOf(']'));
			assertTrue(allQuestionIds.add(questionId), questionId);
			assertTrue(prompts.add(question.questionText()), question.questionText());
			assertTrue(allQuestionTexts.add(question.questionText()), question.questionText());
			assertEquals(5, question.choices().size());
			assertEquals(5, new HashSet<>(question.choices()).size(), question.questionText());
			assertTrue(question.correctChoiceIndex() >= 0 && question.correctChoiceIndex() < 5);
			correctAnswerCounts[question.correctChoiceIndex()]++;
			assertFalse(question.explanation().isBlank());
			assertTrue(question.detailedExplanation().contains(
					"正解: " + (char) ('A' + question.correctChoiceIndex())));
			assertTrue(question.detailedExplanation().contains("各選択肢の解説:"));
			for (char option = 'A'; option <= 'E'; option++) {
				assertTrue(question.detailedExplanation().contains(option + ". "));
			}
			countByDifficulty.merge(question.difficulty(), 1, Integer::sum);
		}
	}
}
