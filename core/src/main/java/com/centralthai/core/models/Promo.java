package com.centralthai.core.models;

import com.centralthai.core.pojo.PromoModel;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Model(adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
        resourceType = "centralthai/components/promo")
@Exporter(name = "jackson", extensions = "json")
public class Promo {

    @Inject
    List<PromoModel> promos = new ArrayList<>();

    @ValueMapValue
    private String defaultxfPath;

    private String promoActivePath;

    List<PromoModel> activePromos = new ArrayList<>();

    @PostConstruct
    private void init() {
        activePromos = promos.stream()
                .filter(x -> x.getStartDate().before(new Date()) && x.getEndDate().after(new Date()))
                .collect(Collectors.toList());
        promoActivePath = activePromos.size() > 0 ? activePromos.get(0).getXfPath() : defaultxfPath;
    }

    public List<PromoModel> getPromos() {
        return promos;
    }

    public String getPromoActivePath() {
        return promoActivePath;
    }

    public String getDefaultxfPath() {
        return defaultxfPath;
    }
}
