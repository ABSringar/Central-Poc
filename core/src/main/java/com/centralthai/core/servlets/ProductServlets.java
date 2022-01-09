/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.centralthai.core.servlets;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.discovery.commons.providers.util.ResourceHelper;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Products Servlet",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "sling.servlet.paths=/central/products",
                "sling.servlet.extensions=json"
        })
@ServiceDescription("Simple Demo Servlet")
public class ProductServlets extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final String BUCKET_PATH = "/etc/central";

    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {
        Resource resource = null;
        try {
            String productJson = getProducts();
            ResourceResolver resourceResolver = req.getResourceResolver();
            resource = resourceResolver.getResource(BUCKET_PATH);
            if (resource == null) {
                Map<String, Object> map = new HashMap<>();
                map.put("jcr:primaryType", "nt:unstructured");
                map.put("products", productJson);
                resource = ResourceUtil.getOrCreateResource(resourceResolver, BUCKET_PATH, map, "abc/abc", true);
            } else {
                ModifiableValueMap modifiableValueMap = resource.adaptTo(ModifiableValueMap.class);
                modifiableValueMap.put("products", productJson);
            }
            resp.setContentType("application/json");
            resourceResolver.commit();
        } catch (Exception e) {

        }
        resp.getWriter().write(resource.adaptTo(ValueMap.class).get("products", ""));
    }

    private String getProducts() {
        String products = "";
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost post = new HttpPost("https://staging-mdc.central.co.th/graphql");
            post.addHeader("Content-Type", "application/json");
            post.addHeader("Accept", "application/json");
            String x = "{\"query\":\"# Welcome to Altair GraphQL Client.\\n# You can send your request using CmdOrCtrl + Enter.\\n\\n# Enter your graphQL query here.\\n{\\nproducts(filter: {sku: {}}) {\\nitems {\\nname\\nsku\\n\\n}\\n}\\n}\",\"variables\":{},\"operationName\":null}";
            post.setEntity(new StringEntity(x));
            HttpResponse response = httpclient.execute(post);
            org.apache.http.HttpEntity entity = response.getEntity();
            if (Optional.ofNullable(entity).isPresent()) {
                products = EntityUtils.toString(entity);
            }

        } catch (Exception exception) {
        }

        return products;
    }
}

