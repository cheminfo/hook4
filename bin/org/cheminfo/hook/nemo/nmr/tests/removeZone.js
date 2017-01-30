/*var spectra = dir("/Research/NMR/AutoAssign/data/experimental1/",{filter:".jdx"});
var lng = spectra.length;
for (var i=0; i<1; i++) {
	var spectraData = SD.load("/Research/NMR/AutoAssign/data/experimental1/h1_0.jdx");//SD.load(spectra[i]);
	save(spectra[i].replace(".jdx","_2.jdx"),spectraData.toJcamp("{encode:'DIFDUP',yfactor:0.0001,type:'NTUPLES',keep:['#param1','#param2']}"));
}*/


var spectraData = SD.load("/org/cheminfo/hook/nemo/nmr/tests/1018.dx");
console.log(spectraData.getFirstX()+" "+spectraData.getLastX());
spectraData.suppressZone( -0.01,-10);
spectraData.suppressZone(5.5, 16);
console.log(spectraData.getFirstX()+" "+spectraData.getLastX());