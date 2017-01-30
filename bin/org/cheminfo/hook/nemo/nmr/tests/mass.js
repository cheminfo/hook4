//var jcamp=ChemCalc.getJcamp("C10H12",{gaussianWidth:10});
var jcamp = load("/org/cheminfo/hook/nemo/nmr/tests/mass3.jdx");
//var jcamp = load("/org/cheminfo/hook/nemo/nmr/tests/ff.jdx");
//jexport("jcamp",jcamp);
//save("/Demo/ChemCalc/SpectraData/spectrum.jdx",jcamp);


var sd=SD.load(jcamp);

var minX=sd.getFirstX()+0.5;
var maxX=sd.getLastX()-0.5;
jexport('minMax',minX+","+maxX+","+sd.getNbPoints());

//jexport("jcamp",{type: "jcamp", value:jcamp});

if (true) {
  var result=sd.getVector(minX, maxX, 128);
  //var result=sd.getVector(133.070, 133.12, 128);
  //var result=sd.getEquallySpacedDataInt(133.070, 133.12, 128);
  //jexport("result",result);
}