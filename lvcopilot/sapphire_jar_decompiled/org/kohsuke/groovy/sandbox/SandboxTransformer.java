/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.Script
 *  org.codehaus.groovy.GroovyBugError
 *  org.codehaus.groovy.ast.ASTNode
 *  org.codehaus.groovy.ast.ClassCodeExpressionTransformer
 *  org.codehaus.groovy.ast.ClassHelper
 *  org.codehaus.groovy.ast.ClassNode
 *  org.codehaus.groovy.ast.ConstructorNode
 *  org.codehaus.groovy.ast.FieldNode
 *  org.codehaus.groovy.ast.GroovyCodeVisitor
 *  org.codehaus.groovy.ast.MethodNode
 *  org.codehaus.groovy.ast.Parameter
 *  org.codehaus.groovy.ast.Variable
 *  org.codehaus.groovy.ast.expr.ArgumentListExpression
 *  org.codehaus.groovy.ast.expr.AttributeExpression
 *  org.codehaus.groovy.ast.expr.BinaryExpression
 *  org.codehaus.groovy.ast.expr.CastExpression
 *  org.codehaus.groovy.ast.expr.ClassExpression
 *  org.codehaus.groovy.ast.expr.ClosureExpression
 *  org.codehaus.groovy.ast.expr.ConstantExpression
 *  org.codehaus.groovy.ast.expr.ConstructorCallExpression
 *  org.codehaus.groovy.ast.expr.DeclarationExpression
 *  org.codehaus.groovy.ast.expr.EmptyExpression
 *  org.codehaus.groovy.ast.expr.Expression
 *  org.codehaus.groovy.ast.expr.FieldExpression
 *  org.codehaus.groovy.ast.expr.ListExpression
 *  org.codehaus.groovy.ast.expr.MethodCallExpression
 *  org.codehaus.groovy.ast.expr.MethodPointerExpression
 *  org.codehaus.groovy.ast.expr.PostfixExpression
 *  org.codehaus.groovy.ast.expr.PrefixExpression
 *  org.codehaus.groovy.ast.expr.PropertyExpression
 *  org.codehaus.groovy.ast.expr.StaticMethodCallExpression
 *  org.codehaus.groovy.ast.expr.TupleExpression
 *  org.codehaus.groovy.ast.expr.VariableExpression
 *  org.codehaus.groovy.ast.stmt.BlockStatement
 *  org.codehaus.groovy.ast.stmt.ExpressionStatement
 *  org.codehaus.groovy.ast.stmt.Statement
 *  org.codehaus.groovy.classgen.GeneratorContext
 *  org.codehaus.groovy.control.CompilePhase
 *  org.codehaus.groovy.control.SourceUnit
 *  org.codehaus.groovy.control.customizers.CompilationCustomizer
 *  org.codehaus.groovy.runtime.ScriptBytecodeAdapter
 *  org.codehaus.groovy.syntax.Token
 *  org.codehaus.groovy.syntax.Types
 */
package org.kohsuke.groovy.sandbox;

import groovy.lang.Script;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.kohsuke.groovy.sandbox.ScopeTrackingClassCodeExpressionTransformer;
import org.kohsuke.groovy.sandbox.StackVariableSet;
import org.kohsuke.groovy.sandbox.impl.Checker;
import org.kohsuke.groovy.sandbox.impl.Ops;
import org.kohsuke.groovy.sandbox.impl.SandboxedMethodClosure;

public class SandboxTransformer
extends CompilationCustomizer {
    boolean interceptMethodCall = true;
    boolean interceptConstructor = true;
    boolean interceptProperty = true;
    boolean interceptArray = true;
    boolean interceptAttribute = true;
    private static final Set<String> TRIVIAL_CONSTRUCTORS = new HashSet<String>(Arrays.asList(Object.class.getName(), Script.class.getName(), "com.cloudbees.groovy.cps.SerializableScript", "org.jenkinsci.plugins.workflow.cps.CpsScript"));
    static final Token ASSIGNMENT_OP = new Token(100, "=", -1, -1);
    static final ClassNode checkerClass = new ClassNode(Checker.class);
    static final ClassNode ScriptBytecodeAdapterClass = new ClassNode(ScriptBytecodeAdapter.class);
    static final Expression CLOSURE_THIS;

    public SandboxTransformer() {
        super(CompilePhase.CANONICALIZATION);
    }

    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        if (classNode == null) {
            return;
        }
        ClassCodeExpressionTransformer visitor = this.createVisitor(source, classNode);
        this.processConstructors(visitor, classNode);
        for (MethodNode m : classNode.getMethods()) {
            visitor.visitMethod(m);
        }
        for (Statement s : classNode.getObjectInitializerStatements()) {
            s.visit((GroovyCodeVisitor)visitor);
        }
        for (FieldNode f : classNode.getFields()) {
            visitor.visitField(f);
        }
    }

    public void processConstructors(final ClassCodeExpressionTransformer visitor, ClassNode classNode) {
        ClassNode superClass = classNode.getSuperClass();
        List<Object> declaredConstructors = classNode.getDeclaredConstructors();
        if (TRIVIAL_CONSTRUCTORS.contains(superClass.getName())) {
            for (ConstructorNode c : declaredConstructors) {
                visitor.visitMethod((MethodNode)c);
            }
        } else {
            if (declaredConstructors.isEmpty()) {
                ConstructorNode syntheticConstructor = new ConstructorNode(1, (Statement)new BlockStatement());
                declaredConstructors = Collections.singletonList(syntheticConstructor);
                classNode.addConstructor(syntheticConstructor);
            } else {
                declaredConstructors = new ArrayList(declaredConstructors);
            }
            for (ConstructorNode c : declaredConstructors) {
                Parameter[] origParams;
                Statement code = c.getCode();
                List body = code instanceof BlockStatement ? ((BlockStatement)code).getStatements() : Collections.singletonList(code);
                TupleExpression superArgs = new TupleExpression();
                if (!body.isEmpty() && body.get(0) instanceof ExpressionStatement && ((ExpressionStatement)body.get(0)).getExpression() instanceof ConstructorCallExpression) {
                    ConstructorCallExpression cce = (ConstructorCallExpression)((ExpressionStatement)body.get(0)).getExpression();
                    if (cce.isThisCall()) {
                        visitor.visitMethod((MethodNode)c);
                        continue;
                    }
                    if (cce.isSuperCall()) {
                        body = body.subList(1, body.size());
                        superArgs = (TupleExpression)cce.getArguments();
                    }
                }
                ArrayList<Object> thisArgs = new ArrayList<Object>();
                final TupleExpression _superArgs = superArgs;
                final AtomicReference superArgsTransformed = new AtomicReference();
                ((ScopeTrackingClassCodeExpressionTransformer)visitor).withMethod((MethodNode)c, new Runnable(){

                    @Override
                    public void run() {
                        superArgsTransformed.set(((VisitorImpl)visitor).transformArguments((Expression)_superArgs));
                    }
                });
                thisArgs.add(((VisitorImpl)visitor).makeCheckedCall("checkedSuperConstructor", new Expression[]{new ClassExpression(superClass), (Expression)superArgsTransformed.get()}));
                for (Parameter parameter : origParams = c.getParameters()) {
                    thisArgs.add(new VariableExpression((Variable)parameter));
                }
                c.setCode((Statement)new BlockStatement(new Statement[]{new ExpressionStatement((Expression)new ConstructorCallExpression(ClassNode.THIS, (Expression)new TupleExpression(thisArgs)))}, c.getVariableScope()));
                Parameter[] params = new Parameter[origParams.length + 1];
                params[0] = new Parameter(new ClassNode(Checker.SuperConstructorWrapper.class), "$scw");
                System.arraycopy(origParams, 0, params, 1, origParams.length);
                ArrayList<MethodCallExpression> scwArgs = new ArrayList<MethodCallExpression>();
                int x = 0;
                for (Object superArg : superArgs) {
                    scwArgs.add(new MethodCallExpression((Expression)new VariableExpression("$scw"), "arg", (Expression)new ConstantExpression((Object)x++)));
                }
                ArrayList<Object> arrayList = new ArrayList<Object>();
                arrayList.add(0, new ExpressionStatement((Expression)new ConstructorCallExpression(ClassNode.SUPER, (Expression)new ArgumentListExpression(scwArgs))));
                for (final Statement s : body) {
                    ((ScopeTrackingClassCodeExpressionTransformer)visitor).withMethod((MethodNode)c, new Runnable(){

                        @Override
                        public void run() {
                            s.visit((GroovyCodeVisitor)visitor);
                        }
                    });
                    arrayList.add(s);
                }
                ConstructorNode c2 = new ConstructorNode(2, params, c.getExceptions(), (Statement)new BlockStatement(arrayList, c.getVariableScope()));
                classNode.addConstructor(c2);
            }
        }
    }

    @Deprecated
    public ClassCodeExpressionTransformer createVisitor(SourceUnit source) {
        return this.createVisitor(source, null);
    }

    public ClassCodeExpressionTransformer createVisitor(SourceUnit source, ClassNode clazz) {
        return new VisitorImpl(source, clazz);
    }

    public static boolean mightBePositionalArgumentConstructor(VariableExpression ve) {
        Class clazz;
        ClassNode type = ve.getType();
        if (type.isArray()) {
            return false;
        }
        try {
            clazz = type.getTypeClass();
        }
        catch (GroovyBugError x) {
            return false;
        }
        return clazz != null && clazz != Object.class && !Modifier.isAbstract(clazz.getModifiers());
    }

    static {
        MethodCallExpression aw = new MethodCallExpression((Expression)new VariableExpression("this"), "asWritable", (Expression)ArgumentListExpression.EMPTY_ARGUMENTS);
        aw.setImplicitThis(true);
        CLOSURE_THIS = new MethodCallExpression((Expression)aw, "getOwner", (Expression)ArgumentListExpression.EMPTY_ARGUMENTS);
    }

    class VisitorImpl
    extends ScopeTrackingClassCodeExpressionTransformer {
        private final SourceUnit sourceUnit;
        private boolean visitingClosureBody;
        private ClassNode clazz;

        VisitorImpl(SourceUnit sourceUnit, ClassNode clazz) {
            this.sourceUnit = sourceUnit;
            this.clazz = clazz;
        }

        @Override
        public void visitMethod(MethodNode node) {
            if (this.clazz == null) {
                this.clazz = node.getDeclaringClass();
            }
            super.visitMethod(node);
        }

        Expression transformArguments(Expression e) {
            List<Expression> l;
            if (e instanceof TupleExpression) {
                List expressions = ((TupleExpression)e).getExpressions();
                l = new ArrayList<Expression>(expressions.size());
                for (Expression expression : expressions) {
                    l.add(this.transform(expression));
                }
            } else {
                l = Collections.singletonList(this.transform(e));
            }
            return (Expression)this.withLoc((ASTNode)e, new MethodCallExpression((Expression)new ListExpression(l), "toArray", (Expression)new ArgumentListExpression()));
        }

        Expression makeCheckedCall(String name, Expression ... arguments) {
            return new StaticMethodCallExpression(checkerClass, name, (Expression)new ArgumentListExpression(arguments));
        }

        public Expression transform(Expression exp) {
            Expression o = this.innerTransform(exp);
            if (o != exp) {
                o.setSourcePosition((ASTNode)exp);
            }
            return o;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private Expression innerTransform(Expression exp) {
            PropertyExpression pe;
            MethodCallExpression call;
            ClosureExpression ce;
            if (exp instanceof ClosureExpression) {
                ce = (ClosureExpression)exp;
                try (StackVariableSet scope = new StackVariableSet(this);){
                    Parameter[] parameters = ce.getParameters();
                    if (parameters != null) {
                        if (parameters.length > 0) {
                            for (Parameter p : parameters) {
                                this.declareVariable((Variable)p);
                            }
                        } else {
                            this.declareVariable((Variable)new Parameter(ClassHelper.DYNAMIC_TYPE, "it"));
                        }
                    }
                    boolean old = this.visitingClosureBody;
                    this.visitingClosureBody = true;
                    try {
                        ce.getCode().visit((GroovyCodeVisitor)this);
                    }
                    finally {
                        this.visitingClosureBody = old;
                    }
                }
            }
            if (exp instanceof MethodCallExpression && SandboxTransformer.this.interceptMethodCall) {
                call = (MethodCallExpression)exp;
                Expression objExp = call.isImplicitThis() && this.visitingClosureBody && !this.isLocalVariableExpression(call.getObjectExpression()) ? CLOSURE_THIS : this.transform(call.getObjectExpression());
                Expression arg1 = call.getMethod();
                Expression arg2 = this.transformArguments(call.getArguments());
                if (call.getObjectExpression() instanceof VariableExpression && ((VariableExpression)call.getObjectExpression()).getName().equals("super")) {
                    if (this.clazz == null) {
                        throw new IllegalStateException("owning class not defined");
                    }
                    return this.makeCheckedCall("checkedSuperCall", new Expression[]{new ClassExpression(this.clazz), objExp, arg1, arg2});
                }
                return this.makeCheckedCall("checkedCall", new Expression[]{objExp, this.boolExp(call.isSafe()), this.boolExp(call.isSpreadSafe()), arg1, arg2});
            }
            if (exp instanceof StaticMethodCallExpression && SandboxTransformer.this.interceptMethodCall) {
                call = (StaticMethodCallExpression)exp;
                return this.makeCheckedCall("checkedStaticCall", new Expression[]{new ClassExpression(call.getOwnerType()), new ConstantExpression((Object)call.getMethod()), this.transformArguments(call.getArguments())});
            }
            if (exp instanceof MethodPointerExpression && SandboxTransformer.this.interceptMethodCall) {
                MethodPointerExpression mpe = (MethodPointerExpression)exp;
                return new ConstructorCallExpression(new ClassNode(SandboxedMethodClosure.class), (Expression)new ArgumentListExpression(mpe.getExpression(), mpe.getMethodName()));
            }
            if (exp instanceof ConstructorCallExpression && SandboxTransformer.this.interceptConstructor && !((ConstructorCallExpression)exp).isSpecialCall()) {
                return this.makeCheckedCall("checkedConstructor", new Expression[]{new ClassExpression(exp.getType()), this.transformArguments(((ConstructorCallExpression)exp).getArguments())});
            }
            if (exp instanceof AttributeExpression && SandboxTransformer.this.interceptAttribute) {
                AttributeExpression ae = (AttributeExpression)exp;
                return this.makeCheckedCall("checkedGetAttribute", new Expression[]{this.transform(ae.getObjectExpression()), this.boolExp(ae.isSafe()), this.boolExp(ae.isSpreadSafe()), this.transform(ae.getProperty())});
            }
            if (exp instanceof PropertyExpression && SandboxTransformer.this.interceptProperty) {
                pe = (PropertyExpression)exp;
                return this.makeCheckedCall("checkedGetProperty", new Expression[]{this.transformObjectExpression(pe), this.boolExp(pe.isSafe()), this.boolExp(pe.isSpreadSafe()), this.transform(pe.getProperty())});
            }
            if (exp instanceof VariableExpression && SandboxTransformer.this.interceptProperty) {
                VariableExpression vexp = (VariableExpression)exp;
                if (this.isLocalVariable(vexp.getName()) || vexp.getName().equals("this") || vexp.getName().equals("super")) {
                    return super.transform(exp);
                }
                PropertyExpression pexp = new PropertyExpression((Expression)VariableExpression.THIS_EXPRESSION, vexp.getName());
                pexp.setImplicitThis(true);
                this.withLoc((ASTNode)exp, pexp);
                return this.transform((Expression)pexp);
            }
            if (exp instanceof DeclarationExpression) {
                this.handleDeclarations((DeclarationExpression)exp);
            }
            if (exp instanceof BinaryExpression) {
                BinaryExpression be = (BinaryExpression)exp;
                if (Types.ofType((int)be.getOperation().getType(), (int)1100)) {
                    Expression lhs = be.getLeftExpression();
                    if (lhs instanceof VariableExpression) {
                        VariableExpression vexp = (VariableExpression)lhs;
                        if (this.isLocalVariable(vexp.getName()) || vexp.getName().equals("this") || vexp.getName().equals("super")) {
                            return super.transform(exp);
                        }
                        PropertyExpression pexp = new PropertyExpression((Expression)VariableExpression.THIS_EXPRESSION, vexp.getName());
                        pexp.setImplicitThis(true);
                        pexp.setSourcePosition((ASTNode)vexp);
                        lhs = pexp;
                    }
                    if (lhs instanceof PropertyExpression) {
                        PropertyExpression pe2 = (PropertyExpression)lhs;
                        String name = null;
                        if (lhs instanceof AttributeExpression) {
                            if (SandboxTransformer.this.interceptAttribute) {
                                name = "checkedSetAttribute";
                            }
                        } else {
                            Expression receiver = pe2.getObjectExpression();
                            if (receiver instanceof VariableExpression && ((VariableExpression)receiver).getName().equals("this")) {
                                FieldNode field;
                                FieldNode fieldNode = field = this.clazz != null ? this.clazz.getField(pe2.getPropertyAsString()) : null;
                                if (field != null) {
                                    return new BinaryExpression(lhs, be.getOperation(), this.transform(be.getRightExpression()));
                                }
                            }
                            if (SandboxTransformer.this.interceptProperty) {
                                name = "checkedSetProperty";
                            }
                        }
                        if (name == null) {
                            return super.transform(exp);
                        }
                        return this.makeCheckedCall(name, new Expression[]{this.transformObjectExpression(pe2), pe2.getProperty(), this.boolExp(pe2.isSafe()), this.boolExp(pe2.isSpreadSafe()), this.intExp(be.getOperation().getType()), this.transform(be.getRightExpression())});
                    }
                    if (lhs instanceof FieldExpression) {
                        return super.transform(exp);
                    }
                    if (lhs instanceof BinaryExpression) {
                        BinaryExpression lbe = (BinaryExpression)lhs;
                        if (lbe.getOperation().getType() == 30 && SandboxTransformer.this.interceptArray) {
                            return this.makeCheckedCall("checkedSetArray", new Expression[]{this.transform(lbe.getLeftExpression()), this.transform(lbe.getRightExpression()), this.intExp(be.getOperation().getType()), this.transform(be.getRightExpression())});
                        }
                    } else {
                        throw new AssertionError((Object)("Unexpected LHS of an assignment: " + lhs.getClass()));
                    }
                }
                if (be.getOperation().getType() == 30) {
                    if (SandboxTransformer.this.interceptArray) {
                        return this.makeCheckedCall("checkedGetArray", this.transform(be.getLeftExpression()), this.transform(be.getRightExpression()));
                    }
                } else {
                    if (be.getOperation().getType() == 544) {
                        return super.transform(exp);
                    }
                    if (Ops.isLogicalOperator(be.getOperation().getType())) {
                        return super.transform(exp);
                    }
                    if (be.getOperation().getType() == 573) {
                        if (SandboxTransformer.this.interceptMethodCall) {
                            return this.makeCheckedCall("checkedCall", new Expression[]{this.transform(be.getRightExpression()), this.boolExp(false), this.boolExp(false), this.stringExp("isCase"), this.transform(be.getLeftExpression())});
                        }
                    } else if (Ops.isRegexpComparisonOperator(be.getOperation().getType())) {
                        if (SandboxTransformer.this.interceptMethodCall) {
                            return this.makeCheckedCall("checkedStaticCall", new Expression[]{this.classExp(ScriptBytecodeAdapterClass), this.stringExp(Ops.binaryOperatorMethods(be.getOperation().getType())), this.transform(be.getLeftExpression()), this.transform(be.getRightExpression())});
                        }
                    } else if (Ops.isComparisionOperator(be.getOperation().getType())) {
                        if (SandboxTransformer.this.interceptMethodCall) {
                            return this.makeCheckedCall("checkedComparison", new Expression[]{this.transform(be.getLeftExpression()), this.intExp(be.getOperation().getType()), this.transform(be.getRightExpression())});
                        }
                    } else if (SandboxTransformer.this.interceptMethodCall) {
                        return this.makeCheckedCall("checkedBinaryOp", new Expression[]{this.transform(be.getLeftExpression()), this.intExp(be.getOperation().getType()), this.transform(be.getRightExpression())});
                    }
                }
            }
            if (exp instanceof PostfixExpression) {
                pe = (PostfixExpression)exp;
                return this.prefixPostfixExp(exp, pe.getExpression(), pe.getOperation(), "Postfix");
            }
            if (exp instanceof PrefixExpression) {
                pe = (PrefixExpression)exp;
                return this.prefixPostfixExp(exp, pe.getExpression(), pe.getOperation(), "Prefix");
            }
            if (exp instanceof CastExpression) {
                ce = (CastExpression)exp;
                return this.makeCheckedCall("checkedCast", new Expression[]{this.classExp(exp.getType()), this.transform(ce.getExpression()), this.boolExp(ce.isIgnoringAutoboxing()), this.boolExp(ce.isCoerce()), this.boolExp(ce.isStrict())});
            }
            return super.transform(exp);
        }

        private Expression prefixPostfixExp(Expression whole, Expression atom, Token opToken, String mode) {
            String op;
            String string = op = opToken.getText().equals("++") ? "next" : "previous";
            if (atom instanceof BinaryExpression && ((BinaryExpression)atom).getOperation().getType() == 30 && SandboxTransformer.this.interceptArray) {
                return this.makeCheckedCall("checked" + mode + "Array", new Expression[]{this.transform(((BinaryExpression)atom).getLeftExpression()), this.transform(((BinaryExpression)atom).getRightExpression()), this.stringExp(op)});
            }
            if (atom instanceof VariableExpression) {
                VariableExpression ve = (VariableExpression)atom;
                if (this.isLocalVariable(ve.getName())) {
                    if (mode.equals("Postfix")) {
                        return this.transform((Expression)this.withLoc((ASTNode)whole, new BinaryExpression((Expression)new ListExpression(Arrays.asList(atom, new BinaryExpression(atom, ASSIGNMENT_OP, (Expression)this.withLoc((ASTNode)atom, new MethodCallExpression(atom, op, (Expression)ArgumentListExpression.EMPTY_ARGUMENTS))))), new Token(30, "[", -1, -1), (Expression)new ConstantExpression((Object)0))));
                    }
                    return this.transform((Expression)this.withLoc((ASTNode)whole, new BinaryExpression(atom, ASSIGNMENT_OP, (Expression)this.withLoc((ASTNode)atom, new MethodCallExpression(atom, op, (Expression)ArgumentListExpression.EMPTY_ARGUMENTS)))));
                }
                PropertyExpression pexp = new PropertyExpression((Expression)VariableExpression.THIS_EXPRESSION, ve.getName());
                pexp.setImplicitThis(true);
                pexp.setSourcePosition((ASTNode)atom);
                atom = pexp;
            }
            if (atom instanceof PropertyExpression && SandboxTransformer.this.interceptProperty) {
                PropertyExpression pe = (PropertyExpression)atom;
                return this.makeCheckedCall("checked" + mode + "Property", new Expression[]{this.transformObjectExpression(pe), pe.getProperty(), this.boolExp(pe.isSafe()), this.boolExp(pe.isSpreadSafe()), this.stringExp(op)});
            }
            return whole;
        }

        private <T extends ASTNode> T withLoc(ASTNode src, T t) {
            t.setSourcePosition(src);
            return t;
        }

        private Expression transformObjectExpression(PropertyExpression exp) {
            if (exp.isImplicitThis() && this.visitingClosureBody && !this.isLocalVariableExpression(exp.getObjectExpression())) {
                return CLOSURE_THIS;
            }
            return this.transform(exp.getObjectExpression());
        }

        private boolean isLocalVariableExpression(Expression exp) {
            if (exp != null && exp instanceof VariableExpression) {
                return this.isLocalVariable(((VariableExpression)exp).getName());
            }
            return false;
        }

        ConstantExpression boolExp(boolean v) {
            return v ? ConstantExpression.PRIM_TRUE : ConstantExpression.PRIM_FALSE;
        }

        ConstantExpression intExp(int v) {
            return new ConstantExpression((Object)v, true);
        }

        ClassExpression classExp(ClassNode c) {
            return new ClassExpression(c);
        }

        ConstantExpression stringExp(String v) {
            return new ConstantExpression((Object)v);
        }

        public void visitExpressionStatement(ExpressionStatement es) {
            Expression exp = es.getExpression();
            if (exp instanceof DeclarationExpression) {
                DeclarationExpression de = (DeclarationExpression)exp;
                Expression leftExpression = de.getLeftExpression();
                if (leftExpression instanceof VariableExpression) {
                    if (!(de.getRightExpression() instanceof EmptyExpression) && SandboxTransformer.mightBePositionalArgumentConstructor((VariableExpression)leftExpression)) {
                        CastExpression ce = new CastExpression(leftExpression.getType(), de.getRightExpression());
                        ce.setCoerce(true);
                        es.setExpression(this.transform((Expression)new DeclarationExpression(leftExpression, de.getOperation(), (Expression)ce)));
                        return;
                    }
                } else {
                    throw new UnsupportedOperationException("not supporting tuples yet");
                }
            }
            super.visitExpressionStatement(es);
        }

        protected SourceUnit getSourceUnit() {
            return this.sourceUnit;
        }
    }
}

