package com.example.graduation_java_game;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Stage設定をDBから検索・保存するための窓口です。
 */
public interface StageDefinitionRepository extends JpaRepository<StageDefinition, Long> {

	/**
	 * 指定したChapterの中から、指定番号のStage設定を探します。
	 */
	Optional<StageDefinition> findByChapter_ChapterIdAndStageNumber(Long chapterId, int stageNumber);
}
