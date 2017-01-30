//console.log(Global);
//var data = File.loadJSON('/org/cheminfo/hook/nemo/nmr/tests/dataAutoAssignCholesterol.txt');
var data = File.loadJSON('/org/cheminfo/hook/nemo/nmr/tests/dataAutoAssignEthyl.txt');
//var data = File.loadJSON('/org/cheminfo/hook/nemo/nmr/tests/dataAutoAssignX.txt');
//var result = SD.autoAssignment(data.diaIDs, data.signals, data.infoCOSY, data.cosy, data.infoHMBC, data.hmbc,false,0.9,200,1); 
//var result = SD.autoAssignment2(data.diaIDs, data.signals, data.infoCOSY, data.cosy, data.infoHMBC, data.hmbc, 0.9, 200, 0, 0, 100); 
var result = SD.autoAssignment(data.diaIDs, data.signals, null, null, null, null, 1, 200, 0, 0, 0); 

//jexport('result',result);
jexport('length',result);