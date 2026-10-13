const chapterSelect = document.querySelector("#chapter-select");
const startButton = document.querySelector("#start-button");
const message = document.querySelector("#message");
const gamePanel = document.querySelector("#game-panel");

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
		startButton.disabled = false;
		message.textContent = "学習テーマを選んでゲームを始めてください。";
	} catch (error) {
		showError(error);
	}
}

async function startGame() {
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
				questionCount: 5
			})
		});

		const current = await requestJson(
			`/api/game-sessions/${game.gameSessionId}/current`
		);
		showQuestion(current);
		message.textContent = "問題を読み込みました。タイマーはまだ開始していません。";
	} catch (error) {
		showError(error);
	} finally {
		startButton.disabled = chapterSelect.disabled;
	}
}

function showQuestion(current) {
	document.querySelector("#question-progress").textContent =
		`Stage 1 · 問題 ${current.questionNumber} / ${current.totalQuestionsInStage}`;
	document.querySelector("#score-status").textContent = `スコア ${current.score}`;
	document.querySelector("#enemy-status").textContent = `敵HP ${current.enemyHp}`;
	document.querySelector("#question-type").textContent = "Javaの問題";
	document.querySelector("#question-title").textContent = current.question.questionText;

	const codeBlock = document.querySelector("#code-block");
	codeBlock.hidden = !current.question.codeBlock;
	codeBlock.querySelector("code").textContent = current.question.codeBlock || "";

	const choices = document.querySelector("#choices");
	choices.replaceChildren();
	for (const choice of current.question.choices) {
		const item = document.createElement("li");
		item.textContent = choice.choiceText;
		choices.append(item);
	}

	document.querySelector("#short-explanation").textContent =
		current.question.shortExplanation;
	gamePanel.hidden = false;
}

function showError(error) {
	message.classList.add("error");
	message.textContent = error.message;
}

startButton.addEventListener("click", startGame);
loadChapters();
