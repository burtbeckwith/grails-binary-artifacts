package com.burtbeckwith.binaryartifacts

import org.codehaus.groovy.grails.commons.ControllerArtefactHandler

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class BinaryAwareControllerArtefactHandler extends ControllerArtefactHandler {

	@Override
	boolean isArtefactClass(Class clazz) {
		ClassRegistry.hasController(clazz) || super.isArtefactClass(clazz)
	}
}
