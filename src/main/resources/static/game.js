const chapterSelect = document.querySelector("#chapter-select");
const selectionModeSelect = document.querySelector("#selection-mode");
const questionCountSelect = document.querySelector("#question-count");
const startButton = document.querySelector("#start-button");
const startPanel = document.querySelector(".start-panel");
const startQuestionButton = document.querySelector("#start-question-button");
const nextQuestionButton = document.querySelector("#next-question-button");
const message = document.querySelector("#message");
const gamePanel = document.querySelector("#game-panel");
const choicesElement = document.querySelector("#choices");
const answerResult = document.querySelector("#answer-result");
const answerResultTitle = document.querySelector("#answer-result-title");
const answerResultDetails = document.querySelector("#answer-result-details");
const answerExplanation = document.querySelector("#answer-explanation");
const timerStatus = document.querySelector("#timer-status");

let gameSessionId = null;
let currentGame = null;
let questionState = "WAITING_TO_START";
let selectedChoiceIds = new Set();
let timerInterval = null;
let timerDeadline = null;

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
	} catch (error) {
		showError(error);
	}
}

async function startGame() {
	stopTimer();
	startButton.disabled = true;
	gamePanel.hidden = true;
	message.classList.remove("error");
	message.textContent = "ゲームの準備をしています...";

	try {
		const game = await requestJson("/api/game-sessions", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({
				chapterId: Number(chapterSelect.value),
				questionCount: Number(questionCountSelect.value),
				selectionMode: selectionModeSelect.value
			})
		});
		gameSessionId = game.gameSessionId;
		const current = await getCurrentGame();
		showQuestion(current);
		message.textContent = "問題を読み込みました。Enterで問題を開始してください。";
		startPanel.hidden = true;
		gamePanel.scrollIntoView({ behavior: "smooth", block: "start" });
	} catch (error) {
		gameSessionId = null;
		showError(error);
	} finally {
		startButton.disabled = chapterSelect.disabled;
	}
}

async function getCurrentGame() {
	return requestJson(`/api/game-sessions/${gameSessionId}/current`);
}

function showQuestion(current) {
	stopTimer();
	message.classList.remove("error");
	currentGame = current;
	questionState = current.questionState;
	selectedChoiceIds = new Set();
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
	startQuestionButton.hidden = questionState !== "WAITING_TO_START";
	startQuestionButton.disabled = questionState !== "WAITING_TO_START";
	timerStatus.textContent = "開始前";
	gamePanel.hidden = false;
	updateChoiceControls();

	if (questionState === "ANSWERING" && current.remainingTimeMs != null) {
		startTimer(current.remainingTimeMs);
	}
}

async function startCurrentQuestion() {
	if (questionState !== "WAITING_TO_START" || !currentGame) {
		return;
	}

	questionState = "STARTING";
	startQuestionButton.disabled = true;
	message.classList.remove("error");
	message.textContent = "問題を開始しています...";
	updateChoiceControls();

	try {
		const started = await requestJson(
			`/api/stage-questions/${currentGame.stageQuestionId}/start`,
			{ method: "POST" }
		);
		questionState = "ANSWERING";
		startQuestionButton.hidden = true;
		message.textContent = "回答してください。";
		startTimer(started.remainingTimeMs);
		updateChoiceControls();
	} catch (error) {
		questionState = "WAITING_TO_START";
		showError(error);
		updateChoiceControls();
	} finally {
		startQuestionButton.disabled = false;
	}
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
	answerExplanation.textContent = currentGame.question.shortExplanation;
	answerResult.hidden = false;
	document.querySelector(".explanation").hidden = true;
	startQuestionButton.hidden = true;

	if (result.stageClear) {
		const hasNextStage = currentGame.currentStageNumber < currentGame.totalStageCount;
		answerResultTitle.textContent = result.perfect ? "PERFECT！ Stage Clear" : "Stage Clear";
		nextQuestionButton.hidden = !hasNextStage;
		nextQuestionButton.textContent = "次のStageへ進む";
		message.textContent = hasNextStage
			? "Stageをクリアしました。次のStageへ進めます。"
			: "すべてのStageをクリアしました。ゲーム終了です。";
		if (hasNextStage) {
			nextQuestionButton.focus();
		}
	} else {
		nextQuestionButton.hidden = false;
		nextQuestionButton.textContent = "次の問題へ";
		message.textContent = "回答結果を確認し、次の問題へ進んでください。";
		nextQuestionButton.focus();
	}
}

async function showNextQuestion() {
	if (questionState !== "RESULT" || !currentGame) {
		return;
	}
	nextQuestionButton.disabled = true;
	try {
		if (currentGame.questionNumber === currentGame.totalQuestionsInStage) {
			await requestJson(
				`/api/game-sessions/${gameSessionId}/advance-stage`,
				{ method: "POST" }
			);
		}
		const next = await getCurrentGame();
		showQuestion(next);
		message.textContent = currentGame.questionNumber === 1
			? "次のStageを開始しました。Enterで問題を開始してください。"
			: "Enterで次の問題を開始してください。";
	} catch (error) {
		showError(error);
	} finally {
		nextQuestionButton.disabled = false;
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

startButton.addEventListener("click", startGame);
selectionModeSelect.addEventListener("change", updateQuestionCountOptions);
questionCountSelect.addEventListener("change", updateQuestionCountOptions);
startQuestionButton.addEventListener("click", startCurrentQuestion);
nextQuestionButton.addEventListener("click", showNextQuestion);
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
