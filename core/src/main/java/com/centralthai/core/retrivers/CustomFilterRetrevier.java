package com.centralthai.core.retrivers;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.core.components.models.retriever.AbstractRetriever;
import com.adobe.cq.commerce.graphql.client.GraphqlResponse;
import com.adobe.cq.commerce.magento.graphql.Query;
import com.adobe.cq.commerce.magento.graphql.gson.Error;

public class CustomFilterRetrevier extends AbstractRetriever {

    public CustomFilterRetrevier(MagentoGraphqlClient client) {
        super(client);
    }

    @Override
    protected void populate() {
        executeQuery();
    }

    @Override
    protected GraphqlResponse<Query, Error> executeQuery() {
        if (query == null) {
            query = "";
        }
        return client.execute(query);
    }


}
