out.println("Starting test script");

about();

// out.println(getUrlContent("http://www.chemcalc.org/cheminfo/servlet/org.chemcalc.ChemCalc?resolution=0.01&mf=Et3N"));
var mfs=[];
var urlContent=getUrlContent("http://www.chemcalc.org/cheminfo/servlet/org.chemcalc.ChemCalcService?monoisotopicMass=200&mfRange=C0-30H0-60N0-5O0-10F0-3Cl0-3&action=em2mf&massRange=0.5");
out.println(urlContent);
var results=eval("("+urlContent+")").results;
for (var i=0; i<results.length && i<4; i++) {
	out.println(results[i].mf);
	mfs[i]=results[i].mf
}

var url='http://www.chemcalc.org/cheminfo/servlet/org.chemcalc.ChemCalc?resolution=0.01&mf=';
//var mfs=["C15H3ClF2N2O","C17H7Cl2F","C11H10BrFN2O2","C12H4ClF3N2O2"];
var localURL='file:';
var firstX=250;
var lastX=350;
var nPoints=16*1024;
var predictions = new Array();
var spectraArray = new Array();
var i=0;
for (i=0;i<mfs.length;i=i+1) {
	predictions[i]= loadJCamp(url+mfs[i]);
	//predictions[i]= loadJCamp(localURL+mfs[i]+".jdx");
	predictions[i]=gaussian(predictions[i],nPoints,0.1);
	//Getting the array representation for each spectrum
	spectraArray[i]=predictions[i].getEquallySpacedDataInt(firstX,lastX,nPoints);
 
}
//Calculating the similarity between each pair of spectra
var similarity = new Array();
for (i=0;i<mfs.length;i=i+1) {
	for (j=i;j<mfs.length;j=j+1) {
		similarity[i*mfs.length+j]=crossCorrelation(spectraArray[i],spectraArray[j],1);
		similarity[i+j*mfs.length]=similarity[i*mfs.length+j];
	}
}
//Printing the similarity matrix
for (i=0;i<mfs.length;i=i+1) {
	var line="";
	for (j=0;j<mfs.length;j=j+1) {
		line=line+similarity[i*mfs.length+j]+" ";
	}
	out.println(line);
}

