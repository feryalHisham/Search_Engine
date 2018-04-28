// <div class="col-12 col-lg-9">
//                     <!-- Single Blog Area  -->
//                     <div class="single-blog-area blog-style-2 mb-50">
//                         <!-- Blog Content -->
//                         <div class="single-blog-content">
//                             <div class="line"></div>
//                             <h4><a href="#" class="post-headline mb-0">Party people in the house</a></h4>
//                             <p>Curabitur venenatis efficitur lorem sed tempor. Integer aliquet tempor cursus. Nullam vestibulum convallis risus vel condimentum. Nullam auctor lorem in libero luctus, vel volutpat quam tincidunt. Nullam vestibulum convallis risus vel condimentum. Nullam auctor lorem in libero.</p>
//                              </div>
//                     </div>
//                 </div>


function searchNow(){

	var SnippetText="el3b el3b lw mkontsh enta tdl3ne men hydl3ne";
	var SnippetLink="www.ruby.com",SnippetLinkText="enta tdl3ne";
	var searchResultsList= document.getElementById("searchResults");



	var snippetParagraph= document.createElement("p");
    var nodeparg = document.createTextNode(SnippetText);
    snippetParagraph.appendChild(nodeparg);
	var searchResLink=document.createElement("a");
	var linktoRes=document.createTextNode(SnippetLinkText);
	searchResLink.appendChild(linktoRes);
	searchResLink.setAttribute("href",SnippetLink);
	searchResLink.setAttribute("class","post-headline mb-0");

	var searchResheader=document.createElement("h4");
	searchResheader.appendChild(searchResLink);

	var lineDiv=document.createElement("div");
	lineDiv.setAttribute("class","line");

    var singleResContent=document.createElement("div");
    singleResContent.setAttribute("class","single-blog-content");
    singleResContent.appendChild(lineDiv);
    singleResContent.appendChild(searchResheader);
    singleResContent.appendChild(snippetParagraph);


    var WholeResDiv= document.createElement("div");
    WholeResDiv.setAttribute("class","single-blog-area blog-style-2 mb-50");
    WholeResDiv.appendChild(singleResContent);

    var outerDiv=document.createElement("div");
    outerDiv.setAttribute("class","col-12 col-lg-9");
    outerDiv.appendChild(WholeResDiv);

    searchResultsList.appendChild(outerDiv);






}