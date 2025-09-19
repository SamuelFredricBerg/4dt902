package ofp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.gui.Trees;

import generated.OFPLexer;
import generated.OFPParser;

public class Main {

    public static void main(String[] args) {

        // Select test program
        String inputDir = "/home/fred/Documents/4dt902/a3/src/input/";
        String testProgram = inputDir + "max.ofp";
        String outputDir = "/home/fred/Documents/4dt902/a3/src/output/";

        // Check if input ends with ".ofp"
        if (!testProgram.endsWith(".ofp")) {
            System.out.println("\nPrograms most end with suffix .ofp! Found " + testProgram);
            System.exit(-1);
        }
        System.out.println("Reading test program from: " + testProgram);

        // Parse input program
        System.out.println("\nParsing started");
        OFPParser parser = null;
        OFPParser.ProgramContext root = null;
        try {
            CharStream inputStream = CharStreams.fromFileName(testProgram);
            OFPLexer lexer = new OFPLexer(inputStream);
            parser = new OFPParser(new BufferedTokenStream(lexer));
            root = parser.program();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\nParsing completed");

        // Display tree
        // Trees.inspect(root, parser); // Uncomment to display tree

        // Indented tree print using a custom listener
        System.out.println("\n===== Print Listener =====");
        ParseTreeWalker walkerTest = new ParseTreeWalker();
        PrintListener printListener = new PrintListener();
        walkerTest.walk(printListener, root);

        // Symbol table construction using a listener ... (This lecture)
        ParseTreeWalker walker = new ParseTreeWalker();
        SymbolTableListener stListener = new SymbolTableListener();
        walker.walk(stListener, root);
        stListener.printSymbolTable();

        ParseTreeProperty<Scope> scopes = stListener.getScope();

        // Symbol reference checking using a listener ... (This lecture)
        System.out.println("\n===== Symbol Refrence Checking =====");
        CheckRefListener checkRefListener = new CheckRefListener(scopes, stListener.getGlobalScope());
        walker.walk(checkRefListener, root);
        checkRefListener.reportErrors();

        // Type checking using a visitor ... (Next lecture)
        System.out.println("===== Type Checking Errors =====");
        TypeCheckingVisitor tcVisitor = new TypeCheckingVisitor(scopes);
        tcVisitor.visit(root);

        // Python Genertion
        System.out.println("\nGenerating Python code...");

        String baseFileName = testProgram.substring(testProgram.lastIndexOf('/') + 1,
                testProgram.lastIndexOf('.'));
        String outputPythonFile = outputDir + baseFileName + ".py";

        PythonGenerator pythonCodeGenerator = new PythonGenerator(scopes);
        String generatedPythonCode = pythonCodeGenerator.visit(root);

        try {
            Files.write(Paths.get(outputPythonFile), generatedPythonCode.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Python code generated and written to: " +
                    outputPythonFile);
        } catch (IOException e) {
            System.err.println("Error writing Python code to file: " + e.getMessage());
        }
    }
}
