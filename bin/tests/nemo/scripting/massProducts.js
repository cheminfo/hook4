/**
 *  USB Project
 */

out.println("Starting test script");

//
var dir ="/home/karina/workspace/hook3/bin/tests/nemo/scripting/";
var openFileMf = dir+"mf.txt";
var writeFile =  dir+"report.txt";
var url="http://www.chemcalc.org/cheminfo/servlet/org.chemcalc.ChemCalc?resolution=0.01&mf=";

//
var mfs= defaultAPI.getMfs(openFileMf);
var internalProduct=defaultAPI.getCodProduct(openFileMf);

//Selected file ::: workspace/hook3/bin/tests/nemo/scripting/files/PLATING_ERREUR_INTERIM_TEST_EPFL.fpt
out.println("Samples File");
var idSpectrum=defaultAPI.makeFileJCamp();
var predictions = new Array();
var globalShift = new Array();
var realFit = new Array();
var peakWidth = new Array();
var similarity = new Array();
var IDbest = new Array();

//obtained MF of chemcalc
for (var j=0;j<mfs.length;j=j+1) {
	predictions[j]= loadJCamp(url+mfs[j]);
}

//
for(var i=0;i<idSpectrum.length;i++){
	var experimental=loadJCamp("file:"+dir+"files/"+idSpectrum[i]+".jdx");
	//apply fit between experimental and MF
	var result=massFitting(experimental,predictions);
	//
	var globalShift[i]=result.getDouble("globalShift");
	var realFit[i]=result.getDouble("R");
	var peakWidth[i]=result.getDouble("gaussParam");
	var similarity[i]=result.getDouble("similarity");
	var IDbest[i]=result.getDouble("IDbest");
}

//
defaultAPI.createReport(writeFile,internalProduct,mfs,idSpectrum,globalShift,realFit,peakWidth,similarity,IDbest);







