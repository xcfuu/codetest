package com.example.demo.config;


import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 多个数据库mybatis 配置数据源
 */
@Configuration
public class DataSourceConfigurer {
    /**
     * 数据库的数据源配置
     */
    @Configuration
    @MapperScan(value = { "com.example.demo.mapper" }, sqlSessionFactoryRef = "codeTestSqlSessionFactory")
    class CodeTestDataSourceConfigurer {
        @Value("${spring.datasource.druid.codeTest.publickey:}")
        private String decodePublicKey;
        static final String mapper_xml_location = "classpath*:mapper/*Mapper.xml";

        @Bean
        @ConfigurationProperties("spring.datasource.druid")
        @Primary
        public DataSource codeTestDataSource() {
            DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
            dataSource.setConnectionInitSqls(Lists.newArrayList("set names utf8mb4"));
            setDataSourcePublickey(dataSource,decodePublicKey);
            return dataSource;
        }

        @Bean
        @Primary
        public DataSourceTransactionManager codeTestTransactionManager() {
            return new DataSourceTransactionManager(codeTestDataSource());
        }

        @Bean
        @Primary
        public SqlSessionFactory codeTestSqlSessionFactory() throws Exception {
            final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
            sessionFactory.setDataSource(codeTestDataSource());
            sessionFactory
                    .setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapper_xml_location));
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

    /**
     * 配置数据源解密的公钥
     * @param DataSource
     * @param decodePublicKey
     */
    private void setDataSourcePublickey(DruidDataSource DataSource,String decodePublicKey) {
        if(!StringUtils.isEmpty(decodePublicKey)){
            DataSource.setConnectionProperties("config.decrypt=true;config.decrypt.key=" + decodePublicKey);
            try {
                DataSource.setFilters("config");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
}
