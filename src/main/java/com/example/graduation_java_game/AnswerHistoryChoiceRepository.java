package com.example.graduation_java_game;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 回答で選ばれた選択肢を履歴に保存します。
 */
public interface AnswerHistoryChoiceRepository extends JpaRepository<AnswerHistoryChoice, Long> {
}
