


    //setInterval(autocomp, 1000);

function autocomp() {
   // console.log("autocomplete called");
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && (this.status == 200 || this.status == 500)) {
            console.log("got response");
             document.getElementById("demo").innerHTML = this.responseText;
        }
    };
    var userWords = document.getElementById("searchBox").value;
    xhttp.open("GET", "Servlet?action=autocomp&userSearch="+userWords, true);
    console.log(userWords);
    xhttp.send();
}



//?action=autocomp&userSearch="+userWords

/*
$(document).onload(function() {

    $("#searchBox").onkeyup(function (e) {

        var userWords = $("input#countryCode").val();
        dataString = "userWordsparam=" + userWords;
        $.ajax({
            type: "GET",
            url: "/auto_comp.txt",
            data: dataString,
            dataType: "json",

            //if received a response from the server
            success: function (data) {
                //our country code was correct so we have some information to display
                if (data.success) {
                    $("#demo").html("");
                    $("#demo").append("<b>Ajax:</b> " + data.toString() + "<br/>");

                }
                //display error message
                else {
                    $("#demo").html("<div><b>no data !</b></div>");
                }
            },

            //If there was no resonse from the server
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("Something really bad happened " + textStatus);

            }
        })

    });
});
*/