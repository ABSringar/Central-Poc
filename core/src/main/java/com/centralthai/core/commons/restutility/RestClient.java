package com.centralthai.core.commons.restutility;

import java.util.Map;

public interface RestClient {

    /**
     * To execute GET request
     *
     * @param url
     * @param username
     * @param password
     * @param headers
     * @return ResponseModal
     */
    ResponseModel httpGet(String url, String username, String password, Map<String,String> headers);

    /**
     * To execute POST request
     *
     * @param url
     * @param username
     * @param password
     * @Param headers
     * @return ResponseModal
     */
    ResponseModel httpPost(String url, String username, String password, String parameters,Map<String,String> headers);
}
