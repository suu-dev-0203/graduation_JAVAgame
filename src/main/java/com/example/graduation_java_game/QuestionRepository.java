package com.example.graduation_java_game;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * QuestionをDBから検索・保存するための窓口です。
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

	/**
	 * 指定したChapterに属する問題を取得します。
	 * ChapterのIDはQuestionが持つChapter関連をたどって検索します。
	 */
	List<Question> findByChapter_ChapterId(Long chapterId);
}
