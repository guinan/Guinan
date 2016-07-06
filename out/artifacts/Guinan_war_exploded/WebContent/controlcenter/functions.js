/**
 * 
 */

 $( document ).ready(function() { 
	 
	 function setStatus(){ 
	       
         $.ajax({ 
             type: "GET",
             url: "http://localhost:10080/Guinan/webapp/GuinanMaster/info",
             crossDomain: "true",
             dataType: "text",
             success: function(data){  
            	 $("#guinan_status").html("<pre>"+data+"</pre>");
                /*alert(data);*/
             },
        
         });
    }
	 //on document ready set the status of guinan
	 
	 setStatus();
	 
    $('#update_status_button').click(setStatus());

 });