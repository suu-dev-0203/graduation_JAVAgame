package com.example.graduation_java_game;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * StageQuestionの開始時刻が初回だけ記録されることを確認します。
 */
class StageQuestionTest {

	/** 初めて開始すると時刻が記録され、2回目はその時刻を変更できないことを確認します。 */
	@Test
	void startRecordsTimeOnlyOnce() {
		StageQuestion stageQuestion = new StageQuestion(null, null, 1);
		Instant firstStartTime = Instant.parse("2026-10-13T01:00:00Z");
		Instant secondStartTime = Instant.parse("2026-10-13T01:01:00Z");

		assertNull(stageQuestion.getStartedAt());
		assertTrue(stageQuestion.start(firstStartTime));
		assertEquals(firstStartTime, stageQuestion.getStartedAt());

		assertFalse(stageQuestion.start(secondStartTime));
		assertEquals(firstStartTime, stageQuestion.getStartedAt());
	}

	/** 開始時刻がnullなら、問題開始に成功したように見せずエラーにすることを確認します。 */
	@Test
	void startRejectsMissingTime() {
		StageQuestion stageQuestion = new StageQuestion(null, null, 1);

		assertThrows(IllegalArgumentException.class, () -> stageQuestion.start(null));
		assertNull(stageQuestion.getStartedAt());
	}

	/** Pause中の時間を記録し、Resume後の累積Pause時間に加えることを確認します。 */
	@Test
	void resumeAddsPausedDurationWithoutResettingQuestionStart() {
		StageQuestion stageQuestion = new StageQuestion(null, null, 1);
		Instant startedAt = Instant.parse("2026-10-13T01:00:00Z");
		Instant pausedAt = startedAt.plusSeconds(5);
		Instant resumedAt = pausedAt.plusSeconds(15);

		assertTrue(stageQuestion.start(startedAt));
		stageQuestion.pause(pausedAt);
		assertEquals(pausedAt, stageQuestion.getPausedAt());
		assertEquals(15_000, stageQuestion.resume(resumedAt));
		assertNull(stageQuestion.getPausedAt());
		assertEquals(15_000, stageQuestion.getTotalPausedMs());
		assertEquals(startedAt, stageQuestion.getStartedAt());
	}
}
