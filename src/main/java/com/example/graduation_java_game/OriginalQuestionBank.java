package com.example.graduation_java_game;

import java.util.List;

/**
 * Java SE Bronzeの出題範囲を意識して作成した、重複のない第1章のオリジナル問題です。
 */
final class OriginalQuestionBank {

	private static final ChapterSeed CHAPTER_ONE = new ChapterSeed(
			"第1章 Javaの基本とプログラムの流れ",
			"Javaの特徴、プログラムの作成と実行、Javaのエディションを学びます。",
			List.of(
					q("CH1-01", "Javaの特徴", "バイトコードとJVMによる実行",
							QuestionDifficulty.EASY,
							"Javaのプログラムの実行方法を最も適切に説明しているものはどれですか？",
							null,
							choices(
									"JavaはソースコードをOSが直接実行する。",
									"Javaはソースコードをバイトコードにコンパイルし、JVMで実行する。",
									"Javaはソースコードを必ずC言語へ変換して実行する。",
									"Javaはコンパイルせず、JVMも使わずに実行する。",
									"Javaは実行前にソースコードを機械語へ手作業で書き換える。"),
							1,
							"Javaコンパイラはソースコードをバイトコードに変換し、JVMがそのバイトコードを実行します。",
							reasons(
									"誤り。ソースコードをOSが直接実行するのではなく、通常はコンパイルとJVMでの実行を行います。",
									"正解。ソースコード、バイトコード、JVMでの実行という流れがJavaの基本です。",
									"誤り。Javaの標準的なコンパイル結果はC言語のソースコードではなく、バイトコードです。",
									"誤り。通常のJava実行ではバイトコードとJVMを使います。",
									"誤り。機械語への手作業の書き換えはJavaの通常の実行手順ではありません。"),
							"ソースコードをコンパイルしてクラスファイルを作り、JVMで実行する流れを復習しましょう。",
							25),
					q("CH1-02", "Javaプログラムの作成と実行", "標準的なコンパイルと実行の順序",
							QuestionDifficulty.EASY,
							"Java 11で Main.java を通常のコンパイル手順で実行します。正しい順序はどれですか？",
							null,
							choices(
									"java Main.java → javac Main",
									"javac Main → java Main.class",
									"javac Main.java → java Main",
									"java Main → javac Main.java",
									"java Main.class → javac Main.java"),
							2,
							"通常の手順では、javacでソースファイルをコンパイルし、javaにクラス名を渡して実行します。",
							reasons(
									"誤り。通常のコンパイル手順では、先にjavacでコンパイルします。",
									"誤り。javacにはソースファイル名を指定し、javaには.classを付けないクラス名を指定します。",
									"正解。javac Main.javaでコンパイルし、java Mainで実行します。",
									"誤り。コンパイル前に通常のクラス実行はできません。",
									"誤り。Java 11にはソースファイル起動モードもありますが、ここで問う通常のコンパイル手順ではjava Mainを使います。"),
							"通常の手順と、Java 11以降で利用できるソースファイル起動モードを区別しましょう。",
							25),
					q("CH1-03", "Javaプログラムの作成と実行", "javacコマンドの役割",
							QuestionDifficulty.EASY,
							"次のコマンドを実行したとき、javacの役割として正しいものはどれですか？",
							"javac Main.java",
							choices(
									"Main.classをJVMで実行する。",
									"Javaの実行環境をインストールする。",
									"Main.javaを編集して保存する。",
									"Main.javaをコンパイルし、成功すれば通常Main.classを生成する。",
									"Main.javaを削除して実行結果だけを保存する。"),
							3,
							"javacはJavaコンパイラを起動するコマンドです。構文などを検査し、成功するとバイトコードを含む.classファイルを生成します。",
							reasons(
									"誤り。クラスファイルを実行するコマンドはjavaです。",
									"誤り。javacはコンパイラを起動するコマンドで、インストーラーではありません。",
									"誤り。ソースファイルを編集・保存するのはエディターなどの役割です。",
									"正解。javacはソースファイルをコンパイルし、通常クラスファイルを生成します。",
									"誤り。コンパイルはソースファイルを削除しません。"),
							"javacはコンパイル、javaは実行という役割の違いを復習しましょう。",
							25),
					q("CH1-04", "Javaプログラムの作成と実行", "javaコマンドに指定する名前",
							QuestionDifficulty.EASY,
							"Main.classがクラスパス上にあり、パッケージ宣言がない場合、実行コマンドとして正しいものはどれですか？",
							null,
							choices(
									"java Main",
									"javac Main",
									"java Main.class",
									"javac Main.class",
									"java Main.java"),
							0,
							"通常のクラス実行では、javaコマンドに拡張子を付けないクラス名を指定します。",
							reasons(
									"正解。クラスパス上のMainクラスを実行する標準的な指定です。",
									"誤り。javacはコンパイル用で、実行コマンドではありません。",
									"誤り。javaコマンドには通常.class拡張子を付けません。",
									"誤り。javacはソースファイルをコンパイルするコマンドです。",
									"誤り。Java 11以降ならソースファイル起動モードで利用できますが、設問は既にあるクラスファイルの通常実行を尋ねています。"),
							"通常のクラス実行ではjavaの後にクラス名を指定し、.classは付けません。",
							25),
					q("CH1-05", "Javaソースとクラスファイル", ".javaと.classの違い",
							QuestionDifficulty.EASY,
							"Javaのソースファイルと、通常のコンパイルで生成されるファイルの組み合わせとして正しいものはどれですか？",
							null,
							choices(
									"ソースファイルは.class、コンパイル後は.java",
									"ソースファイルは.java、コンパイル後のクラスファイルは.class",
									"どちらも必ず.txt",
									"ソースファイルは.jar、コンパイル後は.exe",
									"ソースファイルは.jvm、コンパイル後は.jdk"),
							1,
							"人が記述するJavaソースは.java、コンパイルによって作られるクラスファイルは.classです。",
							reasons(
									"誤り。.javaと.classの対応が逆です。",
									"正解。.javaはソースファイル、.classはコンパイル後のクラスファイルです。",
									"誤り。どちらも必ず.txtになるわけではありません。",
									"誤り。.jarは複数のクラスなどをまとめる形式で、.exeはJavaコンパイルの標準出力形式ではありません。",
									"誤り。.jvmや.jdkはこの用途の標準的なファイル拡張子ではありません。"),
							".javaはソースコード、.classはバイトコードを含むクラスファイルです。",
							20),
					q("CH1-06", "Javaソースとクラスファイル", "publicクラスとソースファイル名",
							QuestionDifficulty.EASY,
							"次の宣言を含むソースファイルの名前として正しいものはどれですか？",
							"public class Welcome {\n}",
							choices(
									"welcome.java",
									"Main.java",
									"Welcome.java",
									"Welcome.class",
									"public.java"),
							2,
							"通常のファイルシステム上のJavaソースでは、publicトップレベルクラス名とソースファイル名を大文字・小文字も含めて一致させます。",
							reasons(
									"誤り。クラス名Welcomeと大文字・小文字が一致しません。",
									"誤り。publicクラス名はMainではなくWelcomeです。",
									"正解。publicクラスWelcomeに対応するソースファイル名はWelcome.javaです。",
									"誤り。.classはコンパイル後のファイル拡張子です。",
									"誤り。ファイル名はpublicというキーワードではなく、クラス名に合わせます。"),
							"publicトップレベルクラスがある場合のクラス名とファイル名の対応を復習しましょう。",
							25),
					q("CH1-07", "コンパイルと実行時の動作", "コンパイルエラーと実行時エラー",
							QuestionDifficulty.EASY,
							"存在しない変数名を式に書いたとき、通常どの段階で問題が見つかりますか？",
							"int total = missingValue + 1;",
							choices(
									"JVMが実行し、必ずNullPointerExceptionになる。",
									"画面への出力時だけに検出される。",
									"コンパイル時に、宣言されていない名前としてエラーになる。",
									"プログラム終了後にだけ検出される。",
									"Javaでは変数名を自由に書けるため、エラーにならない。"),
							2,
							"missingValueが宣言されていないため、コンパイラが名前を解決できず、通常は実行前にコンパイルエラーになります。",
							reasons(
									"誤り。未宣言の名前は通常コンパイルできず、NullPointerExceptionまで実行されません。",
									"誤り。問題は出力処理ではなく、未宣言の名前です。",
									"正解。コンパイラが未宣言の変数を検出します。",
									"誤り。通常はプログラムを実行する前のコンパイル段階で検出されます。",
									"誤り。変数は宣言されている必要があります。"),
							"ソースコードの文法・名前の誤りはコンパイル時、実行中に起きる問題は実行時という区別を復習しましょう。",
							30),
					q("CH1-08", "mainメソッド", "mainメソッドが開始点になる条件",
							QuestionDifficulty.ADVANCED,
							"Java 11の通常のクラス起動で、アプリケーションの開始メソッドとして認識される宣言はどれですか？",
							null,
							choices(
									"public void main(String[] args)",
									"private static void main(String[] args)",
									"public static void main(String[] args)",
									"public static int main(String[] args)",
									"public static void start(String[] args)"),
							2,
							"通常のJavaアプリケーションの開始メソッドはpublic static void main(String[] args)の形式です。",
							reasons(
									"誤り。staticがないため、標準の開始メソッドの形式ではありません。",
									"誤り。privateでは起動用のmainとして認識されません。",
									"正解。public、static、void、main、String配列の引数を持つ標準形式です。",
									"誤り。戻り値はintではなくvoidです。",
									"誤り。メソッド名はstartではなくmainです。"),
							"public・static・void・main・String配列の引数という標準形式を確認しましょう。",
							30),
					q("CH1-09", "mainメソッド", "開始メソッドの戻り値",
							QuestionDifficulty.ADVANCED,
							"次の候補のうち、Javaアプリケーションの標準の開始メソッドとして誤っている宣言はどれですか？",
							null,
							choices(
									"public static void main(String[] args)",
									"public static void main(String args[])",
									"static public void main(String[] args)",
									"public static int main(String[] args)",
									"public static void main(String... args)"),
							3,
							"標準の開始メソッドは戻り値がvoidです。intを返す宣言は起動用のmainの条件を満たしません。",
							reasons(
									"正しい形式です。",
									"正しい形式です。配列の角括弧は型名の後ろにも書けます。",
									"正しい形式です。修飾子の順序は入れ替えられます。",
									"正解。標準の開始メソッドの戻り値はvoidであり、intではありません。",
									"正しい形式です。String...は可変長引数で、String[]と同等の配列引数として扱われます。"),
							"正しい形式には書き方のバリエーションがありますが、戻り値型はvoidであることを復習しましょう。",
							35),
					q("CH1-10", "mainメソッド", "mainメソッドのオーバーロード",
							QuestionDifficulty.ADVANCED,
							"次のクラスを java Launcher で起動したとき、最初に呼び出されるメソッドはどれですか？",
							"class Launcher {\n"
									+ "    public static void main(String[] args) {\n"
									+ "        System.out.println(\"配列引数\");\n"
									+ "    }\n"
									+ "    public static void main() {\n"
									+ "        System.out.println(\"引数なし\");\n"
									+ "    }\n"
									+ "}",
							choices(
									"引数なしのmainだけが呼ばれる。",
									"String[]を引数に取るmainが起動用メソッドとして呼ばれる。",
									"どちらも同時に呼ばれる。",
									"同名のため必ずコンパイルエラーになる。",
									"戻り値がないため、どちらも呼ばれない。"),
							1,
							"mainという名前のメソッドを複数定義できますが、通常の起動ではString配列を受け取る標準形式が呼び出されます。",
							reasons(
									"誤り。起動用として呼び出されるのは標準形式のString配列を受け取る方です。",
									"正解。起動時にランチャーが探す標準形式のmainです。",
									"誤り。オーバーロードされたメソッドが自動的に両方呼び出されるわけではありません。",
									"誤り。引数の個数が異なるため、オーバーロードとして定義できます。",
									"誤り。voidは標準形式の戻り値として正しい型です。"),
							"同じ名前で引数が異なるメソッドを定義するオーバーロードと、起動時に探されるmainの形式を区別しましょう。",
							45),
					q("CH1-11", "mainメソッド", "コマンドライン引数の型",
							QuestionDifficulty.ADVANCED,
							"mainメソッドの引数がString[]である理由として最も適切なものはどれですか？",
							"public static void main(String[] args)",
							choices(
									"実行時に渡す引数は、最初からすべてint型だから。",
									"String[]は画面表示を行う専用の型だから。",
									"argsはJVMのメモリ量を示す数値だから。",
									"コマンドラインから渡された引数を文字列の配列として受け取るから。",
									"String[]を使うと引数を受け取れなくなるから。"),
							3,
							"起動時に渡した値は文字列としてargsに入り、必要ならプログラム内で数値などへ変換します。",
							reasons(
									"誤り。コマンドライン引数はStringとして受け取ります。",
									"誤り。String[]は文字列の配列を表し、画面表示専用ではありません。",
									"誤り。argsは起動時引数であり、メモリ量ではありません。",
									"正解。コマンドライン引数はStringの配列として受け取ります。",
									"誤り。String[] argsは引数を受け取るための宣言です。"),
							"argsの各要素は文字列です。数値として使うときは適切な変換が必要です。",
							35),
					q("CH1-12", "Javaの実行環境", "JDK・JRE・JVMの役割",
							QuestionDifficulty.ADVANCED,
							"JDK・JRE・JVMの役割を最も適切に説明しているものはどれですか？",
							null,
							choices(
									"JDKはJavaのソースコード、JREはコンパイラ、JVMはOSを表す。",
									"JDKは開発用ツールを含む開発キット、JREは実行に必要な環境という概念、JVMはバイトコードを実行する仮想マシンである。",
									"JDK・JRE・JVMはすべてコンパイラの別名である。",
									"JVMはJavaソースを編集するエディターである。",
									"JREはJavaのエディション名で、Java SEと同じ意味である。"),
							1,
							"JDKは開発ツールを含むキット、JREは実行環境を指す概念、JVMはバイトコード実行の中心となる仮想マシンです。配布形態はJavaのバージョンや製品により異なります。",
							reasons(
									"誤り。各用語の役割が異なります。",
									"正解。開発、実行環境、バイトコード実行という役割を区別しています。JREが独立した製品として配布されるかは環境によります。",
									"誤り。JDK・JRE・JVMは同じものではありません。",
									"誤り。JVMはソースコード編集用のエディターではありません。",
									"誤り。JREは実行環境を表す概念であり、Java SEというエディション名ではありません。"),
							"JDK・JRE・JVMの役割と、役割の説明と製品としての配布形態は別であることを復習しましょう。",
							45),
					q("CH1-13", "Javaプログラムの作成と実行", "Java 11のソースファイル起動",
							QuestionDifficulty.ADVANCED,
							"Java 11のソースファイル起動モードについて正しい説明はどれですか？",
							null,
							choices(
									"java Main.javaはどのJavaバージョンでも使えるため、Java 8でも標準機能である。",
									"ソースファイル起動モードでは、JVMを使わずOSがJavaソースを直接実行する。",
									"javac Main.javaを実行しない限り、Java 11でもソースファイルを起動できない。",
									"ソースファイル起動モードでは、必ず先に手作業で.classファイルを作る。",
									"Java 11ではjava Main.javaのようにソースファイルを指定して実行できる。"),
							4,
							"Java 11では単一ソースファイル起動モードが追加され、javaコマンドに.javaファイルを指定して実行できます。これは通常のjavac後にクラス名で起動する手順とは別の機能です。",
							reasons(
									"誤り。この起動モードはJava 11で導入され、Java 8の標準機能ではありません。",
									"誤り。ソースファイル起動モードもJavaランタイムを使います。",
									"誤り。Java 11以降では、単一ソースファイルをjavaコマンドで起動できます。",
									"誤り。このモードでは、利用者が事前にjavacで.classを作る必要はありません。",
									"正解。Java 11以降ではjava Main.javaの形でソースファイル起動ができます。"),
							"Java 11のソースファイル起動モードと、javacでコンパイルしてから起動する通常手順を区別しましょう。",
							45),
					q("CH1-14", "Javaプログラムの作成と実行", "パッケージを含むクラスの起動",
							QuestionDifficulty.ADVANCED,
							"package lesson;と宣言されたMainクラスを、クラスパスのルートから起動するときの指定として正しいものはどれですか？",
							"package lesson;\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"start\");\n    }\n}",
							choices(
									"java lesson.Main",
									"java lesson/Main.class",
									"javac lesson.Main",
									"java Main.class",
									"java package.lesson.Main.java"),
							0,
							"パッケージに属するクラスは、通常、javaコマンドに完全修飾クラス名（パッケージ名とクラス名）を指定して起動します。",
							reasons(
									"正解。lessonがパッケージ名、Mainがクラス名です。",
									"誤り。通常のクラス起動ではパス表記や.class拡張子を付けません。",
									"誤り。javacはソースファイル名を使ってコンパイルします。",
									"誤り。パッケージ名が省かれ、.class拡張子も付いています。",
									"誤り。packageキーワードをクラス名の一部として指定することはありません。"),
							"package宣言と完全修飾クラス名、およびjavaコマンドでのクラス指定を復習しましょう。",
							45),
					q("CH1-15", "Javaのエディション", "Java SEの用途",
							QuestionDifficulty.BRONZE_STYLE,
							"Java SEの説明として最も適切なものはどれですか？",
							null,
							choices(
									"Javaの仮想マシンだけを指す名称である。",
									"Javaの基本言語機能と標準APIを提供する基盤で、学習用や一般的なアプリ開発にも使われる。",
									"企業向けWebアプリケーション専用の仕様である。",
									"組み込み機器専用で、一般的なJavaプログラムには使えない。",
									"Javaソースを保存するファイル拡張子である。"),
							1,
							"Java SE（Standard Edition）は、Javaの基本機能と標準APIを学び、一般的なアプリケーションを作るための標準的な基盤です。",
							reasons(
									"誤り。仮想マシンはJVMであり、Java SEそのものではありません。",
									"正解。Java SEは基本言語と標準APIの基盤です。",
									"誤り。企業向けの追加仕様はJava EE（現在のJakarta EE）に関連します。",
									"誤り。組み込み用途を主対象とするものはJava MEです。",
									"誤り。Javaソースファイルの拡張子は.javaです。"),
							"SEはStandard Editionの略で、Javaの基本機能を学ぶときの土台になります。",
							30),
					q("CH1-16", "Javaのエディション", "Java EEとJakarta EEの位置付け",
							QuestionDifficulty.BRONZE_STYLE,
							"Java EEについて、現在の名称も踏まえた説明として最も適切なものはどれですか？",
							null,
							choices(
									"Java SEの別名であり、同じ仕様を指す。",
									"Javaコンパイラの製品名である。",
									"小型組み込み機器だけを対象にしたエディションである。",
									"企業向けサーバーアプリケーションの仕様群として発展し、現在はJakarta EEの名称で継続している。",
									"Javaのソースファイル形式を定める仕様である。"),
							3,
							"Java EEは企業向けサーバーアプリケーションの仕様群として使われた名称で、現在はJakarta EEとして発展しています。",
							reasons(
									"誤り。Java SEとJava EEは用途の異なる仕様群です。",
									"誤り。コンパイラ製品名ではありません。",
									"誤り。小型機器・組み込み分野はJava MEの主な対象です。",
									"正解。企業向けサーバーアプリケーションを支える仕様群で、後継の名称はJakarta EEです。",
									"誤り。ソースファイルの形式を定めるものではありません。"),
							"試験範囲で使われるJava EEという名称と、現在のJakarta EEという名称の関係を覚えましょう。",
							40),
					q("CH1-17", "Javaのエディション", "Java MEの用途",
							QuestionDifficulty.BRONZE_STYLE,
							"Java MEが主に想定する開発対象として最も適切なものはどれですか？",
							null,
							choices(
									"Javaの文法を変換するコンパイラ",
									"企業の大規模サーバーアプリケーションだけ",
									"機能や資源に制約がある小型機器・組み込み環境向けのアプリケーション",
									"Javaの標準出力専用のアプリケーション",
									"WindowsのOSそのもの"),
							2,
							"Java ME（Micro Edition）は、機器の能力や利用できる資源に制約がある環境向けのJava仕様です。",
							reasons(
									"誤り。コンパイラは開発ツールであり、Java MEの用途ではありません。",
									"誤り。企業向けサーバー用途はJava EE/Jakarta EEの領域です。",
									"正解。Java MEは小型機器や組み込み環境などを想定します。",
									"誤り。標準出力はJavaの基本APIの一部で、Java ME専用ではありません。",
									"誤り。Java MEはOSそのものを開発するエディションではありません。"),
							"MEはMicro Editionの略で、小型機器などの環境を対象にする仕様です。",
							35),
					q("CH1-18", "Javaの特徴", "異なるOSで実行する条件",
							QuestionDifficulty.BRONZE_STYLE,
							"あるOSでコンパイルしたJavaプログラムを別のOSで実行する説明として正しいものはどれですか？",
							null,
							choices(
									"対象OSに対応したJVMや必要なライブラリがあれば、同じバイトコードを実行できる場合がある。",
									"JVMがなくても、すべてのOSが.classファイルを直接実行できる。",
									"Javaは一度コンパイルすると、OSやCPUを問わず必ず動作する。",
									"移植には必ずソースコードを別のプログラミング言語へ書き直す必要がある。",
									"JavaのバイトコードはコンパイルしたOS専用の機械語である。"),
							0,
							"Javaの移植性は、バイトコードを各環境向けJVMが実行する仕組みによります。ただし、対応JVMや外部ライブラリなどの条件が必要です。",
							reasons(
									"正解。対象環境のJVMと依存ライブラリがそろえば、同じバイトコードを実行できる場合があります。",
									"誤り。.classファイルを直接実行するのではなく、通常JVMが必要です。",
									"誤り。OS、CPU、JVMの対応状況、外部依存などの条件があるため、無条件に動くわけではありません。",
									"誤り。移植性のあるJavaコードなら、別言語へ書き換えずに実行できる場合があります。",
									"誤り。バイトコードは特定OS専用の機械語ではありません。"),
							"「どこでも動く」はJVMなどの実行環境があることを前提とした表現です。",
							45),
					q("CH1-19", "Javaの特徴", "JVMと実行環境の関係",
							QuestionDifficulty.BRONZE_STYLE,
							"JavaのバイトコードとJVMについて正しい説明はどれですか？",
							null,
							choices(
									"バイトコードは人が読むJavaソースコードそのものである。",
									"JVMは.javaファイルの編集専用アプリケーションである。",
									"JVMがなくても、バイトコードをOSが必ず直接実行する。",
									"バイトコードはJVMが実行するための形式で、JVMは実行環境ごとに用意される。",
									"JVMはjavacの別名であり、どちらもコンパイルだけを行う。"),
							3,
							"バイトコードはJavaコンパイラが生成する中間形式で、JVMがそれを実行します。JVMの実装は実行環境に対応して提供されます。",
							reasons(
									"誤り。人が記述するソースコードは.javaファイルで、バイトコードとは別の形式です。",
									"誤り。JVMはソースコード編集用のアプリケーションではありません。",
									"誤り。通常、バイトコードの実行にはJVMが必要です。",
									"正解。バイトコードはJVMで実行され、JVM自体はOSなどの実行環境に合わせて提供されます。",
									"誤り。javacはコンパイラ、JVMはバイトコードの実行環境です。"),
							"バイトコード、JVM、javacそれぞれの役割を混同しないようにしましょう。",
							40),
					q("CH1-20", "Javaプログラムの作成と実行", "標準出力とプログラムの実行",
							QuestionDifficulty.BRONZE_STYLE,
							"次のmainメソッドを実行したとき、System.out.printlnの動作として正しいものはどれですか？",
							"public static void main(String[] args) {\n"
									+ "    System.out.println(\"開始\");\n"
									+ "}",
							choices(
									"文字列を標準出力へ表示し、その後に改行する。",
									"文字列を標準入力から読み込む。",
									"文字列を表示してから、必ずJVMを終了する。",
									"文字列を.classファイルへ書き込む。",
									"文字列の内容に関係なくコンパイルエラーになる。"),
							0,
							"System.out.printlnは値を標準出力へ出力し、最後に改行します。これ自体がプログラムを終了させるわけではありません。",
							reasons(
									"正解。標準出力へ文字列を出し、printlnの名のとおり改行します。",
									"誤り。標準入力から読む処理ではありません。",
									"誤り。printlnは出力後にJVMを終了させません。",
									"誤り。クラスファイルを生成する処理はコンパイル時に行います。",
									"誤り。示されたコードは正しいJava構文です。"),
							"System.outは標準出力、printlnは出力後に改行するメソッドです。",
							35)));

	private OriginalQuestionBank() {
	}

	static List<ChapterSeed> chapters() {
		return java.util.stream.Stream.of(
				List.of(CHAPTER_ONE),
				OriginalQuestionBankChapters2To4.chapters(),
				OriginalQuestionBankChapters5To6.chapters(),
				OriginalQuestionBankChapters7To8.chapters())
				.flatMap(List::stream)
				.toList();
	}

	static BankQuestion q(
			String id,
			String checklist,
			String theme,
			QuestionDifficulty difficulty,
			String prompt,
			String codeBlock,
			List<String> choices,
			int correctChoiceIndex,
			String explanation,
			List<String> choiceReasons,
			String review,
			int timeLimitSeconds) {
		// 問題IDから正解位置を安定して分散し、正解位置自体が手掛かりになるのを防ぎます。
		int displayedCorrectChoiceIndex = Math.floorMod(id.hashCode(), choices.size());
		int choiceRotation = Math.floorMod(
				displayedCorrectChoiceIndex - correctChoiceIndex,
				choices.size());
		List<String> displayedChoices = rotate(choices, choiceRotation);
		List<String> displayedReasons = rotate(choiceReasons, choiceRotation);
		String detailedExplanation = "対応する出題範囲: " + checklist
				+ "\n学習テーマ: " + theme
				+ "\n\n正解: " + (char) ('A' + displayedCorrectChoiceIndex)
				+ "\n" + explanation
				+ "\n\n各選択肢の解説:\n"
				+ java.util.stream.IntStream.range(0, displayedChoices.size())
						.mapToObj(index -> (char) ('A' + index) + ". " + displayedReasons.get(index))
						.collect(java.util.stream.Collectors.joining("\n"))
				+ "\n\n間違えた場合の復習: " + review;
		return new BankQuestion(
				difficulty,
				"[" + id + "] " + theme + "\n" + prompt,
				codeBlock,
				displayedChoices,
				displayedCorrectChoiceIndex,
				explanation,
				detailedExplanation,
				timeLimitSeconds);
	}

	private static List<String> rotate(List<String> values, int offset) {
		return java.util.stream.IntStream.range(0, values.size())
				.mapToObj(index -> values.get(
						(index - offset + values.size()) % values.size()))
				.toList();
	}

	static List<String> choices(String a, String b, String c, String d, String e) {
		return List.of(a, b, c, d, e);
	}

	static List<String> reasons(String a, String b, String c, String d, String e) {
		return List.of(a, b, c, d, e);
	}

	record ChapterSeed(String name, String description, List<BankQuestion> questions) {
	}

	record BankQuestion(
			QuestionDifficulty difficulty,
			String questionText,
			String codeBlock,
			List<String> choices,
			int correctChoiceIndex,
			String explanation,
			String detailedExplanation,
			int timeLimitSeconds) {
	}
}
