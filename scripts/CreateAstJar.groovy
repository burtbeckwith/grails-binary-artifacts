includeTargets << grailsScript('_GrailsBootstrap')

target(createAstJar: 'Creates a jar with the AST transformation class and support classes') {
	depends checkVersion, configureProxy, clean, compile, compilePlugins

	ant.jar(destfile: "$basedir/binaryartifacts-ast.jar") {
		fileset(dir: classesDir) {
			include name: '**/com/burtbeckwith/binaryartifacts/*.class'
		}
		metainf dir: 'resources/META-INF'
	}
}

setDefaultTarget createAstJar
