package ofp;

import java.io.IOException;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.gui.Trees;

import generated.OFPLexer;
import generated.OFPParser;

public class Main {

    public static void main(String[] args) {

        // Select test program
        String testDir = "/home/fred/Documents/4dt902/a2/src/inputs/";
        String testProgram = testDir + "max.ofp";

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
        Trees.inspect(root, parser);

    }
}
