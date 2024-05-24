package com.mobigen.dolphin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.mobigen.dolphin.repository.openmetadata",
        entityManagerFactoryRef = "openMetadataEntityManager",
        transactionManagerRef = "openMetadataTransactionManager"
)
public class OpenMetadataConfiguration {
    @Bean(name = "openmetadata")
    @ConfigurationProperties(prefix = "spring.openmetadata-datasource")
    public DataSource openMetadataDataSource() {
        return DataSourceBuilder.create()
                .build();
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean openMetadataEntityManager() {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(openMetadataDataSource());
        em.setPackagesToScan(new String[]{"com.mobigen.dolphin.entity.openmetadata"});
        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        //Hibernate 설정
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto","none");
        properties.put("hibernate.dialect","org.hibernate.dialect.H2Dialect");
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager openMetadataTransactionManager() {
        var transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(openMetadataEntityManager().getObject());
        return transactionManager;
    }
}