package io.pivotal.cf.dh.service;

import feign.Param;
import feign.RequestLine;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository {

    @RequestLine("GET /yql?q={query}&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")
    public String getQuote(@Param("query") String query);
}