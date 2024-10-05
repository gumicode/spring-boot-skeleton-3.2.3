package com.gumicode.database.member;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;


@Configuration
@EnableJpaRepositories(
        basePackages = "com.gumicode.database.member",
        transactionManagerRef = "memberTransactionManager",
        entityManagerFactoryRef = "memberEntityManagerFactory")
public class MemberDataSourceConfiguration {

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

        MemberRoutingDataSourceConfiguration routingDataSource = new MemberRoutingDataSourceConfiguration();

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
            @Qualifier("memberRoutingDataSource") DataSource rouutingDataSource, ConfigurableListableBeanFactory beanFactory) {
        Map<String, String> jpaPropertyMap = jpaProperties().getProperties();
        Map<String, Object> hibernatePropertyMap =
                hibernateProperties().determineHibernateProperties(jpaPropertyMap, new HibernateSettings());

        LocalContainerEntityManagerFactoryBean build = new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), jpaPropertyMap, null)
                .dataSource(rouutingDataSource)
                .properties(hibernatePropertyMap)
                .persistenceUnit("memberEntityManager")
                .packages("com.gumicode.database.member.entity") // entity packages
                .build();

        build.getJpaPropertyMap().put(AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(beanFactory)); // jpa bean 의존성 주입
        return build;
    }

    @Bean
    public PlatformTransactionManager memberTransactionManager(
            @Qualifier("memberEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
