package com.example.graduation_java_game;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ChapterをDBから検索・保存するためのRepositoryです。
 */
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
}
