package com.centralthai.core.models;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.core.components.models.common.CommerceIdentifier;
import com.adobe.cq.commerce.core.components.services.urls.UrlProvider;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.centralthai.core.pojo.CustomProductPojo;
import com.centralthai.core.retrivers.AbstractCustomProductRetriever;
import com.centralthai.core.retrivers.CustomProductRetriever;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = {CustomProduct.class, ComponentExporter.class},
        resourceType = CustomProductImpl.RESOURCE_TYPE)
@Exporter(
        name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
        extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class CustomProductImpl implements CustomProduct {

    protected static final String RESOURCE_TYPE = "centralthai/components/customcifproduct";

    private static final String SELECTION_PROPERTY = "searchTerm";

    @Self
    private SlingHttpServletRequest request;

    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private MagentoGraphqlClient magentoGraphqlClient;

    @ScriptVariable
    private Page currentPage;

    @OSGiService
    private UrlProvider urlProvider;


    @ValueMapValue(
            name = SELECTION_PROPERTY,
            injectionStrategy = InjectionStrategy.OPTIONAL)
    private String searchTerm;


    List<CustomProductPojo> productslist;

    private AbstractCustomProductRetriever abstractCustomProductRetriever;

    @PostConstruct
    protected void init() {
        if (magentoGraphqlClient != null) {
            abstractCustomProductRetriever = new CustomProductRetriever(magentoGraphqlClient);
            abstractCustomProductRetriever.setIdentifier(searchTerm);
            productslist = abstractCustomProductRetriever.fetchProducts().stream()
                    .map(product -> {
                        CustomProductPojo customProductPojo = new CustomProductPojo();
                        customProductPojo.setName(product.getName());
                        customProductPojo.setSku(product.getSku());
                        customProductPojo.setUrl(product.getUrlKey());
                        return customProductPojo;
                    }).collect(Collectors.toList());
        }
    }

    public List<CustomProductPojo> getProductslist() {
        return productslist;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    @Override
    public CommerceIdentifier getCommerceIdentifier() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getSku() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public List<CustomProductPojo> getProducts() {
        return null;
    }

    @Override
    public AbstractCustomProductRetriever getCustomProductRetriever() {
        return null;
    }
}
