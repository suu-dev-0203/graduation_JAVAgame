package com.example.graduation_java_game;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 回答履歴を保存し、回答済みかどうかを確認します。
 */
public interface AnswerHistoryRepository extends JpaRepository<AnswerHistory, Long> {

	boolean existsByStageQuestion_StageQuestionId(Long stageQuestionId);

	Optional<AnswerHistory> findByStageQuestion_StageQuestionId(Long stageQuestionId);

	List<AnswerHistory> findByStageSession_StageSessionIdOrderByStageQuestion_QuestionOrder(
			Long stageSessionId);

	List<AnswerHistory> findByStageQuestion_Question_Chapter_ChapterId(Long chapterId);

	List<AnswerHistory> findByGameSession_GameSessionId(Long gameSessionId);
}
