
var hook3Servlet='/cheminfo/servlet/org.cheminfo.hook.appli.HookServlet';
var servletURL='/cheminfo/servlet/org.dbcreator.MainServlet';
document.form.action=hook3Servlet;

var defaultXml="&lt;xmlFile&gt;"+
		"&lt;"+nemoWindow.spectrumType+"&gt;"+
		"&lt;_"+nemoWindow.spectrumType+"ID&gt;$spectrumID&lt;/_"+nemoWindow.spectrumType+"ID&gt;"+
		"&lt;"+nemoWindow.spectrumType+"Parameter action='add'&gt;"+
		"&lt;description&gt;$fieldType&lt;/description&gt;"+
		"&lt;value&gt;$xmlView&lt;/value&gt;"+
		"&lt;remarks&gt;$remarks&lt;/remarks&gt;"+
		"&lt;/"+nemoWindow.spectrumType+"Parameter&gt;"+
		"&lt;/"+nemoWindow.spectrumType+"&gt;"+
		"&lt;/xmlFile&gt;";

var nmrXml="&lt;xmlFile&gt;"+
		"&lt;nmr&gt;"+
		"&lt;_nmrID&gt;$spectrumID&lt;/_nmrID&gt;"+
		"&lt;nmrParameter action='add'&gt;"+
		"&lt;description&gt;xmlView&lt;/description&gt;"+
		"&lt;value&gt;$xmlView&lt;/value&gt;"+
		"&lt;remarks&gt;$remarks&lt;/remarks&gt;"+
		"&lt;/nmrParameter&gt;"+
		"&lt;nmrParameter action='add'&gt;"+
		"&lt;description&gt;nmrString&lt;/description&gt;"+
		"&lt;value mimeType='text/html'&gt;$nmrString&lt;/value&gt;"+
		"&lt;/nmrParameter&gt;"+
		"&lt;/nmr&gt;"+
		"&lt;/xmlFile&gt;";




function deleteView(url) {
	deleteViewGeneric(url)
}



function loadViews() {
	loadViewsGeneric(nemo, servletURL,
		{action: 'PowerSearch', format: 'hookView2010', 
			target: nemoWindow.spectrumType+'Parameter', 
			sort: '>'+  nemoWindow.spectrumType+'Parameter._dateLastModified',
			realQuery: nemoWindow.spectrumType+'Parameter.description=xmlView and '+nemoWindow.spectrumType+'Parameter._'+nemoWindow.spectrumType+'ID='+nemoWindow.spectrumID,
			random: Math.random()
		}
	);
	
	new Ajax.Request(servletURL+'?action=PowerSearch&format=hookNmrAssignment&target=nmr&realQuery=nmrLine.delta1<>0+and+nmr._entryID='+nemoWindow.entryID, 
		{	onSuccess: function(resp) {
				var assignments=document.getElementById("spectrumAssignments");
				//alert(resp.responseText);
				if (resp.responseText.length>20) {
					assignments.innerHTML="Apply an existing assignment:<p>"+resp.responseText;
				}
			}
		}
	);
}

function submitView() {
    var spectra=JSON.parse(nemo.getJSON());
    if (spectra.spectraDisplay && spectra.spectraDisplay[0] && spectra.spectraDisplay[0].spectra && spectra.spectraDisplay[0].spectra[0]) {
        var spectrum=spectra.spectraDisplay[0].spectra[0];
        if (nemoWindow.spectrumType=="nmr") {
                setNmrXmlAssignment(nemo.getXML()+'', nemo.getAssignmentString()+'');
                if (spectrum.smartPeakLabels) {
                        var xml=convertSmartPeakLabelsToXML(spectrum.smartPeakLabels);
                        submitXml(xml);
                        // add we will also save the hose code for future prediction
                        // var xml=convertHoseToXML(nemo.getHoseCodes4Assignment()+'');
                        // submitXml(xml);
                }
        }
        
        if (spectrum.peakLabels && !spectrum.smartPeakLabels) {
                var xml=convertPeakLabelsToXML(spectrum.peakLabels);
                submitXml(xml);
        }

        var xml=defaultXml;
        // we should replace &gt; ... in case we are using internet explorer
        xml=decodeXml(xml);
        xml=xml.replace(/\$fieldType/,"xmlView");
        xml=xml.replace(/\$spectrumID/,nemoWindow.spectrumID);
        xml=xml.replace(/\$xmlView/,""+encodeXml(nemo.getXML()+""));
        xml=xml.replace(/\$remarks/,""+$("viewName").value);
        submitXml(xml);
    }
}

function submitPrefView() {
	var xml=defaultXml;
	// we should replace &gt; ... in case we are using internet explorer
	xml=decodeXml(xml);
	xml=xml.replace(/\$fieldType/,"xmlPrefView");
	xml=xml.replace(/\$spectrumID/,nemoWindow.spectrumID);
	xml=xml.replace(/\$xmlView/,""+encodeXml(nemo.getXMLView()+""));
	xml=xml.replace(/\$remarks/,""+$("prefName").value);
	submitXml(xml);
}


function setMF() {
	$('mf').value=nemoWindow.mf;
}

function generateMassSpectrumFromMF(mf,resolution) {
	if (mf.length<2) {
		alert("MF not valid");
		return;
	}
	
	nemo.addJcamp('/cheminfo/servlet/org.chemcalc.ChemCalc?action=GetChemCalcDistribution&mf='+escape(mf).replace(/\+/g,"%2B")+'&resolution='+resolution);
	nemo.setColor('red');
	
	/*
	$('massButton').hide;
	new Ajax.Request('/cheminfo/servlet/org.chemcalc.ChemCalc',
	         {       method: 'post',
						parameters: {mf:mf+"", resolution: resolution},
	                 onSuccess: function(resp) {
							nemo.addJcamp(resp.responseText);
							nemo.setColor('red');
							$('massButton').show();
	                 }
	         }
	);
	*/
}
var SIMULATOR_URL="http://www.nmrdb.org/service/predictor";
var requester=null;
function initializeRequester() {
	/*Even though the XMLHttpRequest object allows us to call the open() method multiple times, 
	each object can really only be used for one call, as the onreadystatechange event doesn't 
	update again once readyState changes to "4" (in Mozilla). Therefore, we have to create a new 
	XMLHttpRequest object every time we want to make a remote call.
	 */
	/* Check for running connections */ 
	if (requester != null && requester.readyState != 0 && requester.readyState != 4) {
		requester.abort(); 
	} 
	
	try { 
		requester = new XMLHttpRequest(); 
	} catch (error) { 
		try { 
			requester = new ActiveXObject("Microsoft.XMLHTTP"); 
		} catch (error) { 
			requester = null;
			return false; 
		} 
	}
	return true; 
}
var option = 1;
var canonizeMolfile=null;
function predictSpectrum(molfile,mode) {
    //castillo
    option=mode;
    molfile = document.nemo.canonizeMolfile(molfile);
    canonizeMolfile=molfile;
	if (initializeRequester()) {
		/* Changes in the readyState variable can be monitored using a special onreadystatechange listener,
		so we'll need to set up a function to handle the event when the readyState is changed.
		*/
		parameters="molfile="+escape(molfile);
		requester.onreadystatechange = showPage;
		
		requester.open('POST', SIMULATOR_URL, true);
		requester.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		requester.setRequestHeader("Content-length", parameters.length);
		requester.setRequestHeader("Connection", "close");
		requester.send(parameters);
	} else {
		alert("Unable to initialize Ajax");
	}
}

function showPage() {
	// 0   Uninitialised
	// 1  Loading
	// 2  Loaded
	// 3  Interactive
	// 4  Completed
	if (requester.readyState == 4) {
		if (requester.status == 200) {
			// we need to convert the answer to a inline assignment (ACS type) so that we can use it directly
			var acsStyle="";
			var tabStyle="";
			var answer=requester.responseText;
			//document.form.tableformat.value=answer;

			answer=answer.replace(/\n\r/g,"\r");
			answer=answer.replace(/\n/g,"\r");
			var lines=answer.split("\r");
			// alert(answer);

			// we will create the tab-delimited prediction data
			for (var i=0; i<(lines.length-1); i++) {
				var fields=lines[i].split("\t");
				if (fields.length>3) {
					tabStyle+=fields[0]+"\t1H\t"+fields[2]+"\t\t\t1\t";
					var numberJ=fields[3];
                    if (numberJ>0) {
                            for (var j=0; j<numberJ; j++) {
                                    tabStyle+=fields[j*3+4]+"\td\t"+fields[j*3+6]+"\t";
                            }
					}
					tabStyle+="\r";
				}
			}
			//document.form.tableformat.value=tabStyle;


			//TODO we should take into account symmetrical molcules
				
			// we will suppress the lines that are identical and add in the last column the integration
			var numberIdentical=1;
			for (var i=0; i<(lines.length-1); i++) {
				
				// we should not take into account the first column ...
				var currentLine=lines[i];
				var nextLine=lines[i+1];
				
				if (currentLine.indexOf("\t")>0) currentLine=currentLine.substring(currentLine.indexOf("\t"));
				if (nextLine.indexOf("\t")>0) nextLine=nextLine.substring(nextLine.indexOf("\t"));
				
				if (currentLine==nextLine) {
					numberIdentical++;
					lines[i]="";
				} else {
					lines[i]+="\t"+numberIdentical;
					numberIdentical=1;
				}
				
				
			}
			
			
			for (var i=0; i<lines.length; i++) {
				var fields=lines[i].split("\t");
				
				if (fields.length>3) {
				
					var hID=fields[0];
					var cID=fields[1];
					var delta=fields[2];
					var numberJ=fields[3];
					var integration=fields[numberJ*3+4];
				
					if (acsStyle.length>0) acsStyle+=", ";
					
					acsStyle+=delta;
					if (numberJ>0) {
						var multiplicity="";
						var multiplet=2;
						var coupling="";

						var jCoupling=new Array();
						for (var j=0; j<numberJ; j++) {
							jCoupling[j]=fields[j*3+6];
						}

						jCoupling.sort(sortNumber);

						for (var j=0; j<numberJ; j++) {
							// what about the same coupling constant ???
							// we should check and compile the multiplicity
							if ((j<(numberJ-1)) && (jCoupling[j]==jCoupling[j+1])) {
								multiplet+=1;
							} else {
								multiplicity+=getMultiplet(multiplet);
								if (coupling.length>0) coupling+=", ";
								coupling+="J="+jCoupling[j];
								multiplet=2;
							}
						}
						acsStyle+=" ("+cID+", "+integration+"H, "+multiplicity+", "+coupling+")";
					} else {
						acsStyle+=" ("+cID+", "+integration+"H)";
					
					}
				}
			}

			//document.form.assignment.value=acsStyle;
			if(option==1){
				document.nemo.clearAll();
				document.nemo.setMolfile(canonizeMolfile);
				document.nemo.addSimulatedSpectrum(acsStyle,1);
			}
			else{
				document.nemo.setMolfile(canonizeMolfile);
				document.nemo.addSimulatedSpectrum(tabStyle,1);
			}
		}
	}
}

function sortNumber(a, b) {
	return b - a;
}

function getMultiplet(multiplet) {
	switch (multiplet) {
		case 1: 
			return "s";
		case 2: 
			return "d";
		case 3: 
			return "t";
		case 4: 
			return "q";
		case 5: 
			return "quint";
		case 6: 
			return "hex";
		case 7: 
			return "hept";
		case 8: 
			return "o";
		case 9: 
			return "n";
		default : 
			return "m";
	}

}

function startEditor() {
	window.open('/tools/jme/jme_window.html','JME','width=500,height=450,scrollbars=no,resizable=yes');
}

function fromEditor(mol, smiles) {

  if (smiles=="") {
    alert ("no molecule submitted");
    return;
  }
  document.form.molfile.value=mol;
  document.nemo.clearAll(); 
  var molcan = document.nemo.setMolfile(document.form.molfile.value);
  getPrediction(molcan,1);
}



function setNmrXmlAssignment(xmlView, nmrString) {
	var xml=nmrXml;
	// we should replace &gt; ... in case we are using internet explorer
	xml=decodeXml(xml);
	xml=xml.replace(/\$spectrumID/,nemoWindow.spectrumID);
	xml=xml.replace(/\$xmlView/,""+encodeXml(xmlView+""));
	xml=xml.replace(/\$nmrString/,encodeXml(nmrString+""));
	xml=xml.replace(/\$remarks/,""+$("viewName").value);
	submitXml(xml, true);
}

function convertHoseToXML(tabValue) {
	var xml="<xmlFile>";
	xml+="<nmr>";
	xml+="<_nmrID>"+nemoWindow.spectrumID+"</_nmrID>";
	
	var lines=tabValue.split(/[\r\n]+/);
	for (var i=0; i<lines.length; i++) {
		var fields=lines[i].split(/\t/);
		if (fields.length>6) {
			for (var j=2; j<fields.length; j++) {
				xml+="<nmrHOSE>";
				xml+="<delta>"+fields[1]+"</delta>";
				xml+="<code>"+fields[j]+"</code>";
				xml+="<version>1</version>";
				xml+="</nmrHOSE>";
			}
		}
	}
	xml+="</nmr>";
	xml+="</xmlFile>";
	return xml;
}


function convertSmartPeakLabelsToXML(labels) {
    // removed username / password, should be imported with the cheminfo.config specified user
    var xml="<xmlFile>";
    xml+="<nmr>";
    xml+="<_nmrID>"+nemoWindow.spectrumID+"</_nmrID>";
    for (var i=0; i<labels.length; i++) {
            var label=labels[i];
            xml+="<nmrLine>";
            var assignment=label.atomID;
            if (label.pubAssignment) assignment=label.pubAssignent;
            xml+="<assignment>"+assignment+"</assignment>";
            xml+="<nucleus>"+label.nucleus+"</nucleus>";
            xml+="<delta1>"+label.delta1+"</delta1>";
            if (label.delta2) {
                    xml+="<delta2>"+label.delta2+"</delta2>";
            }
            var integration=Math.round(label.integration*1000)/1000;
            if (label.pubIntegration && ! isNaN(label.pubIntegration)) integration=label.pubIntegration*1;
            if (isNaN(integration)) integration=0;
            xml+="<intensity>"+integration+"</intensity>";

            var multiplicity="";
            if (label.couplings && label.couplings.length>0) {
                    for (var j=0; j<label.couplings.length; j++) {
                            var coupling=label.couplings[j];
                            xml+="<nmrJ><multiplicity>"+fields[j]+"</multiplicity><coupling>"+coupling.value+"</coupling></nmrJ>";
                            multiplicity+=coupling.multiplicity;
                    }
            }
            xml+="<multiplicity>"+label.multiplicity+"</multiplicity>";
            xml+="</nmrLine>";
    }
    xml+="</nmr>";
    xml+="</xmlFile>";
    return xml;
}

function convertPeakLabelsToXML(labels) {
	var type=nemoWindow.spectrumType;
    // removed username / password, should be imported with the cheminfo.config specified user
    var xml="<xmlFile>";
    xml+="<"+type+">";
    xml+="<_"+type+"ID>"+nemoWindow.spectrumID+"</_"+type+"ID>";
    for (var i=0; i<labels.length; i++) {
            var label=labels[i];
            xml+="<"+type+"Line>";
            xml+="<assignment>"+label.comment+"</assignment>";
            xml+="<value>"+label.xPosition+"</value>";
            xml+="<percent>"+label.intensity+"</percent>";
            xml+="</"+type+"Line>";
    }
    xml+="</"+type+">";
    xml+="</xmlFile>";
    return xml;
}

function submitXml(xmlFile, useCurrentUser) {
	// alert(xmlFile);
		new Ajax.Request('/cheminfo/servlet/org.dbcreator.MainServlet', 
			{	method: 'post',  
				parameters: {action: 'GenerateDBDataFromXmlZipAction', 
							useCurrentUser: true,
				 			xmlArea: xmlFile},
				onSuccess: function(resp) {
	//alert(resp.responseText);
					 loadViews();
				}		 			
			}
		);
	}

function encodeXml(value) {
	var newValue=value.replace(/&/g,"\&amp;");
	newValue=newValue.replace(/>/g,"&gt;");
	newValue=newValue.replace(/</g,"&lt;");
	return newValue;
}

function decodeXml(value) {
	var newValue=value.replace(/&gt;/g,">");
	newValue=newValue.replace(/&lt;/g,"<");
	newValue=newValue.replace(/&amp;/g,"&");
	return newValue;
}

