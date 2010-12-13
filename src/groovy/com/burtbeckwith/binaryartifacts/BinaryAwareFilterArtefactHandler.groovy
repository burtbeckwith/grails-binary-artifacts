package com.burtbeckwith.binaryartifacts

import java.lang.reflect.Method

import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy

import org.codehaus.groovy.grails.plugins.web.filters.FiltersConfigArtefactHandler

/**
 * Proxies the instance that Grails creates to override the isArtefactClass method.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class BinaryAwareFilterArtefactHandler {

	static FiltersConfigArtefactHandler proxy(FiltersConfigArtefactHandler handler) {

		Enhancer enhancer = new Enhancer(superclass: FiltersConfigArtefactHandler)

		enhancer.callback = new MethodInterceptor() {
			Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) {
				if ('isArtefactClass'.equals(method.name)) {
					if (ClassRegistry.hasFilter(args[0])) {
						return true
					}
				}

				method.accessible = true
				method.invoke handler, args
			}
		}

		enhancer.create()
	}
}
