package com.burtbeckwith.binaryartifacts

import org.codehaus.groovy.grails.commons.CodecArtefactHandler

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class BinaryAwareCodecArtefactHandler extends CodecArtefactHandler {

	@Override
	boolean isArtefactClass(Class clazz) {
		ClassRegistry.hasCodec(clazz) || super.isArtefactClass(clazz)
	}
}
