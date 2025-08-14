//package com.booking.booking_service.config;
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
//import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
//
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.util.Date;
//import java.util.List;
//
//@Configuration
//@EnableMongoRepositories(basePackages = "com.booking.booking_service.repository")
//public class MongoConfig extends AbstractMongoClientConfiguration {
//
//    @Override
//    protected String getDatabaseName() {
//        return "booking-service";
//    }
//
//    @Bean
//    @Override
//    public MongoCustomConversions customConversions() {
//        return new MongoCustomConversions(List.of(
//                new LocalDateTimeToDateConverter(),
//                new DateToLocalDateTimeConverter()
//        ));
//    }
//
//    // Converter để chuyển đổi LocalDateTime thành Date cho MongoDB
//    private static class LocalDateTimeToDateConverter implements org.springframework.core.convert.converter.Converter<LocalDateTime, Date> {
//        @Override
//        public Date convert(LocalDateTime source) {
//            return Date.from(source.toInstant(ZoneOffset.UTC));
//        }
//    }
//
//    // Converter để chuyển đổi Date thành LocalDateTime từ MongoDB
//    private static class DateToLocalDateTimeConverter implements org.springframework.core.convert.converter.Converter<Date, LocalDateTime> {
//        @Override
//        public LocalDateTime convert(Date source) {
//            return LocalDateTime.ofInstant(source.toInstant(), ZoneOffset.UTC);
//        }
//    }
//}