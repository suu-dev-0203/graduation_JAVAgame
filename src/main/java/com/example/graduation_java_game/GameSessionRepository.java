package com.example.graduation_java_game;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ゲーム全体の記録をDBから検索・保存するための窓口です。
 */
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

	List<GameSession> findBySessionTypeAndStatusOrderByEndedAtDesc(
			GameSessionType sessionType,
			GameSessionStatus status);
}
