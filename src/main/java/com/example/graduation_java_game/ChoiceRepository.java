package com.example.graduation_java_game;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ChoiceをDBから検索・保存するためのRepositoryです。
 */
public interface ChoiceRepository extends JpaRepository<Choice, Long> {

	List<Choice> findByQuestion_QuestionIdOrderByChoiceOrder(Long questionId);
}
