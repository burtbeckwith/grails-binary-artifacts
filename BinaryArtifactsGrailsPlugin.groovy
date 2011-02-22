import com.burtbeckwith.binaryartifacts.ClassRegistry

import grails.util.BuildSettingsHolder
import grails.util.GrailsUtil

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.web.filters.FiltersGrailsPlugin

class BinaryArtifactsGrailsPlugin {

	String version = '1.0.1'
	String grailsVersion = '1.3.3 > *'
	String author = 'Burt Beckwith'
	String authorEmail = 'burt@burtbeckwith.com'
	String title = 'Binary Artifacts Plugin'
	String description = 'The Binary Artifacts plugin lets you create plugins comprised of compiled classes and handles their registration as artifacts.'
	String documentation = 'http://grails.org/plugin/binary-artifacts'
	List pluginExcludes = [
		'scripts/CreateAstJar.groovy',
		'docs/**',
		'src/docs/**'
	]

	def loadBefore = ['codecs', 'controllers', 'domainClass',
	                  'filters', 'groovyPages', 'services']

	private Logger log = Logger.getLogger(getClass())
	private stubs = []

	def doWithWebDescriptor = { xml ->

		if (!application.warDeployed) {
			// run-app
			// TODO don't use holders
			ClassRegistry.loadFromProperties BuildSettingsHolder.settings.resourcesDir, application.classLoader
			createStubs application
		}

		callDoWithWebDescriptor xml, delegate
	}

	def doWithSpring = {
		if (application.warDeployed) {
			ClassRegistry.loadFromProperties application.classLoader
			createStubs application
		}

		ClassRegistry.registerCodecClasses application
		ClassRegistry.registerControllerClasses application
		ClassRegistry.registerDomainClasses application
		ClassRegistry.registerFilterClasses application
		ClassRegistry.registerServices application
		ClassRegistry.registerTaglibs application

		// there's a bug in loadBefore resolution where sometimes the order isn't correct and
		// the filters plugin loads first. so register the Spring beans here - worst case is
		// they get replaced by the filters plugin's beans
		for (filter in application.filtersClasses) {
			def callable = FiltersGrailsPlugin.BEANS.curry(filter)
			callable.delegate = delegate
			callable.call()
		}

		callDoWithSpring delegate
	}

	private void createStubs(application) {
		for (className in ClassRegistry.closureStubClassNames) {
			stubs << application.classLoader.loadClass(className).newInstance()
		}
	}

	def doWithDynamicMethods = { ctx ->
		callDoWithDynamicMethods ctx, delegate
	}

	def doWithApplicationContext = { ctx ->

		callDoWithApplicationContext ctx, delegate

		ClassRegistry.registerPrecompiledGsps ctx, application
	}

	def onChange = { event ->
		// not supported, no convenient way to have plugins register what they're watching
	}

	def onConfigChange = { event ->
		callOnConfigChange event, delegate
	}

	private void callDoWithWebDescriptor(xml, del) {
		for (stub in stubs) {
			try {
				if (stub.metaClass.hasProperty(stub, 'doWithWebDescriptor')) {
					stub.doWithWebDescriptor.delegate = del
					stub.doWithWebDescriptor xml
				}
			}
			catch (e) {
				GrailsUtil.deepSanitize e
				log.error "Error calling doWithWebDescriptor for $stub: $e.message", e
			}
		}
	}

	private void callDoWithSpring(del) {
		for (stub in stubs) {
			try {
				if (stub.metaClass.hasProperty(stub, 'doWithSpring')) {
					stub.doWithSpring.delegate = del
					stub.doWithSpring()
				}
			}
			catch (e) {
				GrailsUtil.deepSanitize e
				log.error "Error calling doWithSpring for $stub: $e.message", e
			}
		}
	}

	private void callDoWithDynamicMethods(ctx, del) {
		for (stub in stubs) {
			try {
				if (stub.metaClass.hasProperty(stub, 'doWithDynamicMethods')) {
					stub.doWithDynamicMethods.delegate = del
					stub.doWithDynamicMethods ctx
				}
			}
			catch (e) {
				GrailsUtil.deepSanitize e
				log.error "Error calling doWithDynamicMethods for $stub: $e.message", e
			}
		}
	}

	private void callDoWithApplicationContext(ctx, del) {
		for (stub in stubs) {
			try {
				if (stub.metaClass.hasProperty(stub, 'doWithApplicationContext')) {
					stub.doWithApplicationContext.delegate = del
					stub.doWithApplicationContext ctx
				}
			}
			catch (e) {
				GrailsUtil.deepSanitize e
				log.error "Error calling doWithApplicationContext for $stub: $e.message", e
			}
		}
	}

	private void callOnConfigChange(event, del) {
		for (stub in stubs) {
			try {
				if (stub.metaClass.hasProperty(stub, 'onConfigChange')) {
					stub.onConfigChange.delegate = del
					stub.onConfigChange event
				}
			}
			catch (e) {
				GrailsUtil.deepSanitize e
				log.error "Error calling onConfigChange for $stub: $e.message", e
			}
		}
	}
}
