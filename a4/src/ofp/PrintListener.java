package ofp;

import generated.OFPBaseListener;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Listener that prints the parse tree rule names with indentation based on
 * depth.
 */
public class PrintListener extends OFPBaseListener {
    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        printCurrent(ctx);
    }

    public void printCurrent(ParserRuleContext ctx) {
        System.out.println("    ".repeat(ctx.depth() - 1) + ctx.getClass().getSimpleName());
    }
}
