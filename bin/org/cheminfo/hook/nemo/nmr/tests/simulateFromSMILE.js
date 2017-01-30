
var mol = File.load('/org/cheminfo/hook/nemo/nmr/tests/mol12Hatoms.mol');
//out.println(mol);
var spinus = SD.spinusPred1H(mol);
//var pred = JSON.parse(File.load('/org/cheminfo/hook/nemo/nmr/tests/leucine.spinus'));
//var table = File.load('/org/cheminfo/hook/nemo/nmr/tests/spinustable.txt');
//var table = File.load('/org/cheminfo/hook/nemo/nmr/tests/spinsystem79.txt');
//var pred = SD.spinusParser(table);
//var pred = SD.spinusPred1H(mol);
jexport('nSpins',spinus.length);
var spectraData = SD.simulateNMRSpectrum(spinus, {maxClusterSize:12,nbPoints:16*1024});
spectraData.fourierTransform();
//out.println(spectraData.toJcamp({encode:'DIFDUP',yfactor:1,type:"SIMPLE"}));
//out.println(spectraData.toJcamp());

/*var signals = [{"atomIDs":["7"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"10","coupling":7.27,"multiplicity":"d"}],"multiplicity":"d","units":"PPM","assignment":"7","startX":1.606,"pattern":"s","stopX":1.606,"observe":400000000,"referenceAtomID":0,"asymmetric":false,"delta1":1.606,"integralData":{"to":1.606,"value":1,"from":1.606},"nucleus":"1H","peaks":[{"intensity":1,"x":1.606}]},{"atomIDs":["8"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"10","coupling":7.27,"multiplicity":"d"}],"multiplicity":"d","units":"PPM","assignment":"8","startX":1.606,"pattern":"s","stopX":1.606,"observe":400000000,"referenceAtomID":0,"asymmetric":false,"delta1":1.606,"integralData":{"to":1.606,"value":1,"from":1.606},"nucleus":"1H","peaks":[{"intensity":1,"x":1.606}]},{"atomIDs":["9"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"10","coupling":7.27,"multiplicity":"d"}],"multiplicity":"d","units":"PPM","assignment":"9","startX":1.606,"pattern":"s","stopX":1.606,"observe":400000000,"referenceAtomID":0,"asymmetric":false,"delta1":1.606,"integralData":{"to":1.606,"value":1,"from":1.606},"nucleus":"1H","peaks":[{"intensity":1,"x":1.606}]},{"atomIDs":["10"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"7","coupling":7.27,"multiplicity":"d"},{"assignmentTo":"8","coupling":7.27,"multiplicity":"d"},{"assignmentTo":"9","coupling":7.27,"multiplicity":"d"}],"multiplicity":"ddd","units":"PPM","assignment":"10","startX":5.401,"pattern":"s","stopX":5.401,"observe":400000000,"referenceAtomID":1,"asymmetric":false,"delta1":5.401,"integralData":{"to":5.401,"value":1,"from":5.401},"nucleus":"1H","peaks":[{"intensity":1,"x":5.401}]},{"atomIDs":["11"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"13","coupling":6.91,"multiplicity":"d"},{"assignmentTo":"14","coupling":7.66,"multiplicity":"d"},{"assignmentTo":"12","coupling":10.745,"multiplicity":"d"}],"multiplicity":"ddd","units":"PPM","assignment":"11","startX":2.697,"pattern":"s","stopX":2.697,"observe":400000000,"referenceAtomID":3,"asymmetric":false,"delta1":2.697,"integralData":{"to":2.697,"value":1,"from":2.697},"nucleus":"1H","peaks":[{"intensity":1,"x":2.697}]},{"atomIDs":["12"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"13","coupling":1.77,"multiplicity":"d"},{"assignmentTo":"14","coupling":6.92,"multiplicity":"d"},{"assignmentTo":"11","coupling":10.745,"multiplicity":"d"}],"multiplicity":"ddd","units":"PPM","assignment":"12","startX":2.55,"pattern":"s","stopX":2.55,"observe":400000000,"referenceAtomID":3,"asymmetric":false,"delta1":2.55,"integralData":{"to":2.55,"value":1,"from":2.55},"nucleus":"1H","peaks":[{"intensity":1,"x":2.55}]},{"atomIDs":["13"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"11","coupling":6.91,"multiplicity":"d"},{"assignmentTo":"12","coupling":1.77,"multiplicity":"d"},{"assignmentTo":"14","coupling":7.819,"multiplicity":"d"}],"multiplicity":"ddd","units":"PPM","assignment":"13","startX":3.801,"pattern":"s","stopX":3.801,"observe":400000000,"referenceAtomID":4,"asymmetric":false,"delta1":3.801,"integralData":{"to":3.801,"value":1,"from":3.801},"nucleus":"1H","peaks":[{"intensity":1,"x":3.801}]},{"atomIDs":["14"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"11","coupling":7.66,"multiplicity":"d"},{"assignmentTo":"12","coupling":6.92,"multiplicity":"d"},{"assignmentTo":"13","coupling":7.819,"multiplicity":"d"}],"multiplicity":"ddd","units":"PPM","assignment":"14","startX":3.954,"pattern":"s","stopX":3.954,"observe":400000000,"referenceAtomID":4,"asymmetric":false,"delta1":3.954,"integralData":{"to":3.954,"value":1,"from":3.954},"nucleus":"1H","peaks":[{"intensity":1,"x":3.954}]},{"atomIDs":["15"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"16","coupling":8.559,"multiplicity":"d"}],"multiplicity":"d","units":"PPM","assignment":"15","startX":3.897,"pattern":"s","stopX":3.897,"observe":400000000,"referenceAtomID":6,"asymmetric":false,"delta1":3.897,"integralData":{"to":3.897,"value":1,"from":3.897},"nucleus":"1H","peaks":[{"intensity":1,"x":3.897}]},{"atomIDs":["16"],"nbPeaks":1,"nmrJs":[{"assignmentTo":"15","coupling":8.559,"multiplicity":"d"}],"multiplicity":"d","units":"PPM","assignment":"16","startX":4.446,"pattern":"s","stopX":4.446,"observe":400000000,"referenceAtomID":6,"asymmetric":false,"delta1":4.446,"integralData":{"to":4.446,"value":1,"from":4.446},"nucleus":"1H","peaks":[{"intensity":1,"x":4.446}]}];
var diaIDs = [{"id":"gOqHHIeJV}ZfhA~dPNET","atoms":[6],"element":"C","nbEquivalent":1},{"id":"gOqHJIeJeZvjhA~dPNET","atoms":[2],"element":"C","nbEquivalent":1},{"id":"gOqHJIeIYZyjhA~dPNET","atoms":[0],"element":"C","nbEquivalent":1},{"id":"gOqHJIeJYSZZhA~dPNET","atoms":[3],"element":"C","nbEquivalent":1},{"id":"gOqHHIeJW\\ZihA~dPNET","atoms":[4],"element":"C","nbEquivalent":1},{"id":"gOqHJIeJ[ZvjhA~dPNET","atoms":[1],"element":"C","nbEquivalent":1}];
var spectraData = SD.simulateNMRSpectrum(signals,{from:0,to:12,nbPoints:1024*16,maxClusterSize:8,diaIDs:diaIDs,assign:true});  

*/