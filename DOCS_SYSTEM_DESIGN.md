# 🛠️ 実装ロードマップと自動化パイプライン手順書

10/25のMVP完成に向け、手戻りを最小限に抑えて「データモデルの構築」から「AIを活用した問題自動生成」までを最速で進めるための具体的な開発手順です。

---

## 1. 最速MVP開発ロードマップ

画面から作ると「裏側のデータ構造が変わって全修正」という大変面倒な事態に陥りやすいため、本システムでは**「下（DB）から上（画面）へ積み上げる」**順番で実装を進めます。

1. **【Step 1】データモデルとRepositoryの作成（DBの土台）** ── **本日着手！**
2. **【Step 2】一括インポート機能の作成** ── AI量産データの投入確認
3. **【Step 3】コアロジック（Serviceクラス）の実装** ── 正誤判定・状態管理
4. **【Step 4】API（Controller）の実装** ── JSONを返すバックエンドの完成
5. **【Step 5】フロントエンド（HTML/CSS/JS）の実装** ── ゲーム演出・画面と合体

---

## 2. 【Step 1】データモデルとRepositoryの具体的な実装内容

Spring Bootプロジェクトを立ち上げたら、まずはクイズシステムの核となる `Question`（問題）と `Choice`（選択肢）のエンティティクラス、およびデータアクセスのための `Repository` を作成します。

### 2.1 Entityクラスの作成（JPA / Hibernate）

仕様書の「14.2 / 14.3 データモデル」をベースに、`Choice` から `Question` へ多対一（`@ManyToOne`）のリレーションを構築します。

#### `Question.java`
```java
package com.example.javagame.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String codeBlock;

    private int requiredAnswerCount; // 単一選択なら1、複数選択なら2
    private int timeLimitSeconds;
    private String topic;            // 割り当てられたTopicコード
    private String difficulty;       // BRONZE, SILVERなど
    private String explanation;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Choice> choices;

    // TODO: Getter, Setterの追加（またはLombokの@Dataを使用）
}
```

#### `Choice.java`
```java
package com.example.javagame.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "choices")
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    private String choiceLabel; // A, B, C, Dなど
    private String choiceText;
    private boolean correct;     // 正解フラグ（true = 正解, false = 不正解）

    // TODO: Getter, Setterの追加（またはLombokの@Dataを使用）
}
```

### 2.2 Repositoryインターフェースの作成

Spring Data JPAを継承し、特定の学習テーマ（`topic`）に紐づく問題をプールからランダム、または一括で取得できるようにします。

#### `QuestionRepository.java`
```java
package com.example.javagame.repository;

import com.example.javagame.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // 特定の学習項目（Topic）に合致する問題一覧を取得する
    List<Question> findByTopic(String topic);
}
```

---

## 3. 【Step 2】AIを活用した「問題データ半自動量産」パイプライン

既存教材の著作権を完全に保護（文章のコピーや改変を排除）しつつ、Java Bronze相当のオリジナル問題を効率よく量産するためのAI指示書テンプレートです。

### 3.1 AI量産用プロンプトテンプレート

以下の点線内のテキストをAI（ChatGPT等）にそのまま入力し、`【ターゲットテーマ（Topic）】` 部分を以下の対応表のコードに書き換えて指示を出してください。

```text
--------------------------------------------------------------------------------
# 目的
Java Bronze（初学者向け試験）のレベル感に合わせた、完全オリジナルの選択式クイズを指定のJSONフォーマットで作成してください。

# ターゲットテーマ (Topic)
【ここに生成したいテーマを記入。例: VARIABLE_DATA_TYPE / CONDITIONAL_BRANCH など】

# 難易度基準 (Java Bronzeレベル)
・基本文法（変数、データ型、演算子、制御構文）
・配列の宣言と基本操作
・オブジェクト指向の基本概念（クラスの定義、インスタンス化、参照型）
・文字列操作（String型）の基本、標準出力の仕様

# 絶対制約（著作権・セキュリティ保護）
1. 既存の技術書籍（『スッキリわかるJava入門』など）や実際の資格試験の問題文、コード例、解説文をそのまま流用、あるいは単なる単語の言い換えで作成してはなりません。
2. 登場する変数名、設定、数値、選択肢の構成、解説にいたるまで、すべてゼロから新規設計してください。
3. コード例を提示する場合、文法的に正しくコンパイルが通るもの、あるいは「意図的なコンパイルエラー」を狙った問題であるかを明確に区別してロジックを組んでください。

# 出力フォーマット
以下のJSON構造を持つ配列（[]）の形式で出力してください。マークダウンのコードブロック（```json ... ```）で囲んでください。

[
  {
    "subject": "JAVA",
    "level": "BRONZE",
    "topic": "【指定されたTopicコード】",
    "questionType": "SINGLE_CHOICE",
    "timeLimitSeconds": 30,
    "questionText": "【問題文。ソースコードを含める場合は適切に \\n で改行を入れてください】",
    "choices": [
      {"label": "A", "text": "【選択肢1】", "isCorrect": true},
      {"label": "B", "text": "【選択肢2】", "isCorrect": false},
      {"label": "C", "text": "【選択肢3】", "isCorrect": false},
      {"label": "D", "text": "【選択肢4】", "isCorrect": false}
    ],
    "explanation": "【初学者が納得できる、誤りの選択肢がなぜ違うのかも含めた丁寧な解説】"
  }
]
--------------------------------------------------------------------------------
```

### 3.2 設計材料から抽出した「Topicコード対応表」

既存教材（chap00〜chap17）の構成から、Java Bronzeの出題範囲に合致する抽象的な学習項目を切り出した管理コード一覧です。

| 教材の対象章 | 割り当てる `topic` コード | カバーする具体的な学習内容 |
| :--- | :--- | :--- |
| 入門・第1章 / 第2章 | `VARIABLE_DATA_TYPE` | 変数の宣言、初期化、基本データ型（`int`, `double`等）、型変換 |
| 入門・第2章 | `OPERATOR_EXPRESSION` | 算術演算子、インクリメント、文字列結合、評価の優先順位 |
| 入門・第3章 | `CONDITIONAL_BRANCH` | `if` 文、`switch` 文、関係演算子、論理演算子（`&&`, `\|\|`） |
| 入門・第3章 | `LOOP_STATEMENT` | `for` 文、`while` 文、無限ループの回避、ループのネスト |
| 入門・第4章 | `ARRAY_BASIC` | 配列の宣言・参照、`length` 属性、拡張 `for` 文、例外（境界外） |
| 入門・第5章 / 第6章 | `METHOD_BASIC` | メソッドの定義、引数、戻り値、オーバーロードの基本 |
| 入門・第8章 / 第9章 | `CLASS_OBJECT` | クラスの定義、newによるインスタンス化、フィールド参照 |
| 実践・第1章 | `STRING_OPERATION` | `String` クラスの基本メソッド（`length()`, `equals()` など） |

---

### 3.3 自動生成される「問題データ（JSON）」の具体例

上記のシステムフローによって出力され、Spring Bootのインポータにそのまま投入できるデータ構造のサンプルです。

```json
[
  {
    "subject": "JAVA",
    "level": "BRONZE",
    "topic": "OPERATOR_EXPRESSION",
    "questionType": "SINGLE_CHOICE",
    "timeLimitSeconds": 30,
    "questionText": "次のコードを実行したとき、標準出力に表示される結果として適切なものを1つ選びなさい。\n\nint x = 10;\nint y = 3;\nSystem.out.println(x / y + \" : \" + (x % y));",
    "choices": [
      {"label": "A", "text": "3.333... : 1", "isCorrect": false},
      {"label": "B", "text": "3 : 1", "isCorrect": true},
      {"label": "C", "text": "3 : 0.333...", "isCorrect": false},
      {"label": "D", "text": "コンパイルエラーが発生する", "isCorrect": false}
    ],
    "explanation": "Javaにおいて、int型同士の除算（x / y）は小数点以下が切り捨てられた整数となるため、10 / 3 は 3 となります。また、剰余演算子（x % y）は割った余りを表すため、10 を 3 で割った余りの 1 になります。これらが文字列 \" : \" と結合されるため、正解はB（3 : 1）です。"
  }
]
```
