package com.burtbeckwith.binaryartifacts;

import org.codehaus.groovy.grails.commons.DefaultGrailsServiceClass;

/**
 * Subclasses the default implementation to allow specifying the Spring bean name
 * rather than inferring it from the class name.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class BinaryAwareServiceClass extends DefaultGrailsServiceClass {

	private String _beanName;

	/**
	 * Constructor.
	 *
	 * @param clazz the implementing class
	 * @param beanName the Spring bean name to use
	 */
	public BinaryAwareServiceClass(Class<?> clazz, String beanName) {
		super(clazz);
		_beanName = beanName;
	}

	@Override
	public String getPropertyName() { return _beanName; }
}
