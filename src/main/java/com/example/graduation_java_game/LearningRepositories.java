package com.example.graduation_java_game;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Chapter、問題、選択肢、問題の分類のDB検索・保存を担当します。
 * publicを付けていないinterfaceは、このpackage内で使います。
 * Spring Data JPAはJpaRepositoryを継承したinterfaceを自動で登録します。
 */

/**
 * ChapterをDBから検索・保存するための窓口です。
 * findAllやsaveなどの基本処理はJpaRepositoryが用意します。
 */
interface ChapterRepository extends JpaRepository<Chapter, Long> {
}

/**
 * QuestionをDBから検索・保存するための窓口です。
 */
interface QuestionRepository extends JpaRepository<Question, Long> {

	/**
	 * 指定したChapterに属する問題を取得します。
	 * ChapterのIDはQuestionが持つChapter関連をたどって検索します。
	 */
	List<Question> findByChapter_ChapterId(Long chapterId);
}

/**
 * ChoiceをDBから検索・保存するための窓口です。
 */
interface ChoiceRepository extends JpaRepository<Choice, Long> {

	/**
	 * 1つの問題の選択肢を、画面に表示する順番で取得します。
	 */
	List<Choice> findByQuestion_QuestionIdOrderByChoiceOrder(Long questionId);
}

/** 問題ごとの現在の分類を検索・保存します。 */
interface QuestionMasteryRepository extends JpaRepository<QuestionMastery, Long> {

	Optional<QuestionMastery> findByQuestion_QuestionId(Long questionId);

	List<QuestionMastery> findByQuestion_Chapter_ChapterId(Long chapterId);
}
