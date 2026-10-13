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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long chapterId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;

	protected Chapter() {
		// JPAがDBからChapterを読み込むときに使います。
	}

	public Chapter(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public Long getChapterId() {
		return chapterId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
