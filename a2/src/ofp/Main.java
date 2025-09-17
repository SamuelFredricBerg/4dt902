package ofp;

import java.io.IOException;

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
        String testDir = "/home/fred/Documents/4dt902/a2/src/inputs/";
        String testProgram = testDir + "test.ofp";

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
        // ParseTreeWalker walkerTest = new ParseTreeWalker();
        // PrintListener printListener = new PrintListener();
        // walkerTest.walk(printListener, root);

        // Symbol table construction using a listener ... (This lecture)
        ParseTreeWalker walker = new ParseTreeWalker();
        SymbolTableListener stListener = new SymbolTableListener();
        walker.walk(stListener, root);

        stListener.printSymbolTable();

        ParseTreeProperty<Scope> scopes = stListener.getScope();

        // Symbol reference checking using a listener ... (This lecture)
        CheckRefListener checkRefListener = new CheckRefListener(scopes, stListener.getGlobalScope());
        walker.walk(checkRefListener, root);

        checkRefListener.reportErrors();

        // Type checking using a visitor ... (Next lecture)
        TypeCheckingVisitor tcVisitor = new TypeCheckingVisitor(scopes);
        tcVisitor.visit(root);
    }
}
