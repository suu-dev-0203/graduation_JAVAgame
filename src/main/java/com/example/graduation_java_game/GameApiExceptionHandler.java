package com.example.graduation_java_game;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ゲームAPIで発生した想定内のエラーを、決められたJSON形式に変換します。
 */
@RestControllerAdvice
public class GameApiExceptionHandler {

	/**
	 * ゲーム処理で明示したエラーを、HTTPステータスと説明付きJSONで返します。
	 */
	@ExceptionHandler(GameApiException.class)
	public ResponseEntity<ErrorResponse> handleGameApiException(GameApiException exception) {
		ErrorResponse response = new ErrorResponse(exception.getCode(), exception.getMessage());
		return ResponseEntity.status(exception.getStatus()).body(response);
	}
}
