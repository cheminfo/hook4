/*var spectra = dir("/Research/NMR/AutoAssign/data/experimental1/",{filter:".jdx"});
var lng = spectra.length;
for (var i=0; i<1; i++) {
	var spectraData = SD.load("/Research/NMR/AutoAssign/data/experimental1/h1_0.jdx");//SD.load(spectra[i]);
	save(spectra[i].replace(".jdx","_2.jdx"),spectraData.toJcamp("{encode:'DIFDUP',yfactor:0.0001,type:'NTUPLES',keep:['#param1','#param2']}"));
}*/


var spectraData = SD.load("/Research/NMR/AutoAssign/data/experimental1/h1_0.jdx");
spectraData.putParam('$param1',1927);
spectraData.putParam('$param2','castillo');
save("/Research/NMR/AutoAssign/data/experimental1/xxx.jdx",spectraData.toJcamp("{encode:'DIFDUP',yfactor:0.0001,type:'SIMPLE',keep:['$param1','$param2']}"));
var spectraData2 = SD.load("/Research/NMR/AutoAssign/data/experimental1/xxx.jdx");
jexport('#param1',spectraData2.getParamDouble('$param1',0));
jexport('#param2',spectraData2.getParamString('$param2',""));