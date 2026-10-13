package com.example.graduation_java_game;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * QuestionをDBから検索・保存するためのRepositoryです。
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

	List<Question> findByChapter_ChapterId(Long chapterId);
}
