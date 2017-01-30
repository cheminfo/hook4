/**
 *  Mass Fitting
 */
var url='http://www.chemcalc.org/cheminfo/servlet/org.chemcalc.ChemCalc?resolution=0.01&mf=';
var mfs=["Al22O16Me35"];
var localURL='file:';
var  predictions = new Array();
var i=0;
for (i=0;i<mfs.length;i=i+1) {
	predictions[i]= loadJCamp(url+mfs[i]);
	//predictions[i]= loadJCamp(localURL+mfs[i]+".jdx");
}
var spectraData=loadJCamp("file:Al22O16Me35-exp.jdx");
var result=massFitting(spectraData,predictions);
out.println("Global shift: "+result.get(0).get("globalShift"));
out.println("Gauss Parameter: "+result.get(0).get("gaussParam"));