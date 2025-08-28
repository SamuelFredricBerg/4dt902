# The One-file Program Project

**Assignment 1 is individual. Each students should submit their own solution.**

The goal of practical assignments 1–4  is to implement a compiler frontend and code generation for the One-file Programming language (OFP). We will not implement the lexical and syntax analysis from scratch. Instead we will use a parser generating tool named ANTLR.

## Using ANTLR

ANTLR (ANother Tool for Language Recognition) is a parser generating tool that reads a grammar specification and converts it to a (Java) program that can recognize matches to the grammar. ANTLR is a LL(*) parser generator that can handle grammars with productions like:

```ANTLR4
Expression : BoolExp (BoolOp BoolExp)* ;
```

which corresponds to the following BNF grammar production:

```BNF
Expression --> BoolExp (BoolOp BoolExp)*
```

where Expression, BoolExp, and BoolOp are non-terminals.

We have put together a simple **ANTLR Start Kit** (see the attachment below) where you can learn the basics. It comes with an introduction to ANTLR (note: the official ANTLR documentation is now hosted at [Github](https://github.com/antlr/antlr4/blob/master/doc/index.md)), the three JAR files, and two sample exercises. See the provided introduction PDF file for more information.

Besides this, there is plenty of information on ANTLR available on the Internet. However, the documentation available at the [ANTLR home page](http://www.antlr.org) should be sufficient to get you started.

Also, we made a  video showing how to install and get started with ANTLR. They are a part of the Lecture 2 material. The video assume that we are using the VS Code IDE and that we have installed the corresponding ANTLR plugin. Alternatively, take a look at the ANTLR plugins for other IDEs listed at <https://www.antlr.org/tools.html> , or consider using ANTLR from command line in the worst case (make sure the ANTLR JAR file is on your Java class path, and follow the instructions from the official [Getting Started](https://github.com/antlr/antlr4/blob/master/doc/getting-started.md) guide). However, we recommend you to use VS Code as your IDE and this is the setup that we support.

**Finally, we strongly recommend you to play around with ANTLR for a while before starting to work on the actual assignment.  Make sure you can run the "PlusMult" example first, then the "Expressions" example. Handling the rest of exercises in the Start Kit tutorial is a good way to continue.**

## What is the One-file Program Language?

The input to ANTLR is a lexical specification (token definitions) and a syntax specification (context free grammar). Together they form a formal specification of the One-file Programming language (OFP). We provide two resources for you to help you understand which language onstructs we consider to be OFP:

1. We have put together a number of OFP **examples** (see the attachment below). The programs can be seen as a list of Java features that are a part of OFP. Being able to handle all these programs correctly is a minimum requirement for your OFP parser.

2. We have put together a **non-formal description of OFP** (see the attachment below) describing the overall structure of a OFP program, what type of statements are a part of OFP, and what different types of expressions and operators are used in OFP.

These two resources should give you a rather precise idea of OFP. It will be your task to turn these two resources into a formal language specification.

## Step 1: Constructing the Syntax Tree

In the first step we will take care of the lexical analysis, and the syntax analysis using ANTLR. Your task is basically to turn our rather informal definition of OFP above into a formal specification that can be handled by ANTLR. The ANTLR generated parser should be able to verify that a given input program is a lexically and syntactically correct OFP program. It should of course also be able to reject any non-correct OFP program. Please note that you should think not only about the examples provided in the OFP definition, but ask yourselves the "What if?.." questions and think about the ways various data types and expressions could be combined in a proper way.

In this step we do not bother with storing information in symbol tables or semantic analysis. These issues will be dealt with in the next step. Our goal is to use ANTLR to construct a parser that builds up a parse tree, and verifies that the program at hand is lexically, and syntactically correct.

### Recommended Approach

We strongly suggest a "small step" approach where you for each step add a small set of OFP language features. We also suggest an inside out approach where you start with small language constructs like expressions and assignments and add larger constructs like while statements and functions later on.

1. Start working on your grammar file. Remember that ANTLR supports **lexer** and **parser** rules: parser rule titles must start with a **lowercase** letter; lexer rule titles must start with an **uppercase** letter, and it's a good idea to place the lexer rules **at the end of the grammar file**. Also remember to support rules for whitespace and comments.

2. At the beginning of your work, focus on the assignment statement. It is somewhat tricky to get correct precedence rules for the binary operators in the right hand side expression (see <https://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html>). Use the generated parse tree in order to check if you got the operator precedence correct.

3. The next tricky part is expressions using composite unary expressions like: `b[8] = e()[g(3).length];` What is the corresponding parse tree?

The proper look of the parse tree is **very important** and it will have impact on your grade, in particular, it is important to get the correct operator precedence (not only for binary operators). Think of it this way: in the following assignments, you will have to traverse and analyse parse trees from your Java code using methods similar to parseTreeNode.getFirstChild() and parseTreeNode.getChild(2). Therefore, it is in your own interests to implement a grammar that produces reliable parse trees with proper hierarchies of nodes. Look at the attached "Good and bad parse tree examples" file for some ideas. You might also want to use [ANTLR rule element labels](https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#rule-element-labels), e.g. "expression bop='+' expression" to name the plus node "bop" for "binary operator".

**Remember:** This first step is the fundament upon which your compiler implementation will be built up. Efforts spent here will pay back later. Correcting an error in the parsing process will be twice as difficult when you have added additional features to your compiler. Hence, do testing, testing, testing, and more testing .... Test using correct OFP programs to see if the corresponding syntax tree looks correct. Test using incorrect OFP programs to verify that your parser can recognize (and reject) them.

## Report on Step 1

Before the given deadline for step 1 (23:59) you must upload your solution to Moodle.

**Your submission shall only contain your .g4 file.**

### Attached Files

* [OFP_Examples.zip](./ofp_examples.zip)

* [ANTLR_Starter_Kit.zip](./ANTLR_kit.zip)

* [One_File_Programs.pdf](./one_file_programs.pdf)
