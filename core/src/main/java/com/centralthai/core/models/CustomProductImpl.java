package com.centralthai.core.models;

import com.adobe.cq.commerce.core.components.client.MagentoGraphqlClient;
import com.adobe.cq.commerce.core.components.models.common.CommerceIdentifier;
import com.adobe.cq.commerce.core.components.services.urls.UrlProvider;
import com.adobe.cq.commerce.core.search.models.FilterAttributeMetadata;
import com.adobe.cq.commerce.core.search.services.SearchFilterService;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.centralthai.core.pojo.CustomProductPojo;
import com.centralthai.core.retrivers.AbstractCustomProductRetriever;
import com.centralthai.core.retrivers.CustomProductRetriever;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.shopify.graphql.support.SchemaViolationError;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.*;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = {CustomProduct.class, ComponentExporter.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        resourceType = CustomProductImpl.RESOURCE_TYPE)
@Exporter(
        name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
        extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class CustomProductImpl implements CustomProduct {

    protected static final String RESOURCE_TYPE = "centralthai/components/customcifproduct";

    private static final String FASHION_STYLE = "fashion_style";

    @Self
    private SlingHttpServletRequest request;

    @Self(injectionStrategy = InjectionStrategy.OPTIONAL)
    private MagentoGraphqlClient magentoGraphqlClient;

    @ScriptVariable
    private Page currentPage;

    @OSGiService
    private UrlProvider urlProvider;

    @OSGiService
    private SearchFilterService searchFilterService;

    @SlingObject
    private ResourceResolver resourceResolver;

    @ValueMapValue
    private String[] categoryId;

    @ValueMapValue
    private String[] filterTags;

    @ValueMapValue
    private int noOfProducts;

    @ValueMapValue
    private String sortOrder;

    @ValueMapValue
    private String sortAttribute;

    @ValueMapValue
    private String filterType;

    @ValueMapValue
    private String[] productSku;

    List<FilterAttributeMetadata> availableFilters;

    Map<String, Object> identifiers = new HashMap<>();

    List<CustomProductPojo> productslist;

    private AbstractCustomProductRetriever abstractCustomProductRetriever;

    @PostConstruct
    protected void init() {
        if (magentoGraphqlClient != null) {
            availableFilters = this.searchFilterService.retrieveCurrentlyAvailableCommerceFilters(currentPage);
            abstractCustomProductRetriever = new CustomProductRetriever(magentoGraphqlClient);
            if (filterType.equals("sku")) {
                if (Optional.ofNullable(productSku).isPresent()) {
                    identifiers.put("sku", Arrays.stream(productSku).collect(Collectors.toList()));
                }
            } else {
                getFilterArguments();
                if (Optional.ofNullable(categoryId).isPresent()) {
                    identifiers.put("category_uid", Arrays.stream(categoryId).collect(Collectors.toList()));
                }
            }
            getFilteredProducts();

        }
    }

    /**
     *
     */
    private void getFilterArguments() {
        if (filterTags != null) {
            for (String tagId : filterTags) {
                Tag tag = adaptResourceToTag(tagId);
                String attributeName = tag.getParent().getName();
                String attributeValue = tag.getName();
                List<String> values = Optional.ofNullable(identifiers.get(attributeName)).isPresent()
                        ? (List<String>) identifiers.get(attributeName) : new ArrayList<>();
                values.add(attributeValue);
                identifiers.put(attributeName, values);
            }
        }
    }

    /**
     * @param tagId
     * @return
     */
    public Tag adaptResourceToTag(String tagId) {
        Tag tag = null;
        if (Optional.ofNullable(tagId).isPresent()) {
            TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
            if (Optional.ofNullable(tagManager).isPresent()) {
                tag = tagManager.resolve(tagId);
            }
        }
        return tag;
    }

    /**
     *
     */
    private void getFilteredProducts() {
        if (identifiers.size() > 0) {
            abstractCustomProductRetriever.setIdentifier(identifiers, sortAttribute, sortOrder, availableFilters, noOfProducts);
            abstractCustomProductRetriever.extendProductQueryWith(p -> p.createdAt()
                    .addCustomSimpleField(FASHION_STYLE));
            productslist = abstractCustomProductRetriever.fetchProducts().stream()
                    .map(product -> {
                        CustomProductPojo customProductPojo = new CustomProductPojo();
                        customProductPojo.setName(product.getName());
                        customProductPojo.setSku(product.getSku());
                        customProductPojo.setUrl("/content/centralthai/us/en/products/product-page.html/" + product.getUrlKey() + ".html");
                        customProductPojo.setImage(product.getImage().getUrl());
                        customProductPojo.setPrice(product.getPriceRange().getMinimumPrice().getFinalPrice().getValue().toString());
                        try {
                            if (!product.get(FASHION_STYLE).toString().equals("null"))
                                customProductPojo.setStyle(product.getAsString(FASHION_STYLE));
                        } catch (SchemaViolationError schemaViolationError) {
                            schemaViolationError.printStackTrace();
                        }
                        return customProductPojo;
                    }).collect(Collectors.toList());

        }
    }

    public List<CustomProductPojo> getProductslist() {
        return productslist;
    }


    public String[] getCategoryId() {
        return categoryId;
    }

    public String[] getFilterTags() {
        return filterTags;
    }

    @Override
    public List<CustomProductPojo> getProducts() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public AbstractCustomProductRetriever getCustomProductRetriever() {
        return null;
    }
}
