4DV507/4DT902: Practical Assignment - Step 2

# The One-file Program Project: Step 2 - Symbol Table and Type Checking

**First of all, in Assignment 2 you are encouraged to work in pairs.** It is not mandatory, you can work by yourself if you want to, but remember that Assignment 2 is both tricky and time consuming. Hence, it makes sense to work in pairs.

In Assignment 1, we used ANTLR to generate an OFP parser that performed the lexical and syntax analysis, resulting in a syntax tree (or a parse tree) for input programs written in the OFP language. The tasks in Assignment 2 are the symbol table construction and the semantic analysis for the OFP compiler.

Starting with Assignment 2, you are going to work on your implementation in Java — take a look at the Lecture 4 video Connecting ANTLR to Java for how to get started using Java.

Several lectures in this course discuss implementation details related to Assignment 2. Please take a look at the lecture notes related to symbol tables and type checking.

**Note related to Assignment 1:** As mentioned in the instructions for Assignment 1, practical assignments in this course form a part of a single project. If you have missed or failed Assignment 1, you still have to get a working grammar which would allow you to continue with the rest of assignments. Thus, fix any known problems in Assignment 1 before you start working on Assignment 2. In most cases, even if you passed Assignment 1, you should consider modifying your grammar file for Assignment 2 to make the further steps (symbol table construction, semantic analysis, etc.) easier — such modifications are perfectly fine from our (teachers') side. Hints for adjustments are provided in the lecture slides. **Most importantly, add alternative labels to name the alternative branches for (at least) statements and expressions.** See lecture slides for details about alternative labels.

## Symbol Table Construction

What follows is a list of comments that serve as guidelines for how to implement the symbol table for your project. They define at the same time what we expect you to do in this phase of the assignment.

- The symbol table is a data structure where the information related to individual identifiers is stored. It should be easy to add and look up information in the symbol table.

- Each identifier is represented by a symbol record that is stored in the symbol table. Different types of identifiers (e.g., functions, parameters, and variables) may require different types of records. We therefore suggest a hierarchy of symbol types. **Note:** OFP does not support [method overloading](https://docs.oracle.com/javase/tutorial/java/javaOO/methods.html), so you can safely use the function name as an identifier in your symbol table, and treat duplicate function names as an issue during the analyses.

- Each identifier in an OFP program has a scope in which it is visible. For example, all function identifiers have a global visibility, whereas variables defined within a block are only visible within that block. We expect the symbol table to be able to deal with scopes. **Note:** OFP supports [variable shadowing](https://en.wikipedia.org/wiki/Variable_shadowing), that is, a variable with name foo (and some type X) defined within a block shall overshadow any variable with the same name in **enclosing** scopes (e.g., a function parameter or a variable declared in an outer block). An attempt to declare a duplicate variable/parameter in **the same** scope is considered an error, though. Your implementation should be able to handle this properly by using scopes.

- We suggest that you use the approach presented in the lectures. That is, a tree of scopes where each scope is a map from names/identifiers to symbol records, and a mapping from parse tree nodes to scopes with the [ParseTreeProperty](https://www.antlr.org/api/Java/org/antlr/v4/runtime/tree/ParseTreeProperty.html) class provided by ANTLR. You can use other approaches as long as they are reasonably fast (at least faster than *O(N)* where *N* is the number of different scopes).

- By *constructing* a symbol table, we mean traversing the syntax tree and adding a new record to the appropriate scope for each *declaration* found in the program (think about reporting *duplicate declaration* errors, though...). At this point we only deal with declarations. *Use* and *definitions* will be considered in the following semantic analysis. However, the construction of the symbol table is a good place to associate each identifier with a type. Hence, we expect you to add type information to each new record created. (The type of a function is its return type.)

- The scope implementation according to the above descriptions may (but does not need to) look approximately like the following:

```java
public class Scope {
    private Scope enclosingScope; // null if global (outermost) scope
    private Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();
    public Scope(Scope enclosingScope) { ... }
    public Scope getEnclosingScope() { ...}
    public void define(Symbol sym) { symbols.put(sym.getName(), sym); }
    // If a name cannot be resolved in this scope,
    // try enclosing/parent scope recursively.
    // If null is returned, name/identifier not found in the symbol table!
    public Symbol resolve(String name) { ... }
    // Print the contents of this scope
    @Override
    public String toString() { ... }
}
```

- We recommend you to implement a listener (using ANTLR-generated base listeners) to construct the symbol table in a single left-to-right depth-first traversal of the syntax tree. Implementing symbol table construction through a visitor is also a viable option (but more complicated and not recommended).

- We expect you to implement a print method that displays the content (name and type of each symbol) and the structure of the symbol table (tree of scopes). A very simple print routine is sufficient.

- You should be able to handle the symbol table construction after Lecture 4.

## Semantic Analysis

The semantic analysis can be divided into two different tasks:

1. Semantic checking where we try to identify semantic errors like:
    - Undeclared identifiers.

    - Type errors (e.g., actual return type of a function not in agreement with declared return type).

    - Function call errors (e.g., wrong number of parameters or wrong type of parameters).

2. Collecting further information about the identifiers (if necessary).

We have divided the work into several steps that we suggest you handle in the following order:

1. Preparations: In order to perform the following tasks, we might have to do some minor adjustments of the symbol table listener.

2. Check for undeclared identifiers: to check for *undeclared* identifiers, we simply do a lookup for every identifier we find. Every identifier should already be stored in the symbol table if it has been declared.  We recommend a second traversal of the syntax tree (using a new Listener) to handle this step.

3. Type checking for expressions: We check that all expressions have a correct (or expected) type, starting with literals, variables, and simple arithmetical expressions. **We recommend you to implement a visitor that will include the code for this and the next stages of semantic analysis.**

4. Verify that arrays, strings, and the corresponding operations have expected types.

5. Verify that function call expressions return expected types in agreement with target function declaration.

6. Verify that function call arguments (number/order and type) are in agreement with target function declaration. 

7. Make sure to test your implementation with both **valid** and **invalid** test programs.

This is just a subset of the semantic analyses that take place in an ordinary compiler. **However, we limit ourselves to these cases to avoid too much work.**

#### Note

- The semantic analysis can be done in a single or multiple left-to-right traversals of the parse tree. Use a separate type analysis visitor that uses the information stored in the symbol table to do the semantic analysis. To simplify the code, you might want to implement a separate listener to check the references for undeclared identifiers before the main type checking stage.

## Preparations

The upcoming type analysis requires that each function symbol keeps a list of references to its parameters: in order to check that a call `f()` is semantically correct, we must check that the number of parameters and their types are correct. Hence, having identified the function symbol, we must be able to obtain information about the parameters. We can handle this requirement by making some minor adjustments to the symbol table listener and by adding an extra member to the function symbol class.

### Check undeclared identifiers and uninitialized variables 

To verify that each identifier in use is declared, we must perform a lookup on all the identifiers in the parse tree.

### Type Analysis

- **Expressions:** The type of every kind of expression should be verified to be correct. For example, every left and right operands in `"Expr: Expr (PLUS | MINUS) Expr"` productions should be of the same type. The `"=="` operator is valid for characters, integers, and decimals only. 

- **Statements:**
    - Check that `LeftSide.type == RightSide.type` in assignments

    - Check that conditions in `if-` and `while-statements` are of type bool

    - Check that Expression in print statements is of type `int`, `float`, `bool`, `char`, or `string`

- **Function Declarations:** Check that the type of a return expression equals the declared return type. Remember to handle the void functions!

- **Function Calls:** Check that the number of parameters and their types are correct in function calls.

- **Arrays and Strings:** Verify that `.length` and `[]` are only applied to arrays (only `int[]`, `float[]`, and `char[]` are allowed in OFP) or `strings`, and also verify that the type of the expression within `[]` is always `int`.

We have *probably* forgotten to mention some further checks — use your common sense to decide what more to check. At the same time, we explicitly ignore the following check (to avoid overcomplicating this project):

- We do not ensure that every code execution path for a non-void function returns some value.

Remember, this is the last step in the analysis phase — the program to be compiled is declared as "correct" after this phase.

Finally, we expect that your Assignment 2 implementation analyses the complete input program and reports as complete information about semantic errors as possible rather than throwing an exception and aborting on the first encountered semantic error (e.g., right after detecting the very first duplicate declaration issue). A good idea is to implement a **counter of detected errors**, and print the final count after finishing the analysis (zero detected errors should indicate a valid input program). 

## Report on Step 2

Before the given deadline for Assignment 2 you must upload your solution to MyMoodle.

Your submission shall contain:

1. The final OFP grammar file for ANTLR (.g4) that you used to generate the source code for this assignment

2. All Java project files required to run and compile the program

3. Instructions for how to compile and run your program (if necessary)

Also observe the following:

- If you work on practical assignments in a group of two students, please submit the assignment **only once for each group** — the submitter should state the name of his/her group mate.

- Your implementation of Assignment 2 must be carried out in Java (any version between Java **6** and Java **11** as of this course offering).

- It is prohibited to use any third-party libraries in your project (with the exception of ANTLR, of course).
