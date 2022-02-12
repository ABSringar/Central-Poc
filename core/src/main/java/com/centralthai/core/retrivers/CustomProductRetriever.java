package com.centralthai.core.retrivers;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.magento.graphql.ProductInterfaceQuery;
import com.adobe.cq.commerce.magento.graphql.ProductInterfaceQueryDefinition;
import com.adobe.cq.commerce.magento.graphql.ProductPriceQueryDefinition;

public class CustomProductRetriever extends AbstractCustomProductRetriever {

    public CustomProductRetriever(MagentoGraphqlClient client) {
        super(client);
    }

    @Override
    protected ProductInterfaceQueryDefinition generateCustomProductQuery() {
        return (ProductInterfaceQuery q) -> {
            q.name().sku().urlKey().image(productImageQuery -> productImageQuery.url()).priceRange(r -> r
                    .minimumPrice(generatePriceQuery()));
            if (productQueryHook != null) {
                productQueryHook.accept(q);
            }
        };

    }

    protected ProductPriceQueryDefinition generatePriceQuery() {
        return q -> q
                .regularPrice(r -> r
                        .value()
                        .currency())
                .finalPrice(f -> f
                        .value()
                        .currency())
                .discount(d -> d
                        .amountOff()
                        .percentOff());
    }
}
