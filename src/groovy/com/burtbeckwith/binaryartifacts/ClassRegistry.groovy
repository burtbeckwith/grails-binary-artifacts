package com.burtbeckwith.binaryartifacts

import grails.util.GrailsUtil

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.CodecArtefactHandler
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.ServiceArtefactHandler
import org.codehaus.groovy.grails.commons.TagLibArtefactHandler
import org.codehaus.groovy.grails.plugins.web.filters.FiltersConfigArtefactHandler

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class ClassRegistry {

	private static final Logger LOG = Logger.getLogger(this)

	private static Set<String> codecClassNames = []
	private static Set<String> controllerClassNames = []
	private static Set<String> domainClassNames = []
	private static Set<String> filterClassNames = []
	private static Map<String, String> serviceClassNames = [:]
	private static Set<String> taglibClassNames = []
	private static Set<String> gspPropertyFileNames = []
	private static Set<String> closureStubClassNames = []

	private ClassRegistry() {
		// static only
	}

	// run-app
	static void loadFromProperties(File resourceDirectory, classLoader) {
		for (File file in resourceDirectory.listFiles()) {
			if (file.name.toLowerCase().endsWith('-binaryartifacts.properties')) {
				loadFromPropertyFile new FileInputStream(file), file.name, classLoader
			}
		}
	}

	// war
	static void loadFromProperties(ClassLoader classLoader) {
		def artifactNames = classLoader.getResource('binary-artifacts-names.properties')
		if (artifactNames) {
			for (name in artifactNames.text.split('\n')) {
				def resource = classLoader.getResource(name)
				loadFromPropertyFile resource.openConnection().inputStream, resource.path, classLoader
			}
		}
		else {
			// TODO
		}
	}

	static void loadFromPropertyFile(InputStream inputStream, String name, classLoader) {
		try {
			def props = new Properties()
			props.load inputStream

			LOG.debug "Loading properties $props from $name"

			if (props.stub) {
				registerPluginStub props.stub.toString().trim()
			}

			if (props.gspPropertyFile) {
				registerGspPropertyFile props.gspPropertyFile.toString().trim()
			}

			for (codec in split(props.codecs)) {
				registerCodec codec
			}

			for (controller in split(props.controllers)) {
				registerController controller
			}

			for (domainClass in split(props.domainClasses)) {
				registerDomainClass domainClass
			}

			for (filter in split(props.filters)) {
				registerFilter filter
			}

			for (taglib in split(props.taglibs)) {
				registerTaglib taglib
			}

			for (String serviceAndBeanName in split(props.services)) {
				def parts = serviceAndBeanName.split(':')
				if (parts.length != 2) {
					// TODO
				}

				registerService parts[0].trim(),  parts[1].trim()
			}
		}
		catch (e) {
			GrailsUtil.deepSanitize e
			LOG.debug "Error loading properties from $name: $e.message", e
		}
	}

	private static List<String> split(String s) {
		def tokens = []
		for (token in s?.tokenize(',')) {
			if (token) {
				tokens << token.trim()
			}
		}
		tokens
	}

	/**
	 * Register a Codec class.
	 *
	 * @param className the class name; must end in 'Codec'
	 */
	static void registerCodec(String className) {
		codecClassNames << className
		LOG.debug "Registered Codec class $className"
	}

	/**
	 * Register a Controller class.
	 *
	 * @param className the class name; must end in 'Controller'
	 */
	static void registerController(String className) {
		controllerClassNames << className
		LOG.debug "Registered Controller class $className"
	}

	/**
	 * Register a Domain class.
	 *
	 * @param className the class name; can be any name like regular domain classes
	 */
	static void registerDomainClass(String className) {
		domainClassNames << className
		LOG.debug "Registered Domain class $className"
	}

	/**
	 * Register a Filter class.
	 *
	 * @param className the class name; doesn't have to end in 'Filters' like regular filter classes
	 */
	static void registerFilter(String className) {
		filterClassNames << className
		LOG.debug "Registered Filter class $className"
	}

	/**
	 * Register a Service class.
	 *
	 * @param className the class name; doesn't have to end in 'Service' like regular service classes
	 * @param beanName the Spring bean name
	 */
	static void registerService(String className, String beanName) {
		serviceClassNames[className] = beanName
		LOG.debug "Registered Service class $className with bean name $beanName"
	}

	/**
	 * Register a Taglib class.
	 *
	 * @param className the class name; doesn't have to end in 'TagLib' like regular taglib classes
	 */
	static void registerTaglib(String className) {
		taglibClassNames << className
		LOG.debug "Registered Taglib class $className"
	}

	/**
	 * Register a properties file with compiled GSP information.
	 *
	 * @param path path in classpath
	 */
	static void registerGspPropertyFile(String path) {
		gspPropertyFileNames << path
		LOG.debug "Registered properties file $path"
	}

	/**
	 * Register a class with callback closures corresponding to the "doWithXXX" closures in
	 * a plugin descriptor. Allows a compiled class to act as the plugin descriptor.
	 *
	 * @param className the class name
	 */
	static void registerPluginStub(String className) {
		closureStubClassNames << className
		LOG.debug "Registered Plugin Stub $className"
	}

	static Set<String> getCodecClassNames() { codecClassNames.asImmutable() }
	static Set<String> getControllerClassNames() { controllerClassNames.asImmutable() }
	static Set<String> getDomainClassNames() { domainClassNames.asImmutable() }
	static Set<String> getFilterClassNames() { filterClassNames.asImmutable() }
	static Map<String, String> getServiceClassNames() { serviceClassNames.asImmutable() }
	static Set<String> getTaglibClassNames() { taglibClassNames.asImmutable() }
	static Set<String> getGspPropertyFileNames() { gspPropertyFileNames.asImmutable() }
	static Set<String> getClosureStubClassNames() { closureStubClassNames.asImmutable() }

	static boolean hasCodec(Class clazz) {
		codecClassNames.contains clazz.name
	}

	static boolean hasController(Class clazz) {
		controllerClassNames.contains clazz.name
	}

	static boolean hasDomainClass(Class clazz) {
		domainClassNames.contains clazz.name
	}

	static boolean isDomainClass(String filePath) {
		isRegisteredClass filePath, domainClassNames
	}

	static boolean isRegisteredClass(String filePath, Set names) {
		for (String name in names) {
			if (filePath.endsWith('/' + name.replaceAll('\\.', File.separator) + '.groovy')) {
				return true
			}
		}

		false
	}

	static boolean isRegistered(String filePath) {
		isRegisteredClass(filePath, domainClassNames) ||
			isRegisteredClass(filePath, controllerClassNames) ||
			isRegisteredClass(filePath, serviceClassNames.keySet()) ||
			isRegisteredClass(filePath, codecClassNames) ||
			isRegisteredClass(filePath, filterClassNames) ||
			isRegisteredClass(filePath, taglibClassNames)
	}

	static boolean hasFilter(Class clazz) {
		filterClassNames.contains clazz.name
	}

	static boolean hasService(Class clazz) {
		serviceClassNames.containsKey clazz.name
	}

	static String getServiceBeanName(Class clazz) {
		serviceClassNames[clazz.name]
	}

	static boolean hasTaglib(Class clazz) {
		taglibClassNames.contains clazz.name
	}

	/**
	 * Called by plugin descriptor at startup
	 * @param application the application
	 */
	static void registerCodecClasses(GrailsApplication application) {

		CodecArtefactHandler handler = application.getArtefactHandler(CodecArtefactHandler.TYPE)
		application.registerArtefactHandler BinaryAwareCodecArtefactHandler.proxy(handler)

		for (name in codecClassNames) {
			addArtefact application, CodecArtefactHandler.TYPE, name
		}
	}

	/**
	 * Called by plugin descriptor at startup
	 * @param application the application
	 */
	static void registerControllerClasses(GrailsApplication application) {

		ControllerArtefactHandler handler = application.getArtefactHandler(ControllerArtefactHandler.TYPE)
		application.registerArtefactHandler BinaryAwareControllerArtefactHandler.proxy(handler)

		for (name in controllerClassNames) {
			addArtefact application, ControllerArtefactHandler.TYPE, name
		}
	}

	/**
	 * Called by plugin descriptor at startup
	 * @param application the application
	 */
	static void registerDomainClasses(GrailsApplication application) {

		DomainClassArtefactHandler handler = application.getArtefactHandler(DomainClassArtefactHandler.TYPE)
		application.registerArtefactHandler BinaryAwareDomainClassArtefactHandler.proxy(handler)

		for (name in domainClassNames) {
			addArtefact application, DomainClassArtefactHandler.TYPE, name
		}
	}

	/**
	 * Called by plugin descriptor at startup
	 * @param application the application
	 */
	static void registerFilterClasses(GrailsApplication application) {

		FiltersConfigArtefactHandler handler = application.getArtefactHandler(FiltersConfigArtefactHandler.TYPE)
		application.registerArtefactHandler BinaryAwareFilterArtefactHandler.proxy(handler)

		for (name in filterClassNames) {
			addArtefact application, FiltersConfigArtefactHandler.TYPE, name
		}
	}

	/**
	 * Called by plugin descriptor at startup
	 * @param application the application
	 */
	static void registerServices(GrailsApplication application) {

		ServiceArtefactHandler handler = application.getArtefactHandler(ServiceArtefactHandler.TYPE)
		application.registerArtefactHandler BinaryAwareServiceArtefactHandler.proxy(handler)

		for (String name in serviceClassNames.keySet()) {
			addArtefact application, ServiceArtefactHandler.TYPE, name
		}
	}

	/**
	 * Called by plugin descriptor at startup
	 * @param application the application
	 */
	static void registerTaglibs(GrailsApplication application) {

		TagLibArtefactHandler handler = application.getArtefactHandler(TagLibArtefactHandler.TYPE)
		application.registerArtefactHandler BinaryAwareTaglibArtefactHandler.proxy(handler)

		for (String name in taglibClassNames) {
			addArtefact application, TagLibArtefactHandler.TYPE, name
		}
	}

	/**
	 * Called by plugin descriptor at startup.
	 * @param application the application
	 */
	static void registerPrecompiledGsps(ctx, GrailsApplication application) {
		def precompiledGspMap = ctx.groovyPagesTemplateEngine.precompiledGspMap
		if (precompiledGspMap == null) {
			return
		}

		for (name in gspPropertyFileNames) {
			try {
				def props = new Properties()
				def inputStream = application.classLoader.getResourceAsStream(name)
				if (inputStream) {
					props.load inputStream
					precompiledGspMap.putAll props
				}
				else {
					LOG.warn "getResourceAsStream was null for property file $name"
				}
			}
			catch (e) {
				GrailsUtil.deepSanitize e
				LOG.error "Error loading precompiled GSP properties: $e.message", e
			}
		}

		LOG.debug "precompiledGspMap is now $precompiledGspMap"
	}

	static void addArtefact(GrailsApplication application, String type, String className) {
		try {
			LOG.debug "Adding $type artifact $className"

			// TODO CNFE?
			Class clazz = application.classLoader.loadClass(className)
			if (clazz) {
				application.addArtefact type, clazz
			}
			else {
				LOG.warn "Artefact class $className not found, skipping"
			}
		}
		catch (e) {
			GrailsUtil.deepSanitize e
			LOG.error "Error adding $type artifact $className: $e.message", e
		}
	}
}
