package ofp;

import org.antlr.v4.runtime.tree.ParseTree;

import generated.OFPBaseVisitor;

public class PrintVisitor extends OFPBaseVisitor<Object> {
    // ... One method for each non-terminal type
    public ParseTree visitAllChildren(ParseTree node) {
        System.out.println(node.getClass().getName() + ": " + node.getChildCount());

        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child instanceof TerminalNode)
                visitTerminalNode((TerminalNode) child);
            else
                visit(child);
        }
        return node;
    }

    public void visitTerminalNode(TerminalNode node) {
        System.out.println("\t" + node.getClass().getName() + ": " + node.getText());
    }
}
