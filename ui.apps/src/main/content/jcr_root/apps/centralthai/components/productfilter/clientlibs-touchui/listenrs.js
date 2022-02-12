(function(document, $) {

    "use strict";


    $(document).on("dialog-ready", function() {
        $(".update_filters").click(function(){
            updateFilterAttributes();
        });
    });

    function updateFilterAttributes() {
        $.ajax({
            url: "/central/createFilterTags.abc.json",
            method: "GET",
            success: function(result) {
                $(window).adaptTo("foundation-ui").alert("Success", "Successfully created Filter tags");
            }
        });
    }
})(document, Granite.$);