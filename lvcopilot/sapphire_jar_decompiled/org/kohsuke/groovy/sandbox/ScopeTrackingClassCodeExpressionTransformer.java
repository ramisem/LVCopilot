/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.codehaus.groovy.ast.ClassCodeExpressionTransformer
 *  org.codehaus.groovy.ast.FieldNode
 *  org.codehaus.groovy.ast.MethodNode
 *  org.codehaus.groovy.ast.Parameter
 *  org.codehaus.groovy.ast.Variable
 *  org.codehaus.groovy.ast.expr.ClosureExpression
 *  org.codehaus.groovy.ast.expr.DeclarationExpression
 *  org.codehaus.groovy.ast.expr.Expression
 *  org.codehaus.groovy.ast.expr.TupleExpression
 *  org.codehaus.groovy.ast.expr.VariableExpression
 *  org.codehaus.groovy.ast.stmt.BlockStatement
 *  org.codehaus.groovy.ast.stmt.CatchStatement
 *  org.codehaus.groovy.ast.stmt.DoWhileStatement
 *  org.codehaus.groovy.ast.stmt.ForStatement
 *  org.codehaus.groovy.ast.stmt.IfStatement
 *  org.codehaus.groovy.ast.stmt.SwitchStatement
 *  org.codehaus.groovy.ast.stmt.SynchronizedStatement
 *  org.codehaus.groovy.ast.stmt.TryCatchStatement
 *  org.codehaus.groovy.ast.stmt.WhileStatement
 */
package org.kohsuke.groovy.sandbox;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.kohsuke.groovy.sandbox.StackVariableSet;

abstract class ScopeTrackingClassCodeExpressionTransformer
extends ClassCodeExpressionTransformer {
    StackVariableSet varScope;

    ScopeTrackingClassCodeExpressionTransformer() {
    }

    public boolean isLocalVariable(String name) {
        return this.varScope.has(name);
    }

    public void visitMethod(MethodNode node) {
        this.varScope = null;
        try (StackVariableSet scope = new StackVariableSet(this);){
            for (Parameter p : node.getParameters()) {
                this.declareVariable((Variable)p);
            }
            super.visitMethod(node);
        }
    }

    void withMethod(MethodNode node, Runnable r) {
        this.varScope = null;
        try (StackVariableSet scope = new StackVariableSet(this);){
            for (Parameter p : node.getParameters()) {
                this.declareVariable((Variable)p);
            }
            r.run();
        }
    }

    public void visitField(FieldNode node) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            super.visitField(node);
        }
    }

    public void visitBlockStatement(BlockStatement block) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            super.visitBlockStatement(block);
        }
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            super.visitDoWhileLoop(loop);
        }
    }

    public void visitForLoop(ForStatement forLoop) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            this.declareVariable((Variable)forLoop.getVariable());
            super.visitForLoop(forLoop);
        }
    }

    public void visitIfElse(IfStatement ifElse) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            super.visitIfElse(ifElse);
        }
    }

    public void visitSwitch(SwitchStatement statement) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            super.visitSwitch(statement);
        }
    }

    public void visitSynchronizedStatement(SynchronizedStatement sync) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            super.visitSynchronizedStatement(sync);
        }
    }

    public void visitTryCatchFinally(TryCatchStatement statement) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            super.visitTryCatchFinally(statement);
        }
    }

    public void visitCatchStatement(CatchStatement statement) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            this.declareVariable((Variable)statement.getVariable());
            super.visitCatchStatement(statement);
        }
    }

    public void visitWhileLoop(WhileStatement loop) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            super.visitWhileLoop(loop);
        }
    }

    public void visitClosureExpression(ClosureExpression expression) {
        try (StackVariableSet scope = new StackVariableSet(this);){
            super.visitClosureExpression(expression);
        }
    }

    void handleDeclarations(DeclarationExpression exp) {
        Expression leftExpression = exp.getLeftExpression();
        if (leftExpression instanceof VariableExpression) {
            this.declareVariable((Variable)((VariableExpression)leftExpression));
        } else if (leftExpression instanceof TupleExpression) {
            TupleExpression te = (TupleExpression)leftExpression;
            for (Expression e : te.getExpressions()) {
                this.declareVariable((Variable)((VariableExpression)e));
            }
        }
    }

    void declareVariable(Variable exp) {
        this.varScope.declare(exp.getName());
    }
}

