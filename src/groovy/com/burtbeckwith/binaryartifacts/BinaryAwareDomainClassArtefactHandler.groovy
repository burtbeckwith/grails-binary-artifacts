package com.burtbeckwith.binaryartifacts

import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class BinaryAwareDomainClassArtefactHandler extends DomainClassArtefactHandler {

	@Override
	boolean isArtefactClass(Class clazz) {
		ClassRegistry.hasDomainClass(clazz) || super.isArtefactClass(clazz)
	}
}
