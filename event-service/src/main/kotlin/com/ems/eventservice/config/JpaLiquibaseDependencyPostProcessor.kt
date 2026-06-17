package com.ems.eventservice.config

import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.stereotype.Component

@Component
class JpaLiquibaseDependencyPostProcessor : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        if (!beanFactory.containsBeanDefinition(ENTITY_MANAGER_FACTORY_BEAN)) {
            return
        }

        val liquibaseBeans = beanFactory.getBeanNamesForType(SpringLiquibase::class.java, true, false)
        if (liquibaseBeans.isEmpty()) {
            return
        }

        val entityManagerFactory = beanFactory.getBeanDefinition(ENTITY_MANAGER_FACTORY_BEAN)
        val dependencies = entityManagerFactory.dependsOn.orEmpty()
            .toList()
            .plus(liquibaseBeans.toList())
            .distinct()
            .toTypedArray()
        entityManagerFactory.setDependsOn(*dependencies)
    }

    private companion object {
        const val ENTITY_MANAGER_FACTORY_BEAN = "entityManagerFactory"
    }
}
