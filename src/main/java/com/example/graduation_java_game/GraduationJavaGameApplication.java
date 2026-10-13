package com.example.graduation_java_game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Bootアプリ全体の起動地点です。
 */
@SpringBootApplication
public class GraduationJavaGameApplication {

	/**
	 * Spring Bootを起動し、Webアプリとデータベースの準備を始めます。
	 */
	public static void main(String[] args) {
		SpringApplication.run(GraduationJavaGameApplication.class, args);
	}

}
