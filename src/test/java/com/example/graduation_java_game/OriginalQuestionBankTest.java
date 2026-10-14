package com.example.graduation_java_game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class OriginalQuestionBankTest {

	@Test
	void createsSeventeenChaptersWithTwentyQuestionsAtEveryLevel() {
		assertEquals(17, OriginalQuestionBank.chapters().size());

		for (OriginalQuestionBank.ChapterSeed chapter : OriginalQuestionBank.chapters()) {
			assertEquals(5, chapter.concepts().size());
			assertEquals(20, OriginalQuestionBank.questionsFor(chapter, QuestionDifficulty.EASY).size());
			assertEquals(20, OriginalQuestionBank.questionsFor(chapter, QuestionDifficulty.ADVANCED).size());
			assertEquals(20, OriginalQuestionBank.questionsFor(chapter, QuestionDifficulty.BRONZE_STYLE).size());

			Set<String> allPrompts = new HashSet<>();
			for (QuestionDifficulty difficulty : QuestionDifficulty.values()) {
				for (OriginalQuestionBank.BankQuestion question
						: OriginalQuestionBank.questionsFor(chapter, difficulty)) {
					assertFalse(question.questionText().isBlank());
					assertEquals(3, question.choices().size());
					assertEquals(question.explanation(), question.choices().get(question.correctChoiceIndex()));
					assertFalse(question.explanation().isBlank());
					allPrompts.add(question.questionText());
				}
			}
			assertEquals(60, allPrompts.size(), chapter.name());
		}
	}
}
