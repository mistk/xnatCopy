package my.dao.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
//@EnableJpaRepositories(basePackages = {"my.dao.mysql.repository"}, entityManagerFactoryRef = "mysqlEntityManagerFactory", transactionManagerRef = "mysqlTransactionManager")
public class MysqlConfig extends BaseJpaConfig {

	@Autowired
    private JpaProperties jpaProperties;
	
	@Bean
	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix="spring.datasource.mysql")
	public DataSource mysqlDataSource() {
		DataSource dataSource = DataSourceBuilder.create().build();
		return dataSource;
	}
	
	
	@Bean
	@ConditionalOnMissingBean
	public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(EntityManagerFactoryBuilder builder) {
		DataSource db = mysqlDataSource();
		return builder.dataSource(db)
				.packages("my.dao.mysql.entity")
				.properties(jpaProperties.getHibernateProperties(db))
				.build();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public PlatformTransactionManager mysqlTransactionManager(EntityManagerFactoryBuilder builder) {
		return new JpaTransactionManager(mysqlEntityManagerFactory(builder).getObject());
	}
}
