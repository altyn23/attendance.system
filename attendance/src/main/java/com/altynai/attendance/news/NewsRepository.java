package com.altynai.attendance.news;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsRepository extends MongoRepository<News, String> {
    List<News> findTop10ByOrderByCreatedAtDesc();
}
