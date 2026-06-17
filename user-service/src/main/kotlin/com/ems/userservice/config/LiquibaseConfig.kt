package com.ems.userservice.config

import javax.sql.DataSource
import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LiquibaseConfig {
    @Bean
    @ConditionalOnMissingBean(SpringLiquibase::class)
    @ConditionalOnProperty(prefix = "spring.liquibase", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun liquibase(
        dataSource: DataSource,
        @Value("\${spring.liquibase.change-log}") changeLog: String,
    ): SpringLiquibase =
        SpringLiquibase().apply {
            this.dataSource = dataSource
            this.changeLog = changeLog
        }
}
