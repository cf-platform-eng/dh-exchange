package io.pivotal.cf.dh;

import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
interface ServerRepository {

    @RequestLine("GET /quote/{symbol}")
    public Map<String, Object> getQuote(@Param("symbol") String symbol, @HeaderMap Map<String, Object> headerMap);

    @RequestLine("GET /pubKey?name={name}&pubKey={pubKey}")
    public Map<String, String> getKey(@Param("name") String name, @Param(value = "pubKey") String key);
}