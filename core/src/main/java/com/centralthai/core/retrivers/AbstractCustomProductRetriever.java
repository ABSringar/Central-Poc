package com.centralthai.core.retrivers;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.core.components.models.retriever.AbstractRetriever;
import com.adobe.cq.commerce.core.search.models.FilterAttributeMetadata;
import com.adobe.cq.commerce.graphql.client.GraphqlResponse;
import com.adobe.cq.commerce.magento.graphql.*;
import com.adobe.cq.commerce.magento.graphql.gson.Error;
import com.centralthai.core.models.CustomGenericProductAttributeFilterInput;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractCustomProductRetriever extends AbstractRetriever {
    protected Map<String, Object> identifier;
    protected String sortKey;
    protected String sortOrder;
    protected int limit;
    protected List<FilterAttributeMetadata> availableFilters;

    protected Consumer<ProductInterfaceQuery> productQueryHook;

    protected List<ProductInterface> products;

    AbstractCustomProductRetriever(MagentoGraphqlClient client) {
        super(client);
    }

    /**
     * Method to fetch products by creating graphql query and executing it
     * @return
     */
    public List<ProductInterface> fetchProducts() {
        if (this.products == null) {
            populate();
        }
        return this.products;
    }


    /**
     * To set filter and sort attributes
     *
     * @param identifier
     * @param sortKey
     * @param sortOrder
     * @param availableFilters
     * @param noOfProducts
     */
    public void setIdentifier(Map<String, Object> identifier, String sortKey, String sortOrder, List<FilterAttributeMetadata> availableFilters, int noOfProducts) {
        products = null;
        query = null;
        this.identifier = identifier;
        this.sortKey = sortKey;
        this.sortOrder = sortOrder;
        this.availableFilters = availableFilters;
        this.limit = noOfProducts;
    }

    /**
     * Method to extend product query in models and servlets
     *
     * @param productQueryHook
     */
    public void extendProductQueryWith(Consumer<ProductInterfaceQuery> productQueryHook) {
        if (this.productQueryHook == null) {
            this.productQueryHook = productQueryHook;
        } else {
            this.productQueryHook = this.productQueryHook.andThen(productQueryHook);
        }
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

    /**
     * This method constructs Graphql query based on the filter attributes configured by an author
     *
     * @return query
     */
    private String generateProductsQueryString() {
        CustomGenericProductAttributeFilterInput filterInputs = new CustomGenericProductAttributeFilterInput();
        identifier.entrySet().stream().filter((field) -> {
            return availableFilters.stream().anyMatch((item) -> {
                return item.getAttributeCode().equals(field.getKey());
            });
        }).forEach((filterCandidate) -> {
            String code = (String) filterCandidate.getKey();
            FilterAttributeMetadata filterAttributeMetadata = (FilterAttributeMetadata) availableFilters.stream().filter((item) -> {
                return item.getAttributeCode().equals(code);
            }).findFirst().get();
            if ("FilterEqualTypeInput".equals(filterAttributeMetadata.getFilterInputType())) {
                FilterEqualTypeInput filter = new FilterEqualTypeInput();
                filter.setIn((List<String>) filterCandidate.getValue());
                filterInputs.addEqualTypeInput(code, filter);
            } else if ("FilterRangeTypeInput".equals(filterAttributeMetadata.getFilterInputType())) {
                FilterRangeTypeInput filterxx = new FilterRangeTypeInput();
                String[] rangeValues = String.valueOf(((ArrayList) filterCandidate.getValue()).get(0)).split("_");
                if (rangeValues.length == 1 && StringUtils.isNumeric(rangeValues[0])) {
                    filterxx.setFrom(rangeValues[0]);
                    filterxx.setTo(rangeValues[0]);
                    filterInputs.addRangeTypeInput(code, filterxx);
                } else if (rangeValues.length > 1) {
                    if (StringUtils.isNumeric(rangeValues[0]) && StringUtils.isNumeric(rangeValues[1])) {
                        filterxx.setFrom(rangeValues[0]);
                        filterxx.setTo(rangeValues[1]);
                    }

                    filterInputs.addRangeTypeInput(code, filterxx);
                }
            }

        });
        QueryQuery.ProductsArgumentsDefinition searchArgs = getSearchArguments(filterInputs);
        ProductsQueryDefinition queryArgs = (productsQuery) -> {
            productsQuery.totalCount().items(generateCustomProductQuery());
        };
        return Operations.query((query) -> {
            query.products(searchArgs, queryArgs);
        }).toString();
    }

    /**
     * This method is used to create search argument quries based on the filter inputs
     *
     * @param filterInputs
     * @return search argument query
     */
    protected QueryQuery.ProductsArgumentsDefinition getSearchArguments(CustomGenericProductAttributeFilterInput filterInputs) {
        QueryQuery.ProductsArgumentsDefinition searchArgs = (productArguments) -> {
            productArguments.pageSize(limit);
            productArguments.filter(filterInputs);
            ProductAttributeSortInput sort = new ProductAttributeSortInput();
            SortEnum sortEnum = SortEnum.valueOf(sortOrder);
            boolean validSortKey = true;
            if ("relevance".equals(sortKey)) {
                sort.setRelevance(sortEnum);
            } else if ("name".equals(sortKey)) {
                sort.setName(sortEnum);
            } else if ("price".equals(sortKey)) {
                sort.setPrice(sortEnum);
            } else if ("position".equals(sortKey)) {
                sort.setPosition(sortEnum);
            } else {
                validSortKey = false;
            }

            if (validSortKey) {
                productArguments.sort(sort);
            }
        };
        return searchArgs;
    }

    /**
     * Method to execute graphql request after query construction
     *
     * @return GraphqlResponse
     */
    protected GraphqlResponse<Query, Error> executeQuery() {
        if (query == null) {
            query = generateProductsQueryString();
        }
        return client.execute(query);
    }

    protected abstract ProductInterfaceQueryDefinition generateCustomProductQuery();
}
