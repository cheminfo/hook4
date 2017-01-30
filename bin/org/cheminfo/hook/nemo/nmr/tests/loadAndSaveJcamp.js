/*var spectraData = SD.load('/org/cheminfo/hook/nemo/nmr/tests/t2-xy.jdx');
var jcamp = spectraData.toJcamp({encode:'PAC',yfactor:1,type:"SIMPLE"});
save('/org/cheminfo/hook/nemo/nmr/tests/t2-xy-hook.jdx',jcamp);
*/
//console.log(Global);
/*
var spectraData2 = SD.load("/org/cheminfo/hook/nemo/nmr/tests/10.jdx");
//var spectraData2 = SD.load("/org/cheminfo/hook/nemo/nmr/tests/ethylbenzene.jdx");
var maxI=10;
var fn=[];

for(var i=0;i<maxI;i++)
  fn[i]=1/maxI;

spectraData2.correlationFilter(fn);
var signals1H  = spectraData2.nmrPeakDetection({nStddev:5,baselineRejoin:10,compute:true});
//console.log(signals1H);
var acs = SD.signals2Acs(signals1H,{solvent:"CDCl5",rangeForMultiplet:false});
//console.log(acs);
//console.log(signals1H);
var vector = spectraData2.getVector(-10,1,1024);
//console.log(vector);
var jcamp = spectraData2.toJcamp({encode:'FIX',yfactor:1,type:"SIMPLE"});

//console.log(Global);
File.save('/org/cheminfo/hook/nemo/nmr/tests/ethylbenzene-hook.jdx',jcamp);
*/

var cosy = SD.load("/org/cheminfo/hook/nemo/nmr/tests/cosy_28.jdx");
File.save('/org/cheminfo/hook/nemo/nmr/tests/zcosy_28.jdx',
		cosy.toJcamp({encode:'DIFDUP',yfactor:0.0,type:"NTUPLES",xUnits:"PPM",yUnits:"HZ"}));
console.log("OK");