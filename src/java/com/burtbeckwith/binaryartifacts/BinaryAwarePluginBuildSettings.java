package com.burtbeckwith.binaryartifacts;

import grails.util.BuildSettings;
import grails.util.PluginBuildSettings;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.plugins.GrailsPluginInfo;
import org.codehaus.groovy.grails.plugins.GrailsPluginManager;

/**
 * Overrides getPluginInfoForSource so the AST considers binary artifacts to be valid.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class BinaryAwarePluginBuildSettings extends PluginBuildSettings {

	private String basedir;
	private Logger log = Logger.getLogger(getClass());

	/**
	 * Constructor.
	 *
	 * @param buildSettings
	 * @param pluginManager
	 * @param basedir
	 */
	public BinaryAwarePluginBuildSettings(BuildSettings buildSettings,
			GrailsPluginManager pluginManager, String basedir) {
		super(buildSettings, pluginManager);
		this.basedir = basedir;
	}

	@Override
	public GrailsPluginInfo getPluginInfoForSource(String sourceFile) {
		if (ClassRegistry.isRegistered(sourceFile)) {
			if (log.isDebugEnabled()) log.debug("Returning plugin info for " + sourceFile);
			return getPluginInfo(basedir);
		}

		return super.getPluginInfoForSource(sourceFile);
	}
}
