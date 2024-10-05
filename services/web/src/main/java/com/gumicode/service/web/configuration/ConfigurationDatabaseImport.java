package com.gumicode.service.web.configuration;

import com.gumicode.cache.ConfigurationMemberCacheSource;
import com.gumicode.database.member.MemberImportConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MemberImportConfiguration.class, ConfigurationMemberCacheSource.class})
public class ConfigurationDatabaseImport {
}
