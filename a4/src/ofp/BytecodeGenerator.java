package ofp;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import generated.OFPBaseVisitor;
import generated.OFPParser;

public class BytecodeGenerator extends OFPBaseVisitor<Type> implements Opcodes {
    private String fileName;
    private ClassWriter cw;
    private GeneratorAdapter mg;
    private ParseTreeProperty<Scope> scopes;
    private Scope globalScope;
    private Scope currentScope = null;
    private FunctionSymbol currentFunctionSymbol;

    public BytecodeGenerator(String fileName, ParseTreeProperty<Scope> scopes, Scope globalScope) {
        this.fileName = fileName;
        this.scopes = scopes;
        this.globalScope = globalScope;
    }

    public ClassWriter getClassWriter() {
        return cw;
    }

    @Override
    public Type visitProgram(OFPParser.ProgramContext ctx) {
        currentScope = scopes.get(ctx);
        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_1, ACC_PUBLIC, fileName, null, "java/lang/Object", null);

        Method constructor = Method.getMethod("void <init> ()");
        mg = new GeneratorAdapter(ACC_PUBLIC, constructor, null, null, cw);
        mg.loadThis();
        mg.invokeConstructor(Type.getType(Object.class), constructor);
        mg.returnValue();
        mg.endMethod();

        visitChildren(ctx);
        cw.visitEnd();

        return null;
    }
}
