package com.example.graduation_java_game;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * 回答内容と選んだ選択肢の履歴について、DB検索・保存を担当します。
 * publicを付けていないinterfaceは、このpackage内で使います。
 * Spring Data JPAはJpaRepositoryを継承したinterfaceを自動で登録します。
 */

/**
 * 回答履歴を保存し、回答済みかどうかを確認します。
 */
interface AnswerHistoryRepository extends JpaRepository<AnswerHistory, Long> {

	boolean existsByStageQuestion_StageQuestionId(Long stageQuestionId);

	boolean existsByStageQuestion_Question_QuestionId(Long questionId);

	Optional<AnswerHistory> findByStageQuestion_StageQuestionId(Long stageQuestionId);

	List<AnswerHistory> findByStageSession_StageSessionIdOrderByStageQuestion_QuestionOrder(
			Long stageSessionId);

	List<AnswerHistory> findByStageQuestion_Question_Chapter_ChapterId(Long chapterId);

	List<AnswerHistory> findByGameSession_GameSessionId(Long gameSessionId);
}

/**
 * 回答で選ばれた選択肢を履歴に保存します。
 */
interface AnswerHistoryChoiceRepository extends JpaRepository<AnswerHistoryChoice, Long> {

	List<AnswerHistoryChoice> findByAnswerHistory_AnswerHistoryId(Long answerHistoryId);
}
