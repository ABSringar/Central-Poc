package com.centralthai.core.models;

import com.adobe.cq.commerce.core.components.models.common.CommerceIdentifier;
import com.adobe.cq.wcm.core.components.models.Component;
import com.centralthai.core.pojo.CustomProductPojo;
import com.centralthai.core.retrivers.AbstractCustomProductRetriever;

import java.util.List;

public interface CustomProduct extends Component {

    CommerceIdentifier getCommerceIdentifier();

    String getName();

    String getSku();

    String getUrl();

    List<CustomProductPojo> getProducts();

    AbstractCustomProductRetriever getCustomProductRetriever();

}
