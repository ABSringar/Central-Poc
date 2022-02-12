package com.centralthai.core.models;

import com.adobe.cq.commerce.magento.graphql.FilterEqualTypeInput;
import com.adobe.cq.commerce.magento.graphql.FilterMatchTypeInput;
import com.adobe.cq.commerce.magento.graphql.FilterRangeTypeInput;
import com.adobe.cq.commerce.magento.graphql.ProductAttributeFilterInput;
import com.shopify.graphql.support.Input;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CustomGenericProductAttributeFilterInput extends ProductAttributeFilterInput {
    private Map<String, Input<FilterEqualTypeInput>> equalInputs = new HashMap();
    private Map<String, Input<FilterMatchTypeInput>> matchInputs = new HashMap();
    private Map<String, Input<FilterRangeTypeInput>> rangeInputs = new HashMap();

    public CustomGenericProductAttributeFilterInput() {
    }

    public void addEqualTypeInput(String key, FilterEqualTypeInput input) {
        this.equalInputs.put(key, Input.optional(input));
    }

    public void addMatchTypeInput(String key, FilterMatchTypeInput input) {
        this.matchInputs.put(key, Input.optional(input));
    }

    public void addRangeTypeInput(String key, FilterRangeTypeInput input) {
        this.rangeInputs.put(key, Input.optional(input));
    }

    public void appendTo(StringBuilder _queryBuilder) {
        String separator = "";
        _queryBuilder.append('{');
        Iterator var3 = this.equalInputs.entrySet().iterator();

        while(true) {
            Map.Entry input;
            while(var3.hasNext()) {
                input = (Map.Entry)var3.next();
                _queryBuilder.append(separator);
                separator = ",";
                _queryBuilder.append((String)input.getKey() + ":");
                if (input.getValue() != null && ((Input)input.getValue()).getValue() != null) {
                    ((FilterEqualTypeInput)((Input)input.getValue()).getValue()).appendTo(_queryBuilder);
                } else {
                    _queryBuilder.append("null");
                }
            }

            var3 = this.rangeInputs.entrySet().iterator();

            while(true) {
                while(var3.hasNext()) {
                    input = (Map.Entry)var3.next();
                    _queryBuilder.append(separator);
                    separator = ",";
                    _queryBuilder.append((String)input.getKey() + ":");
                    if (input.getValue() != null && ((Input)input.getValue()).getValue() != null) {
                        ((FilterRangeTypeInput)((Input)input.getValue()).getValue()).appendTo(_queryBuilder);
                    } else {
                        _queryBuilder.append("null");
                    }
                }

                var3 = this.matchInputs.entrySet().iterator();

                while(true) {
                    while(var3.hasNext()) {
                        input = (Map.Entry)var3.next();
                        _queryBuilder.append(separator);
                        separator = ",";
                        _queryBuilder.append((String)input.getKey() + ":");
                        if (input.getValue() != null && ((Input)input.getValue()).getValue() != null) {
                            ((FilterMatchTypeInput)((Input)input.getValue()).getValue()).appendTo(_queryBuilder);
                        } else {
                            _queryBuilder.append("null");
                        }
                    }

                    _queryBuilder.append('}');
                    return;
                }
            }
        }
    }
}
