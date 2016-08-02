package io.pivotal.cf.dh;

import feign.RequestLine;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository {

    @RequestLine("GET /client/pubKey")
    public String getKey();
}