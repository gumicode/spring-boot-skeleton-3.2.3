package com.gumicode.service.web.configuration;

import com.gumicode.database.member.ConfigurationMemberDataSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ConfigurationMemberDataSource.class)
public class ConfigurationDatabaseImport {
}
