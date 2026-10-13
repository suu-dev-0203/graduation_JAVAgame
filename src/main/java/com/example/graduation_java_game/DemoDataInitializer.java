package com.example.graduation_java_game;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 画面を試すための架空データをdemoプロファイルでだけ登録します。
 */
@Configuration
@Profile("demo")
public class DemoDataInitializer {

	private static final String DEMO_CHAPTER_NAME = "Java入門（デモ）";

	/**
	 * アプリ起動時にデモChapterがなければ、5問とStage 1を一度だけ作ります。
	 * 通常プロファイルではこの設定自体が読み込まれません。
	 */
	@Bean
	ApplicationRunner loadDemoData(
			ChapterRepository chapterRepository,
			QuestionRepository questionRepository,
			ChoiceRepository choiceRepository,
			StageDefinitionRepository stageDefinitionRepository) {
		return arguments -> {
			boolean demoChapterExists = chapterRepository.findAll().stream()
					.anyMatch(chapter -> DEMO_CHAPTER_NAME.equals(chapter.getName()));
			if (demoChapterExists) {
				return;
			}

			Chapter chapter = chapterRepository.save(new Chapter(
					DEMO_CHAPTER_NAME,
					"画面操作を試すためのサンプル問題です。"));
			stageDefinitionRepository.save(new StageDefinition(
					chapter,
					1,
					"SWORDSMAN",
					"JAVA_GUARD",
					"GRASSLAND",
					MiniGameType.TYPING_SAMURAI,
					5));

			List<DemoQuestion> demoQuestions = List.of(
					new DemoQuestion(
							"Javaで文字列を表す型はどれですか？",
							"文字列はString型で表します。",
							List.of("String", "int", "boolean"),
							0),
					new DemoQuestion(
							"整数を入れる変数の型はどれですか？",
							"int型は整数を扱います。",
							List.of("double", "int", "char"),
							1),
					new DemoQuestion(
							"条件が正しいときに実行する文を表すキーワードはどれですか？",
							"if文は条件によって処理を分けます。",
							List.of("for", "new", "if"),
							2),
					new DemoQuestion(
							"同じ処理を繰り返すときに使えるキーワードはどれですか？",
							"for文は処理を繰り返すときに使えます。",
							List.of("return", "for", "class"),
							1),
					new DemoQuestion(
							"真または偽を表す型はどれですか？",
							"boolean型はtrueまたはfalseを扱います。",
							List.of("String", "boolean", "long"),
							1));

			for (DemoQuestion demoQuestion : demoQuestions) {
				Question question = questionRepository.save(new Question(
						chapter,
						QuestionType.SINGLE_A_E,
						demoQuestion.text(),
						null,
						20,
						demoQuestion.explanation(),
						demoQuestion.explanation()));
				for (int index = 0; index < demoQuestion.choices().size(); index++) {
					choiceRepository.save(new Choice(
							question,
							demoQuestion.choices().get(index),
							index + 1,
							index == demoQuestion.correctChoiceIndex()));
				}
			}
		};
	}

	/**
	 * Javaの問題文、短い説明、選択肢、正解の位置をまとめるデモ専用データです。
	 */
	private record DemoQuestion(
			String text,
			String explanation,
			List<String> choices,
			int correctChoiceIndex) {
	}
}
