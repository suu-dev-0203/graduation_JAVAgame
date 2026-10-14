const chapterSelect = document.querySelector("#chapter-select");
const selectionModeSelect = document.querySelector("#selection-mode");
const questionCountSelect = document.querySelector("#question-count");
const startButton = document.querySelector("#start-button");
const startPanel = document.querySelector(".start-panel");
const resumeSavedButton = document.querySelector("#resume-saved-button");
const startQuestionButton = document.querySelector("#start-question-button");
const nextQuestionButton = document.querySelector("#next-question-button");
const nextStageButton = document.querySelector("#next-stage-button");
const finishGameButton = document.querySelector("#finish-game-button");
const returnHomeButton = document.querySelector("#return-home-button");
const reviewButton = document.querySelector("#review-button");
const reviewHomeButton = document.querySelector("#review-home-button");
const backToGameResultButton = document.querySelector("#back-to-game-result-button");
const pauseButton = document.querySelector("#pause-button");
const resumeButton = document.querySelector("#resume-button");
const abandonButton = document.querySelector("#abandon-button");
const message = document.querySelector("#message");
const gamePanel = document.querySelector("#game-panel");
const stageResultPanel = document.querySelector("#stage-result-panel");
const gameResultPanel = document.querySelector("#game-result-panel");
const pausePanel = document.querySelector("#pause-panel");
const reviewPanel = document.querySelector("#review-panel");
const reviewItems = document.querySelector("#review-items");
const learningProgressPanel = document.querySelector("#learning-progress-panel");
const loadLearningProgressButton = document.querySelector("#load-learning-progress-button");
const learningProgressContent = document.querySelector("#learning-progress-content");
const chapterProgressList = document.querySelector("#chapter-progress-list");
const trainingRecordList = document.querySelector("#training-record-list");
const countdown = document.querySelector("#countdown");
const countdownValue = document.querySelector("#countdown-value");
const questionContent = document.querySelector("#question-content");
const choicesElement = document.querySelector("#choices");
const answerResult = document.querySelector("#answer-result");
const answerResultTitle = document.querySelector("#answer-result-title");
const answerResultDetails = document.querySelector("#answer-result-details");
const answerChoices = document.querySelector("#answer-choices");
const answerExplanation = document.querySelector("#answer-explanation");
const answerDetailedExplanationDetails = document.querySelector("#answer-detailed-explanation-details");
const answerDetailedExplanation = document.querySelector("#answer-detailed-explanation");
const timerStatus = document.querySelector("#timer-status");

let gameSessionId = null;
let currentGame = null;
let questionState = "WAITING_TO_START";
let selectedChoiceIds = new Set();
let timerInterval = null;
let timerDeadline = null;
let answerAdvanceTimeout = null;
let learningProgressLoaded = false;
let gameStartInProgress = false;

async function requestJson(url, options = {}) {
	const response = await fetch(url, options);
	const body = await response.json();

	if (!response.ok) {
		throw new Error(body.message || `通信に失敗しました（${response.status}）`);
	}

	return body;
}

async function loadChapters() {
	try {
		const chapters = await requestJson("/api/chapters");
		chapterSelect.replaceChildren();

		if (chapters.length === 0) {
			const emptyOption = document.createElement("option");
			emptyOption.textContent = "登録済みの学習テーマはありません";
			chapterSelect.append(emptyOption);
			message.textContent = "開発用サンプルを使う場合は、demoプロファイルでアプリを起動してください。";
			return;
		}

		for (const chapter of chapters) {
			const option = document.createElement("option");
			option.value = chapter.chapterId;
			option.textContent = chapter.name;
			chapterSelect.append(option);
		}

		chapterSelect.disabled = false;
		selectionModeSelect.disabled = false;
		questionCountSelect.disabled = false;
		startButton.disabled = false;
		message.textContent = "学習テーマを選んでゲームを始めてください。";
		updateQuestionCountOptions();
		await restoreSavedGame();
	} catch (error) {
		showError(error);
	}
}

async function startGame(sessionType = "STANDARD", chapterIdOverride = null) {
	if (gameStartInProgress) {
		return;
	}
	gameStartInProgress = true;
	const trainingButtons = chapterProgressList.querySelectorAll("button[data-training-chapter-id]");
	trainingButtons.forEach((button) => { button.disabled = true; });
	stopTimer();
	startButton.disabled = true;
	gamePanel.hidden = true;
	stageResultPanel.hidden = true;
	gameResultPanel.hidden = true;
	reviewPanel.hidden = true;
	message.classList.remove("error");
	message.textContent = "ゲームの準備をしています...";

	try {
		const game = await requestJson("/api/game-sessions", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({
				chapterId: Number(chapterIdOverride ?? chapterSelect.value),
				questionCount: sessionType === "WEAK_TRAINING" ? 5 : Number(questionCountSelect.value),
				selectionMode: sessionType === "WEAK_TRAINING" ? "RANDOM" : selectionModeSelect.value,
				sessionType
			})
		});
		gameSessionId = game.gameSessionId;
		localStorage.setItem("gameSessionId", String(gameSessionId));
		const current = await getCurrentGame();
		showQuestion(current);
		message.textContent = "「スタート」またはEnterキーで始められます。";
		startPanel.hidden = true;
		learningProgressPanel.hidden = true;
		gamePanel.scrollIntoView({ behavior: "smooth", block: "start" });
	} catch (error) {
		gameSessionId = null;
		showError(error);
	} finally {
		gameStartInProgress = false;
		startButton.disabled = chapterSelect.disabled;
		trainingButtons.forEach((button) => {
			button.disabled = Number(button.dataset.weakQuestionCount) === 0;
		});
	}
}

async function getCurrentGame() {
	return requestJson(`/api/game-sessions/${gameSessionId}/current`);
}

function showQuestion(current) {
	stopTimer();
	clearAnswerAdvanceTimeout();
	countdown.hidden = true;
	message.classList.remove("error");
	currentGame = current;
	questionState = current.questionState;
	selectedChoiceIds = new Set();
	questionContent.hidden = current.questionNumber === 1
			&& questionState === "WAITING_TO_START";
	startQuestionButton.textContent = "スタート";
	document.querySelector("#question-progress").textContent =
		`Stage ${current.currentStageNumber} / ${current.totalStageCount} · 問題 ${current.questionNumber} / ${current.totalQuestionsInStage}`;
	document.querySelector("#score-status").textContent = `スコア ${current.score}`;
	document.querySelector("#enemy-status").textContent = `敵HP ${current.enemyHp}`;
	document.querySelector("#combo-status").textContent = `Combo ${current.combo}`;
	document.querySelector("#question-type").textContent = difficultyLabel(current.difficulty);
	document.querySelector("#question-title").textContent = current.question.questionText;

	const codeBlock = document.querySelector("#code-block");
	codeBlock.hidden = !current.question.codeBlock;
	codeBlock.querySelector("code").textContent = current.question.codeBlock || "";

	choicesElement.replaceChildren();
	for (const choice of current.question.choices) {
		const item = document.createElement("li");
		const button = document.createElement("button");
		const letter = document.createElement("span");
		const text = document.createElement("span");
		button.className = "choice-button";
		button.type = "button";
		button.dataset.choiceId = choice.choiceId;
		button.setAttribute("aria-pressed", "false");
		letter.className = "choice-letter";
		letter.textContent = String.fromCharCode(64 + choice.choiceOrder);
		text.className = "choice-text";
		text.textContent = choice.choiceText;
		button.append(letter, text);
		item.append(button);
		choicesElement.append(item);
	}

	document.querySelector("#short-explanation").textContent =
		current.question.shortExplanation;
	document.querySelector(".explanation").open = false;
	document.querySelector(".explanation").hidden = true;
	answerResult.hidden = true;
	nextQuestionButton.hidden = true;
	startQuestionButton.hidden = questionState !== "WAITING_TO_START"
			|| current.questionNumber !== 1;
	startQuestionButton.disabled = questionState !== "WAITING_TO_START";
	timerStatus.textContent = "開始前";
	gamePanel.hidden = false;
	learningProgressPanel.hidden = true;
	updateChoiceControls();

	pauseButton.hidden = questionState !== "ANSWERING"
			|| current.gameSessionStatus !== "PLAYING"
			|| current.stageSessionStatus !== "PLAYING";
	if (questionState === "ANSWERING"
			&& current.gameSessionStatus === "PLAYING"
			&& current.stageSessionStatus === "PLAYING"
			&& current.remainingTimeMs != null) {
		startTimer(current.remainingTimeMs);
	} else if (current.gameSessionStatus === "PAUSED") {
		timerStatus.textContent = "一時停止中";
	}
	if (questionState === "RESULT" && current.answerResult) {
		showAnswerResult(current.answerResult);
	} else if (questionState === "WAITING_TO_START" && current.questionNumber > 1) {
		window.setTimeout(() => startCurrentQuestion(), 0);
	}
	if (questionState !== "RESULT") {
		if (questionContent.hidden) {
			startQuestionButton.focus({ preventScroll: true });
		} else {
			document.querySelector("#question-title").focus({ preventScroll: true });
		}
	}
}

async function startCurrentQuestion() {
	if (questionState !== "WAITING_TO_START" || !currentGame) {
		return;
	}

	questionState = "STARTING";
	startQuestionButton.disabled = true;
	startQuestionButton.hidden = true;
	message.classList.remove("error");
	message.textContent = currentGame.questionNumber === 1
			? "3・2・1のカウントダウンを始めます..."
			: "次の問題を開始しています...";
	updateChoiceControls();

	try {
		if (currentGame.questionNumber === 1) {
			await runStageCountdown();
		}
		const started = await requestJson(
			`/api/stage-questions/${currentGame.stageQuestionId}/start`,
			{ method: "POST" }
		);
		questionState = "ANSWERING";
		startQuestionButton.hidden = true;
		pauseButton.hidden = false;
		message.textContent = "回答してください。";
		startTimer(started.remainingTimeMs);
		updateChoiceControls();
	} catch (error) {
		countdown.hidden = true;
		questionState = "WAITING_TO_START";
		startQuestionButton.hidden = false;
		questionContent.hidden = currentGame.questionNumber === 1;
		startQuestionButton.textContent = "もう一度試す";
		showError(error);
		updateChoiceControls();
	} finally {
		startQuestionButton.disabled = false;
	}
}

async function runStageCountdown() {
	countdown.hidden = false;
	for (const value of ["3", "2", "1"]) {
		countdownValue.textContent = value;
		await delay(1000);
	}
	countdownValue.textContent = "スタート";
	questionContent.hidden = false;
	document.querySelector("#question-title").focus({ preventScroll: true });
	await delay(300);
	countdown.hidden = true;
}

function delay(milliseconds) {
	return new Promise((resolve) => window.setTimeout(resolve, milliseconds));
}

function clearAnswerAdvanceTimeout() {
	if (answerAdvanceTimeout !== null) {
		window.clearTimeout(answerAdvanceTimeout);
	}
	answerAdvanceTimeout = null;
}

function startTimer(remainingTimeMs) {
	stopTimer();
	timerDeadline = performance.now() + remainingTimeMs;
	updateTimer();
	timerInterval = window.setInterval(updateTimer, 100);
}

function updateTimer() {
	if (questionState !== "ANSWERING" || timerDeadline == null) {
		return;
	}

	const remainingTimeMs = Math.max(0, timerDeadline - performance.now());
	timerStatus.textContent = `残り ${Math.ceil(remainingTimeMs / 1000)} 秒`;
	if (remainingTimeMs === 0) {
		stopTimer();
		submitAnswer([]);
	}
}

function stopTimer() {
	if (timerInterval !== null) {
		window.clearInterval(timerInterval);
	}
	timerInterval = null;
	timerDeadline = null;
}

function requiredChoiceCount() {
	const type = currentGame.question.questionType;
	if (type.startsWith("MULTIPLE_3_")) {
		return 3;
	}
	return type.includes("MULTIPLE_2_") ? 2 : 1;
}

function selectChoice(choiceId) {
	if (questionState !== "ANSWERING") {
		return;
	}

	if (requiredChoiceCount() === 1) {
		submitAnswer([choiceId]);
		return;
	}

	if (selectedChoiceIds.has(choiceId)) {
		selectedChoiceIds.delete(choiceId);
	} else if (selectedChoiceIds.size < requiredChoiceCount()) {
		selectedChoiceIds.add(choiceId);
	} else {
		message.textContent = `回答は${requiredChoiceCount()}つ選択してください。`;
		return;
	}
	updateChoiceControls();
	message.textContent = `${selectedChoiceIds.size} / ${requiredChoiceCount()} 個選択中です。Enterで回答を確定します。`;
}

function updateChoiceControls() {
	for (const button of choicesElement.querySelectorAll("button[data-choice-id]")) {
		const selected = selectedChoiceIds.has(Number(button.dataset.choiceId));
		button.disabled = questionState !== "ANSWERING";
		button.classList.toggle("selected", selected);
		button.setAttribute("aria-pressed", String(selected));
	}
}

async function submitAnswer(choiceIds) {
	if (questionState !== "ANSWERING" || !currentGame) {
		return;
	}

	questionState = "SUBMITTING";
	stopTimer();
	updateChoiceControls();
	startQuestionButton.disabled = true;
	message.classList.remove("error");
	message.textContent = "回答を判定しています...";

	try {
		const result = await requestJson("/api/answers", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({
				stageQuestionId: currentGame.stageQuestionId,
				selectedChoiceIds: choiceIds
			})
		});
		showAnswerResult(result);
	} catch (error) {
		showError(error);
		try {
			const latest = await getCurrentGame();
			if (latest.stageQuestionId !== currentGame.stageQuestionId) {
				showQuestion(latest);
			} else if (latest.questionState === "ANSWERING") {
				questionState = "ANSWERING";
				updateChoiceControls();
				startTimer(latest.remainingTimeMs);
			}
		} catch (syncError) {
			showError(syncError);
		}
	}
}

function showAnswerResult(result) {
	questionState = "RESULT";
	message.classList.remove("error");
	timerStatus.textContent = result.timeout ? "時間切れ" : "回答済み";
	updateChoiceControls();
	document.querySelector("#score-status").textContent = `スコア ${result.score}`;
	document.querySelector("#enemy-status").textContent = `敵HP ${result.enemyHp}`;
	document.querySelector("#combo-status").textContent = `Combo ${result.combo}`;
	answerResultTitle.textContent = result.timeout
		? "時間切れ"
		: result.correct ? "正解！" : "不正解";
	answerResultTitle.className = result.correct ? "correct-result" : "incorrect-result";
	answerResultDetails.textContent =
		`敵HP ${result.enemyHp} · Combo ${result.combo} · Score ${result.score} · ${result.effectType}`;
	answerChoices.replaceChildren();
	for (const choice of result.choices || []) {
		const item = document.createElement("li");
		const labels = [
			choice.selected ? "あなたの回答" : "",
			choice.correct ? "正解" : ""
		].filter(Boolean);
		item.className = [
			choice.correct ? "correct" : "",
			choice.selected ? "selected" : ""
		].filter(Boolean).join(" ");
		item.textContent = `${String.fromCharCode(64 + choice.choiceOrder)}. ${choice.choiceText}`
				+ (labels.length ? `（${labels.join("・")}）` : "");
		answerChoices.append(item);
	}
	answerExplanation.textContent = result.shortExplanation || currentGame.question.shortExplanation;
	answerDetailedExplanation.textContent = result.detailedExplanation || "詳しい解説はありません。";
	answerDetailedExplanationDetails.open = false;
	answerResult.hidden = false;
	document.querySelector(".explanation").hidden = true;
	startQuestionButton.hidden = true;
	pauseButton.hidden = true;

	if (result.stageClear) {
		answerResultTitle.textContent = result.perfect ? "PERFECT！ Stage Clear" : "Stage Clear";
		nextQuestionButton.hidden = false;
		nextQuestionButton.textContent = "Stage結果を見る（自動で表示します）";
		message.textContent = "Stage結果を表示しています。詳しい解説を読む場合は、下の項目を開いてください。";
		answerDetailedExplanationDetails.querySelector("summary").focus();
	} else {
		nextQuestionButton.hidden = false;
		nextQuestionButton.textContent = "次の問題へ（自動で進みます）";
		message.textContent = "回答結果を確認してください。詳しい解説を開くと自動進行を停止します。";
		answerDetailedExplanationDetails.querySelector("summary").focus();
	}
	clearAnswerAdvanceTimeout();
	scheduleAnswerAdvance();
}

function scheduleAnswerAdvance() {
	clearAnswerAdvanceTimeout();
	if (questionState !== "RESULT" || answerDetailedExplanationDetails.open) {
		return;
	}
	answerAdvanceTimeout = window.setTimeout(() => {
		answerAdvanceTimeout = null;
		showNextQuestion();
	}, 2000);
}

function updateAnswerExplanationPause() {
	if (questionState !== "RESULT") {
		return;
	}
	if (answerDetailedExplanationDetails.open) {
		clearAnswerAdvanceTimeout();
		message.textContent = "詳しい解説を表示中です。閉じると2秒後に次へ進みます。";
		nextQuestionButton.textContent = currentGame.questionNumber === currentGame.totalQuestionsInStage
			? "Stage結果へ進む"
			: "次の問題へ進む";
		return;
	}
	message.textContent = "解説を閉じました。2秒後に次へ進みます。";
	nextQuestionButton.textContent = currentGame.questionNumber === currentGame.totalQuestionsInStage
		? "Stage結果を見る（自動で表示します）"
		: "次の問題へ（自動で進みます）";
	scheduleAnswerAdvance();
}

async function showNextQuestion() {
	if (questionState !== "RESULT" || !currentGame || nextQuestionButton.disabled) {
		return;
	}
	clearAnswerAdvanceTimeout();
	nextQuestionButton.disabled = true;
	try {
		if (currentGame.questionNumber === currentGame.totalQuestionsInStage) {
			await showStageResult();
			return;
		}
		const next = await getCurrentGame();
		showQuestion(next);
		message.textContent = "次の問題を自動で開始します。";
	} catch (error) {
		showError(error);
	} finally {
		nextQuestionButton.disabled = false;
	}
}

async function showStageResult() {
	const result = await requestJson(
		`/api/game-sessions/${gameSessionId}/stages/${currentGame.currentStageNumber}/result`
	);
	document.querySelector("#stage-result-title").textContent = result.perfect
		? `Stage ${result.stageNumber} PERFECT！`
		: `Stage ${result.stageNumber} 結果`;
	document.querySelector("#stage-result-summary").textContent =
		`正解 ${result.correctCount} / 5問 · Stageスコア ${result.stageScore}`;
	document.querySelector("#stage-result-combo").textContent =
		`最大Combo ${result.maxCombo}`;
	nextStageButton.hidden = !result.hasNextStage;
	finishGameButton.hidden = !result.gameCompleted;
	gamePanel.hidden = true;
	stageResultPanel.hidden = false;
	stageResultPanel.scrollIntoView({ behavior: "smooth", block: "start" });
	document.querySelector("#stage-result-title").focus({ preventScroll: true });
	message.textContent = result.gameCompleted
		? "最終Stageの結果です。Game Resultへ進んでください。"
		: "Stage結果を確認し、次のStageへ進んでください。";
}

async function showGameResult() {
	finishGameButton.disabled = true;
	try {
		const result = await requestJson(`/api/game-sessions/${gameSessionId}/result`);
		const isWeakTraining = result.sessionType === "WEAK_TRAINING";
		document.querySelector("#game-result-title").textContent = isWeakTraining
			? "苦手特訓の結果"
			: "ゲームクリア";
		document.querySelector("#game-result-summary").textContent =
			`${result.questionCount}問中 ${result.totalCorrectCount}問正解 · 合計スコア ${result.totalScore}`;
		document.querySelector("#game-result-perfect").textContent =
			`クリアしたStage ${result.completedStageCount} / ${result.totalStageCount} · PERFECT ${result.perfectStageCount}回`;
		stageResultPanel.hidden = true;
		gameResultPanel.hidden = false;
		reviewPanel.hidden = true;
		gameResultPanel.scrollIntoView({ behavior: "smooth", block: "start" });
		message.textContent = isWeakTraining
			? "苦手特訓を完了しました。結果は学習状況に保存されています。"
			: "ゲーム全体の結果です。おつかれさまでした。";
	} catch (error) {
		showError(error);
	} finally {
		finishGameButton.disabled = false;
	}
}

async function startNextStage() {
	nextStageButton.disabled = true;
	try {
		await requestJson(
			`/api/game-sessions/${gameSessionId}/advance-stage`,
			{ method: "POST" }
		);
		const next = await getCurrentGame();
		stageResultPanel.hidden = true;
		showQuestion(next);
		message.textContent = "次のStageです。「スタート」またはEnterキーで始められます。";
		gamePanel.scrollIntoView({ behavior: "smooth", block: "start" });
	} catch (error) {
		showError(error);
	} finally {
		nextStageButton.disabled = false;
	}
}

function returnToHome() {
	gameSessionId = null;
	localStorage.removeItem("gameSessionId");
	currentGame = null;
	questionState = "WAITING_TO_START";
	gameResultPanel.hidden = true;
	reviewPanel.hidden = true;
	stageResultPanel.hidden = true;
	pausePanel.hidden = true;
	gamePanel.hidden = true;
	startPanel.hidden = false;
	learningProgressPanel.hidden = false;
	message.textContent = "学習テーマを選んでゲームを始めてください。";
	if (learningProgressLoaded) {
		loadLearningProgress();
	}
	startPanel.scrollIntoView({ behavior: "smooth", block: "start" });
}

async function showReview() {
	reviewButton.disabled = true;
	try {
		const review = await requestJson(`/api/game-sessions/${gameSessionId}/review`);
		reviewItems.replaceChildren();
		for (const item of review.items) {
			const card = document.createElement("article");
			const heading = document.createElement("h3");
			const question = document.createElement("p");
			const outcome = document.createElement("p");
			const answerTime = document.createElement("p");
			const choices = document.createElement("ul");
			const explanation = document.createElement("p");
			const masteryLabel = document.createElement("label");
			const masterySelect = document.createElement("select");
			card.className = "review-item";
			card.dataset.questionId = item.questionId;
			heading.textContent = `Stage ${item.stageNumber} · 問題 ${item.questionNumber}`;
			question.className = "review-question";
			question.textContent = item.questionText;
			outcome.className = "review-answer";
			outcome.textContent = item.timeout
				? "結果：時間切れ"
				: item.correct ? "結果：正解" : "結果：不正解";
			answerTime.textContent = `回答時間：${(item.answerTimeMs / 1000).toFixed(1)}秒`;
			choices.className = "review-choices";
			for (const choice of item.choices) {
				const entry = document.createElement("li");
				const labels = [
					choice.selected ? "あなたの回答" : "",
					choice.correct ? "正解" : ""
				].filter(Boolean);
				entry.className = [
					choice.correct ? "correct" : "",
					choice.selected ? "selected" : ""
				].filter(Boolean).join(" ");
				entry.textContent =
					`${String.fromCharCode(64 + choice.choiceOrder)}. ${choice.choiceText}`
					+ (labels.length ? `（${labels.join("・")}）` : "");
				choices.append(entry);
			}
			explanation.className = "review-explanation";
			explanation.textContent = `詳しい解説：${item.detailedExplanation}`;
			masteryLabel.className = "mastery-control";
			masteryLabel.textContent = "この問題の分類：";
			masterySelect.className = "mastery-select";
			masterySelect.dataset.masteryQuestionId = item.questionId;
			for (const [value, label] of Object.entries(MASTERY_STATUS_LABELS)) {
				const option = document.createElement("option");
				option.value = value;
				option.textContent = label;
				masterySelect.append(option);
			}
			masterySelect.value = item.masteryStatus;
			masterySelect.dataset.previousStatus = item.masteryStatus;
			masteryLabel.append(masterySelect);
			card.append(heading, question, outcome, answerTime, choices, explanation, masteryLabel);
			reviewItems.append(card);
		}
		gameResultPanel.hidden = true;
		reviewPanel.hidden = false;
		reviewPanel.scrollIntoView({ behavior: "smooth", block: "start" });
		message.textContent = `${review.items.length}問の解答履歴を表示しています。`;
	} catch (error) {
		showError(error);
	} finally {
		reviewButton.disabled = false;
	}
}

const MASTERY_STATUS_LABELS = {
	OK: "OK",
	ALMOST: "あと一歩",
	WEAK: "苦手",
	VERY_WEAK: "超苦手"
};

async function updateMasteryStatus(questionId, status, selectElement) {
	const previousStatus = selectElement.dataset.previousStatus || selectElement.value;
	selectElement.disabled = true;
	try {
		const result = await requestJson(`/api/learning-progress/questions/${questionId}`, {
			method: "PUT",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ status })
		});
		selectElement.value = result.status;
		selectElement.dataset.previousStatus = result.status;
		message.textContent = `問題の分類を「${MASTERY_STATUS_LABELS[result.status]}」に変更しました。`;
		if (learningProgressLoaded) {
			await loadLearningProgress();
		}
	} catch (error) {
		selectElement.value = previousStatus;
		showError(error);
	} finally {
		selectElement.disabled = false;
	}
}

async function loadLearningProgress() {
	loadLearningProgressButton.disabled = true;
	try {
		const [progress, records] = await Promise.all([
			requestJson("/api/learning-progress/chapters"),
			requestJson("/api/learning-progress/training-records")
		]);
		renderChapterProgress(progress.chapters);
		renderTrainingRecords(records.items);
		learningProgressLoaded = true;
		learningProgressContent.hidden = false;
		loadLearningProgressButton.textContent = "学習状況を更新";
		message.textContent = "学習状況と苦手特訓の記録を読み込みました。";
	} catch (error) {
		showError(error);
	} finally {
		loadLearningProgressButton.disabled = false;
	}
}

function renderChapterProgress(chapters) {
	chapterProgressList.replaceChildren();
	if (chapters.length === 0) {
		chapterProgressList.textContent = "学習テーマが登録されていません。";
		return;
	}

	for (const chapter of chapters) {
		const card = document.createElement("article");
		const title = document.createElement("h4");
		const counts = document.createElement("p");
		const weakList = document.createElement("ul");
		const trainButton = document.createElement("button");
		const weakCount = chapter.weakCount + chapter.veryWeakCount;
		card.className = "chapter-progress-card";
		title.textContent = chapter.chapterName;
		counts.className = "mastery-counts";
		counts.textContent = `未出題 ${chapter.unseenCount}問 · OK ${chapter.okCount}問 · あと一歩 ${chapter.almostCount}問 · 苦手 ${chapter.weakCount}問 · 超苦手 ${chapter.veryWeakCount}問`;
		weakList.className = "weak-question-list";
		for (const question of chapter.weakQuestions) {
			const item = document.createElement("li");
			item.textContent = `「${question.questionText}」：${MASTERY_STATUS_LABELS[question.status]}（回答 ${question.attemptCount}回）`;
			weakList.append(item);
		}
		if (chapter.weakQuestions.length === 0) {
			const item = document.createElement("li");
			item.textContent = "特訓対象の問題はありません。";
			weakList.append(item);
		}
		trainButton.type = "button";
		trainButton.className = "secondary-button";
		trainButton.dataset.trainingChapterId = chapter.chapterId;
		trainButton.dataset.weakQuestionCount = weakCount;
		trainButton.textContent = `このChapterの苦手特訓を始める（${weakCount}問）`;
		trainButton.disabled = weakCount === 0;
		card.append(title, counts, weakList, trainButton);
		chapterProgressList.append(card);
	}
}

function renderTrainingRecords(items) {
	trainingRecordList.replaceChildren();
	if (items.length === 0) {
		trainingRecordList.textContent = "完了した苦手特訓はまだありません。";
		return;
	}

	const list = document.createElement("ul");
	list.className = "training-record-list";
	for (const record of items) {
		const item = document.createElement("li");
		const completedAt = record.endedAt
			? new Date(record.endedAt).toLocaleString("ja-JP")
			: "日時不明";
		item.textContent = `${completedAt} · ${record.chapterName} · ${record.correctCount}/${record.questionCount}問正解 · スコア ${record.score}`;
		list.append(item);
	}
	trainingRecordList.append(list);
}

function returnToGameResult() {
	reviewPanel.hidden = true;
	gameResultPanel.hidden = false;
	gameResultPanel.scrollIntoView({ behavior: "smooth", block: "start" });
	message.textContent = "Game Resultに戻りました。";
}

async function pauseCurrentGame() {
	if (questionState !== "ANSWERING" || !currentGame) {
		return;
	}
	pauseButton.disabled = true;
	try {
		await requestJson(
			`/api/stage-sessions/${currentGame.stageSessionId}/pause`,
			{ method: "POST" }
		);
		stopTimer();
		gamePanel.hidden = true;
		pausePanel.hidden = false;
		pausePanel.scrollIntoView({ behavior: "smooth", block: "start" });
		message.textContent = "ゲームを一時停止しました。";
	} catch (error) {
		showError(error);
	} finally {
		pauseButton.disabled = false;
	}
}

async function resumeCurrentGame() {
	if (!currentGame) {
		return;
	}
	resumeButton.disabled = true;
	try {
		const current = await requestJson(
			`/api/stage-sessions/${currentGame.stageSessionId}/resume`,
			{ method: "POST" }
		);
		pausePanel.hidden = true;
		showQuestion(current);
		message.textContent = current.questionState === "ANSWERING"
			? "ゲームを再開しました。残り時間は一時停止前の続きから進みます。"
			: "ゲームを再開しました。";
		gamePanel.scrollIntoView({ behavior: "smooth", block: "start" });
		if (current.stageSessionStatus === "CLEARED") {
			await showStageResult();
		}
	} catch (error) {
		showError(error);
	} finally {
		resumeButton.disabled = false;
	}
}

async function abandonCurrentGame(confirmBeforeEnd = true) {
	if (!gameSessionId
			|| (confirmBeforeEnd
				&& !window.confirm("セーブされませんがゲームを終了しますか？"))) {
		return;
	}
	abandonButton.disabled = true;
	try {
		await requestJson(
			`/api/game-sessions/${gameSessionId}/abandon`,
			{ method: "POST" }
		);
		returnToHome();
		message.textContent = "ゲームを終了しました。保存済みの回答履歴は残っています。";
	} catch (error) {
		showError(error);
	} finally {
		abandonButton.disabled = false;
	}
}

async function restoreSavedGame(forceResume = false) {
	const savedGameSessionId = localStorage.getItem("gameSessionId");
	if (!savedGameSessionId) {
		return;
	}
	if (!forceResume && !window.confirm("続きから再開しますか？")) {
		if (window.confirm("セーブされませんがゲームを終了しますか？")) {
			gameSessionId = Number(savedGameSessionId);
			await abandonCurrentGame(false);
			return;
		}
		resumeSavedButton.hidden = false;
		startButton.disabled = true;
		chapterSelect.disabled = true;
		selectionModeSelect.disabled = true;
		questionCountSelect.disabled = true;
		message.textContent = "保存したゲームがあります。再開ボタンから続きを遊べます。";
		return;
	}

	gameSessionId = Number(savedGameSessionId);
	try {
		const current = await getCurrentGame();
		if (current.gameSessionStatus === "COMPLETED"
				|| current.gameSessionStatus === "ABANDONED") {
			localStorage.removeItem("gameSessionId");
			gameSessionId = null;
			message.textContent = "前回のゲームは終了しています。新しいゲームを始めてください。";
			return;
		}
		const playing = current.gameSessionStatus === "PLAYING"
				&& current.stageSessionStatus === "PLAYING";
		const paused = current.gameSessionStatus === "PAUSED"
				&& current.stageSessionStatus === "PAUSED";
		const cleared = current.gameSessionStatus === "PLAYING"
				&& current.stageSessionStatus === "CLEARED";
		if (!playing && !paused && !cleared) {
			throw new Error("ゲームとStageの状態が一致しないため、安全に再開できません。");
		}

		resumeSavedButton.hidden = true;
		startPanel.hidden = true;
		learningProgressPanel.hidden = true;
		showQuestion(current);
		if (paused) {
			gamePanel.hidden = true;
			pausePanel.hidden = false;
			pausePanel.scrollIntoView({ behavior: "smooth", block: "start" });
			message.textContent = "一時停止した状態から再開できます。";
		} else if (cleared) {
			await showStageResult();
		} else {
			gamePanel.scrollIntoView({ behavior: "smooth", block: "start" });
			message.textContent = current.questionState === "ANSWERING"
				? "サーバーに保存された残り時間からゲームを再開しました。"
				: "ゲームの続きです。Enterで問題を開始してください。";
		}
	} catch (error) {
		showError(error);
	}
}

function difficultyLabel(difficulty) {
	return {
		EASY: "入門問題",
		ADVANCED: "応用問題",
		BRONZE_STYLE: "Java Bronze風オリジナル問題"
	}[difficulty] || "Javaの問題";
}

function updateQuestionCountOptions() {
	const maximum = selectionModeSelect.value === "EASY" ? 15 : 20;
	for (const option of questionCountSelect.options) {
		option.disabled = Number(option.value) > maximum;
	}
	if (Number(questionCountSelect.value) > maximum) {
		questionCountSelect.value = String(maximum);
	}
	startButton.textContent = `${questionCountSelect.value}問のゲームを始める`;
}

function showError(error) {
	message.classList.add("error");
	message.textContent = error.message;
}

startButton.addEventListener("click", () => startGame());
loadLearningProgressButton.addEventListener("click", loadLearningProgress);
reviewItems.addEventListener("change", (event) => {
	if (!(event.target instanceof HTMLSelectElement)
			|| !event.target.matches("[data-mastery-question-id]")) {
		return;
	}
	updateMasteryStatus(
		event.target.dataset.masteryQuestionId,
		event.target.value,
		event.target);
});
chapterProgressList.addEventListener("click", (event) => {
	if (!(event.target instanceof Element)) {
		return;
	}
	const button = event.target.closest("button[data-training-chapter-id]");
	if (button && !button.disabled) {
		startGame("WEAK_TRAINING", button.dataset.trainingChapterId);
	}
});
selectionModeSelect.addEventListener("change", updateQuestionCountOptions);
questionCountSelect.addEventListener("change", updateQuestionCountOptions);
startQuestionButton.addEventListener("click", startCurrentQuestion);
answerDetailedExplanationDetails.addEventListener("toggle", updateAnswerExplanationPause);
pauseButton.addEventListener("click", pauseCurrentGame);
resumeButton.addEventListener("click", resumeCurrentGame);
abandonButton.addEventListener("click", abandonCurrentGame);
resumeSavedButton.addEventListener("click", () => restoreSavedGame(true));
nextQuestionButton.addEventListener("click", showNextQuestion);
nextStageButton.addEventListener("click", startNextStage);
finishGameButton.addEventListener("click", showGameResult);
returnHomeButton.addEventListener("click", returnToHome);
reviewButton.addEventListener("click", showReview);
reviewHomeButton.addEventListener("click", returnToHome);
backToGameResultButton.addEventListener("click", returnToGameResult);
choicesElement.addEventListener("click", (event) => {
	if (!(event.target instanceof Element)) {
		return;
	}
	const button = event.target.closest("button[data-choice-id]");
	if (button) {
		selectChoice(button.dataset.choiceId);
	}
});
document.addEventListener("keydown", (event) => {
	if (event.target instanceof Element
			&& event.target.closest("button, summary, input, textarea, select")) {
		return;
	}
	if (questionState === "RESULT" && answerDetailedExplanationDetails.open) {
		return;
	}
	if (!pausePanel.hidden) {
		return;
	}
	if (event.key === "Enter") {
		if (questionState === "WAITING_TO_START") {
			event.preventDefault();
			startCurrentQuestion();
		} else if (questionState === "ANSWERING" && requiredChoiceCount() > 1) {
			event.preventDefault();
			if (selectedChoiceIds.size === requiredChoiceCount()) {
				submitAnswer([...selectedChoiceIds]);
			} else {
				message.textContent = `回答を確定するには${requiredChoiceCount()}つ選択してください。`;
			}
		} else if (questionState === "RESULT" && !nextQuestionButton.hidden) {
			event.preventDefault();
			showNextQuestion();
		}
		return;
	}

	if (event.repeat || questionState !== "ANSWERING" || !/^[a-g]$/i.test(event.key)) {
		return;
	}
	const choiceOrder = event.key.toUpperCase().charCodeAt(0) - 64;
	const choice = currentGame.question.choices.find((option) => option.choiceOrder === choiceOrder);
	if (choice) {
		event.preventDefault();
		selectChoice(choice.choiceId);
	}
});

loadChapters();
