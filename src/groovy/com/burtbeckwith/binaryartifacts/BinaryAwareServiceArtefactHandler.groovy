package com.burtbeckwith.binaryartifacts

import java.lang.reflect.Method

import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy

import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.ServiceArtefactHandler

/**
 * Proxies the instance that Grails creates to override the isArtefactClass and newArtefactClass methods.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class BinaryAwareServiceArtefactHandler {

	static ServiceArtefactHandler proxy(ServiceArtefactHandler handler) {

		Enhancer enhancer = new Enhancer(superclass: ServiceArtefactHandler)

		enhancer.callback = new MethodInterceptor() {
			Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) {
				if ('isArtefactClass'.equals(method.name)) {
					if (ClassRegistry.hasService(args[0])) {
						return true
					}
				}
				else if ('newArtefactClass'.equals(method.name)) {
					if (ClassRegistry.hasService(args[0])) {
						return new BinaryAwareServiceClass(args[0], ClassRegistry.getServiceBeanName(args[0]))
					}
				}

				method.accessible = true
				method.invoke handler, args
			}
		}

		enhancer.create()
	}
}
