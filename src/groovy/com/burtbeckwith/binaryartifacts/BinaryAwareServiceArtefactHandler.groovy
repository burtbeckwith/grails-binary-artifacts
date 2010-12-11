package com.burtbeckwith.binaryartifacts

import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.ServiceArtefactHandler

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class BinaryAwareServiceArtefactHandler extends ServiceArtefactHandler {

	@Override
	boolean isArtefactClass(Class clazz) {
		ClassRegistry.hasService(clazz) || super.isArtefactClass(clazz)
	}

	GrailsClass newArtefactClass(Class clazz) {
		if (ClassRegistry.hasService(clazz)) {
			return new BinaryAwareServiceClass(clazz, ClassRegistry.getServiceBeanName(clazz))
		}
		super.newArtefactClass clazz
	}
}
