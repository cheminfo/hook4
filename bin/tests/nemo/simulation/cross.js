	var  arraySpectraData1 = new Array();
	var  arraySpectraData2 = new Array();
	
	var spectraData1=getSpectraData("spectraData1");
	var nPoints1= spectraData1.getNbPoints();
	var firstX1=spectraData1.getFirstX();
	var lastX1 =spectraData1.getLastX();
	
	spectraData1.setDataClass(2); 
	gaussian(spectraData1,1000,1);
	
	var i=0;
	for (i=0;i<(nPoints1);i=i+1) {
		arraySpectraData1[i]=spectraData1.getEquallySpacedDataInt(firstX1,lastX1,nPoints1);
	}
	
	var spectraData2=getSpectraData("spectraData2");
	var nPoints2= spectraData2.getNbPoints();
	var firstX2=spectraData2.getFirstX();
	var lastX2=spectraData2.getLastX();

	spectraData2.setDataClass(2); 
	gaussian(spectraData2,1000,1);
	
	var i=0;
	for (i=0;i<(nPoints2);i=i+1) {
		arraySpectraData2[i]=spectraData2.getEquallySpacedDataInt(firstX2,lastX2,nPoints2);
	}
	
	//Calculating the similarity between each pair of spectra
	var similarity = new Array();
	for (i=0;i<(nPoints2);i=i+1) {
		similarity[i]=crossCorrelation(arraySpectraData1[i],arraySpectraData2[i],nPoints2);
		out.println(similarity[i]);
	}
	
	//Printing the similarity matrix
	var line="";
	var y1[];
	var y2[];
	for (i=0;i<(nPoints2);i=i+1) {
		line=line+similarity[i]+" ";
		y1[i] = spectraData1.getY(i);
		y2[i] = spectraData2.getY(i);
	}
	defineSpectraData(spectraData1, similarity, y1);
	defineSpectraData(spectraData2, similarity, y2);
	out.println(line);
	out.println("nPoints1: "+nPoints1);
