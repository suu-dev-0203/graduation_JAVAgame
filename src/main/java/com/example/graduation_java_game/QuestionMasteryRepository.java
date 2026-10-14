package com.example.graduation_java_game;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** 問題ごとの現在の分類を検索・保存します。 */
public interface QuestionMasteryRepository extends JpaRepository<QuestionMastery, Long> {

	Optional<QuestionMastery> findByQuestion_QuestionId(Long questionId);

	List<QuestionMastery> findByQuestion_Chapter_ChapterId(Long chapterId);
}
