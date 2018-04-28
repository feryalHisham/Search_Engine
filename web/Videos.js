
function searchAgain() {
    console.log("ana honaaaa");
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && (this.status == 200 || this.status == 500)) {
            console.log("got response");
            document.getElementById("searchResults").innerHTML = this.responseText;

        }
    };
    var userWords = document.getElementById("search").value;
    var vidoeClicked =document.getElementById('videoChkeckBox').checked;
    xhttp.open("GET", "Servlet?action=search&userSearch="+userWords+"&isvideos="+vidoeClicked, true);

    xhttp.send();


}

function searchAgain2() {
    console.log("ana honaaaa");
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && (this.status == 200 || this.status == 500)) {
            console.log("got response");
            document.getElementById("searchResults").innerHTML = this.responseText;

        }
    };
    var userWords = document.getElementById("search").value;
    var vidoeClicked =document.getElementById('videoChkeckBox').checked;
    xhttp.open("GET", "Servlet?action=search2&userSearch="+userWords+"&isvideos="+vidoeClicked, true);

    xhttp.send();


}

function searchAgain3() {
    console.log("ana honaaaa");
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && (this.status == 200 || this.status == 500)) {
            console.log("got response");
            document.getElementById("searchResults").innerHTML = this.responseText;

        }
    };
    var userWords = document.getElementById("search").value;
    var vidoeClicked =document.getElementById('videoChkeckBox').checked;
    xhttp.open("GET", "Servlet?action=search3&userSearch="+userWords+"&isvideos="+vidoeClicked, true);

    xhttp.send();


}