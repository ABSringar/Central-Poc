(function (document, $) {

"use strict";


$(document).on("dialog-ready", function () {
    $('.product_class').find('select').append('<option value="Value">Shirt</option>');
    $('.product_class').find('ul').append('<li class="coral-SelectList-item coral-SelectList-item--option is-highlighted" data-value="value" aria-selected="false" role="option" id="coral-2" tabindex="0"><img src="/content/dam/centralthai/asset.jpg" width="30px" height="30px">Shirt</li>')
});
})(document, Granite.$);