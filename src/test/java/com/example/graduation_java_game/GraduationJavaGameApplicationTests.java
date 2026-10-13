package com.example.graduation_java_game;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Bootの起動と、これまでに作ったデータ保存・APIを確認するテストです。
 */
@SpringBootTest
@AutoConfigureMockMvc
class GraduationJavaGameApplicationTests {

	/** テスト中にHTTPリクエストを模擬してAPIの応答を確認します。 */
	@Autowired
	private MockMvc mockMvc;

	/** Chapterをテスト用DBに保存・検索する窓口です。 */
	@Autowired
	private ChapterRepository chapterRepository;

	/** 問題をテスト用DBに保存するためのRepositoryです。 */
	@Autowired
	private QuestionRepository questionRepository;

	/** 選択肢をテスト用DBに保存するためのRepositoryです。 */
	@Autowired
	private ChoiceRepository choiceRepository;

	/** Stage設定をテスト用DBに保存するためのRepositoryです。 */
	@Autowired
	private StageDefinitionRepository stageDefinitionRepository;

	/** ゲーム全体の進行記録をテスト用DBに保存するRepositoryです。 */
	@Autowired
	private GameSessionRepository gameSessionRepository;

	/** Stageごとの進行記録をテスト用DBに保存するRepositoryです。 */
	@Autowired
	private StageSessionRepository stageSessionRepository;

	/** Stageで出題する問題と、その順番をテスト用DBから確認します。 */
	@Autowired
	private StageQuestionRepository stageQuestionRepository;

	/** 回答履歴が作成されたことを確認します。 */
	@Autowired
	private AnswerHistoryRepository answerHistoryRepository;

	/** 回答履歴に選択内容が記録されたことを確認します。 */
	@Autowired
	private AnswerHistoryChoiceRepository answerHistoryChoiceRepository;

	/** ゲーム開始処理で最初のゲームデータを準備します。 */
	@Autowired
	private GameSessionService gameSessionService;

	/** Spring Bootが必要な設定を読み込み、起動できることを確認します。 */
	@Test
	void contextLoads() {
	}

	/** トップページからブラウザー向けのゲーム画面を配信できることを確認します。 */
	@Test
	void homePageReturnsTheGameScreen() throws Exception {
		mockMvc.perform(get("/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("id=\"start-button\"")))
				.andExpect(content().string(containsString("id=\"game-panel\"")));
	}

	/** Chapter一覧APIがDBに登録した名前と説明を返すことを確認します。 */
	@Test
	@Transactional
	void getChaptersReturnsSavedChapters() throws Exception {
		// テスト用のChapterをDBに保存してから、一覧APIを呼び出します。
		chapterRepository.save(new Chapter("Java Bronze 基礎", "基本文法を学びます。"));

		mockMvc.perform(get("/api/chapters"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].chapterId").isNumber())
				.andExpect(jsonPath("$[0].name").value("Java Bronze 基礎"))
				.andExpect(jsonPath("$[0].description").value("基本文法を学びます。"));
	}

	/** 問題と選択肢を保存し、選択肢が表示順に取り出せることを確認します。 */
	@Test
	@Transactional
	void choicesAreSavedForAQuestionAndReturnedInDisplayOrder() {
		// 問題はChapterに属し、選択肢は問題に属する関係で保存します。
		Chapter chapter = chapterRepository.save(
				new Chapter("Java Bronze 基礎", "基本文法を学びます。"));
		Question question = questionRepository.save(new Question(
				chapter,
				QuestionType.SINGLE_A_E,
				"正しい選択肢を選んでください。",
				null,
				20,
				"短い解説です。",
				"詳しい解説です。"));

		// 保存順とは異なる順番で登録し、Repositoryが表示順で検索することを確かめます。
		choiceRepository.save(new Choice(question, "選択肢B", 2, false));
		choiceRepository.save(new Choice(question, "選択肢A", 1, true));

		var choices = choiceRepository.findByQuestion_QuestionIdOrderByChoiceOrder(
				question.getQuestionId());

		assertEquals(2, choices.size());
		assertEquals("選択肢A", choices.get(0).getChoiceText());
		assertEquals("選択肢B", choices.get(1).getChoiceText());
		assertTrue(choices.get(0).isCorrect());
	}

	/** Stage設定を保存し、ChapterとStage番号を使って検索できることを確認します。 */
	@Test
	@Transactional
	void stageDefinitionIsFoundByChapterAndStageNumber() {
		// StageはChapterに属し、使うキャラクターなどの設定を持ちます。
		Chapter chapter = chapterRepository.save(
				new Chapter("Java Bronze 基礎", "基本文法を学びます。"));
		StageDefinition stage = stageDefinitionRepository.save(new StageDefinition(
				chapter,
				1,
				"SWORDSMAN",
				"JAVA_GUARD",
				"GRASSLAND",
				MiniGameType.TYPING_SAMURAI,
				5));

		// GameSession開始時に必要となるChapter内のStage番号で設定を検索します。
		StageDefinition foundStage = stageDefinitionRepository
				.findByChapter_ChapterIdAndStageNumber(chapter.getChapterId(), 1)
				.orElseThrow();

		assertEquals(stage.getStageId(), foundStage.getStageId());
		assertEquals("SWORDSMAN", foundStage.getPlayerCharacterKey());
		assertEquals("JAVA_GUARD", foundStage.getEnemyKey());
		assertEquals("GRASSLAND", foundStage.getFieldKey());
		assertEquals(MiniGameType.TYPING_SAMURAI, foundStage.getMiniGameType());
		assertEquals(5, foundStage.getMaxEnemyHp());
	}

	/** ゲーム開始直後の記録を保存し、そのStageの初期状態を読み戻せることを確認します。 */
	@Test
	@Transactional
	void gameAndStageSessionsAreSavedWithTheirStartingState() {
		Chapter chapter = chapterRepository.save(
				new Chapter("Java Bronze 基礎", "基本文法を学びます。"));
		StageDefinition stage = stageDefinitionRepository.save(new StageDefinition(
				chapter,
				1,
				"SWORDSMAN",
				"JAVA_GUARD",
				"GRASSLAND",
				MiniGameType.TYPING_SAMURAI,
				5));
		Instant startedAt = Instant.parse("2026-10-13T01:00:00Z");

		GameSession gameSession = gameSessionRepository.save(new GameSession(
				chapter,
				5,
				1,
				1,
				0,
				GameSessionStatus.PLAYING,
				startedAt,
				null));
		stageSessionRepository.save(new StageSession(
				gameSession,
				stage,
				1,
				0,
				5,
				0,
				StageSessionStatus.PLAYING,
				startedAt,
				null,
				null,
				0));

		// 同じゲーム・同じStageが二重に作られていないことを検索できます。
		var stageSessions = stageSessionRepository
				.findByGameSession_GameSessionId(gameSession.getGameSessionId());
		assertEquals(1, stageSessions.size());
		var foundStageSession = stageSessions.get(0);
		assertTrue(stageSessionRepository
				.existsByGameSession_GameSessionIdAndStageDefinition_StageId(
						gameSession.getGameSessionId(),
						stage.getStageId()));
		assertEquals(GameSessionStatus.PLAYING, gameSession.getStatus());
		assertEquals(StageSessionStatus.PLAYING, foundStageSession.getStatus());
		assertEquals(1, foundStageSession.getCurrentQuestionIndex());
		assertEquals(0, foundStageSession.getCorrectCount());
		assertEquals(5, foundStageSession.getEnemyHp());
		assertEquals(0, foundStageSession.getCombo());
		assertEquals(0, foundStageSession.getTotalPausedMs());
	}

	/** 5・10・20問を受け付け、最初のStageの5問だけを作ることを確認します。 */
	@Test
	@Transactional
	void createGameSessionStartsFirstStageAndCreatesOnlyItsFiveQuestions() throws Exception {
		Chapter chapter = createChapterWithQuestions(20);
		for (int stageNumber = 1; stageNumber <= 4; stageNumber++) {
			createStageDefinition(chapter, stageNumber);
		}

		// どの選択肢でもゲームを始められ、まだ最初のStageだけが作られます。
		for (int questionCount : List.of(5, 10, 20)) {
			mockMvc.perform(post("/api/game-sessions")
					.contentType("application/json")
					.content("""
							{"chapterId": %d, "questionCount": %d}
							""".formatted(chapter.getChapterId(), questionCount)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.gameSessionId").isNumber())
					.andExpect(jsonPath("$.chapterId").value(chapter.getChapterId()))
					.andExpect(jsonPath("$.questionCount").value(questionCount))
					.andExpect(jsonPath("$.totalStageCount").value(questionCount / 5))
					.andExpect(jsonPath("$.currentStageNumber").value(1))
					.andExpect(jsonPath("$.status").value("PLAYING"));
		}

		var gameSessions = gameSessionRepository.findAll();
		assertEquals(3, gameSessions.size());
		for (GameSession gameSession : gameSessions) {
			var stageSessions = stageSessionRepository
					.findByGameSession_GameSessionId(gameSession.getGameSessionId());
			assertEquals(1, stageSessions.size());
			assertEquals(1, stageSessions.get(0).getStageDefinition().getStageNumber());
			assertEquals(5, stageQuestionRepository
					.findByStageSession_StageSessionIdOrderByQuestionOrder(
							stageSessions.get(0).getStageSessionId())
					.size());
		}
	}

	/** 5・10・20以外の問題数は受け付けず、ゲーム記録も作らないことを確認します。 */
	@Test
	@Transactional
	void createGameSessionRejectsUnsupportedQuestionCount() throws Exception {
		Chapter chapter = chapterRepository.save(
				new Chapter("Java Bronze 基礎", "基本文法を学びます。"));

		mockMvc.perform(post("/api/game-sessions")
				.contentType("application/json")
				.content("""
						{"chapterId": %d, "questionCount": 7}
						""".formatted(chapter.getChapterId())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

		assertEquals(0, gameSessionRepository.count());
	}

	/** 存在しないChapter IDを指定した場合は404を返すことを確認します。 */
	@Test
	@Transactional
	void createGameSessionRejectsUnknownChapter() throws Exception {
		mockMvc.perform(post("/api/game-sessions")
				.contentType("application/json")
				.content("""
						{"chapterId": 999, "questionCount": 5}
						"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));

		assertEquals(0, gameSessionRepository.count());
	}

	/** Chapterの問題が不足している場合は409を返し、部分的なゲーム記録を残さないことを確認します。 */
	@Test
	@Transactional
	void createGameSessionRejectsInsufficientQuestions() throws Exception {
		Chapter chapter = createChapterWithQuestions(4);
		createStageDefinition(chapter, 1);

		mockMvc.perform(post("/api/game-sessions")
				.contentType("application/json")
				.content("""
						{"chapterId": %d, "questionCount": 5}
						""".formatted(chapter.getChapterId())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_QUESTIONS"));

		assertEquals(0, gameSessionRepository.count());
		assertEquals(0, stageSessionRepository.count());
	}

	/** 必要なStage設定がないとき、ゲーム開始を拒否することを確認します。 */
	@Test
	@Transactional
	void createGameSessionRejectsMissingStageDefinition() throws Exception {
		Chapter chapter = createChapterWithQuestions(10);
		createStageDefinition(chapter, 1);

		mockMvc.perform(post("/api/game-sessions")
				.contentType("application/json")
				.content("""
						{"chapterId": %d, "questionCount": 10}
						""".formatted(chapter.getChapterId())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_GAME_STATE"));

		assertEquals(0, gameSessionRepository.count());
	}

	/** 現在の問題と選択肢を返し、Timer未開始と正解非公開を確認します。 */
	@Test
	@Transactional
	void getCurrentGameReturnsQuestionWithoutExposingCorrectAnswers() throws Exception {
		Chapter chapter = createChapterWithQuestions(5);
		createStageDefinition(chapter, 1);
		for (Question question : questionRepository.findByChapter_ChapterId(chapter.getChapterId())) {
			choiceRepository.save(new Choice(question, "不正解の選択肢", 2, false));
			choiceRepository.save(new Choice(question, "正解の選択肢", 1, true));
		}
		GameSessionCreateResponse startedGame = gameSessionService.create(
				new GameSessionCreateRequest(chapter.getChapterId(), 5));

		mockMvc.perform(get("/api/game-sessions/{id}/current", startedGame.gameSessionId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.gameSessionId").value(startedGame.gameSessionId()))
				.andExpect(jsonPath("$.stageSessionId").isNumber())
				.andExpect(jsonPath("$.stageQuestionId").isNumber())
				.andExpect(jsonPath("$.questionState").value("WAITING_TO_START"))
				.andExpect(jsonPath("$.questionNumber").value(1))
				.andExpect(jsonPath("$.totalQuestionsInStage").value(5))
				.andExpect(jsonPath("$.remainingTimeMs").doesNotExist())
				.andExpect(jsonPath("$.enemyHp").value(5))
				.andExpect(jsonPath("$.combo").value(0))
				.andExpect(jsonPath("$.score").value(0))
				.andExpect(jsonPath("$.gameSessionStatus").value("PLAYING"))
				.andExpect(jsonPath("$.stageSessionStatus").value("PLAYING"))
				.andExpect(jsonPath("$.question.questionId").isNumber())
				.andExpect(jsonPath("$.question.questionType").value("SINGLE_A_E"))
				.andExpect(jsonPath("$.question.questionText").isString())
				.andExpect(jsonPath("$.question.timeLimitSeconds").value(20))
				.andExpect(jsonPath("$.question.choices", hasSize(2)))
				.andExpect(jsonPath("$.question.choices[0].choiceText").value("正解の選択肢"))
				.andExpect(jsonPath("$.question.choices[0].choiceOrder").value(1))
				.andExpect(jsonPath("$.question.choices[0].correct").doesNotExist())
				.andExpect(jsonPath("$.playerCharacterKey").value("SWORDSMAN"))
				.andExpect(jsonPath("$.enemyKey").value("JAVA_GUARD"))
				.andExpect(jsonPath("$.fieldKey").value("GRASSLAND"))
				.andExpect(jsonPath("$.miniGameType").value("TYPING_SAMURAI"));
	}

	/** Start APIで開始時刻と残り時間を返し、同じ問題の再Startを拒否します。 */
	@Test
	@Transactional
	void startStageQuestionRecordsStartTimeAndRejectsRestart() throws Exception {
		Chapter chapter = createChapterWithQuestions(5);
		createStageDefinition(chapter, 1);
		GameSessionCreateResponse startedGame = gameSessionService.create(
				new GameSessionCreateRequest(chapter.getChapterId(), 5));
		CurrentGameResponse currentGame = gameSessionService.findCurrent(startedGame.gameSessionId());

		mockMvc.perform(post("/api/stage-questions/{id}/start", currentGame.stageQuestionId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stageQuestionId").value(currentGame.stageQuestionId()))
				.andExpect(jsonPath("$.startedAt").isString())
				.andExpect(jsonPath("$.remainingTimeMs").isNumber());

		mockMvc.perform(get("/api/game-sessions/{id}/current", startedGame.gameSessionId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.questionState").value("ANSWERING"))
				.andExpect(jsonPath("$.remainingTimeMs").isNumber());

		mockMvc.perform(post("/api/stage-questions/{id}/start", currentGame.stageQuestionId()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_GAME_STATE"));
	}

	/** 存在しないStageQuestion IDでStartした場合は404を返します。 */
	@Test
	void startStageQuestionRejectsUnknownId() throws Exception {
		mockMvc.perform(post("/api/stage-questions/{id}/start", 999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

	/** 正解回答を記録し、Score・Combo・次の問題を返します。 */
	@Test
	@Transactional
	void submitAnswerUpdatesGameAndReturnsNextQuestion() throws Exception {
		Chapter chapter = createChapterWithQuestions(5);
		createStageDefinition(chapter, 1);
		createChoicesForQuestions(chapter);
		GameSessionCreateResponse startedGame = gameSessionService.create(
				new GameSessionCreateRequest(chapter.getChapterId(), 5));
		CurrentGameResponse currentGame = gameSessionService.findCurrent(startedGame.gameSessionId());
		List<Choice> choices = choiceRepository.findByQuestion_QuestionIdOrderByChoiceOrder(
				currentGame.question().questionId());
		Long correctChoiceId = choices.stream()
				.filter(choice -> choice.isCorrect())
				.findFirst()
				.orElseThrow()
				.getChoiceId();

		mockMvc.perform(post("/api/stage-questions/{id}/start", currentGame.stageQuestionId()))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/answers")
				.contentType("application/json")
				.content("""
						{"stageQuestionId": %d, "selectedChoiceIds": [%d]}
						""".formatted(currentGame.stageQuestionId(), correctChoiceId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correct").value(true))
				.andExpect(jsonPath("$.timeout").value(false))
				.andExpect(jsonPath("$.enemyHp").value(4))
				.andExpect(jsonPath("$.combo").value(1))
				.andExpect(jsonPath("$.score").value(100))
				.andExpect(jsonPath("$.effectType").value("HIT"))
				.andExpect(jsonPath("$.stageCorrectCount").value(1))
				.andExpect(jsonPath("$.stageScore").value(100))
				.andExpect(jsonPath("$.maxCombo").value(1))
				.andExpect(jsonPath("$.stageClear").value(false))
				.andExpect(jsonPath("$.perfect").value(false))
				.andExpect(jsonPath("$.nextStageQuestionId").isNumber());

		mockMvc.perform(get("/api/game-sessions/{id}/current", startedGame.gameSessionId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.questionNumber").value(2))
				.andExpect(jsonPath("$.questionState").value("WAITING_TO_START"))
				.andExpect(jsonPath("$.score").value(100))
				.andExpect(jsonPath("$.enemyHp").value(4))
				.andExpect(jsonPath("$.combo").value(1));
		assertEquals(1, answerHistoryRepository.count());
		assertEquals(1, answerHistoryChoiceRepository.count());
	}

	/** 時間が残っている空回答は入力不正として扱い、問題を未回答のままにします。 */
	@Test
	@Transactional
	void submitAnswerRejectsEmptySelectionBeforeTimeout() throws Exception {
		Chapter chapter = createChapterWithQuestions(5);
		createStageDefinition(chapter, 1);
		createChoicesForQuestions(chapter);
		GameSessionCreateResponse startedGame = gameSessionService.create(
				new GameSessionCreateRequest(chapter.getChapterId(), 5));
		CurrentGameResponse currentGame = gameSessionService.findCurrent(startedGame.gameSessionId());
		mockMvc.perform(post("/api/stage-questions/{id}/start", currentGame.stageQuestionId()))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/answers")
				.contentType("application/json")
				.content("""
						{"stageQuestionId": %d, "selectedChoiceIds": []}
						""".formatted(currentGame.stageQuestionId())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_ANSWER_COUNT"));

		assertEquals(0, answerHistoryRepository.count());
	}

	/** 別問題の選択肢を送信した回答は受け付けません。 */
	@Test
	@Transactional
	void submitAnswerRejectsChoiceFromAnotherQuestion() throws Exception {
		Chapter chapter = createChapterWithQuestions(5);
		createStageDefinition(chapter, 1);
		List<Question> questions = questionRepository.findByChapter_ChapterId(chapter.getChapterId());
		createChoicesForQuestions(chapter);
		GameSessionCreateResponse startedGame = gameSessionService.create(
				new GameSessionCreateRequest(chapter.getChapterId(), 5));
		CurrentGameResponse currentGame = gameSessionService.findCurrent(startedGame.gameSessionId());
		Long unrelatedChoiceId = choiceRepository
				.findByQuestion_QuestionIdOrderByChoiceOrder(
						questions.stream()
								.filter(question -> !question.getQuestionId()
										.equals(currentGame.question().questionId()))
								.findFirst()
								.orElseThrow()
								.getQuestionId())
				.get(0)
				.getChoiceId();
		mockMvc.perform(post("/api/stage-questions/{id}/start", currentGame.stageQuestionId()))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/answers")
				.contentType("application/json")
				.content("""
						{"stageQuestionId": %d, "selectedChoiceIds": [%d]}
						""".formatted(currentGame.stageQuestionId(), unrelatedChoiceId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_ANSWER"));

		assertEquals(0, answerHistoryRepository.count());
	}

	/** 回答済みの問題に対する再送信は409を返します。 */
	@Test
	@Transactional
	void submitAnswerRejectsDuplicateSubmission() throws Exception {
		Chapter chapter = createChapterWithQuestions(5);
		createStageDefinition(chapter, 1);
		createChoicesForQuestions(chapter);
		GameSessionCreateResponse startedGame = gameSessionService.create(
				new GameSessionCreateRequest(chapter.getChapterId(), 5));
		CurrentGameResponse currentGame = gameSessionService.findCurrent(startedGame.gameSessionId());
		Long choiceId = choiceRepository
				.findByQuestion_QuestionIdOrderByChoiceOrder(currentGame.question().questionId())
				.get(0)
				.getChoiceId();
		String answerBody = """
				{"stageQuestionId": %d, "selectedChoiceIds": [%d]}
				""".formatted(currentGame.stageQuestionId(), choiceId);
		mockMvc.perform(post("/api/stage-questions/{id}/start", currentGame.stageQuestionId()))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/answers")
				.contentType("application/json")
				.content(answerBody))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/answers")
				.contentType("application/json")
				.content(answerBody))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ALREADY_ANSWERED"));

		assertEquals(1, answerHistoryRepository.count());
	}

	/** 制限時間を超えた回答はTIME UPとして処理し、正解選択肢でもHPを減らしません。 */
	@Test
	@Transactional
	void submitAnswerPrioritizesServerSideTimeout() throws Exception {
		Chapter chapter = createChapterWithQuestions(5);
		createStageDefinition(chapter, 1);
		createChoicesForQuestions(chapter);
		GameSessionCreateResponse startedGame = gameSessionService.create(
				new GameSessionCreateRequest(chapter.getChapterId(), 5));
		CurrentGameResponse currentGame = gameSessionService.findCurrent(startedGame.gameSessionId());
		StageQuestion stageQuestion = stageQuestionRepository
				.findById(currentGame.stageQuestionId())
				.orElseThrow();
		stageQuestion.start(Instant.now().minusSeconds(30));
		stageQuestionRepository.save(stageQuestion);
		Long correctChoiceId = choiceRepository
				.findByQuestion_QuestionIdOrderByChoiceOrder(currentGame.question().questionId())
				.stream()
				.filter(choice -> choice.isCorrect())
				.findFirst()
				.orElseThrow()
				.getChoiceId();

		mockMvc.perform(post("/api/answers")
				.contentType("application/json")
				.content("""
						{"stageQuestionId": %d, "selectedChoiceIds": [%d]}
						""".formatted(currentGame.stageQuestionId(), correctChoiceId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correct").value(false))
				.andExpect(jsonPath("$.timeout").value(true))
				.andExpect(jsonPath("$.enemyHp").value(5))
				.andExpect(jsonPath("$.combo").value(0))
				.andExpect(jsonPath("$.score").value(0))
				.andExpect(jsonPath("$.effectType").value("TIME_UP"));
	}

	/** 複数選択は必要数を満たした選択肢の集合が正解集合と一致した場合だけ正解です。 */
	@Test
	@Transactional
	void submitAnswerValidatesMultipleChoiceCountAndExactSet() throws Exception {
		Chapter chapter = chapterRepository.save(new Chapter("複数選択", "複数選択テスト"));
		createStageDefinition(chapter, 1);
		for (int index = 1; index <= 5; index++) {
			Question question = questionRepository.save(new Question(
					chapter,
					QuestionType.MULTIPLE_2_A_E,
					"複数選択問題 " + index,
					null,
					20,
					"短い解説です。",
					"詳しい解説です。"));
			choiceRepository.save(new Choice(question, "選択肢A", 1, true));
			choiceRepository.save(new Choice(question, "選択肢B", 2, false));
			choiceRepository.save(new Choice(question, "選択肢C", 3, true));
		}
		GameSessionCreateResponse startedGame = gameSessionService.create(
				new GameSessionCreateRequest(chapter.getChapterId(), 5));
		CurrentGameResponse currentGame = gameSessionService.findCurrent(startedGame.gameSessionId());
		List<Choice> choices = choiceRepository.findByQuestion_QuestionIdOrderByChoiceOrder(
				currentGame.question().questionId());
		Long choiceA = choices.get(0).getChoiceId();
		Long choiceC = choices.get(2).getChoiceId();
		mockMvc.perform(post("/api/stage-questions/{id}/start", currentGame.stageQuestionId()))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/answers")
				.contentType("application/json")
				.content("""
						{"stageQuestionId": %d, "selectedChoiceIds": [%d]}
						""".formatted(currentGame.stageQuestionId(), choiceA)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_ANSWER_COUNT"));

		mockMvc.perform(post("/api/answers")
				.contentType("application/json")
				.content("""
						{"stageQuestionId": %d, "selectedChoiceIds": [%d, %d]}
						""".formatted(currentGame.stageQuestionId(), choiceC, choiceA)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correct").value(true))
				.andExpect(jsonPath("$.score").value(100));
		assertEquals(2, answerHistoryChoiceRepository.count());
	}

	/** 5問すべて正解した場合にPERFECT・Stage Clear・ゲーム完了を返します。 */
	@Test
	@Transactional
	void completingFiveCorrectAnswersClearsStageAndGame() throws Exception {
		Chapter chapter = createChapterWithQuestions(5);
		createStageDefinition(chapter, 1);
		createChoicesForQuestions(chapter);
		GameSessionCreateResponse startedGame = gameSessionService.create(
				new GameSessionCreateRequest(chapter.getChapterId(), 5));

		for (int index = 0; index < 5; index++) {
			CurrentGameResponse currentGame = gameSessionService.findCurrent(startedGame.gameSessionId());
			Long correctChoiceId = choiceRepository
					.findByQuestion_QuestionIdOrderByChoiceOrder(currentGame.question().questionId())
					.stream()
					.filter(choice -> choice.isCorrect())
					.findFirst()
					.orElseThrow()
					.getChoiceId();
			mockMvc.perform(post("/api/stage-questions/{id}/start", currentGame.stageQuestionId()))
					.andExpect(status().isOk());
			var result = mockMvc.perform(post("/api/answers")
					.contentType("application/json")
					.content("""
							{"stageQuestionId": %d, "selectedChoiceIds": [%d]}
							""".formatted(currentGame.stageQuestionId(), correctChoiceId)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.correct").value(true))
					.andExpect(jsonPath("$.score").value((index + 1) * 100))
					.andExpect(jsonPath("$.maxCombo").value(index + 1));
			if (index == 4) {
				result.andExpect(jsonPath("$.effectType").value("PERFECT"))
						.andExpect(jsonPath("$.perfect").value(true))
						.andExpect(jsonPath("$.stageClear").value(true))
						.andExpect(jsonPath("$.stageCorrectCount").value(5))
						.andExpect(jsonPath("$.nextStageQuestionId").value(org.hamcrest.Matchers.nullValue()));
			} else {
				result.andExpect(jsonPath("$.stageClear").value(false))
						.andExpect(jsonPath("$.nextStageQuestionId").isNumber());
			}
		}

		assertEquals(GameSessionStatus.COMPLETED, gameSessionRepository
				.findById(startedGame.gameSessionId())
				.orElseThrow()
				.getStatus());
	}

	/** 存在しないゲームIDで現在状態を要求した場合、404を返すことを確認します。 */
	@Test
	@Transactional
	void getCurrentGameRejectsUnknownGameSession() throws Exception {
		mockMvc.perform(get("/api/game-sessions/{id}/current", 999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

	/** テスト用のChapterと、指定された個数の問題をDBに準備します。 */
	private Chapter createChapterWithQuestions(int questionCount) {
		Chapter chapter = chapterRepository.save(
				new Chapter("Java Bronze 基礎", "基本文法を学びます。"));
		for (int index = 1; index <= questionCount; index++) {
			questionRepository.save(new Question(
					chapter,
					QuestionType.SINGLE_A_E,
					"テスト問題 " + index,
					null,
					20,
					"短い解説です。",
					"詳しい解説です。"));
		}
		return chapter;
	}

	/** テスト用のChapterに、MVPルールに合ったStage設定を1件作ります。 */
	private void createStageDefinition(Chapter chapter, int stageNumber) {
		stageDefinitionRepository.save(new StageDefinition(
				chapter,
				stageNumber,
				"SWORDSMAN",
				"JAVA_GUARD",
				"GRASSLAND",
				MiniGameType.TYPING_SAMURAI,
				5));
	}

	/** 各問題に正解・不正解の選択肢を作ります。 */
	private void createChoicesForQuestions(Chapter chapter) {
		for (Question question : questionRepository.findByChapter_ChapterId(chapter.getChapterId())) {
			choiceRepository.save(new Choice(question, "不正解", 1, false));
			choiceRepository.save(new Choice(question, "正解", 2, true));
		}
	}
}
