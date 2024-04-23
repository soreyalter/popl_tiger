package cfg;

import util.Id;
import util.Label;
import util.Todo;

import java.util.List;

public class Cfg {

    // the pretty printer
    private static int indentLevel = 0;

    private static void indent() {
        indentLevel += 4;
    }

    private static void unIndent() {
        indentLevel -= 4;
    }

    private static void printSpaces() {
        int i = indentLevel;
        while (i-- != 0) {
            say(" ");
        }
    }

    private static <T> void sayln(T s) {
        System.out.println(s);
    }

    private static <T> void say(T s) {
        System.out.print(s);
    }

    //  ///////////////////////////////////////////////////////////
    //  type
    public static class Type {
        public sealed interface T
                permits ClassType, Int, IntArray, CodePtr {
        }

        public record Int() implements T {
        }

        public record ClassType(Id id) implements T {
        }

        public record IntArray() implements T {
        }

        public record CodePtr() implements T {
        }

        public static void pp(T ty) {
            switch (ty) {
                case Int() -> say("int");
                case ClassType(Id id) -> say(id.toString());
                case IntArray() -> say("int[]");
                case CodePtr() -> say("CodePtr");
            }
        }
    }

    // ///////////////////////////////////////////////////
    // declaration
    public static class Dec {
        public sealed interface T permits Singleton {
        }

        public record Singleton(Type.T type,
                                Id id) implements T {
            // only compare "id"
            @Override
            public boolean equals(Object o) {
                if (o == null)
                    return false;
                if (!(o instanceof Singleton))
                    return false;
                return this.id().equals(((Dec.Singleton) o).id());
            }

            @Override
            public int hashCode() {
                return this.id().hashCode();
            }
        }

        public static void pp(T dec) {
            switch (dec) {
                case Singleton(Type.T type, Id id) -> {
                    Type.pp(type);
                    say(STR." \{id}");
                }
            }
        }


    }

    // /////////////////////////////////////////////////////////
    // virtual function table
    public static class Vtable {
        public sealed interface T permits Singleton {
        }

        public record Entry(Type.T retType,
                            Id classId,
                            Id functionId,
                            List<Dec.T> argTypes) {
        }

        public record Singleton(Id name,
                                List<Entry> funcTypes) implements T {
        }

        public static void pp(T vtable) {
            switch (vtable) {
                case Singleton(Id name, List<Entry> funcTypes) -> {
                    printSpaces();
                    say(STR."""
struct V_\{name} {
""");
                    // all entries
                    indent();
                    for (Entry e : funcTypes) {
                        printSpaces();
                        Type.pp(e.retType);
                        say(STR." \{e.functionId}(");
                        for (Dec.T dec : e.argTypes) {
                            Dec.pp(dec);
                            say(", ");
                        }
                        say(");\n");
                    }
                    unIndent();
                    printSpaces();
                    say(STR."""
} V_\{name}_ = {
""");
                    indent();
                    for (Entry e : funcTypes) {
                        printSpaces();
                        say(STR.".\{e.functionId} = \{e.classId}_\{e.functionId}");
                        say(",\n");
                    }
                    unIndent();
                    printSpaces();
                    say("};\n\n");
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////
    // structures
    public static class Struct {
        public sealed interface T
                permits Singleton {
        }

        public record Singleton(Id className,
                                List<Cfg.Dec.T> fields) implements T {
        }

        public static void pp(T s) {
            switch (s) {
                case Singleton(
                        Id clsName,
                        List<Cfg.Dec.T> fields
                ) -> {
                    printSpaces();
                    say(STR."""
struct S_\{clsName.toString()} {
""");
                    indent();
                    // the first field is special
                    printSpaces();
                    say(STR."""
struct V_\{clsName} *vptr;
""");
                    for (Cfg.Dec.T dec : fields) {
                        printSpaces();
                        Dec.pp(dec);
                    }
                    unIndent();
                    printSpaces();
                    say(STR."""
} S_\{clsName}_ = {
""");
                    indent();
                    printSpaces();
                    say(STR."""
.vptr = &V_\{clsName}_;
""");
                    unIndent();
                    printSpaces();
                    say("};\n\n");
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////
    // values
    public static class Value {
        public sealed interface T
                permits Int, Vid {
        }

        // integer constant
        public record Int(int n) implements T {
        }

        // variable
        public record Vid(Id x, Type.T ty) implements T {
        }

        public static void pp(T ty) {
            switch (ty) {
                case Int(int n) -> say(Integer.toString(n));
                case Vid(Id x, _) -> say(x.toString());
            }
        }
    }
    // end of value

    // /////////////////////////////////////////////////////////
    // statement
    public static class Stm {
        public sealed interface T
                permits Assign, AssignBop, AssignCall, AssignNew, AssignArray, Print, GetMethod {
        }

        // assign
        public record Assign(Id leftId, Value.T right, Type.T type) implements T {
        }

        // assign
        public record AssignBop(Id leftId, Value.T left, String bop, Value.T right, Type.T type) implements T {
        }

        // assign
        public record AssignCall(Id id, Id func, List<Value.T> args, Type.T retType) implements T {
        }

        public record AssignNew(Id id, Id cls) implements T {
        }

        // assign-array
        public record AssignArray(Id arrayId, Value.T index, Value.T right) implements T {
        }

        // Print
        public record Print(Value.T value) implements T {
        }

        // get virtual method:
        // leftId = getmethod(value, cls, method)
        public record GetMethod(Id leftId, Value.T value, Id classId, Id methodId) implements T {
        }

        public static void pp(T t) {
            switch (t) {
                case Assign(Id id, Value.T right, Type.T type) -> {
                    printSpaces();
                    say(STR."\{id} = ");
                    Value.pp(right);
                    say(";  @ty:");
                    Type.pp(type);
                    sayln("");
                }
                case AssignBop(Id id, Value.T left, String op, Value.T right, Type.T type) -> {
                    printSpaces();
                    say(STR."\{id} = ");
                    Value.pp(left);
                    say(STR." \{op} ");
                    Value.pp(right);
                    say(";  @ty:");
                    Type.pp(type);
                    sayln("");
                }
                case AssignCall(Id id, Id func, List<Value.T> args, Type.T retType) -> {
                    printSpaces();
                    say(STR."\{id} = \{func}(");
                    for (Value.T arg : args) {
                        Value.pp(arg);
                        say(", ");
                    }
                    say(");  @ty:");
                    Type.pp(retType);
                    sayln("");
                }
                case AssignNew(Id id, Id classId) -> {
                    printSpaces();
                    say(STR."""
\{id} = new \{classId}();
""");
                }
                case Print(Value.T value) -> {
                    printSpaces();
                    say("print(");
                    Value.pp(value);
                    say(");\n");
                }
                case GetMethod(Id id, Value.T value, Id cls, Id methodName) -> {
                    printSpaces();
                    say(STR."\{id} = getMethod(");
                    Value.pp(value);
                    say(STR.", \"\{cls.toString()}\", \"\{methodName}\");");
                    say("\n");
                }
                default -> throw new Todo(t);
            }
        }
    }
    // end of statement

    // /////////////////////////////////////////////////////////
    // transfer
    public static class Transfer {
        public sealed interface T
                permits If, Jmp, Ret {
        }

        public record If(Value.T value,
                         Block.T trueBlock,
                         Block.T falseBlock) implements T {
        }

        public record Jmp(Block.T target) implements T {
        }

        public record Ret(Value.T retValue) implements T {

        }

        public static void pp(T t) {
            switch (t) {
                case If(Value.T value, Block.T thenn, Block.T elsee) -> {
                    printSpaces();
                    say("if(");
                    Value.pp(value);
                    say(STR.", \{Block.getName(thenn)}, \{Block.getName(elsee)});");
                }
                case Jmp(Block.T target) -> {
                    printSpaces();
                    say(STR."jmp \{Block.getName(target)}");

                }
                case Ret(Value.T value) -> {
                    printSpaces();
                    say("ret ");
                    Value.pp(value);
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////
    // block
    public static class Block {
        public sealed interface T permits Singleton {
        }

        public record Singleton(Label label,
                                List<Stm.T> stms,
                                // this is a special hack
                                // the transfer field is final, so that
                                // we use a list instead of a singleton field
                                List<Transfer.T> transfer) implements T {
        }

        public static void add(T b, Stm.T s) {
            switch (b) {
                case Singleton(
                        _,
                        List<Stm.T> stms,
                        _
                ) -> stms.add(s);
            }
        }

        public static void addTransfer(T b, Transfer.T s) {
            switch (b) {
                case Singleton(
                        Label _,
                        List<Stm.T> _,
                        List<Transfer.T> transfer
                ) -> transfer.add(s);
            }
        }

        public static String getName(Block.T t) {
            switch (t) {
                case Singleton(
                        Label label,
                        List<Stm.T> _,
                        List<Transfer.T> _
                ) -> {
                    return label.toString();
                }
            }
        }

        public static void pp(T b) {
            switch (b) {
                case Singleton(
                        Label label,
                        List<Stm.T> stms,
                        List<Transfer.T> transfer
                ) -> {
                    printSpaces();
                    say(STR."""
\{label.toString()}:
""");
                    indent();
                    stms.forEach(Stm::pp);
                    Transfer.pp(transfer.getFirst());
                    unIndent();
                    sayln("");
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////
    // function
    public static class Function {
        public sealed interface T
                permits Singleton {
        }

        public record Singleton(Type.T retType,
                                Id classId,
                                Id functionId,
                                List<Dec.T> formals,
                                List<Dec.T> locals,
                                List<Block.T> blocks) implements T {
        }

        public static void addBlock(T func, Block.T block) {
            switch (func) {
                case Singleton(
                        Type.T _,
                        Id _,
                        Id _,
                        List<Dec.T> _,
                        List<Dec.T> _,
                        List<Block.T> blocks
                ) -> blocks.add(block);
            }
        }

        public static void addFirstFormal(T func, Dec.T formal) {
            switch (func) {
                case Singleton(
                        _,
                        _,
                        _,
                        List<Dec.T> formals,
                        _,
                        _
                ) -> formals.addFirst(formal);
            }
        }

        public static void addDecs(T func, List<Dec.T> decs) {
            switch (func) {
                case Singleton(
                        _,
                        _,
                        _,
                        _,
                        List<Dec.T> locals,
                        _
                ) -> locals.addAll(decs);
            }
        }

        public static void pp(T f) {
            switch (f) {
                case Singleton(
                        Type.T retType,
                        Id classId,
                        Id id,
                        List<Dec.T> formals,
                        List<Dec.T> locals,
                        List<Block.T> blocks
                ) -> {
                    printSpaces();
                    Type.pp(retType);
                    say(STR." \{id}(");
                    formals.forEach(x -> {
                        Dec.pp(x);
                        say(", ");
                    });
                    say(STR."){ @classId: \{classId.toString()}\n");
                    indent();
                    locals.forEach(x -> {
                        printSpaces();
                        Dec.pp(x);
                        sayln(";");
                    });
                    blocks.forEach(Block::pp);
                    unIndent();
                    printSpaces();
                    say("}\n\n");
                }

            }
        }
    }

    // whole program
    public static class Program {
        public sealed interface T permits Singleton {
        }

        public record Singleton(Id mainClassId,
                                Id mainFuncId, // name of the entry function
                                List<Vtable.T> vtables,
                                List<Struct.T> structs,
                                List<Function.T> functions) implements T {
        }

        public static void pp(T prog) {
            switch (prog) {
                case Singleton(
                        Id mainClassId,
                        Id mainFuncId,
                        List<Vtable.T> vtables,
                        List<Struct.T> structs,
                        List<Function.T> functions
                ) -> {
                    printSpaces();
                    sayln(STR."// the entry function name: \{mainClassId}: \{mainFuncId}");
                    // vtables
                    vtables.forEach(Vtable::pp);
                    // structs
                    structs.forEach(Struct::pp);
                    // functions:
                    functions.forEach(Function::pp);
                }
            }
        }
    }
}
