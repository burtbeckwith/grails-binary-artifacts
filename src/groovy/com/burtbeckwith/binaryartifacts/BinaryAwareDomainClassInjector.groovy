package com.burtbeckwith.binaryartifacts

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class BinaryAwareDomainClassInjector extends DefaultGrailsDomainClassInjector {

	@Override
	protected boolean isDomainClass(ClassNode classNode, SourceUnit sourceNode) {
		ClassRegistry.isDomainClass(sourceNode.name) || super.isDomainClass(classNode, sourceNode)
	}
}
