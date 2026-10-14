package com.example.graduation_java_game;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * GameSessionとStageSessionのDB検索・保存を担当します。
 * publicを付けていないinterfaceは、このpackage内で使います。
 * Spring Data JPAはJpaRepositoryを継承したinterfaceを自動で登録します。
 */

/**
 * ゲーム全体の記録をDBから検索・保存するための窓口です。
 */
interface GameSessionRepository extends JpaRepository<GameSession, Long> {

	List<GameSession> findBySessionTypeAndStatusOrderByEndedAtDesc(
			GameSessionType sessionType,
			GameSessionStatus status);
}

/**
 * Stageごとの進行記録をDBから検索・保存するための窓口です。
 */
interface StageSessionRepository extends JpaRepository<StageSession, Long> {

	/**
	 * 同じゲームで同じStageの記録がすでに作られているか調べます。
	 * DBにも一意制約がありますが、処理前に状態を確認するときにも使えます。
	 */
	boolean existsByGameSession_GameSessionIdAndStageDefinition_StageId(
			Long gameSessionId,
			Long stageId);

	/**
	 * 1回のゲームに含まれるStage記録を一覧で取得します。
	 */
	List<StageSession> findByGameSession_GameSessionId(Long gameSessionId);
}
