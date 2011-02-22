import grails.util.GrailsNameUtils
import grails.util.PluginBuildSettings

import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.web.pages.GroovyPageCompilerTask

includeTargets << grailsScript('_GrailsBootstrap')
includeTargets << grailsScript('_PluginDependencies')
includeTargets << grailsScript('_GrailsClean')

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */

target(packageBinaryPlugin: 'Replacement for package-plugin') {
	depends checkVersion, configureProxy, compile

	packageFiles basedir

	configureAst()

	registerArtifacts()

	// clean and recompile now that the AST is configured
	cleanAndRecompile()

	def conf = appCtx.grailsApplication.config.com.burtbeckwith.binaryartifacts

	File gspProperties = new File(classesDir, "$grailsAppName-gsp-views.properties")
	File artifactsProperties = new File(grailsSettings.resourcesDir, "$grailsAppName-binaryartifacts.properties")

	File gspFolder = new File(conf.gspFolder ?: "$basedir/grails-app/views")

	boolean compileGsp = conf.compileGsp instanceof Boolean ? conf.compileGsp : true
	compileGsp &= gspFolder.exists()

	if (compileGsp) {
		compileGsps conf, gspProperties, gspFolder
	}

	String jarName = "$grailsSettings.projectTargetDir/$grailsAppName-binaryartifacts.jar"

	buildJar jarName, gspProperties, artifactsProperties

	buildZip jarName, compileGsp, gspProperties, gspFolder, artifactsProperties
}

void configureAst() {
	rootLoader.addURL new File(binaryArtifactsPluginDir, 'binaryartifacts-ast.jar').toURI().toURL()

	// configure the override build settings so GlobalPluginAwareEntityASTTransformation can do its thing
	def realGetPluginInfoForSource = PluginBuildSettings.metaClass.getMetaMethod(
		'getPluginInfoForSource', [String] as Class[])
	def ClassRegistry = classLoader.loadClass('com.burtbeckwith.binaryartifacts.ClassRegistry')
	PluginBuildSettings.metaClass.getPluginInfoForSource = { String sourceFile ->
		if (ClassRegistry.isRegistered(sourceFile)) {
			return delegate.getPluginInfo(basedir)
		}

		realGetPluginInfoForSource.invoke sourceFile
	}

	pluginSettings = new PluginBuildSettings(grailsSettings)
	GrailsPluginUtils.pluginBuildSettings = pluginSettings
}

void registerArtifacts() {
	def ClassRegistry = classLoader.loadClass('com.burtbeckwith.binaryartifacts.ClassRegistry')
	ClassRegistry.loadFromProperties grailsSettings.resourcesDir, classLoader
}

void cleanAndRecompile() {
	clean()
	compilePlugins()
	bootstrap()
}

void compileGsps(conf, File gspProperties, File gspFolder) {
	ant.taskdef name: 'gspc', classname: GroovyPageCompilerTask.name

	// TODO specify pattern?
	ant.gspc destdir: classesDir,
	         srcdir: gspFolder.path,
	         packagename: GrailsNameUtils.getPropertyNameForLowerCaseHyphenSeparatedName(grailsAppName),
	         serverpath: '/WEB-INF/grails-app/views/',
	         classpathref: 'grails.compile.classpath',
	         tmpdir: new File(grailsSettings.projectWorkDir, 'gspcompile')

	ant.move file: new File(classesDir, 'gsp/views.properties').path,
	         tofile: gspProperties.path
}

void buildJar(String jarName, File gspProperties, File artifactsProperties) {
	// TODO specify packages?

	ant.jar(destfile: jarName, filesonly: true) {
		fileset(dir: classesDir) {
			exclude name: 'application.properties'
			exclude name: gspProperties.name
			exclude name: '*BootStrap*.class'
			exclude name: '*Config*.class'
			exclude name: '*DataSource*.class'
			exclude name: '*GrailsPlugin*.class'
			exclude name: '*UrlMappings*.class'
		}
		fileset(dir: grailsSettings.resourcesDir.path) {
			exclude name: artifactsProperties.name
			exclude name: 'web.xml'
		}
	}
}

void buildZip(String jarName, boolean compileGsp, File gspProperties, File gspFolder, File artifactsProperties) {

	File pluginFile
	for (File file in new File(basedir).listFiles()) {
		if (file.name.endsWith('GrailsPlugin.groovy')) {
			pluginFile = file
			break
		}
	}
	if (!pluginFile) {
		ant.fail "Plugin file not found for plugin project"
	}
	def descriptor = generatePluginXml(pluginFile)

	File tempEmptyGspFolder = new File('target/tempemptygsp')

	ant.zip(filesonly: true,
	        destfile: "$basedir/grails-${GrailsNameUtils.getPluginName(pluginFile.name)}-${descriptor.version}.zip") {

		fileset(dir: basedir) { include name: '*GrailsPlugin.groovy' }

		fileset file: "$basedir/application.properties"
		fileset file: "$basedir/LICENSE"
		fileset file: "$basedir/LICENSE.txt"
		fileset file: "$basedir/plugin.xml"
		zipfileset file: "$basedir/grails-app/conf/BuildConfig.groovy",
		           fullpath: 'dependencies.groovy'

		zipfileset(dir: "$basedir/web-app", prefix: 'web-app') {
			exclude name: '**/*.tld'
		}

		zipfileset dir: "$basedir/scripts", prefix: 'scripts'

		zipfileset file: jarName, prefix: 'lib'

		zipfileset file: gspProperties.path, prefix: 'grails-app/conf'
		zipfileset file: artifactsProperties.path, prefix: 'grails-app/conf'

		if (compileGsp) {
			// precompiled GSPs are only used if the GSP file exists, so this creates empty files
			tempEmptyGspFolder.deleteDir() // in case it wasn't deleted last time
			gspFolder.eachFileRecurse { file ->
				if (!file.directory) {
					String relative = (file.path - gspFolder.path)[1..-1]
					def emptyGspFile = new File(tempEmptyGspFolder, relative)
					emptyGspFile.parentFile.mkdirs()
					emptyGspFile.createNewFile()
				}
			}
			zipfileset dir: tempEmptyGspFolder.path, prefix: 'grails-app/views'
		}
		else if (gspFolder.exists()) {
			zipfileset dir: gspFolder.path, prefix: 'grails-app/views'
		}
	}

	tempEmptyGspFolder.deleteDir()
}

setDefaultTarget packageBinaryPlugin
