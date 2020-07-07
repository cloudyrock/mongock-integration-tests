package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;


import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.testConfiguration.TestConfigurationChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;
import com.github.cloudyrock.spring.v5.MongockTestConfiguration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;


//TODO: Use testContainers here
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = MongockTestConfigurationTest.ApplicationConfiguration.class,
        initializers = MongockTestConfigurationTest.ApplicationInitializer.class
)
@DataMongoTest
@EnableMongoRepositories(basePackageClasses = ClientRepository.class)
public class MongockTestConfigurationTest {


    @Autowired
    private SpringDataMongo3Driver mongockDriver;

    private MongockTemplate mongockTemplate;

    @Configuration
    @MongockTestConfiguration
    public static class ApplicationConfiguration {
    }

    public static class ApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            GenericContainer mongo = RuntimeTestUtil.startMongoDbContainer("mongo:4.2.0");
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                     String.format("spring.data.mongodb.uri=mongodb://%s:%d", mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext, "spring.data.mongodb.database=" + RuntimeTestUtil.DEFAULT_DATABASE_NAME);
        }
    }

    @BeforeEach
    void beforeEach() {
        this.mongockTemplate = mongockDriver.getMongockTemplate();
        mongockTemplate.getCollection(TestConfigurationChangeLog.COLLECTION_NAME).deleteMany(new Document().append("field", "value"));
    }

    @Test
    void shouldInjectMongockTemplateToContext_wheMongockTestConfigurationAnnotation() {

        new TestConfigurationChangeLog().testConfigurationWithMongockTemplate(mongockTemplate);

        Assertions.assertEquals(
                1,
                mongockTemplate.getCollection(TestConfigurationChangeLog.COLLECTION_NAME).countDocuments(new Document().append("field", "value")));

    }

}
