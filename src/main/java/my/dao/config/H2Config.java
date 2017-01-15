package my.dao.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(basePackages = {"my.dao.h2.repository"}, entityManagerFactoryRef = "h2EntityManagerFactory", transactionManagerRef = "h2TransactionManager")
public class H2Config extends BaseJpaConfig {
	
	@Autowired
    private JpaProperties jpaProperties;
	
	@Bean
	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix="spring.datasource.h2")
	public DataSource h2DataSource() {
		DataSource dataSource = DataSourceBuilder.create().build();
		return dataSource;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public LocalContainerEntityManagerFactoryBean h2EntityManagerFactory(EntityManagerFactoryBuilder builder) {
		DataSource db = h2DataSource();
		return builder.dataSource(db)
				.packages("my.dao.h2.entity")
				.properties(jpaProperties.getHibernateProperties(db))
				.build();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public PlatformTransactionManager h2TransactionManager(EntityManagerFactoryBuilder builder) {
		return new JpaTransactionManager(h2EntityManagerFactory(builder).getObject());
	}
}
