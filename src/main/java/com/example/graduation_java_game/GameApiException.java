package com.example.graduation_java_game;

import org.springframework.http.HttpStatus;

/**
 * APIで予想できる入力・状態エラーを、HTTPステータスとエラーコードで伝えます。
 */
public class GameApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;

	public GameApiException(HttpStatus status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	/** HTTPレスポンスに使うステータスを返します。 */
	public HttpStatus getStatus() {
		return status;
	}

	/** API利用者がエラー内容を判別するためのコードを返します。 */
	public String getCode() {
		return code;
	}
}
