package com.centralthai.core.retrivers;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.magento.graphql.ProductInterfaceQuery;
import com.adobe.cq.commerce.magento.graphql.ProductInterfaceQueryDefinition;

public class CustomProductRetriever extends AbstractCustomProductRetriever {

    public CustomProductRetriever(MagentoGraphqlClient client) {
        super(client);
    }

    @Override
    protected ProductInterfaceQueryDefinition generateCustomProductQuery() {
        return  (ProductInterfaceQuery q) -> {
            q.name().sku().urlKey();
            if (productQueryHook != null) {
                productQueryHook.accept(q);
            }
        } ;
    }
}
