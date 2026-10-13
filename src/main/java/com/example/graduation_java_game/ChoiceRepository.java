package com.example.graduation_java_game;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ChoiceをDBから検索・保存するための窓口です。
 */
public interface ChoiceRepository extends JpaRepository<Choice, Long> {

	/**
	 * 1つの問題の選択肢を、画面に表示する順番で取得します。
	 */
	List<Choice> findByQuestion_QuestionIdOrderByChoiceOrder(Long questionId);
}
