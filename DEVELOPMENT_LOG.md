# 開発環境構築・学習記録

## 記録について
このSTEPは、現在のプロジェクト構成を把握するための概要メモです。各ファイルの詳細や生成手順をすべて理解したという意味ではありません。今後、実際のファイルやターミナル操作を一つずつ確認し、目的・操作手順・実行結果・学んだことを追記します。

## このドキュメントについて

このドキュメントでは、Java Learning Systemの開発環境を準備した経緯、基本ファイルの役割、動作確認の方法、セキュリティ上の注意点を記録する。

アプリの目的・機能・データ構造などの正式な設計仕様は `README.md` を参照する。本ドキュメントでは、開発環境の準備と実装作業に関する記録を扱う。

過去の操作については、実際のファイルやGitHubの履歴から確認できる事実と、確認できていない内容を区別する。

## STEP 1：Spring Bootプロジェクトの基本構成を準備する

### 1. 目的

JavaでWebアプリケーションを開発するために、必要な設定ファイル、起動用クラス、ライブラリ、テストの基本構成を準備する。

### 2. プロジェクト作成方法について

これまでの作業記録では、VS CodeのSpring Initializrを利用してSpring Bootプロジェクトの基本構成を作成したと整理されている。

Spring Initializrは、選択したJavaのバージョンやライブラリなどに応じて、開発開始に必要なファイルを生成する仕組みである。

ただし、当時のターミナル入力履歴や画面操作のすべては確認できていない。そのため、未確認のコマンドを実行済みとして記録しない。

### 3. 現在の設定ファイルから確認できる内容

`pom.xml` から、次の設定を確認できる。

- Javaのバージョン：17
- Spring Bootのバージョン：4.1.1
- ビルド・依存関係管理：Maven
- Webアプリケーション関連：Spring Boot Web MVC
- データベース関連：Spring Data JPA、H2 Database
- コード記述支援：Lombok
- テスト関連：Spring Bootのテスト用ライブラリ

### 4. 基本ファイルを用意する理由

- `pom.xml`：使用するライブラリやJavaのバージョンなどを管理する。
- `GraduationJavaGameApplication.java`：Spring Bootアプリの起動処理を行う。
- `application.properties`：アプリケーションの設定を管理する。
- `GraduationJavaGameApplicationTests.java`：アプリの基本構成をテストする。
- `.gitignore`：Gitで管理しないファイルを指定する。
- `.gitattributes`：ファイルの改行コードなどの扱いを統一する。
- `mvnw`、`mvnw.cmd`、`.mvn/wrapper/maven-wrapper.properties`：プロジェクトで指定したMavenを利用してビルドできるようにする。

これらのファイルはそれぞれ役割が異なる。すべてを手作業で作成するのではなく、Spring Initializrなどで生成された基本構成を確認し、必要に応じて設定を変更する。

### 5. セキュリティ上の注意点

`.gitignore` には、ビルド結果、ローカルの開発環境用ファイル、環境変数ファイル、鍵やログファイルなどを除外するルールがある。

これらのルールは、不要なファイルや機密情報を誤ってGitHubへ登録するリスクを減らすために役立つ。

ただし、`.gitignore` を設定しただけで、すべての機密情報が保護されるわけではない。すでにGitで管理されているファイルや過去のコミットに含まれた情報は、別途確認する必要がある。

また、`.gitattributes` は主にファイルの扱いを統一するための設定であり、秘密情報を保護するための設定とは役割が異なる。

### 6. 動作確認について

テスト実行時に `BUILD SUCCESS` と表示されたことを確認した。

これは実行したビルド・テスト処理が成功したことを示す。ただし、ゲームの機能が完成したことや、アプリ本体がブラウザから正常に利用できることまで保証するものではない。

今後は、テストの実行とアプリ本体の起動を分けて確認する。

### 7. 今後の作業

1. ターミナルの基本操作を理解する。
2. `pom.xml` と依存ライブラリの役割を確認する。
3. Spring Bootアプリ本体を起動し、起動結果を確認する。
4. 必要なファイルがGitでどのように管理されているか確認する。
5. 基本構成が動作することを確かめてから、READMEの設計仕様に沿ってゲーム機能を実装する。

### 8. 学習記録の方針

作業ごとに「目的」「実際に行った操作」「確認結果」「学んだこと」「次に行うこと」を記録する。

確認できていない操作や、まだ実装していない機能を、実施済みとして記録しない。

## STEP 2：Chapter一覧APIを作る

### 1. 目的

READMEの補助APIとして記載されている `GET /api/chapters` を作り、ブラウザからChapter一覧を取得できるようにする。

### 2. 実装した内容

- `Chapter`：ChapterのID・名前・説明をDBに保存するEntity。
- `ChapterRepository`：Spring Data JPAを使ってChapterを検索・保存する窓口。
- `ChapterService`：Repositoryから取得したEntityをAPI用データに変換する処理。
- `ChapterResponse`：APIから返すID・名前・説明だけをまとめるDTO。
- `ChapterController`：`GET /api/chapters` のリクエストを受け付けるController。

ControllerがEntityをそのままJSONにしないよう、Serviceで `ChapterResponse` に変換してから返す。Chapterがまだ登録されていない場合は、空の配列を返す。READMEにないChapterの初期データは追加していない。

### 3. 動作確認

Chapterをテスト用DBに保存してから `GET /api/chapters` を呼び出し、HTTP 200とID・名前・説明が返ることをMockMvcで確認した。既存のSpring Boot起動テストも含めて、テスト2件が成功した（失敗0件）。

最初のテスト実行では `JAVA_HOME` が未設定だったため、インストール済みJDK 25をその実行コマンド内で指定した。また、Spring Boot 4の `@AutoConfigureMockMvc` は以前とpackageが異なっていたため、実際の依存ライブラリに含まれるpackageに修正してから再実行した。`pom.xml` のJavaバージョン設定は変更していない。

### 4. 学んだこと

- EntityはDBに保存するデータ、RepositoryはDB操作、Serviceはアプリの処理、ControllerはHTTP APIの窓口を担当する。
- DTOを使うと、画面に返すデータをEntityの内部構造から分けられる。
- テストではMockMvcを使って、アプリを起動した状態に近い形でHTTP APIを確認できる。

### 5. 次に行うこと

Chapter一覧を画面から利用するか、READMEの実装優先順位に沿ってQuestionとChoiceのデータ・APIを小さな単位で追加する。各変更はテストし、今回と同様に実際の確認結果を記録してからコミットする。

## STEP 3：問題と選択肢をDBに保存する

### 1. 目的

READMEに書かれているChapter・Question・Choiceのつながりを作り、問題と選択肢をDBに保存できるようにする。

### 2. 実装した内容

- `QuestionType`：READMEで定められた7種類の問題形式を表すEnum。
- `Question`：Chapterとの関連、問題形式、問題文、コード、制限時間、解説を保存するEntity。
- `Choice`：Questionとの関連、選択肢の文章・表示順・正解情報を保存するEntity。
- `QuestionRepository`：Chapter IDを指定して問題を検索するメソッドを追加。
- `ChoiceRepository`：問題IDを指定して表示順に選択肢を検索するメソッドを追加。
- 統合テスト：問題と選択肢をDBに保存し、選択肢が表示順に取得できることを確認。

QuestionとChoiceから親データへの関連には、READMEの方針に沿ってLAZY読み込みの@ManyToOneを使用する。QuestionTypeは文字列としてDBに保存する。Choiceの正解情報はDB内の判定用データであり、ブラウザへ返すAPIはまだ作成していない。

### 3. 動作確認

Chapter一覧API、問題・選択肢の保存と表示順、Spring Bootの起動を確認するテストを実行した。テスト3件が成功し、失敗・エラーは0件だった。

### 4. 学んだこと

- Enumを使うと、問題形式を決められた値から選ぶ形にできる。
- `@ManyToOne`は「複数の問題が1つのChapterに属する」などの関係を表す。
- Repositoryのメソッド名に検索条件や並び順を含めると、Spring Data JPAがその条件に沿ったDB検索を行う。
- 正解情報はサーバー内に保管し、画面向けデータとは分ける必要がある。

### 5. 次に行うこと

問題と選択肢を画面に渡す読み取りAPIを、READMEのAPI仕様に沿って作る。その後、Stageと回答処理へ小さく進み、各段階でテストしてからコミットする。

## STEP 4：ChapterごとのStage設定を保存する

### 1. 今回しようと考えたこと

問題と選択肢の次に、Chapterをゲームとして進めるためのStage設定が必要になる。まず1つのStageに「何番目のStageか」「どのキャラクター・敵・背景を使うか」「どのミニゲームを使うか」を保存し、ChapterとStage番号を使って検索できるようにする。

この段階ではゲーム開始APIや画面はまだ作らない。Stage設定を安全に保存・検索できることだけを先に確かめ、次の作業と原因を切り分けやすくする。

### 2. 新しく作ったファイル

- `MiniGameType.java`：READMEで決められたMVPのミニゲーム2種類（`TYPING_SAMURAI`、`CODE_TARGET`）を表すEnum。
- `StageDefinition.java`：Chapter、Stage番号、キャラクター・敵・背景のKey、ミニゲーム種類、敵HPをDBに保存するEntity。
- `StageDefinitionRepository.java`：Chapter IDとStage番号を指定してStage設定を検索するRepository。

既存の `GraduationJavaGameApplicationTests.java` に、Stage設定を保存して検索するテストも追加した。敵HPはREADMEにあるMVPの固定値5を使って確認する。

### 3. 動作確認

Spring Bootの起動、Chapter一覧API、問題・選択肢の保存、Stage設定の保存・検索を含むテストを実行した。**テスト4件が成功し、失敗・エラーは0件**だった。Javaの診断でもエラーはなく、`git diff --check`も成功した。

### 4. 初心者向け用語メモ

- **Stage**：5問をひとまとまりにしたゲーム上のステージ。
- **StageDefinition**：そのStageで使う設定。今回はStage 1の設定データを保存できるようにした。
- **Key**：キャラクターや背景などを識別するための文字列。画像そのものではなく、あとで対応する画像を選ぶ目印。
- **Enum**：プログラムで選べる値をあらかじめ限定する仕組み。ミニゲーム種類の入力間違いを防ぎやすくする。
- **Repository**：Javaの処理からデータベースを検索・保存する窓口。

### 5. まだ作っていないもの

このコミットでは、ChapterにStage設定を作成するAPI、GameSession開始、Stageの自動進行、実際のゲーム画面はまだ作っていない。保存処理があることと、プレイヤーがゲームを遊べることは別の段階である。

### 6. 次に行うこと

QuestionとChoiceを安全な画面用データに変換して返す読み取りAPIを作り、その後GameSessionとStageSessionのデータを1つずつ追加する。どの工程も、機能・テスト・作業記録を一緒に確認してから個別にコミットする。

## STEP 6：ゲームを開始するAPIを作る

### 1. 今回しようと考えたこと

これまでにデータベースへ保存できるようにしたChapter・Question・Stage設定を、1回のゲームとしてつなぐ。ブラウザからChapter IDと問題数を受け取り、遊べる内容がそろっているか確認してから、GameSession・最初のStageSession・そのStageの5問をまとめて作る。

ゲーム開始時にはすべてのStageの問題を作らず、最初のStage分だけ用意する。これはREADMEのルールに沿った動きで、10問や20問を選んでも、次のStageの記録はそのStageへ進む処理を作るまで作らない。

### 2. 新しく作ったファイル

- `GameSessionController.java`：`POST /api/game-sessions`を受け付けるAPIの窓口。
- `GameSessionService.java`：入力、Chapter、問題数、必要なStage設定を確認し、ゲーム開始時の記録を作る処理。
- `GameSessionCreateRequest.java`：ブラウザから受け取るChapter IDと問題数。
- `GameSessionCreateResponse.java`：開始後にブラウザへ返すゲームID、問題数、Stage数、現在Stage、状態。
- `StageQuestion.java`、`StageQuestionRepository.java`：最初のStageで出題する5問と順番を保存するEntity・Repository。
- `GameApiException.java`、`GameApiExceptionHandler.java`、`ErrorResponse.java`：Chapter不存在や問題不足などを、HTTPステータスとエラーコード付きJSONで返す仕組み。

### 3. 今回のチェックとゲーム開始の流れ

1. `chapterId`と`questionCount`が指定されているか確認する。
2. 問題数がREADMEで許可された5・10・20のいずれかか確認する。
3. Chapterが存在するか確認する。
4. 遊ぶStage数分（問題数÷5）のStage設定が存在するか確認する。
5. Chapterに選んだ問題数以上の問題があるか確認する。
6. ゲーム全体の記録とStage 1の進行記録を作る。
7. Chapter内から重複しない問題を5問選び、出題順1～5で保存する。
8. Game IDと開始時の状態を返す。

すべての保存を1つのトランザクションで行うため、途中でエラーになった場合にGameSessionだけが残る、といった不完全な状態を防ぐ。エラーは隠さず、`400 INVALID_REQUEST`、`404 NOT_FOUND`、`409 INSUFFICIENT_QUESTIONS`、Stage設定不足時の`409 INVALID_GAME_STATE`として伝える。

### 4. 動作確認

MockMvcでAPIを呼び出し、5問・10問・20問すべてで開始できること、レスポンスの問題数・Stage数・最初のStage番号・状態を確認した。10問・20問の場合も、最初に作られるStageSessionがStage 1の1件だけで、StageQuestionはそのStageの5件だけであることを確認した。

さらに、未対応の問題数、存在しないChapter、Chapter内の問題不足、必要なStage設定不足をそれぞれ確認し、失敗時にGameSessionが保存されないことをテストした。テストは**10件成功、失敗・エラー0件**、Java診断エラーなし、`git diff --check`成功。

### 5. 現時点での出題と、次に必要な改良

現在は回答履歴を保存する機能がまだないため、Chapter内の問題をシャッフルしてStage 1用の5問を選ぶ。これにより1回のゲームの中では、同じ問題が重複しない。READMEで定めた「過去の最新回答を見て未回答・復習・正解済みの優先順にする」処理は、回答履歴が存在しない今の段階ではまだ判定できない。回答履歴を作る工程でこの選び方を拡張する。

また、今回作ったのはゲーム開始APIまでで、画面に問題文を返すAPI、問題のタイマー開始、回答判定、次のStageへの進行はまだ実装していない。

### 6. 次に行うこと

Stage 1で選ばれた問題と選択肢を画面用DTOに変換し、正解情報を外に漏らさない問題取得APIを作る。その後、Start APIでJava側の開始時刻を記録してから、回答・履歴保存へ進む。機能ごとにテストし、実際に確認した結果を作業ログへ追記してからコミットする。

## STEP 7：現在の問題と選択肢を取得する

### 1. 今回しようと考えたこと

ゲームを開始して問題をデータベースへ用意できても、ブラウザがその問題を受け取るAPIがなければ画面に表示できない。そこで、開始済みのゲームIDから今のStageと問題を探し、画面に必要な情報だけを返すAPIを作る。

問題を取得した時点ではまだ回答時間を始めない。READMEに合わせて状態は`WAITING_TO_START`、`remainingTimeMs`は`null`にし、次の工程で作るStart APIが成功してからタイマーを動かす。

### 2. 新しく作ったファイル

- `CurrentGameResponse.java`：現在のゲーム状態、問題、画面に出す選択肢、キャラクターや背景のKeyをまとめたレスポンスDTO。

### 3. 修正したファイル

- `GameSessionController.java`：`GET /api/game-sessions/{gameSessionId}/current`を追加。
- `GameSessionService.java`：現在のGameSession・StageSession・StageQuestionを探し、問題と表示順の選択肢を画面用DTOへ変換する処理を追加。
- `StageQuestionRepository.java`：Stage内の出題番号から現在のStageQuestionを探すメソッドを追加。
- `GraduationJavaGameApplicationTests.java`：現在の問題が取得できること、開始前のタイマー状態、選択肢の表示順、正解情報が返らないこと、存在しないゲームIDのエラーをテスト。

画面へ返す選択肢にはID・文章・表示順だけを入れる。Entityにある`Choice.correct`はサーバー内の回答判定用情報なので、画面用DTOに含めない。

### 4. テストと修正の記録

最初のビルドでは、問題の選択肢を探す`ChoiceRepository`をServiceの依存関係に追加し忘れていることがコンパイルエラーで分かった。ServiceのコンストラクターでRepositoryを受け取るように修正し、改めてテストを実行した。

最終確認では、ゲーム開始を含むテスト**12件が成功し、失敗・エラーは0件**だった。問題文・問題形式・制限時間・選択肢・Stageの画面設定を取得でき、`correct`がJSONに含まれないこと、開始前は`WAITING_TO_START`でタイマー用の残り時間がないこと、存在しないゲームIDには404 `NOT_FOUND`を返すことを確認した。Java診断エラーはなく、`git diff --check`も成功。

### 5. まだできないこと

この工程でできるのは、現在の問題を画面向けに取得するところまで。問題のStart API、タイマー開始、選択肢を送る回答API、正誤判定とスコア更新、画面そのものは次の工程で作る。

### 6. 次に行うこと

StageQuestionのStart APIを作り、未開始の問題にJava側の開始日時を一度だけ記録する。再Startでタイマーをリセットできないようにし、API成功時の残り時間を返すテストを追加する。その後、回答APIと画面の実装へ進む。

## STEP 5：ゲーム全体とStageの進行記録を保存する

### 1. 今回しようと考えたこと

Stageの設定だけでは、プレイヤーが今何問目まで進んだか、ゲームを一時停止しているか、何点取ったかを記録できない。そこで、READMEで役割が分かれている2種類の記録を追加する。

- **GameSession**：1回のゲーム全体を記録する。選んだChapter、問題数、Stage数、現在のStage、得点、全体状態を持つ。
- **StageSession**：そのゲームの各Stageを個別に記録する。現在の問題番号、正解数、敵HP、コンボ、Pause状態を持つ。

こうして全体の進み具合と各Stageの途中経過を別々に記録すると、Stageごとの結果を保ちながら、次のStageへ進める土台になる。

### 2. 新しく作ったファイル

- `GameSessionStatus.java`：ゲーム全体の状態（プレイ中・一時停止・完了・中断）を表すEnum。
- `StageSessionStatus.java`：Stageの状態（プレイ中・一時停止・クリア・中断）を表すEnum。
- `GameSession.java`：ゲーム全体の記録をデータベースに保存するEntity。
- `StageSession.java`：1つのStageの進行状況をデータベースに保存するEntity。
- `GameSessionRepository.java`：ゲーム全体の記録を保存・検索するRepository。
- `StageSessionRepository.java`：Stageの記録を保存・検索するRepository。同じゲームに同じStageを重複作成しないための確認メソッドも追加した。

`StageSession`には、同じゲームIDとStage IDの組み合わせを2回登録できないDB制約も設定した。Repositoryで事前確認する方法とDB制約を組み合わせ、Stageの二重作成を防ぐ準備をする。

### 3. 既存ファイルで変更した内容

- `GraduationJavaGameApplicationTests.java`：ゲームとStageの記録をDBに保存し、開始状態やStage検索結果を確認するテストを追加した。
- `GraduationJavaGameApplication.java`：アプリの起動地点と`main`メソッドの役割をコメントで説明した。
- Chapter、Question、Choice、StageDefinitionのEntity：DBに保存する各項目と取得メソッドの役割をコメントで説明した。
- 各Repository：DB操作の窓口としての役割と、独自検索メソッドの意味をコメントで説明した。
- ChapterController、ChapterService、ChapterResponse：APIリクエスト、データ変換、画面へ返す項目の役割をコメントで説明した。
- QuestionType、MiniGameType：選べる各値の意味をコメントで説明した。

今回の作業で`src`にあるJavaファイル21個を確認し、クラスやEnumの役割、初学者が迷いやすいフィールド・処理に日本語コメントを追加・補足した。コードを一行ずつ言い換えるのではなく、「このファイルや値は何のためにあるか」を読んで説明できるコメントにする。

### 4. 動作確認

Chapter一覧API、問題・選択肢の保存と並び順、Stage設定の保存と検索、新しいGameSession・StageSessionの保存をテストした。**テスト5件が成功し、失敗・エラーは0件**だった。Javaの診断エラーはなく、`git diff --check`も成功した。

### 5. まだ作っていないもの

今回作ったのはゲームの進行状況を保存する土台であり、GameSessionをブラウザから開始するAPI、5・10・20問の入力検証、Stage開始処理、実際に遊べる画面はまだない。

### 6. 次に行うこと

READMEに沿ってGameSession開始の処理を作る。入力されたChapterと5・10・20問の選択を検証し、必要なStage設定や問題数を確認してから、最初のGameSessionとStageSessionを作成し、APIのテストを追加する。入力エラーのHTTPレスポンスもREADMEの仕様に合わせて確認する。