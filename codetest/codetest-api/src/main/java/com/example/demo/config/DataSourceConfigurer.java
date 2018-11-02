package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author xuechaofu
 * @date 2018/11/218:43
 */
@Configuration
public class DataSourceConfigurer {

    /**
     * saas数据库的数据源配置
     */
    @Configuration
    @MapperScan(value = { "com.tuhu.saas.inventory.mapper" }, sqlSessionFactoryRef = "inventorySqlSessionFactory")
    class ProductDataSourceConfigurer {
        @Value("${spring.datasource.druid.inventory.publickey:}")
        private String decodePublicKey;
        static final String MAPPER_XML_LOCATION = "classpath*:sqlmap/saas/**/*Mapper.xml";

        @Bean(initMethod = "init")
        @ConfigurationProperties("spring.datasource.druid.inventory")
        @Primary
        public DataSource DataSource() {
            DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
            dataSource.setConnectionInitSqls(Lists.newArrayList("set names utf8mb4"));
            setDataSourcePublickey(dataSource,decodePublicKey);
            return dataSource;
        }

        @Bean
        @Primary
        public DataSourceTransactionManager TransactionManager() {
            return new DataSourceTransactionManager(inventoryDataSource());
        }

        @Bean
        @Primary
        public SqlSessionFactory SqlSessionFactory() throws Exception {
            final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
            sessionFactory.setDataSource(inventoryDataSource());
            sessionFactory
                    .setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MAPPER_XML_LOCATION));
            PageHelper pageHelper = new PageHelper();
            Properties properties = new Properties();
            properties.setProperty("reasonable", "true");
            properties.setProperty("supportMethodsArguments", "true");
            properties.setProperty("returnPageInfo", "check");
            properties.setProperty("params", "count=countSql");
            pageHelper.setProperties(properties);
            //添加插件
            sessionFactory.setPlugins(new Interceptor[]{pageHelper});
            SqlSessionFactory sqlSessionFactory = sessionFactory.getObject();
            sqlSessionFactory.getConfiguration().setMapUnderscoreToCamelCase(true);

            return sqlSessionFactory;
        }

    }
}
