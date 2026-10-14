package com.example.graduation_java_game;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Stage設定とStageに登録する問題のDB検索・保存を担当します。
 * publicを付けていないinterfaceは、このpackage内で使います。
 * Spring Data JPAはJpaRepositoryを継承したinterfaceを自動で登録します。
 */

/**
 * Stage設定をDBから検索・保存するための窓口です。
 */
interface StageDefinitionRepository extends JpaRepository<StageDefinition, Long> {

	/**
	 * 指定したChapterの中から、指定番号のStage設定を探します。
	 */
	Optional<StageDefinition> findByChapter_ChapterIdAndStageNumber(Long chapterId, int stageNumber);
}

/**
 * Stageで出題する問題を保存し、順番に取り出すRepositoryです。
 */
interface StageQuestionRepository extends JpaRepository<StageQuestion, Long> {

	/**
	 * 1つのStageに登録された問題を、出題順に取得します。
	 */
	List<StageQuestion> findByStageSession_StageSessionIdOrderByQuestionOrder(Long stageSessionId);

	List<StageQuestion> findByStageSession_GameSession_GameSessionId(Long gameSessionId);

	/**
	 * Stage内の指定された出題順にあたる問題を1件取得します。
	 */
	java.util.Optional<StageQuestion> findByStageSession_StageSessionIdAndQuestionOrder(
			Long stageSessionId,
			int questionOrder);
}
