package com.centralthai.core.retrivers;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.core.components.models.retriever.AbstractRetriever;
import com.adobe.cq.commerce.graphql.client.GraphqlResponse;
import com.adobe.cq.commerce.magento.graphql.*;
import com.adobe.cq.commerce.magento.graphql.gson.Error;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractCustomProductRetriever extends AbstractRetriever {
    protected String identifier;

    protected Consumer<ProductInterfaceQuery> productQueryHook;

    protected List<ProductInterface> products;

    public AbstractCustomProductRetriever(MagentoGraphqlClient client) {
        super(client);
    }

    public List<ProductInterface> fetchProducts() {
        if (this.products == null) {
            populate();
        }
        return this.products;
    }

    public void setIdentifier(String identifier) {
        products = null;
        query = null;
        this.identifier = identifier;
    }

    @Override
    protected void populate() {
        GraphqlResponse<Query, Error> response = executeQuery();
        if (CollectionUtils.isEmpty(response.getErrors())) {
            Query rootQuery = response.getData();
            products = rootQuery.getProducts().getItems();
        } else {
            products = Collections.emptyList();
        }
    }

    protected String generateQuery(String identifier) {
        //FilterEqualTypeInput identifierFilter = new FilterEqualTypeInput().setEq(identifier);
        //ProductAttributeFilterInput filter = new ProductAttributeFilterInput().setSku(identifierFilter);

        QueryQuery.ProductsArgumentsDefinition searchArgs = s -> s.search(String.valueOf(identifier));

        ProductsQueryDefinition queryArgs = q -> q.items(generateCustomProductQuery());
        return Operations.query(query -> query
                .products(searchArgs, queryArgs)).toString();
    }

    protected GraphqlResponse<Query, Error> executeQuery() {
        if (query == null) {
            query = generateQuery(identifier);
        }
        return client.execute(query);
    }

    protected abstract ProductInterfaceQueryDefinition generateCustomProductQuery();
}
