package ast;

import ast.Ast.*;
import ast.Ast.Exp.*;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.While;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import util.Id;
import util.Tuple;

import java.util.List;

public class SamplePrograms {
    // Lab2, exercise 2: read the following code and make
    // sure you understand how the sample program "test/Factorial.java" is
    // encoded.

    // /////////////////////////////////////////////////////
    // To represent the "Factorial.java" program in memory manually
    // this is for demonstration purpose only, and
    // no one would want to do this in reality (boring and error-prone).
    /*
     * class Factorial {
     *     public static void main(String[] a) {
     *         System.out.println(new Fac().ComputeFac(10));
     *     }
     * }
     *
     * class Fac {
     *     public int ComputeFac(int num) {
     *         int num_aux;
     *         if (num < 1)
     *             num_aux = 1;
     *         else
     *             num_aux = num * (this.ComputeFac(num-1));
     *         return num_aux;
     *     }
     * }
     */

    // // main class: "Factorial"
    static MainClass.T factorial = new MainClass.Singleton(
            Id.newName("Factorial"),
            new AstId(Id.newName("a")),
            new Print(new Call(new NewObject(Id.newName("Fac")),
                    new AstId(Id.newName("ComputeFac")),
                    List.of(new Num(10)),
                    new Tuple.One<>(),
                    new Tuple.One<>())));

    // // class "Fac"
    static ast.Ast.Class.T fac = new ast.Ast.Class.Singleton(
            Id.newName("Fac"),
            null,
            List.of(), // arguments
            List.of(new Method.Singleton(
                    Type.getInt(),
                    new AstId(Id.newName("ComputeFac")),
                    List.of(new Dec.Singleton(Type.getInt(), new AstId(Id.newName("num")))),
                    List.of(new Dec.Singleton(Type.getInt(), new AstId(Id.newName("num_aux")))),
                    List.of(new If(
                            new Bop(new ExpId(new AstId(Id.newName("num"))),
                                    "<",
                                    new Num(1)),
                            new Assign(new AstId(Id.newName("num_aux")), new Num(1)),
                            new Assign(
                                    new AstId(Id.newName("num_aux")),
                                    new Bop(new ExpId(new AstId(Id.newName("num"))),
                                            "*",
                                            new Call(new This(), new AstId(Id.newName("ComputeFac")),
                                                    List.of(new Bop(new ExpId(new AstId(Id.newName("num"))),
                                                            "-",
                                                            new Num(1))),
                                                    new Tuple.One<>(),
                                                    new Tuple.One<>()))))),
                    new ExpId(new AstId(Id.newName("num_aux"))))),
            new Tuple.One<>());

    // program
    public static Program.T progFac = new Program.Singleton(factorial, List.of(fac));


    // to encode "test/SumRec.java"
//    class SumRec {
//        public static void main(String[] a) {
//            System.out.println(new Doit().doit(100));
//        }
//    }
//
//    class Doit {
//        public int doit(int n) {
//            int sum;
//            if (n < 1)
//                sum = 0;
//            else
//                sum = n + (this.doit(n - 1));
//            return sum;
//        }
//    }
    static MainClass.T sumRec = new MainClass.Singleton(
            Id.newName("Factorial"),
            new AstId(Id.newName("n")),
            new Print(new Call(new NewObject(Id.newName("Doit")),
                    new AstId(Id.newName("doit")),
                    List.of(new Num(100)),
                    new Tuple.One<>(),
                    new Tuple.One<>())));

    // // class "Doit"
    static ast.Ast.Class.T doitSumRec = new ast.Ast.Class.Singleton(
            Id.newName("Doit"),
            null,
            List.of(),
            List.of(new Method.Singleton(
                    Type.getInt(),
                    new AstId(Id.newName("doit")),
                    List.of(new Dec.Singleton(Type.getInt(), new AstId(Id.newName("n")))),
                    List.of(new Dec.Singleton(Type.getInt(), new AstId(Id.newName("sum")))),
                    List.of(new If(
                            new Bop(new ExpId(new AstId(Id.newName("n"))),
                                    "<",
                                    new Num(1)),
                            new Assign(new AstId(Id.newName("sum")), new Num(0)),
                            new Assign(
                                    new AstId(Id.newName("sum")),
                                    new Bop(new ExpId(new AstId(Id.newName("n"))),
                                            "+",
                                            new Call(new This(),
                                                    new AstId(Id.newName("doit")),
                                                    List.of(new Bop(new ExpId(new AstId(Id.newName("n"))),
                                                            "-",
                                                            new Num(1))),
                                                    new Tuple.One<>(),
                                                    new Tuple.One<>()))))),
                    new ExpId(new AstId(Id.newName("sum"))))),
            new Tuple.One<>());

    public static Program.T progSumRec = new Program.Singleton(sumRec, List.of(doitSumRec));


    // Lab2, exercise 2: you should write some code to
    // encode the program "test/Sum.java".
    // Your code here:
    // public static Program.T progSum = null;
    static MainClass.T sum = new MainClass.Singleton(
            Id.newName("Sum"),
            new AstId(Id.newName("a")),
            new Print(new Call(new NewObject(Id.newName("Doit")),
                    new AstId(Id.newName("doit")),
                    List.of(new Num(101)),
                    new Tuple.One<>(),
                    new Tuple.One<>()))
    );

    static ast.Ast.Class.T doit = new Ast.Class.Singleton(
            Id.newName("Doit"),
            null,
            List.of(),
            // 方法声明
            List.of(new Method.Singleton(
                    Type.getInt(),
                    new AstId(Id.newName("doit")),
                    // 形参列表
                    List.of(new Dec.Singleton(Type.getInt(), new AstId(Id.newName("n")))),
                    // 变量声明语句
                    List.of(new Dec.Singleton(Type.getInt(), new AstId(Id.newName("sum"))),
                            new Dec.Singleton(Type.getInt(), new AstId(Id.newName("i")))),
                    // statements 列表
                    List.of(new Assign(new AstId(Id.newName("i")), new Num(0)),
                            new Assign(new AstId(Id.newName("sum")), new Num(0)),
                            new While(
                                    // 条件 cond
                                    new Bop(
                                            new ExpId(new AstId(Id.newName("i"))),
                                            "<",
                                            new ExpId(new AstId(Id.newName("n")))),
                                    new Assign(
                                            new AstId(Id.newName("sum")),
                                            new Bop(
                                                    new ExpId(new AstId(Id.newName("sum"))),
                                                    "+",
                                                    new ExpId(new AstId(Id.newName("i")))
                                            )
                                    )
                            )
                    ),
                    // 返回值
                    new ExpId(new AstId(Id.newName("sum")))
            )),
            new Tuple.One<>()
    );

    public static Program.T progSum = new Program.Singleton(sum, List.of(doit));

}




