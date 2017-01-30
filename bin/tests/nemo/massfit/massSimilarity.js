/**
 *  Mass spectra similarity
 */
var url='http://www.chemcalc.org/cheminfo/servlet/org.chemcalc.ChemCalc?resolution=0.01&mf=';
var mfs=["C15H3ClF2N2O","C17H7Cl2F","C11H10BrFN2O2","C12H4ClF3N2O2","C13H14BrClO","C14H8Cl2F2O","C8H11BrF2N2O3","C10H15BrClFO2","C11H9Cl2F3O2","C16H10BrF","C17H4ClF3","C20N2O2","C13H11BrF2O","C17HFN2O3","C10H12BrF3O2","C19H5ClO2","C13H11Cl3N2","C16H6ClFO3","C10H12Cl3FN2O","C7H13Cl3F2N2O2","C19H2F2O2","C12H14BrClN2","C13H8Cl2F2N2","C16H3F3O3","C9H15BrClFN2O","C10H9Cl2F3N2O","C11H19BrCl2","C12H13Cl3F2","C9H14Cl3F3O","C12H10Cl2N2O3","C12H11BrF2N2","C9H12BrF3N2O","C11H15Cl3O3","C18H5ClN2O","C11H16BrClF2"];
//var mfs=["C15H3ClF2N2O","C17H7Cl2F","C11H10BrFN2O2","C12H4ClF3N2O2"];
var localURL='file:';
var firstX=250;
var lastX=350;
var nPoints=16*1024;
var  predictions = new Array();
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