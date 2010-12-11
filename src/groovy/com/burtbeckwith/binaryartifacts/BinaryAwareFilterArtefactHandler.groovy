package com.burtbeckwith.binaryartifacts

import org.codehaus.groovy.grails.plugins.web.filters.FiltersConfigArtefactHandler

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class BinaryAwareFilterArtefactHandler extends FiltersConfigArtefactHandler {

	@Override
	boolean isArtefactClass(Class clazz) {
		ClassRegistry.hasFilter(clazz) || super.isArtefactClass(clazz)
	}
}
