package com.gumicode.database.member;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;


@Configuration
@EnableJpaRepositories(
        basePackages = "com.gumicode",
        transactionManagerRef = "memberTransactionManager",
        entityManagerFactoryRef = "memberEntityManagerFactory")
public class ConfigurationMemberDataSource {


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource memberWriteDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource memberReadDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource memberIntegrationRoutingDataSource(@Qualifier("memberWriteDataSource") DataSource writeDataSource,
                                                         @Qualifier("memberReadDataSource") DataSource readDataSource) {

        ConfigurationMemberRoutingDataSource routingDataSource = new ConfigurationMemberRoutingDataSource();

        Map<Object, Object> targetDataSourceMap = Map.of("read", readDataSource, "write", writeDataSource);
        routingDataSource.setDefaultTargetDataSource(writeDataSource);
        routingDataSource.setTargetDataSources(targetDataSourceMap);
        return routingDataSource;
    }

    @Bean
    public DataSource memberRoutingDataSource(@Qualifier("memberIntegrationRoutingDataSource") DataSource integrationRoutingDataSource) {
        return new LazyConnectionDataSourceProxy(integrationRoutingDataSource);
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.jpa")
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.jpa.hibernate")
    public HibernateProperties hibernateProperties() {
        return new HibernateProperties();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean memberEntityManagerFactory(
            @Qualifier("memberRoutingDataSource") DataSource rouutingDataSource) {
        Map<String, String> jpaPropertyMap = jpaProperties().getProperties();
        Map<String, Object> hibernatePropertyMap =
                hibernateProperties().determineHibernateProperties(jpaPropertyMap, new HibernateSettings());

        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), jpaPropertyMap, null)
                .dataSource(rouutingDataSource)
                .properties(hibernatePropertyMap)
                .persistenceUnit("gumicode")
                .packages("com.gumicode")
                .build();
    }

    @Bean
    public PlatformTransactionManager memberTransactionManager(
            @Qualifier("memberEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
