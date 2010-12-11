package com.burtbeckwith.binaryartifacts

import org.codehaus.groovy.grails.commons.TagLibArtefactHandler

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class BinaryAwareTaglibArtefactHandler extends TagLibArtefactHandler {

	@Override
	boolean isArtefactClass(Class clazz) {
		ClassRegistry.hasTaglib(clazz) || super.isArtefactClass(clazz)
	}
}
