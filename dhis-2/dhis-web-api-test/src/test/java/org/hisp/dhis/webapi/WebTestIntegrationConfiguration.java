/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.webapi;

import java.util.Date;
import java.util.Properties;

import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

import org.hisp.dhis.cache.CacheProvider;
import org.hisp.dhis.commons.jackson.config.JacksonObjectMapperConfig;
import org.hisp.dhis.config.DataSourceConfig;
import org.hisp.dhis.config.HibernateConfig;
import org.hisp.dhis.config.HibernateEncryptionConfig;
import org.hisp.dhis.config.PostgresDhisConfigurationProvider;
import org.hisp.dhis.config.ServiceConfig;
import org.hisp.dhis.config.StartupConfig;
import org.hisp.dhis.config.StoreConfig;
import org.hisp.dhis.configuration.NotifierConfiguration;
import org.hisp.dhis.container.DhisPostgisContainerProvider;
import org.hisp.dhis.container.DhisPostgreSQLContainer;
import org.hisp.dhis.db.migration.config.FlywayConfig;
import org.hisp.dhis.external.conf.ConfigurationKey;
import org.hisp.dhis.external.conf.DhisConfigurationProvider;
import org.hisp.dhis.jdbc.config.JdbcConfig;
import org.hisp.dhis.leader.election.LeaderElectionConfiguration;
import org.hisp.dhis.leader.election.LeaderManager;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.program.jdbc.JdbcOrgUnitAssociationStoreConfiguration;
import org.hisp.dhis.scheduling.AbstractSchedulingManager;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobConfigurationService;
import org.hisp.dhis.scheduling.JobService;
import org.hisp.dhis.scheduling.SchedulingManager;
import org.hisp.dhis.security.SystemAuthoritiesProvider;
import org.hisp.dhis.startup.DefaultAdminUserPopulator;
import org.hisp.dhis.system.notification.Notifier;
import org.hisp.dhis.webapi.mvc.ContentNegotiationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.JdbcDatabaseContainer;

import com.google.common.collect.ImmutableMap;

// TODO make it independent of the DB (which was my intention). Then add h2 DB/Postgres DB
// on top in each of the respective base test classes/configs
/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com
 */
@Configuration
@ImportResource( locations = { "classpath*:/META-INF/dhis/beans.xml" } )
@ComponentScan( basePackages = { "org.hisp.dhis" }, useDefaultFilters = false, includeFilters = {
    @Filter( type = FilterType.ANNOTATION, value = Service.class ),
    @Filter( type = FilterType.ANNOTATION, value = Component.class ),
    @Filter( type = FilterType.ANNOTATION, value = Repository.class )

}, excludeFilters = @Filter( Configuration.class ) )
@Import( {
    HibernateConfig.class,
    DataSourceConfig.class,
    JdbcConfig.class,
    FlywayConfig.class,
    HibernateEncryptionConfig.class,
    ServiceConfig.class,
    StoreConfig.class,
    LeaderElectionConfiguration.class,
    NotifierConfiguration.class,
    org.hisp.dhis.setting.config.ServiceConfig.class,
    org.hisp.dhis.external.config.ServiceConfig.class,
    org.hisp.dhis.dxf2.config.ServiceConfig.class,
    org.hisp.dhis.support.config.ServiceConfig.class,
    org.hisp.dhis.validation.config.ServiceConfig.class,
    org.hisp.dhis.validation.config.StoreConfig.class,
    org.hisp.dhis.programrule.config.ProgramRuleConfig.class,
    org.hisp.dhis.reporting.config.StoreConfig.class,
    org.hisp.dhis.analytics.config.ServiceConfig.class,
    JacksonObjectMapperConfig.class,
    ContentNegotiationConfig.class,
    JdbcOrgUnitAssociationStoreConfiguration.class,
    StartupConfig.class
} )
@Transactional
@Slf4j
public class WebTestIntegrationConfiguration
{
    @Bean
    public static SessionRegistryImpl sessionRegistry()
    {
        return new SessionRegistryImpl();
    }

    // TODO can I extract/reuse the PostgresConfig now that its reused in
    // IntegrationTestConfig and WebTestNoDBConfig
    private static final String POSTGRES_DATABASE_NAME = "dhis";

    private static final String POSTGRES_CREDENTIALS = "dhis";

    public static final String CREATE_UPDATE_DELETE = "CREATE;UPDATE;DELETE";

    @Bean( name = "dhisConfigurationProvider" )
    public DhisConfigurationProvider dhisConfigurationProvider()
    {
        PostgresDhisConfigurationProvider dhisConfigurationProvider = new PostgresDhisConfigurationProvider();
        JdbcDatabaseContainer<?> postgreSQLContainer = initContainer();

        final String username = postgreSQLContainer.getUsername();
        final String password = postgreSQLContainer.getPassword();

        Properties properties = new Properties();

        String jdbcUrl = postgreSQLContainer.getJdbcUrl();
        properties.setProperty( "connection.url", jdbcUrl );
        properties.setProperty( "connection.dialect", "org.hisp.dhis.hibernate.dialect.DhisPostgresDialect" );
        properties.setProperty( "connection.driver_class", "org.postgresql.Driver" );
        properties.setProperty( "connection.username", username );
        properties.setProperty( "connection.password", password );
        properties.setProperty( ConfigurationKey.AUDIT_USE_IN_MEMORY_QUEUE_ENABLED.getKey(), "off" );
        properties.setProperty( "metadata.audit.persist", "on" );
        properties.setProperty( "tracker.audit.persist", "on" );
        properties.setProperty( "aggregate.audit.persist", "on" );
        properties.setProperty( "audit.metadata", CREATE_UPDATE_DELETE );
        properties.setProperty( "audit.tracker", CREATE_UPDATE_DELETE );
        properties.setProperty( "audit.aggregate", CREATE_UPDATE_DELETE );

        dhisConfigurationProvider.addProperties( properties );

        return dhisConfigurationProvider;
    }

    private JdbcDatabaseContainer<?> initContainer()
    {
        // NOSONAR
        DhisPostgreSQLContainer<?> postgisContainer = ((DhisPostgreSQLContainer<?>) new DhisPostgisContainerProvider()
            .newInstance()) // NOSONAR
                .appendCustomPostgresConfig( "max_locks_per_transaction=100" )
                .withDatabaseName( POSTGRES_DATABASE_NAME )
                .withUsername( POSTGRES_CREDENTIALS )
                .withPassword( POSTGRES_CREDENTIALS );

        postgisContainer.start();

        log.info( String.format( "PostGIS container initialized: %s", postgisContainer.getJdbcUrl() ) );

        return postgisContainer;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LdapAuthenticator ldapAuthenticator()
    {
        return authentication -> null;
    }

    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator()
    {
        return ( dirContextOperations, s ) -> null;
    }

    @Bean( "oAuth2AuthenticationManager" )
    public AuthenticationManager oAuth2AuthenticationManager()
    {
        return authentication -> null;
    }

    @Bean( "authenticationManager" )
    @Primary
    public AuthenticationManager authenticationManager()
    {
        return authentication -> null;
    }

    @Bean
    public DefaultAuthenticationEventPublisher authenticationEventPublisher()
    {
        DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher = new DefaultAuthenticationEventPublisher();
        defaultAuthenticationEventPublisher.setAdditionalExceptionMappings(
            ImmutableMap.of( OAuth2AuthenticationException.class, AuthenticationFailureBadCredentialsEvent.class ) );
        return defaultAuthenticationEventPublisher;
    }

    @Bean
    public SystemAuthoritiesProvider systemAuthoritiesProvider()
    {
        return () -> DefaultAdminUserPopulator.ALL_AUTHORITIES;
    }

    /**
     * During tests we do not want asynchronous job scheduling.
     */
    @Bean
    @Primary
    public SchedulingManager synchronousSchedulingManager( JobService jobService,
        JobConfigurationService jobConfigurationService,
        MessageService messageService, Notifier notifier, LeaderManager leaderManager, CacheProvider cacheProvider )
    {
        return new TestSchedulingManager( jobService, jobConfigurationService, messageService, notifier,
            leaderManager, cacheProvider );
    }

    public static class TestSchedulingManager extends AbstractSchedulingManager
    {
        private boolean enabled = true;

        public TestSchedulingManager( JobService jobService, JobConfigurationService jobConfigurationService,
            MessageService messageService, Notifier notifier, LeaderManager leaderManager, CacheProvider cacheProvider )
        {
            super( jobService, jobConfigurationService, messageService, leaderManager, notifier, cacheProvider );
        }

        @Override
        public void schedule( JobConfiguration configuration )
        {
            // we don't run it
        }

        @Override
        public void scheduleWithStartTime( JobConfiguration configuration, Date startTime )
        {
            // we don't run it
        }

        @Override
        public void stop( JobConfiguration configuration )
        {
            // its either never started or we don't support stop (silent)
        }

        @Override
        public boolean executeNow( JobConfiguration configuration )
        {
            if ( enabled )
            {
                execute( configuration );
            }
            return true;
        }

        public void setEnabled( boolean enabled )
        {

            this.enabled = enabled;
        }
    }
}
