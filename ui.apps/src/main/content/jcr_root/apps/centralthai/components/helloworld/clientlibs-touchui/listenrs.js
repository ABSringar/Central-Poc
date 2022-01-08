(function (document, $) {

"use strict";


$(document).on("dialog-ready", function () {
alert("ASDas");
            $.ajax("/central/getProducts.json").done(handler);
    function handler() {
        alert("ASDas");
    }
});
})(document, Granite.$);