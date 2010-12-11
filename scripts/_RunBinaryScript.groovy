runBinaryScript = { String className ->
	def instance = classLoader.loadClass(className).newInstance()
	instance.executeClosure.delegate = delegate
	instance.executeClosure()
}
