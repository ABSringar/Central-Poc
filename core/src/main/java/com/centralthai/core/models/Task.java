package com.centralthai.core.models;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;

@Model(adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        resourceType = "centralthai/components/task")
@Exporter(name = "jackson", extensions = "json")
public class Task {

    @ValueMapValue
    private String apiType;

    private String apiResponse;

    private static final String INSIDER_API = "https://jsonplaceholder.typicode.com/todos/1";

    private static final String ALGOLIA_API = "https://jsonplaceholder.typicode.com/todos/2";

    @PostConstruct
    private void init() throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(apiType.equals("insider") ? INSIDER_API : ALGOLIA_API);
        get.addHeader("Content-Type", "application/json");
        get.addHeader("Accept", "application/json");
        HttpResponse response = httpclient.execute(get);
        org.apache.http.HttpEntity entity = response.getEntity();
        if (Optional.ofNullable(entity).isPresent()) {
            apiResponse = EntityUtils.toString(entity);
        }
    }


    public String getApiResponse() {
        return apiResponse;
    }
}
