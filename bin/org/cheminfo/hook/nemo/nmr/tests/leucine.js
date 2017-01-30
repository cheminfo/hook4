//var data = File.load('/org/cheminfo/hook/nemo/nmr/tests/leucine.txt');
var data = File.load('/org/cheminfo/hook/nemo/nmr/tests/spinsystem.txt');

var lines=data.split(/[\r\n]+/);
var newdata=[];
for (var i=0; i<lines.length; i++) {
  var newline="";
  var fields=lines[i].split("\t");
  if (fields.length>1) {
    newline+=fields[0]+"\t\t"+fields[1]+"\t\t\t1\t";
      
    for (var j=2; j<(fields.length-1); j=j+2) {
      newline+=fields[j]+"\td\t"+fields[j+1]+"\t";
    }
  }
  newdata.push(newline);
}


var spinus = SD.spinusParser(newdata.join("\r\n"));//SD.spinusPred1H(molfile);
jexport('spinus',spinus);
var spectraData = SD.simulateNMRSpectrum(spinus,{from:0,to:10,nbPoints:1024*64,maxClusterSize:10});
spectraData.fourierTransform();
var jcamp = spectraData.toJcamp({encode:'DIFDUP',yfactor:1,type:"SIMPLE"});
out.println(jcamp);
out.println(spectraData.getFirstX()+" "+spectraData.getLastX());
//save('Demo/Spectra/jcamp_from_molfile/s.jdx',jcamp);
/*jexport('jcamp',{type:'jcamp',value:jcamp});
jexport('molfile',{type:"mol2d", value:molfile});
jexport('spinus',spinus);
*/
//var data =spectraData.getEquallySpacedDataInt(0, 10, 1024);
//var sim = Distance.areaOverlap(data ,data);