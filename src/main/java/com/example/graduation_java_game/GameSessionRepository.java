package com.example.graduation_java_game;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ゲーム全体の記録をDBから検索・保存するための窓口です。
 */
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
	// ID検索や保存などの基本処理はJpaRepositoryが用意します。
	// ゲーム固有の検索が必要になったときに、ここへ検索メソッドを追加します。
}
