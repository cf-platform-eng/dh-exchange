package io.pivotal.cf.dh.service;

import feign.Param;
import feign.RequestLine;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository {

    @RequestLine("GET /quote/{symbol}")
    public String getQuote(@Param("symbol") String symbol);

    @RequestLine("GET /pubKey")
    public String getKey();
}