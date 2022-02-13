(function(document, $) {
    "use strict";
    var productSku = "Product Sku";
    $(document).on("dialog-ready", function() {
        var selectedFilterType = $('.filter-type').find('.coral-Select-button-text').text();
        get(selectedFilterType);
        $(".update_filters").click(function() {
            updateFilterAttributes();
        });
        $(".filter-type").on("selected", function() {
            selectedFilterType = $(this).find('.coral-Select-button-text').text();
            get(selectedFilterType);
        });
    });

    function get(selectedFilterType) {
        if (selectedFilterType === productSku) {
            $('.filter-tag-picker').closest('.coral-Form-fieldwrapper').hide();
            $('category-field').closest('.coral-Form-fieldwrapper').hide();
            $('product-field').closest('.coral-Form-fieldwrapper').show();
        } else {
            $('.filter-tag-picker').closest('.coral-Form-fieldwrapper').show();
            $('category-field').closest('.coral-Form-fieldwrapper').show();
            $('product-field').closest('.coral-Form-fieldwrapper').hide();
        }
    }

    function updateFilterAttributes() {
        $.ajax({
            url: "/central/createFilterTags.filtertags.json",
            method: "GET",
            success: function(result) {
                $(window).adaptTo("foundation-ui").alert("Success", result);
            },
            error: function(result) {
                $(window).adaptTo("foundation-ui").alert("Error", "Oops something went wrong!!!");
            }
        });
    }
})(document, Granite.$);