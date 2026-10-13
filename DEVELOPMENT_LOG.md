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