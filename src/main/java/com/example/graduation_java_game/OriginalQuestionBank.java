package com.example.graduation_java_game;

import java.util.ArrayList;
import java.util.List;

/**
 * 添付された目次の学習範囲をもとにした、書籍本文に依存しないオリジナル問題データです。
 */
final class OriginalQuestionBank {

	private static final List<String> EASY_FORMS = List.of(
			"「%s」について、最も基本的で正しい説明はどれですか？",
			"Javaを学び始めた人が「%s」で覚える内容として正しいものはどれですか？",
			"次のうち、「%s」の使い方・役割を正しく説明しているものはどれですか？",
			"「%s」の説明として、正しいものを選んでください。");

	private static final List<String> ADVANCED_FORMS = List.of(
			"次の要件をJavaで実現するとき、「%s」について正しい説明はどれですか？",
			"コードを変更せずに仕様を満たすため、「%s」の性質として正しいものを選んでください。",
			"実行結果や型の扱いを判断するとき、「%s」について正しいものはどれですか？",
			"次の設計レビューで指摘すべき、「%s」に関する正しい説明はどれですか？");

	private static final List<String> BRONZE_FORMS = List.of(
			"Javaの言語仕様を踏まえ、「%s」に関する記述として正しいものを選んでください。",
			"次の選択肢のうち、コンパイル時・実行時の動作を正確に表しているものはどれですか？（テーマ：%s）",
			"境界条件も考慮した場合、「%s」について正しいものはどれですか？",
			"Java Bronze風の確認問題です。「%s」に関する説明として正しいものを選んでください。");

	private static final List<String> ADVANCED_CONTEXTS = List.of(
			"既存コードの一部を再利用する",
			"複数の値や処理を安全に扱う",
			"条件によって処理結果が変わる",
			"読みやすさと誤動作の防止を両立する");

	private static final List<String> BRONZE_CONTEXTS = List.of(
			"宣言・初期化の順番を区別する",
			"参照先と値の違いを区別する",
			"コンパイル可能な範囲を判断する",
			"例外や境界値を含む動作を判断する");

	private OriginalQuestionBank() {
	}

	static List<ChapterSeed> chapters() {
		return List.of(
				chapter("第1章 プログラムの書き方", "Javaプログラムの基本構造を学びます。",
						c("クラス宣言", "クラスはclassキーワードと名前で宣言する。", "クラスはvarキーワードで宣言する。", "クラス名の後に括弧だけを書けば宣言できる。"),
						c("mainメソッド", "通常のJavaアプリはpublic static void main(String[] args)を開始点にできる。", "mainメソッドの戻り値は必ずintである。", "mainメソッドは引数を必ず1つも受け取れない。"),
						c("文末のセミコロン", "多くのJava文は末尾にセミコロンを書く。", "すべてのJava文は末尾にコロンを書く。", "セミコロンはクラス宣言の直後だけに書く。"),
						c("コメント", "//から行末まで、および/* ... */でコメントを書ける。", "コメントは必ずプログラム実行時に表示される。", "コメントを書くには全行を#で始める。"),
						c("標準出力", "System.out.printlnは値を標準出力へ出し、改行する。", "System.in.printlnは値を画面へ出す。", "printlnは出力後に必ずプログラムを終了する。")),
				chapter("第2章 式と演算子", "値、型変換、演算子の優先順位を学びます。",
						c("整数の割り算", "int同士の整数除算では小数部分が切り捨てられる。", "int同士の除算は必ず小数になる。", "整数除算では分母が0でも例外は起きない。"),
						c("演算子の優先順位", "括弧を使うと式の評価順を明示できる。", "加算は常に乗算より先に評価される。", "演算子の優先順位はソースコードの行順だけで決まる。"),
						c("インクリメント", "x++は式として使うと、式の値を使った後にxを1増やす。", "x++はxを1減らす。", "x++はxを必ず0に戻す。"),
						c("型変換", "小さい範囲の整数型から大きい範囲の整数型への代入は通常拡大変換できる。", "doubleからintへの代入は常に暗黙変換できる。", "型変換を行うと元の値が必ず保存される。"),
						c("論理演算", "&&は左辺がfalseなら右辺を評価しない短絡演算子である。", "&&は左辺がfalseでも必ず右辺を評価する。", "&&は数値の乗算に使う演算子である。")),
				chapter("第3章 条件分岐と繰り返し", "条件に応じた処理とループを学びます。",
						c("if文", "if文は条件式がtrueのときに対応する処理を実行する。", "if文の条件式には文字列をそのまま書く。", "if文は条件がfalseでも必ず本体を一度実行する。"),
						c("for文", "for文は初期化・継続条件・更新処理をまとめて記述できる。", "for文は繰り返しを一度も行えない。", "for文の更新式は必ず配列を作る。"),
						c("while文", "while文は条件を繰り返しの前に判定する。", "while文は条件を一度も判定せずに必ず実行する。", "while文の条件にはvoid型を指定する。"),
						c("break", "breakは現在のループまたはswitch文を抜ける。", "breakはメソッドの戻り値を返す。", "breakはプログラム全体を必ず終了する。"),
						c("continue", "continueは現在のループ反復の残りを飛ばして次の反復へ進む。", "continueはループを永久に終了する。", "continueは例外を投げる文である。")),
				chapter("第4章 配列", "配列の作成、参照、要素数を学びます。",
						c("配列の添字", "Javaの配列の添字は0から始まる。", "配列の最初の添字は1である。", "配列の添字は要素数と同じ値まで必ず有効である。"),
						c("配列のlength", "配列の要素数はlengthフィールドで取得する。", "配列の要素数はlength()メソッドで取得する。", "配列の要素数はsizeプロパティで取得する。"),
						c("配列の初期値", "newで作成したint配列の要素は初期値0になる。", "newで作成したint配列の要素は初期値1になる。", "配列要素はnew直後から必ず未定義値になる。"),
						c("配列の参照", "配列変数には配列オブジェクトへの参照が入る。", "配列変数には配列全要素が1つのintとして入る。", "配列を別変数へ代入すると要素が必ず複製される。"),
						c("範囲外アクセス", "有効範囲外の添字で配列を参照するとArrayIndexOutOfBoundsExceptionが発生する。", "範囲外の添字は常に最後の要素へ丸められる。", "範囲外の添字を使うと戻り値nullになる。")),
				chapter("第5章 メソッド", "引数、戻り値、オーバーロードを学びます。",
						c("戻り値", "戻り値の型がvoidでないメソッドは、型に合う値をreturnする必要がある。", "voidメソッドは必ずintを返す。", "return文はメソッドの外に書く。"),
						c("引数", "仮引数はメソッド宣言で受け取る値の名前と型である。", "引数には型を指定できない。", "実引数はメソッドの戻り値だけを指す。"),
						c("オーバーロード", "同じ名前でも引数の型または個数が異なればオーバーロードできる。", "戻り値の型だけを変えれば同じ引数でもオーバーロードできる。", "オーバーロードしたメソッド名は必ず別名にする。"),
						c("staticメソッド", "staticメソッドはインスタンスを作らずクラス名から呼び出せる。", "staticメソッドは必ずオブジェクトごとに別の状態を持つ。", "staticメソッドからstaticメンバーを参照できない。"),
						c("値渡し", "Javaの引数渡しは値渡しであり、参照型では参照値のコピーが渡される。", "Javaは呼び出し元の変数そのものを参照渡しする。", "メソッド呼び出しでは引数の値が渡らない。")),
				chapter("第6章 複数クラスを用いた開発", "クラスを分けてプログラムを構成する方法を学びます。",
						c("package", "package宣言は通常、ソースファイルの先頭に置く。", "package宣言はmainメソッドの中に書く。", "package宣言は必ずファイル末尾に置く。"),
						c("import", "importは別パッケージの型を単純名で使いやすくする。", "importはクラスを実行時に必ず生成する。", "importを書けば存在しないクラスも使用できる。"),
						c("publicクラスとファイル名", "publicトップレベルクラスのソースファイル名はクラス名に合わせる。", "publicクラスはファイル名と無関係な名前にできる。", "Javaソースファイルにはpublicクラスを必ず10個書く。"),
						c("アクセス修飾子", "publicメンバーはアクセス可能な範囲が広い。", "privateメンバーはすべてのクラスからアクセスできる。", "protectedはprivateより常に狭い範囲だけに公開する。"),
						c("クラスの責務", "関連するデータと処理をクラスにまとめると、役割を分けて管理しやすい。", "すべての処理を1つの巨大なメソッドに集めるのが必須である。", "複数クラスに分けるとJavaではコンパイルできない。")),
				chapter("第7章 オブジェクト指向をはじめよう", "オブジェクト、状態、振る舞いの考え方を学びます。",
						c("クラス", "クラスはオブジェクトの状態や振る舞いを定義する設計図として使える。", "クラスは必ず実行中に画面へ表示される値である。", "クラスは配列の別名である。"),
						c("オブジェクト", "オブジェクトはクラスをもとに作られ、状態と振る舞いを持てる。", "オブジェクトはソースコード上のコメントである。", "オブジェクトは必ず1つの数値だけを保持する。"),
						c("状態", "フィールドはオブジェクトの状態を表すデータを保持できる。", "フィールドは必ず画面の色だけを表す。", "状態はメソッドの戻り値だけで定義される。"),
						c("振る舞い", "メソッドはオブジェクトが行う処理を表せる。", "メソッドはデータを保存するファイル名である。", "メソッドは必ずフィールドを削除する。"),
						c("参照", "参照型変数はオブジェクトそのものではなく、そのオブジェクトを指す値を保持する。", "参照型変数は常にオブジェクトの全メモリ内容を複製する。", "参照型変数には数値型しか代入できない。")),
				chapter("第8章 インスタンスとクラス", "インスタンス化、フィールド、コンストラクタを学びます。",
						c("new", "new演算子でクラスのインスタンスを生成する。", "new演算子は既存の変数を削除する。", "new演算子はメソッドの戻り値をvoidにする。"),
						c("コンストラクタ", "コンストラクタはインスタンス生成時の初期化に使われ、クラス名と同じ名前を持つ。", "コンストラクタは戻り値型voidを必ず宣言する。", "コンストラクタ名はクラス名と異なる必要がある。"),
						c("this", "thisは現在のインスタンスを参照する。", "thisは常に親クラスのインスタンスを指す。", "thisはstaticメソッド内でも現在インスタンスを指す。"),
						c("インスタンスフィールド", "インスタンスフィールドは各インスタンスごとに値を持つ。", "インスタンスフィールドは全オブジェクトで必ず同じ値を共有する。", "インスタンスフィールドはメソッドから参照できない。"),
						c("staticフィールド", "staticフィールドはクラスに属し、インスタンス間で共有される。", "staticフィールドはインスタンス生成のたびに別々に作られる。", "staticフィールドはクラス名から参照できない。")),
				chapter("第9章 さまざまなクラス機構", "static、final、ネストした型などを学びます。",
						c("final変数", "final変数は初期化後に別の値を再代入できない。", "final変数は必ずnullになる。", "final変数は何度でも再代入できる。"),
						c("static初期化", "static初期化ブロックはクラス初期化時に実行される。", "static初期化ブロックはインスタンスメソッド呼び出しのたびに実行される。", "static初期化ブロックには処理を書けない。"),
						c("ネストクラス", "クラスの内部に別のクラスを宣言できる。", "クラスの中にはメソッドしか宣言できない。", "ネストクラスは外側のクラス名を持てない。"),
						c("enum", "enumは固定された選択肢を型安全に表すために使える。", "enumは文字列をコメントとして扱う構文である。", "enumの定数は実行時に自由に追加できる。"),
						c("可変長引数", "可変長引数はメソッド宣言で型...の形式を使い、配列として扱われる。", "可変長引数はメソッド呼び出しで引数を一切渡せない。", "可変長引数は宣言内で複数個指定できる。")),
				chapter("第10章 継承", "親クラス・子クラスとsuperを学びます。",
						c("extends", "クラスはextendsで1つのクラスを継承できる。", "クラスはextendsで複数のクラスを同時に継承できる。", "extendsはインターフェースを実装するためだけに使う。"),
						c("super", "superは親クラスのコンストラクタやメンバーを参照するために使える。", "superは子クラスの新しいインスタンスを生成する。", "superは常に現在のメソッドを終了する。"),
						c("コンストラクタ連鎖", "子クラスのコンストラクタから親クラスのコンストラクタを呼び出せる。", "親クラスのコンストラクタは子クラスから絶対に呼べない。", "コンストラクタ呼び出し後にsuper()を記述する。"),
						c("継承のアクセス", "子クラスはアクセス可能な親クラスのメンバーを利用できる。", "子クラスは親クラスのprivateフィールドへ直接アクセスできる。", "継承すると親クラスのメソッドはすべて削除される。"),
						c("is-a関係", "継承は子クラスが親クラスの一種である関係を表すときに適する。", "無関係なクラスでもコード量を減らすため必ず継承する。", "継承はオブジェクトを配列に変換する機能である。")),
				chapter("第11章 高度な継承", "抽象クラス、オーバーライド、継承時の制約を学びます。",
						c("abstractクラス", "abstractクラスは直接newできず、サブクラス化して利用できる。", "abstractクラスは必ずfinalである。", "abstractクラスには具象メソッドを定義できない。"),
						c("抽象メソッド", "抽象メソッドは本体を持たず、具象サブクラスで実装する必要がある。", "抽象メソッドは必ずprivateで宣言する。", "抽象メソッドは本体の中にreturn文を持つ。"),
						c("オーバーライド", "サブクラスは親クラスのインスタンスメソッドを適切なシグネチャでオーバーライドできる。", "戻り値型だけを変えれば常にオーバーライドになる。", "privateメソッドはサブクラスから通常の意味でオーバーライドできる。"),
						c("@Override", "@Overrideは意図したオーバーライドでない場合にコンパイル時の発見を助ける。", "@Overrideを付けるとメソッドが必ずstaticになる。", "@Overrideはメソッド呼び出しを禁止する。"),
						c("finalクラス", "finalクラスはサブクラス化できない。", "finalクラスはインスタンス化できない。", "finalクラスのメソッドはすべてabstractになる。")),
				chapter("第12章 多態性", "多態性と実行時のメソッド選択を学びます。",
						c("親型の参照", "親クラス型の変数で、そのサブクラスのインスタンスを参照できる。", "親クラス型の変数はサブクラスのインスタンスを参照できない。", "参照型とインスタンスの型は必ず同じ文字列である。"),
						c("動的ディスパッチ", "オーバーライドされたインスタンスメソッドは実行時の実際のオブジェクト型に応じて選ばれる。", "オーバーライドメソッドは常に変数の宣言型だけで選ばれる。", "動的ディスパッチはフィールドの初期値を無視する機能である。"),
						c("オーバーロード選択", "オーバーロードは主にコンパイル時に引数の型や個数で呼び出し先が選ばれる。", "オーバーロードは実行時のオブジェクト型だけで選ばれる。", "オーバーロードは引数の個数を確認しない。"),
						c("ダウンキャスト", "親型参照を子型へキャストする際は、実際のオブジェクト型との整合に注意する。", "どの親型参照も安全に任意の子型へキャストできる。", "ダウンキャストはオブジェクトを複製する。"),
						c("instanceof", "instanceofで実行時オブジェクトが指定型と互換か確認できる。", "instanceofは値を比較して数値を返す演算子である。", "instanceofはクラスを継承させる宣言である。")),
				chapter("第13章 カプセル化", "データの保護と公開範囲の制御を学びます。",
						c("privateフィールド", "privateフィールドで内部データへの直接アクセスをクラス内に制限できる。", "privateフィールドは全パッケージから自由にアクセスできる。", "privateを付けるとフィールドはstaticになる。"),
						c("getter", "getterはフィールド値を読み取るための公開メソッドとして使える。", "getterは必ずフィールド値を削除する。", "getterはコンストラクタを呼び出す予約語である。"),
						c("setterによる検証", "setter内で値を検証してからフィールドを更新できる。", "setterは検証を行うとJavaでコンパイルできない。", "setterは必ず引数を受け取らない。"),
						c("不変オブジェクト", "不変オブジェクトは生成後に観測可能な状態が変わらないよう設計する。", "不変オブジェクトはフィールドを必ずpublicにする。", "不変オブジェクトは生成直後に必ず破棄される。"),
						c("情報隠蔽", "実装の詳細を隠し、必要な操作だけを公開すると変更の影響を抑えやすい。", "情報隠蔽はクラスのすべてのメソッドを削除することを指す。", "情報隠蔽をするとオブジェクト指向は利用できない。")),
				chapter("第14章 Javaを支える標準クラス", "ラッパー型、数値処理など標準クラスを学びます。",
						c("ラッパークラス", "Integerはintの値をオブジェクトとして扱うためのラッパークラスである。", "Integerは配列専用の予約語である。", "Integerはintへ代入できないクラスである。"),
						c("オートボクシング", "対応するプリミティブ型とラッパー型の間ではオートボクシングやアンボクシングが行われる。", "オートボクシングは数値を必ず文字列に変換する。", "オートボクシングはすべての型間で無条件に変換する。"),
						c("nullアンボクシング", "nullのラッパー型をプリミティブ型へアンボクシングするとNullPointerExceptionになり得る。", "nullのIntegerをintへ変換すると必ず0になる。", "nullのアンボクシングはコンパイル時に必ず拒否される。"),
						c("BigDecimal", "BigDecimalは十進小数の精度を重視する金額計算などに使える。", "BigDecimalは常にdoubleより計算速度が速い。", "BigDecimalを使うと丸め規則を考える必要がなくなる。"),
						c("Math", "Mathクラスは平方根や絶対値などの数学処理を提供する。", "Mathクラスはインスタンスごとに異なる乱数状態だけを管理する。", "Mathクラスのメソッドはすべて入力をファイルへ保存する。")),
				chapter("第15章 文字列と日付の扱い", "String、StringBuilder、日付時刻APIを学びます。",
						c("Stringの不変性", "Stringは不変で、文字列を変更する操作は別のStringを返す。", "Stringの内容は+=を書くたびに同じオブジェクト内で必ず変更される。", "Stringは文字を一つも保持できない。"),
						c("文字列比較", "文字列の内容比較にはequalsを使う。", "文字列の内容比較には==だけを使う。", "equalsは文字列の長さを変更する。"),
						c("StringBuilder", "StringBuilderは文字列を繰り返し組み立てる用途に使える可変オブジェクトである。", "StringBuilderは内容を変更できない。", "StringBuilderは日付専用のクラスである。"),
						c("LocalDate", "LocalDateは時刻やタイムゾーンを含まない日付を表す。", "LocalDateは必ず時刻とタイムゾーンを保持する。", "LocalDateは可変で、日付加算で元の値が変更される。"),
						c("日付の不変性", "java.timeの主要な日付時刻型は不変で、操作結果として新しい値を返す。", "LocalDateを変更すると参照先の全インスタンスが変化する。", "java.timeの値はすべて文字列としてしか扱えない。")),
				chapter("第16章 コレクション", "リスト、セット、マップ、ジェネリクスを学びます。",
						c("ArrayList", "ArrayListは要素の順序を保ち、同じ値を複数格納できるList実装である。", "ArrayListは重複を自動的に禁止するSet実装である。", "ArrayListはキーと値の組だけを格納するMap実装である。"),
						c("HashSet", "HashSetは重複要素を許さず、要素の順序を保証しない。", "HashSetは挿入順を必ず維持する。", "HashSetは同じ要素を何度でも別要素として保存する。"),
						c("HashMap", "HashMapはキーと値の対応を保持し、キーは一意である。", "HashMapでは同じキーを複数の別エントリーとして保持する。", "HashMapの値にはnullを一切使えない。"),
						c("ジェネリクス", "ジェネリクスでコレクションに格納する要素型を指定できる。", "ジェネリクスは実行時にすべての型検査をなくす機能である。", "ジェネリクスはクラス名を短縮するコメントである。"),
						c("拡張for文", "拡張for文は配列やIterableの各要素を順に処理できる。", "拡張for文はMapのキーと値を必ず同時に2変数で返す。", "拡張for文は要素を必ず逆順に処理する。")),
				chapter("第17章 例外", "例外の種類、捕捉、送出を学びます。",
						c("try-catch", "try内で発生した例外は、型が合うcatch節で処理できる。", "catch節はtry節の前に必ず書く。", "catch節を書くと例外が発生しなくなる。"),
						c("finally", "finally節は通常、例外の有無にかかわらず後処理に使われる。", "finally節は例外が発生したときだけ実行される。", "finally節は戻り値を宣言するために使う。"),
						c("checked例外", "Exceptionの一部のサブクラスはchecked例外で、捕捉またはthrows宣言が必要である。", "checked例外は常に捕捉不要である。", "RuntimeExceptionの全サブクラスはchecked例外である。"),
						c("throwとthrows", "throwは例外オブジェクトを送出し、throwsはメソッド宣言で送出可能な例外を示す。", "throwとthrowsはどちらもcatch節の開始に使う。", "throwsは例外オブジェクトをその場で生成する演算子である。"),
						c("例外の継承順", "複数catchでは、より具体的な例外型を広い親型より先に記述する。", "catch節の順番はどの例外型でも自由である。", "親例外を先に捕捉しても、後ろの子例外catchが必ず実行される。")));
	}

	static List<BankQuestion> questionsFor(ChapterSeed chapter, QuestionDifficulty difficulty) {
		List<String> forms = switch (difficulty) {
			case EASY -> EASY_FORMS;
			case ADVANCED -> ADVANCED_FORMS;
			case BRONZE_STYLE -> BRONZE_FORMS;
		};
		// 5つの内容に4種類の問い方を用意するので、1Chapter20問になります。
		int variationsPerConcept = forms.size();
		List<BankQuestion> questions = new ArrayList<>(chapter.concepts().size() * variationsPerConcept);
		for (int conceptIndex = 0; conceptIndex < chapter.concepts().size(); conceptIndex++) {
			ConceptSeed concept = chapter.concepts().get(conceptIndex);
			for (int variation = 0; variation < variationsPerConcept; variation++) {
				String topic = difficulty == QuestionDifficulty.EASY
						? concept.topic()
						: concept.topic() + "（" + (difficulty == QuestionDifficulty.ADVANCED
								? ADVANCED_CONTEXTS.get(variation)
								: BRONZE_CONTEXTS.get(variation)) + "）";
				String form = forms.get(variation);
				String prompt = form.formatted(topic);
				List<String> choices = List.of(
						concept.answer(),
						concept.distractorOne(),
						concept.distractorTwo());
				int correctPosition = (conceptIndex + variation) % choices.size();
				List<String> orderedChoices = rotate(choices, correctPosition);
				int timeLimit = switch (difficulty) {
					case EASY -> 20;
					case ADVANCED -> 30;
					case BRONZE_STYLE -> 45;
				};
				questions.add(new BankQuestion(
						difficulty,
						prompt,
						orderedChoices,
						correctPosition,
						concept.answer(),
						timeLimit));
			}
		}
		return List.copyOf(questions);
	}

	private static List<String> rotate(List<String> choices, int correctPosition) {
		List<String> result = new ArrayList<>(choices.size());
		for (int index = 0; index < choices.size(); index++) {
			result.add(choices.get((index - correctPosition + choices.size()) % choices.size()));
		}
		return result;
	}

	private static ChapterSeed chapter(String name, String description, ConceptSeed... concepts) {
		return new ChapterSeed(name, description, List.of(concepts));
	}

	private static ConceptSeed c(String topic, String answer, String distractorOne, String distractorTwo) {
		return new ConceptSeed(topic, answer, distractorOne, distractorTwo);
	}

	record ChapterSeed(String name, String description, List<ConceptSeed> concepts) {
	}

	record ConceptSeed(String topic, String answer, String distractorOne, String distractorTwo) {
	}

	record BankQuestion(
			QuestionDifficulty difficulty,
			String questionText,
			List<String> choices,
			int correctChoiceIndex,
			String explanation,
			int timeLimitSeconds) {
	}
}
