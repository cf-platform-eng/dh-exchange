package io.pivotal.cf.dh;

import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Repository
public interface ServerRepository {

    @RequestLine("GET /quote/{symbol}")
    public String getQuote(@Param("symbol") String symbol, @HeaderMap Map<String, Object> headerMap);

    @RequestLine("GET /pubKey?name={name}&pubKey={pubKey}")
    public Map<String, String> getKey(@Param("name") String name, @Param(value = "pubKey") String key);
}