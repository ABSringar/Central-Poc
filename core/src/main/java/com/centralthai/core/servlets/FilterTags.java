package com.centralthai.core.servlets;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.graphql.client.GraphqlResponse;
import com.adobe.cq.commerce.magento.graphql.*;
import com.adobe.cq.commerce.magento.graphql.gson.Error;
import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.TagManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Create Filter Tags Servlet",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "sling.servlet.paths=/central/createFilterTags",
                "sling.servlet.selectors=abc",
                "sling.servlet.extensions=json"
        })
@ServiceDescription("Create Filter Tags Servlet")
public class FilterTags extends SlingSafeMethodsServlet {

    private transient MagentoGraphqlClient magentoGraphqlClient;
    private transient Resource resource;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {
        resource = req.getResourceResolver().getResource("/content/centralthai/us/en");
        magentoGraphqlClient = resource.adaptTo(MagentoGraphqlClient.class);
        ProductAttributeFilterInput filter = new ProductAttributeFilterInput();
        Object category = "Mg==";
        FilterEqualTypeInput identifierFilter = (new FilterEqualTypeInput()).setEq((String) category);
        filter.setCategoryUid(identifierFilter);
        QueryQuery.ProductsArgumentsDefinition searchArgs = s -> s.filter(filter);
        ProductsQueryDefinition queryArgs = productsQuery ->
                productsQuery.aggregations(aggregationQuery ->
                        aggregationQuery.label().attributeCode()
                                .options(aggregationOptionQuery ->
                                        aggregationOptionQuery.label().value()));
        String queryString = Operations.query(query -> query
                .products(searchArgs, queryArgs)).toString();
        GraphqlResponse<Query, Error> graphqlResponse = magentoGraphqlClient.execute(queryString);
        resp.setContentType("application/text");
        if (CollectionUtils.isEmpty(graphqlResponse.getErrors())) {
            Products products = graphqlResponse.getData().getProducts();
            products.getAggregations().stream()
                    .filter(attr ->
                            !attr.getAttributeCode().equals("category_id"))
                    .forEach(item -> item.getOptions().forEach(
                            data -> createFilterTags(item.getAttributeCode(), data.getLabel(), data.getValue())
                    ));

            resp.getWriter().write(String.valueOf("Created successfully"));
        } else {
            resp.getWriter().write(String.valueOf("Oops something went wrong ....."));
        }
        resource.getResourceResolver().commit();

    }

    /**
     * To create attributes tag
     *
     * @param attributeCode
     * @param label
     * @param value
     */
    private void createFilterTags(String attributeCode, String label, String value) {
        try {
            TagManager tagManager = resource.getResourceResolver().adaptTo(TagManager.class);
            String tagId = "cds:" + attributeCode + "/" + value;
            tagManager.createTag(tagId, label, "", true);
        } catch (InvalidTagFormatException e) {
            e.printStackTrace();
        }
    }
}
