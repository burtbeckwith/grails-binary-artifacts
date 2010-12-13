import java.util.jar.JarEntry
import java.util.jar.JarFile
import org.springframework.util.FileCopyUtils

eventPackagePluginStart = {

	if (grailsAppName != 'grails-binary-artifacts') {
		// only do this for this plugin, not when installed in another
		return
	}

	// hack to get the readme files into the zip
	String dir = "$projectWorkDir/plugin-info"
	ant.mkdir dir: dir
	ant.copy file: "$basedir/binaryartifacts-ast.jar", todir: dir
}

eventCreateWarStart = { warName, stagingDir ->
	def classesDir = new File(stagingDir, 'WEB-INF/classes')

	// create a file with a known name that lists the property files since it's easy
	// to find them here, but impractical when running in a war
	new File(classesDir, 'binary-artifacts-names.properties').withWriter { writer ->
		classesDir.eachFile { file ->
			if (file.name.toLowerCase().endsWith('-binaryartifacts.properties')) {
				writer.write file.name
				writer.write '\n'
			}
		}
	}

	// extra out precompiled GSPs since they don't resolve properly from a jar
	new File(stagingDir, 'WEB-INF/lib').eachFile { file ->
		if (!file.name.toLowerCase().endsWith('-binaryartifacts.jar')) {
			return
		}

		JarFile jf = new JarFile(file)
		jf.entries().each { JarEntry je ->
			if (je.name.startsWith('gsp_')) {
				FileCopyUtils.copy jf.getInputStream(je),
					new FileOutputStream(new File(classesDir, je.name))
			}
		}
	}
}
