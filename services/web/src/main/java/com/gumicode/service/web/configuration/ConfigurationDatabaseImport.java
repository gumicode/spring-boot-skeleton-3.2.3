package com.gumicode.service.web.configuration;

import com.gumicode.cache.ConfigurationMemberCacheSource;
import com.gumicode.database.member.ConfigurationMemberDataSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ConfigurationMemberDataSource.class, ConfigurationMemberCacheSource.class})
public class ConfigurationDatabaseImport {
}
