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
package com.centralthai.core.models;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        resourceType = "centralthai/components/helloworld")
@Exporter(name = "jackson", extensions = "json")
public class HelloWorldModel {

    @ValueMapValue(name = PROPERTY_RESOURCE_TYPE, injectionStrategy = InjectionStrategy.OPTIONAL)
    @Default(values = "No resourceType")
    protected String resourceType;

    @SlingObject
    private Resource currentResource;
    @SlingObject
    private ResourceResolver resourceResolver;




    @ValueMapValue
    private String products;


    private String name;
    private String sku;

    private String message;

    @PostConstruct
    protected void init() {
        String res = null;
        try {ObjectMapper mapper = new ObjectMapper();
            try {
                String jsonString = mapper.writeValueAsString("");
                message = jsonString;
            } catch (JsonProcessingException e) {
            }
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost post = new HttpPost("https://staging-mdc.central.co.th/graphql");
            post.addHeader("Content-Type", "application/json");
            post.addHeader("Accept", "application/json");/*
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("query","query {\\n\" +\n" +
                    "                    \"  episodesByIds(ids: [1, 2]) {\\n\" +\n" +
                    "                    \"    name\\n\" +\n" +
                    "                    \"    characters {\\n\" +\n" +
                    "                    \"      name\\n\" +\n" +
                    "                    \"    }\\n\" +\n" +
                    "                    \"  }\\n\" +\n" +
                    "                    \"}");
            jsonObject.put("variables",new JSONObject());*/
         /*   String productQuery = "{\"operationName\":\"categoryListWithProducts\",\"variables\":{\"pageSize\":25,\"page\":1,\"sort\":{\"name\":\"ASC\"},\"uid\":\"260\"},\"query\":\"query categoryListWithProducts($uid:String!$pageSize:Int!$sort:ProductAttributeSortInput!$page:Int!){categoryList(filters:{category_uid:{eq:$uid}}){image uid name url_key updated_at __typename breadcrumbs{category_uid category_name category_url_key __typename}children{image uid name url_key updated_at __typename children_count product_count breadcrumbs{category_uid category_name category_url_key __typename}}products(pageSize:$pageSize sort:$sort currentPage:$page){page_info{total_pages __typename}total_count items{__typename id uid sku name thumbnail{url __typename}url_key updated_at price_range{minimum_price{final_price{currency value __typename}__typename}__typename}}__typename}}}\"}";
            post.setEntity(new StringEntity(productQuery));
            HttpResponse response = httpclient.execute(post);
            org.apache.http.HttpEntity entity = response.getEntity();
            if(Optional.ofNullable(entity).isPresent()) {
                message = EntityUtils.toString(entity);
                JSONObject jsonObject = new JSONObject(message);
                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONObject("products").getJSONArray("items");

                JSONObject jsonObject1 = new JSONObject(jsonArray.get(0).toString());
                name =jsonObject1.getString("name");
                sku = jsonObject1.getString("sku");
            }*/

        } catch (Exception exception) {
        }
    }


    public String getMessage() {
        return message;
    }


    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }
}
