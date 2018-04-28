function did_you_mean()
{
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && (this.status == 200 || this.status == 500)) {
            console.log("got response did you mean");
            document.getElementById("searchResults").innerHTML = this.responseText;


        }
    };

    if(document.getElementById("did_you_mean")!=null)
    {
        var did_you_mean = document.getElementById("did_you_mean").innerText;
        console.log("-----------"+did_you_mean);
        var vidoeClicked =document.getElementById('videoChkeckBox').checked;
        xhttp.open("GET", "Servlet?action=did_you_mean&meanSearch="+did_you_mean+"&isvideos="+vidoeClicked, true);

        xhttp.send();
    }
}