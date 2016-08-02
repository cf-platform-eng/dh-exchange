package io.pivotal.cf.dh.service;

import feign.RequestLine;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository {

    @RequestLine("GET /client/pubKey")
    public String getKey();
}