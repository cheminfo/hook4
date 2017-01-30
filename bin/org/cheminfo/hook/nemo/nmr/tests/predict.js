var form=form || {"smiles":"[H]c1nc([C@@]([H])([H])[C@@]([H])([H])N([H])[H])c([H])n1[H]"};//"C=CCCCC(C=C)OC(C)=O"};//"CCCCC(C)=O"}//"C=CCCCC(C=C)OC(C)=O"};//"CCOC(=O)C.CC"};
//var form=form || {"smiles":"C=CCCCC(C=C)OC(C)=O"};//"CCCCC(C)=O"}//"C=CCCCC(C=C)OC(C)=O"};//"CCOC(=O)C.CC"};

  var result={};
  
  var molfile;
  var actMol=ACT.load(form.smiles);
  actMol.inventCoordinates();
  actMol.expandHydrogens();
  molfile=actMol.toMolfile();
  
  var frequency=form.frequency||400000000;
  var lineWidth=form.lineWidth||1;
 
  var diaIDs = actMol.getDiastereotopicAtomIDs();
 
  var spinus=SD.spinusPred1H(molfile);
  console.log(spinus);
  
  var d = new Date();
  var n = d.getTime();
  var spectraData=SD.simulateNMRSpectrum(spinus,
                                         {lineWidth:lineWidth,
                                          frequency: frequency,
                                          from:0,
                                          to:10,
                                          nbPoints:1024*32,
                                          maxClusterSize:9,
                                          diaIDs:diaIDs,
                                          method:4}
                                      );
  

  spectraData.fourierTransform();
  d = new Date();
  console.log( "X "+(d.getTime()-n));
  //spectraData.addNoise(1000);

  spectraData.putParam("$SOLVENT", "CDCl3")
  /*jexport("molfile",newMolfile);
  jexport("spinus",spinus);
  jexport("annotations",addAnnotations(spinus));
  jexport("jcamp",{type:"jcamp", value:spectraData.toJcamp({encode:'DIFDUP',type:"SIMPLE",keep:["$SOLVENT"]})});*/

File.save('/org/cheminfo/hook/nemo/nmr/tests/simFromSmile1.jdx',spectraData.toJcamp({encode:'DIFDUP',type:"NTUPLES",keep:["$SOLVENT"]}));




/*
//var name = '/org/cheminfo/hook/nemo/nmr/tests/output.mol';
var name = '/org/cheminfo/hook/nemo/nmr/tests/mol_10.mol';
//var name = '/org/cheminfo/hook/nemo/nmr/tests/mol_166-AZ221.mol';

var molecule=ACT.load(File.load(name));
molecule.expandHydrogens();
var diaIDs=molecule.getDiastereotopicAtomIDs();

var molfile = File.load(name);
var pred1H = SD.spinusPred1H(molfile,{diaIDs:diaIDs});
jexport('size1H',pred1H);
//var pred13C = SD.nmrShiftDBPred13C(molfile);
//jexport('size13c',pred13C);
//File.save('/org/cheminfo/hook/nemo/nmr/tests/kkk.txt',pred13C.length+5);

//simulateNMRSignals2D*/