package com.example.graduation_java_game;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Chapterに関する処理をまとめるServiceです。
 */
@Service
public class ChapterService {

	private final ChapterRepository chapterRepository;

	public ChapterService(ChapterRepository chapterRepository) {
		this.chapterRepository = chapterRepository;
	}

	/**
	 * DBのChapterを、APIで返す形式に変換して一覧で返します。
	 */
	public List<ChapterResponse> findAll() {
		return chapterRepository.findAll().stream()
				.map(chapter -> new ChapterResponse(
						chapter.getChapterId(),
						chapter.getName(),
						chapter.getDescription()))
				.toList();
	}
}
