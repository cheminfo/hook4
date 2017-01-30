

// we will create all the simulated spectra

// In the Lims we have the following username:
/*
| nmr_dft_TZVP_geom_dft_SVP@nmrdb.org  |                  911 |
| chemdraw11@nmrdb.org                 |                 2305 |
| spinus@nmrdb.org                     |                 2302 |
| NMRpredict@nmrdb.org                 |                 2305 |
| ACD_kirill_INC@nmrdb.org             |                 2418 |
| ACD_kirill_NN@nmrdb.org              |                 2418 |
| ACD_misha@nmrdb.org                  |                 2418 |
| ACD_misha_solvent_specific@nmrdb.org |                 2418 |
*/

var base='/nmrdb/';
if(!exists(base))
  mkdir(base);
// we retrieve all the spectra from maybridge (contains experimental spectra)
var source=eval("("+getUrlContent("http://mynmrdb.epfl.ch/lims/default/sample/listJson.jsp?queryUser=jo.milner@maybridge.com&key=Oxs2yQAwuY")+")");
var result=[];
var urls=[];
var size=source.entry.length;
var error=false;
//size=1;
for (var i=0; i<size; i++) {
  var aResult={};
  var entry=eval("("+getUrlContent(source.entry[i].entryDetails)+")").entry[0];
  
  error=false;
  var actelionIDmd5=null;
  try{
     actelionIDmd5=entry.mols[0].actelionIDmd5;
 
  }
  catch(err){
    out.println("error");
    error=true;
  }
  if(!error){
	  	aResult.actelionIDmd5=actelionIDmd5;
	  	aResult.url=source.entry[i].entryDetails;
	  	var sameMolecules=eval("("+getUrlContent("http://mynmrdb.epfl.ch/lims/default/sample/listJson.jsp?queryActelionIDmd5="+escape(actelionIDmd5))+")").entry;
	  	var provider=[];
	 	aResult.provider=provider;
	  
	  	var counter=0;
	  	for (var j=0; j<sameMolecules.length; j++) {
	    	if (sameMolecules[j].user.email.match(/^.*nmrdb.org/)){
	      		if(!(sameMolecules[j].user.email.indexOf("nmr_dft")>=0)){
	        		if(!(sameMolecules[j].user.email.indexOf("kirill")>=0)){
	           		     var resultPred={};
	    				resultPred.email=sameMolecules[j].user.email;
			   		 	resultPred.url=sameMolecules[j].entryDetails;
	   	 	    		provider.push(resultPred);
	        		}
	      		}
	    	}
	  	}
	
	  	if (provider.length>=7) {
	    	//Create a new folder for each molecule
	  		if(!exists(base+actelionIDmd5))
	  			mkdir(base+actelionIDmd5);
	    	save(base+actelionIDmd5+"/experimental.dx", getUrlContent(entry.nmrs[0].resourceURL));
	    	save(base+actelionIDmd5+"/mol2dcan.mol", entry.mols[0].canonizedMol);
	   		//save(base+actelionIDmd5+"/mol2d.mol", entry.mols[0].value);
	   	 	for(var j=0;j<provider.length;j++){
	    		var filename=provider[j].email.substring(0,provider[j].email.indexOf("@"))+".txt";
	      	  	var result = eval("("+getUrlContent(provider[j].url)+")");
	      	  	if(result['entry'][0]['nmrs'][0].nucleus=="1H")
	    			save(base+actelionIDmd5+"/"+filename, JSON.stringify(result['entry'][0]['nmrs'][0]));
	      	  	else
	      	  		save(base+actelionIDmd5+"/"+filename, JSON.stringify(result['entry'][0]['nmrs'][1]));
	   	 	}
	  	}
  }
  
}
