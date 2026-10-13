package com.example.graduation_java_game;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Stage設定をDBから検索・保存するためのRepositoryです。
 */
public interface StageDefinitionRepository extends JpaRepository<StageDefinition, Long> {

	Optional<StageDefinition> findByChapter_ChapterIdAndStageNumber(Long chapterId, int stageNumber);
}
