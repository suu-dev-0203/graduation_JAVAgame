package com.example.graduation_java_game;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.graduation_java_game.GameApiDtos.ChapterResponse;

/**
 * Chapterに関する処理をまとめるServiceです。
 */
@Service
public class ChapterService {

	/** DBからChapterを読み書きする窓口です。 */
	private final ChapterRepository chapterRepository;
	private final QuestionRepository questionRepository;

	public ChapterService(ChapterRepository chapterRepository, QuestionRepository questionRepository) {
		this.chapterRepository = chapterRepository;
		this.questionRepository = questionRepository;
	}

	/**
	 * DBのChapterをAPI用データに変換し、難易度別の問題数と一緒に一覧で返します。
	 */
	public List<ChapterResponse> findAll() {
		return chapterRepository.findAll().stream()
				.map(chapter -> {
					List<Question> chapterQuestions = questionRepository
							.findByChapter_ChapterId(chapter.getChapterId());
					Map<QuestionDifficulty, Integer> questionCounts =
							new EnumMap<>(QuestionDifficulty.class);
					for (QuestionDifficulty difficulty : QuestionDifficulty.values()) {
						questionCounts.put(difficulty, 0);
					}
					for (Question question : chapterQuestions) {
						questionCounts.merge(question.getDifficulty(), 1, Integer::sum);
					}
					Map<QuestionSelectionMode, Integer> selectionModeCounts =
							new EnumMap<>(QuestionSelectionMode.class);
					for (QuestionSelectionMode mode : QuestionSelectionMode.availableModes()) {
						selectionModeCounts.put(
								mode,
								(int) chapterQuestions.stream().filter(mode::matches).count());
					}
					return new ChapterResponse(
							chapter.getChapterId(),
							chapter.getName(),
							chapter.getDescription(),
							Map.copyOf(questionCounts),
							Map.copyOf(selectionModeCounts));
				})
				.toList();
	}
}
