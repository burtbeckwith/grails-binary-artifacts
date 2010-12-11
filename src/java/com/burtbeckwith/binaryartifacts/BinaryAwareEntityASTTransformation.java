package com.burtbeckwith.binaryartifacts;

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * Identical to GlobalEntityASTTransformation except that it replaces the domain class injector
 * with one that's aware of binary artifacts.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class BinaryAwareEntityASTTransformation implements ASTTransformation {

	private BinaryAwareDomainClassInjector domainClassInjector = new BinaryAwareDomainClassInjector();

	public void visit(ASTNode[] nodes, SourceUnit source) {

		ASTNode astNode = nodes[0];
		if (!(astNode instanceof ModuleNode)) {
			return;
		}

		List<ClassNode> classes = ((ModuleNode)astNode).getClasses();
		if (classes.size() > 0) {
			domainClassInjector.performInjection(source, classes.get(0));
		}
	}
}
