package com.example.graduation_java_game;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.graduation_java_game.GameApiDtos.ChapterResponse;

/**
 * Chapterに関する処理をまとめるServiceです。
 */
@Service
public class ChapterService {

	/** DBからChapterを読み書きする窓口です。 */
	private final ChapterRepository chapterRepository;

	public ChapterService(ChapterRepository chapterRepository) {
		this.chapterRepository = chapterRepository;
	}

	/**
	 * DBのChapterをAPI用データに変換して一覧で返します。
	 * Streamのmapは、各ChapterからID・名前・説明だけを取り出します。
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
