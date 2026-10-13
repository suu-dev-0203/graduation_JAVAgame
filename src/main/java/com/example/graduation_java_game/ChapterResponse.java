package com.example.graduation_java_game;

/**
 * Chapter一覧APIでブラウザに返すデータです。
 * Entityを直接返さず、画面に必要な項目だけを公開します。
 *
 * @param chapterId Chapterを特定するID
 * @param name 画面に表示するChapter名
 * @param description Chapterの説明文
 */
public record ChapterResponse(Long chapterId, String name, String description) {
}
