package com.example.graduation_java_game;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * demoプロファイルで17章分のオリジナル問題とStage設定を登録します。
 */
@Configuration
@Profile("demo")
public class OriginalQuestionBankInitializer {

	private static final List<String> STAGE_ENEMIES = List.of(
			"JAVA_GUARD", "BYTE_BEAST", "NULL_SLIME", "BRONZE_DRAGON");

	@Bean
	ApplicationRunner loadOriginalQuestionBank(
			ChapterRepository chapterRepository,
			QuestionRepository questionRepository,
			ChoiceRepository choiceRepository,
			StageDefinitionRepository stageDefinitionRepository) {
		return arguments -> {
			Map<String, Chapter> existingChapters = chapterRepository.findAll().stream()
					.collect(Collectors.toMap(
							chapter -> chapter.getName(),
							chapter -> chapter,
							(first, duplicate) -> first));
			for (OriginalQuestionBank.ChapterSeed chapterSeed : OriginalQuestionBank.chapters()) {
				Chapter chapter = existingChapters.computeIfAbsent(
						chapterSeed.name(),
						name -> chapterRepository.save(
								new Chapter(name, chapterSeed.description())));

				for (int stageNumber = 1; stageNumber <= 4; stageNumber++) {
					int currentStageNumber = stageNumber;
					if (stageDefinitionRepository
							.findByChapter_ChapterIdAndStageNumber(
									chapter.getChapterId(), currentStageNumber)
							.isEmpty()) {
						stageDefinitionRepository.save(new StageDefinition(
								chapter,
								currentStageNumber,
								"SWORDSMAN",
								STAGE_ENEMIES.get(currentStageNumber - 1),
								"GRASSLAND",
								MiniGameType.TYPING_SAMURAI,
								5));
					}
				}

				Map<String, Question> existingQuestions = questionRepository
						.findByChapter_ChapterId(chapter.getChapterId())
						.stream()
						.collect(Collectors.toMap(
								question -> question.getQuestionText(),
								question -> question,
								(first, duplicate) -> first));
				for (QuestionDifficulty difficulty : QuestionDifficulty.values()) {
					for (OriginalQuestionBank.BankQuestion bankQuestion
							: OriginalQuestionBank.questionsFor(chapterSeed, difficulty)) {
						if (existingQuestions.containsKey(bankQuestion.questionText())) {
							continue;
						}
						Question question = questionRepository.save(new Question(
								chapter,
								QuestionType.SINGLE_A_E,
								bankQuestion.difficulty(),
								bankQuestion.questionText(),
								null,
								bankQuestion.timeLimitSeconds(),
								bankQuestion.explanation(),
								bankQuestion.explanation()));
						existingQuestions.put(bankQuestion.questionText(), question);
						for (int index = 0; index < bankQuestion.choices().size(); index++) {
							choiceRepository.save(new Choice(
									question,
									bankQuestion.choices().get(index),
									index + 1,
									index == bankQuestion.correctChoiceIndex()));
						}
					}
				}
			}
		};
	}
}
