# The One-file Program Project: Step 3 - Python Code Generation

After the first two Assignments, you should have implemented support for parsing, symbol table construction, and semantic analysis for input programs in OFP. In Assignment 3, your task is to generate Python 3 code corresponding to the input OFP code, i.e., for a given valid .*ofp* file, produce a corresponding valid .*py* file.

Consider the following pair of OFP and Python programs as an example:

```python
# sum.ofp                   | def ofp_sum(x):
                            |    i = 1
void main() {               |    s = 0
    int len = 10;           |    while i < x + 1:
    string y = "Result: ";  |        s = s + i
    int res = sum(len);     |        i = i + 1
                            |    return s
    print(y); # Result:     |
    println(res); # 55      | #
}                           | # Program entry point - main
                            | #
int sum(int x) {            | ofp_len = 10
    int i = 1;              | y = "Result: "
    int s = 0;              | res = ofp_sum(ofp_len)
    while (i < x+1) {       | print(y, end='')
        s = s + i;          | print(res)
        i = i + 1;          |
    }                       |
    return s;               |
}                           |
```

Notice the following:

* Python code uses indentation to demark nested blocks instead of *{ }*;

* the contents of the OFP *main* function were translated into inline code at the end of the Python program;

* the function *sum* and variable *len* in OFP had to be renamed to avoid a name conflict with Python built-in functions;

* OFP *print* and *println* statements were translated into appropriate invocations of the Python built-in *print* function.

Furthermore,

* Arrays in OFP are replaced with lists in Python

* Characters (type char) in OFP is replaced with strings in Python

* For each OFP program X.ofp we expect a generated (and executable) Python file X.py. Running the generated Python programs will be a part of the A3 code demo.

As a help to get you started we have attached two OFP programs (arrays.ofp, strings.ofp) and their corresponding Python files (arrays.py, strings.py).

The "Code Translation" lecture in this course discusses implementation details related to Assignment 3 — please read the lecture slides carefully.

**Note related to Assignment 2:** Your implementation in Assignment 2 should not affect your grade for Assignment 3 (e.g., if you failed to properly check for some type mismatch errors); furthermore, you can assume that input programs are valid OFP programs. Make sure that valid OFP programs are accepted by your parser + symbol table + type checker, though — if we cannot test Assignment 3 tasks because of Assignment 2 issues, this will affect your grade. If you are not confident about your semantic analysis implementation (e.g., *CheckRefListener* and *TypeCheckingVisitor*), you might even consider commenting out the corresponding parts of the code in your main Java class (i.e., after constructing the symbol table, go directly to the Python generation step and skip the semantic analyses).

## Recommended Approach

We recommend you to implement a visitor for your parse tree. The implementation may (but does not need to) look approximately like the following:

```java
public class PythonGenerator extends OFPBaseVisitor<String> {
 private ParseTreeProperty<Scope> scopes;
 private Scope currentScope;

 // Keeping track of the correct indentation levels
 private int depth = 0;
 private HashMap<Integer,String> indentCache = ...
 private String indent(int indentLevel) { ... }

 // The list of identifiers reserved in Python
 private static HashSet<String> reservedIds = new HashSet<String>(Arrays.asList("False", "None", "True", "and", "as", "assert", "async", "await", "break", "class", "continue", "def", "del", "elif", "else", "except", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", "while", "with", "yield", "ArithmeticError", "AssertionError", "AttributeError", "BaseException", "BlockingIOError", "BrokenPipeError", "BufferError", "BytesWarning", "ChildProcessError", "ConnectionAbortedError", "ConnectionError", "ConnectionRefusedError", "ConnectionResetError", "DeprecationWarning", "EOFError", "Ellipsis", "EnvironmentError", "Exception", "False", "FileExistsError", "FileNotFoundError", "FloatingPointError", "FutureWarning", "GeneratorExit", "IOError", "ImportError", "ImportWarning", "IndentationError", "IndexError", "InterruptedError", "IsADirectoryError", "KeyError", "KeyboardInterrupt", "LookupError", "MemoryError", "NameError", "None", "NotADirectoryError", "NotImplemented", "NotImplementedError", "OSError", "OverflowError", "PendingDeprecationWarning", "PermissionError", "ProcessLookupError", "RecursionError", "ReferenceError", "ResourceWarning", "RuntimeError", "RuntimeWarning", "StopAsyncIteration", "StopIteration", "SyntaxError", "SyntaxWarning", "SystemError", "SystemExit", "TabError", "TimeoutError", "True", "TypeError", "UnboundLocalError", "UnicodeDecodeError", "UnicodeEncodeError", "UnicodeError", "UnicodeTranslateError", "UnicodeWarning", "UserWarning", "ValueError", "Warning", "ZeroDivisionError", "__build_class__", "__debug__", "__doc__", "__import__", "__loader__", "__name__", "__package__", "__spec__", "abs", "all", "any", "ascii", "bin", "bool", "bytearray", "bytes", "callable", "chr", "classmethod", "compile", "complex", "copyright", "credits", "delattr", "dict", "dir", "divmod", "enumerate", "eval", "exec", "exit", "filter", "float", "format", "frozenset", "getattr", "globals", "hasattr", "hash", "help", "hex", "id", "input", "int", "isinstance", "issubclass", "iter", "len", "license", "list", "locals", "map", "max", "memoryview", "min", "next", "object", "oct", "open", "ord", "pow", "print", "property", "quit", "range", "repr", "reversed", "round", "set", "setattr", "slice", "sorted", "staticmethod", "str", "sum", "super", "tuple", "type", "vars", "zip"));

 // Replace an OFP identifier (e.g., variable name) if it belongs to the set above
 private static String getSafePythonId(String id) { ... }


 public PythonGenerator(ParseTreeProperty<Scope> scopes) {
   this.scopes = scopes;
 }

 ...
}
```

You should then implement the methods for visiting various parse tree nodes. Once the parse tree traversal is finished, the visitor should return the resulting string, which should then be saved into a .*py* file. The results are expected to be valid Python programs, i.e., it should be possible to run the standard Python interpreter with such script files and observe results that are (almost) identical to the OFP program results.

More details about particular parse tree nodes and caveats related to Python code generation are provided in the lecture slides.

## Report

Before the given deadline for Assignment 3 you must upload your solution to Moodle.

Your submission shall contain:

1. The final OFP grammar file (.g4) that you used to generate the source code for your parser and listeners/visitors

2. All files required to compile and run the program

3. Instructions for how to compile and run your program (if necessary)

Also observe the following:

* If you work on practical assignments in a group of two students, please submit the assignment **only once for each group** — the submitter should state the name of his/her group mate.

* Your implementation of Assignment 3 must be carried out in Java.

* It is prohibited to use *any third-party libraries* in your project (with the exception of ANTLR, of course).

## Attached files

* [assignment3_attached_files.zip](../assignment3_attached_files.zip)
