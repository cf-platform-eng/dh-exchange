package io.pivotal.cf.dh;

import feign.Param;
import feign.RequestLine;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
interface QuoteRepository {

    @RequestLine("GET /yql?q={query}&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")
    Map<String, Object> getQuote(@Param("query") String query);
}