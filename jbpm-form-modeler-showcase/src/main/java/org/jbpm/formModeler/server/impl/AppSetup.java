package org.jbpm.formModeler.server.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.server.config.ConfigGroup;
import org.guvnor.structure.server.config.ConfigType;
import org.guvnor.structure.server.config.ConfigurationFactory;
import org.guvnor.structure.server.config.ConfigurationService;
import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;
import org.uberfire.io.IOService;

@ApplicationScoped
@Startup(StartupType.BOOTSTRAP)
public class AppSetup {

    private static final String JBPM_REPO_PLAYGROUND = "jbpm-playground";
    private static final String JBPM_URL = "https://github.com/guvnorngtestuser1/jbpm-console-ng-playground-kjar.git";
    private final String userName = "guvnorngtestuser1";
    private final String password = "test1234";

    private static final String GLOBAL_SETTINGS = "settings";

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private RepositoryService repositoryService;

    @Inject
    private OrganizationalUnitService organizationalUnitService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ConfigurationFactory configurationFactory;

    @PostConstruct
    public void onStartup() {
        try {
            Repository jbpmRepo = repositoryService.getRepository( JBPM_REPO_PLAYGROUND );
            if ( jbpmRepo == null ) {
                jbpmRepo = repositoryService.createRepository( "git",
                                                               JBPM_REPO_PLAYGROUND,
                                                               new HashMap<String, Object>() {{
                                                                   put( "origin", JBPM_URL );
                                                                   put( "username", userName );
                                                                   put( "crypt:password", password );
                                                               }} );
            }

            // TODO in case groups are not defined
            Collection<OrganizationalUnit> groups = organizationalUnitService.getOrganizationalUnits();
            if ( groups == null || groups.isEmpty() ) {
                final List<Repository> repositories = new ArrayList<Repository>();
                repositories.add( jbpmRepo );

                organizationalUnitService.createOrganizationalUnit( "demo",
                                                                    "demo@jbpm.org",
                                                                    null,
                                                                    repositories );
            }

            //Define mandatory properties
            List<ConfigGroup> globalConfigGroups = configurationService.getConfiguration( ConfigType.GLOBAL );
            boolean globalSettingsDefined = false;
            for ( ConfigGroup globalConfigGroup : globalConfigGroups ) {
                if ( GLOBAL_SETTINGS.equals( globalConfigGroup.getName() ) ) {
                    globalSettingsDefined = true;
                    break;
                }
            }
            if ( !globalSettingsDefined ) {
                configurationService.addConfiguration( getGlobalConfiguration() );
            }

        } catch ( Exception e ) {
            throw new RuntimeException( "Error when starting Form Modeler " + e.getMessage(), e );
        }
    }

    private ConfigGroup getGlobalConfiguration() {
        //Global Configurations used by many of Drools Workbench editors
        final ConfigGroup group = configurationFactory.newConfigGroup( ConfigType.GLOBAL,
                                                                       GLOBAL_SETTINGS,
                                                                       "" );
        group.addConfigItem( configurationFactory.newConfigItem( "drools.dateformat",
                                                                 "dd-MMM-yyyy" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "drools.datetimeformat",
                                                                 "dd-MMM-yyyy hh:mm:ss" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "drools.defaultlanguage",
                                                                 "en" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "drools.defaultcountry",
                                                                 "US" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "build.enable-incremental",
                                                                 "true" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "rule-modeller-onlyShowDSLStatements",
                                                                 "false" ) );
        return group;
    }
}

