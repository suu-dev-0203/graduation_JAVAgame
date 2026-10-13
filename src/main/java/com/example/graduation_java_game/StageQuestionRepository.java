package com.example.graduation_java_game;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Stageで出題する問題を保存し、順番に取り出すRepositoryです。
 */
public interface StageQuestionRepository extends JpaRepository<StageQuestion, Long> {

	/**
	 * 1つのStageに登録された問題を、出題順に取得します。
	 */
	List<StageQuestion> findByStageSession_StageSessionIdOrderByQuestionOrder(Long stageSessionId);
}
