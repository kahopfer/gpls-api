//package com.designteam1.config;
//
//import com.mongodb.MongoClientURI;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.MongoDbFactory;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
//import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
//import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
//import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
//
//@Configuration
//public class MongoConfig {
//    public @Bean
//    MongoDbFactory mongoDbFactory() throws Exception {
//        return new SimpleMongoDbFactory(new MongoClientURI(System.getProperty("spring.data.mongodb.uri")));
//    }
//
//    public @Bean
//    MongoTemplate mongoTemplate() throws Exception {
//        MappingMongoConverter converter = new MappingMongoConverter(mongoDbFactory(), new MongoMappingContext());
//        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
//        return new MongoTemplate(mongoDbFactory(), converter);
//    }
//}
