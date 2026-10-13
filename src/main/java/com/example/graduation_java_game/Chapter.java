package com.example.graduation_java_game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 学習テーマ（Chapter）をDBに保存するEntityです。
 */
@Entity
@Table(name = "chapters")
public class Chapter {

	/** DBが自動で割り当てるChapterのIDです。 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long chapterId;

	/** Chapterの画面表示名です。 */
	@Column(nullable = false)
	private String name;

	/** 何を学ぶChapterかを説明する文章です。 */
	@Column(nullable = false)
	private String description;

	protected Chapter() {
		// JPAがDBからChapterを読み込むときに使います。
	}

	/**
	 * 新しいChapterを作るときに名前と説明を設定します。
	 */
	public Chapter(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/** このChapterを識別するDB上のIDを返します。 */
	public Long getChapterId() {
		return chapterId;
	}

	/** 画面に表示するChapter名を返します。 */
	public String getName() {
		return name;
	}

	/** Chapterの説明文を返します。 */
	public String getDescription() {
		return description;
	}
}
