var molecule = ACT.load("CCO");//"C1CCCCC1");
molecule.inventCoordinates()
molecule.expandHydrogens();
var diaIDs = molecule.getDiastereotopicAtomIDs();
var molfile = molecule.toMolfile();
//console.log(molfile);
//out.println(molfile);

//1H Prediction
var spinus = SD.spinusPred1H(molfile);
//out.println(JSON.stringify(spinus));
//1H simulation
var spectraData1H = SD.simulateNMRSpectrum(spinus,{from:0,to:10,nbPoints:1024*64,maxClusterSize:9,'diaIDs':diaIDs});
spectraData1H.fourierTransform();
out.println(spectraData1H.toJcamp({encode:'DIFDUP',yfactor:0.01,type:"SIMPLE"}));
//jexport('jcamp1H',{type:'jcamp',value:spectraData1H.toJcamp({encode:'DIFDUP',yfactor:0.01,type:"SIMPLE"})});
