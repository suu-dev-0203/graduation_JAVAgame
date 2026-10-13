package com.example.graduation_java_game;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ChapterをDBから検索・保存するための窓口です。
 * findAllやsaveなどの基本処理はJpaRepositoryが用意します。
 */
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
}
