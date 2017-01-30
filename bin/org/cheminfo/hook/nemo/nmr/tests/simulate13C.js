//var mol = load('/org/cheminfo/hook/nemo/nmr/tests/ethylvinylether.mol');
//out.println(mol);
//var spinus = SD.spinusPred1H(molfile);
//var pred = JSON.parse(File.load('/org/cheminfo/hook/nemo/nmr/tests/leucine.spinus'));
//var table = File.load('/org/cheminfo/hook/nemo/nmr/tests/spinustable.txt');
/*  var nmrSignals13C  = []; 
  for (var j=0; j<3; j++) {
    var signal = {startX:j*10,
                  stopX:j*10,
                  assignment:""+j,
                  atomIDs:[""+j],
                  pattern:"s",
                  observe:100000000,
                  delta1:j*10,
                  multiplicity:"",
                  nucleus:"13C",
                  units:"PPM",
                  peaks:[{intensity:1,x:j*10}],
                  //level:level
                 };  
    nmrSignals13C.push(signal);
                                              
  }
var diaIDs = [{"id":"gGQHDIeIgZhA~dPOeT","atoms":[4],"element":"O","nbEquivalent":1},{"id":"gGQHLIeJYihA~dPOeT","atoms":[2],"element":"O","nbEquivalent":1},{"id":"gGPhEaLiMlu@OtbApj`","atoms":[5],"element":"C","nbEquivalent":1},{"id":"gGPhHaLiU{U@OtbApj`","atoms":[3],"element":"C","nbEquivalent":1},{"id":"gGPhDQLiJuS@OtbApj`","atoms":[0],"element":"C","nbEquivalent":1},{"id":"gGPhHQLiSuS@OtbApj`","atoms":[1],"element":"C","nbEquivalent":1}];
//console.log(nmrSignals13C);
var pred = File.loadJSON('/org/cheminfo/hook/nemo/nmr/tests/spinsystem13C.json');
//var pred = SD.spinusParser(table);
//var pred = SD.spinusPred1H(mol);
jexport('nSpins',pred.length);
var spectraData = SD.simulateNMRSpectrum(nmrSignals13C,{maxClusterSize:9,nbPoints:16*1024,diaIDs:diaIDs,assign:false});
spectraData.fourierTransform();
console.log(spectraData.getAssignmentFromSimulation());
//console.log(spectraData.toJcamp({encode:'DIFDUP',yfactor:1,type:"SIMPLE"}));
//out.println(spectraData.toJcamp());
*/
var signals = [{"min":116.1,"ncs":1,"max":116.1,"atomIDs":["2"],"std":0,"hose2":"gC`HALqp@OzQ@xUP","hose3":"dee@aBXIdiZTjYf@b@@OzP`NET","hose4":"f`qPRBBLUjs`SNn]rULsLlbMKpDPCC@@@RB@\\Jh","diaIDs":["f`qPRABLUjs`SIIYNn]rULsLlbMKpDPCC@@@RB@\\Jh"],"hose5":"undefined","units":"PPM","assignment":"2","startX":116.1,"level":4,"stopX":116.1,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":116.1}]},{"min":127.85,"ncs":4,"max":131,"atomIDs":["10"],"std":1.30066,"hose2":"eMBBYc@OzRCaU@","hose3":"gGXILIgReLa@ARHGBj@","hose4":"deVDbL[`f^yJYYjDHF@@OzP`NET","diaIDs":["f`qPRALJYrs`SNIyri]vRJIQRqJJdc^BA``X@@GzPPCaU@"],"hose5":"dkmDaLunBY{mgIEDhiUJDHF@`@OzP`NET","units":"PPM","assignment":"10","startX":129.0125,"level":5,"stopX":129.0125,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":129.0125}]},{"min":127.85,"ncs":4,"max":131,"atomIDs":["10"],"std":1.30066,"hose2":"eMBBYc@OzRCaU@","hose3":"gGXILIgReLa@ARHGBj@","hose4":"deVDbL[`f^yJYYjDHF@@OzP`NET","diaIDs":["f`qPRALJYrs`SNIyri]vRJIQRqJJdc^BA``X@@GzPPCaU@"],"hose5":"dkmDaLunBY{mgIEDhiUJDHF@`@OzP`NET","units":"PPM","assignment":"10","startX":129.0125,"level":5,"stopX":129.0125,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":129.0125}]},{"min":135.4,"ncs":2,"max":135.4,"atomIDs":["14"],"std":0,"hose2":"eMBBYc@OzRCaU@","hose3":"gGPHALiSH@@_tbApj`","hose4":"did@`@fTieff@@@@OzP`NET","diaIDs":["f`qPRAIJMYw`SLiUMn}rTrsmsTXKP@@TLp@@RB@\\Jh"],"hose5":"dcm@aITIfYVTiegZfGP@@h@@iB@xUP","units":"PPM","assignment":"14","startX":135.39999,"level":5,"stopX":135.39999,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":135.39999}]},{"min":135.4,"ncs":2,"max":135.4,"atomIDs":["14"],"std":0,"hose2":"eMBBYc@OzRCaU@","hose3":"gGPHALiSH@@_tbApj`","hose4":"did@`@fTieff@@@@OzP`NET","diaIDs":["f`qPRAIJMYw`SLiUMn}rTrsmsTXKP@@TLp@@RB@\\Jh"],"hose5":"dcm@aITIfYVTiegZfGP@@h@@iB@xUP","units":"PPM","assignment":"14","startX":135.39999,"level":5,"stopX":135.39999,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":135.39999}]},{"min":123.9,"ncs":6,"max":129.95,"atomIDs":["12"],"std":2.108,"hose2":"eMBBYc@OzRCaU@","hose3":"gJPHALiR`@G}H`\\Jh","hose4":"daF@bJBYjTiVRX@`@C~dHCaU@","diaIDs":["f`qPRAJFEIw`SMIeqi}vRIJIQHsPcMV@HFBF@@GzPPCaU@"],"hose5":"dcNDbJFPfYyJUeeI`BA`@C~dHCaU@","units":"PPM","assignment":"12","startX":126.70833,"level":5,"stopX":126.70833,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":126.70833}]},{"min":123.9,"ncs":6,"max":129.95,"atomIDs":["12"],"std":2.108,"hose2":"eMBBYc@OzRCaU@","hose3":"gJPHALiR`@G}H`\\Jh","hose4":"daF@bJBYjTiVRX@`@C~dHCaU@","diaIDs":["f`qPRAJFEIw`SMIeqi}vRIJIQHsPcMV@HFBF@@GzPPCaU@"],"hose5":"dcNDbJFPfYyJUeeI`BA`@C~dHCaU@","units":"PPM","assignment":"12","startX":126.70833,"level":5,"stopX":126.70833,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":126.70833}]},{"min":135.65,"ncs":1,"max":135.65,"atomIDs":["9"],"std":0,"hose2":"gC`HALqp@OzQ@xUP","hose3":"dax@`@fTjYP@@@iB@xUP","hose4":"dcm@aATIdYVTjYVYi{P@BH@@iB@xUP","diaIDs":["f`qPRAAJMYw`SHiUMn}vRQQIQPsQdnZ@@Haf@@GzPPCaU@"],"hose5":"f`qPRBAJMYw`SMn}vRQQIQPsQdnZ@@Haf@@GzPPCaU@","units":"PPM","assignment":"9","startX":135.64999,"level":5,"stopX":135.64999,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":135.64999}]},{"min":145.7,"ncs":1,"max":145.7,"atomIDs":["3"],"std":0,"hose2":"gChIHIfSG`@iDCaU@","hose3":"diFDbHJ`fZyJfY`X@@iB@xUP","hose4":"dkmDaHtjBYkmgIHhhcEAfA`@`@OzP`NET","diaIDs":["f`qPRAHRIRs`SLIiti]vRQQQFJZHVc^`XB@X@@GzPPCaU@"],"hose5":"f`qPRBHRIRs`SMN]vRQQQFJZHVc^`XB@X@@GzPPCaU@","units":"PPM","assignment":"3","startX":145.7,"level":5,"stopX":145.7,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":145.7}]},{"min":145.7,"ncs":1,"max":145.7,"atomIDs":["3"],"std":0,"hose2":"gChIHIfSG`@iDCaU@","hose3":"diFDbHJ`fZyJfY`X@@iB@xUP","hose4":"dkmDaHtjBYkmgIHhhcEAfA`@`@OzP`NET","diaIDs":["f`qPRAHRIRs`SLIiti]vRQQQFJZHVc^`XB@X@@GzPPCaU@"],"hose5":"f`qPRBHRIRs`SMN]vRQQQFJZHVc^`XB@X@@GzPPCaU@","units":"PPM","assignment":"3","startX":145.7,"level":5,"stopX":145.7,"pattern":"s","observe":100000000,"asymmetric":false,"nucleus":"13C","peaks":[{"intensity":1,"x":145.7}]}];
var diaIDss = [{"id":"f`qPRABLUjs`SIIYNn]rULsLlbMKpDPCC@@@RB@\\Jh","atoms":[2],"element":"C"},{"id":"f`qPRALJYrs`SNIyri]vRJIQRqJJdc^BA``X@@GzPPCaU@","atoms":[10,11],"element":"C"},{"id":"f`qPRAIJMYw`SLiUMn}rTrsmsTXKP@@TLp@@RB@\\Jh","atoms":[14,15],"element":"C"},{"id":"f`qPRAJFEIw`SMIeqi}vRIJIQHsPcMV@HFBF@@GzPPCaU@","atoms":[12,13],"element":"C"},{"id":"f`qPRAAJMYw`SHiUMn}vRQQIQPsQdnZ@@Haf@@GzPPCaU@","atoms":[9],"element":"C"},{"id":"f`qPRAHRIRs`SLIiti]vRQQQFJZHVc^`XB@X@@GzPPCaU@","atoms":[3,4],"element":"C"}];
var spectraData = SD.simulateNMRSpectrum(signals,{maxClusterSize:9,nbPoints:16*1024,assign:false});
spectraData.fourierTransform();
console.log(spectraData.getAssignmentFromSimulation());