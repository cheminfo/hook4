// Write some code here
//File.unzip("./phasing.zip");
//var spectraData = SD.load("/Demo/Spectra/NMR/Bruker/4/4/pdata/1/1r");
//console.log(spectraData);

//jexport("jcamp",{type:"jcamp",value:spectraData.toJcamp({encode:'DIFDUP',yfactor:0.01,type:"SIMPLE"})});
//var fids = ["/org/cheminfo/hook/nemo/nmr/tests/4/fid"];
var fids = ["/org/cheminfo/hook/nemo/nmr/tests/4_new/fid",
            "/org/cheminfo/hook/nemo/nmr/tests/phasing/DA2_1M_25C_proton_AV_dig/997/fid",
            "/org/cheminfo/hook/nemo/nmr/tests/phasing/Imagabalin_flow_pf_AV_dig/11/fid",
            "/org/cheminfo/hook/nemo/nmr/tests/phasing/test_exp_F300/3/fid"];


var result = [];

for(var i=0;i<fids.length;i++){
	var fid = SD.load(fids[i]);
	File.save('/org/cheminfo/hook/nemo/nmr/tests/fid'+i+'.jdx',fid.toJcamp({encode:'DIFDUP',yfactor:0.1,type:"NTUPLES"}));
  	var r1 = SD.load(fids[i].replace("fid","pdata/1/1r"));
    //console.log(r1.getNbPoints());
    fid.zeroFilling(fid.getNbPoints());
    fid.brukerSpectra();
    fid.fourierTransform();
    fid.postFourierTransform();
    //fid.phaseCorrection(0.5, 0.025);
    var phc = fid.automaticPhase();
    console.log(phc);
    var fid2 = SD.load(fids[i]);
    fid2.zeroFilling(fid2.getNbPoints());
    fid2.brukerSpectra();
    fid2.apodization("exp",3);
    fid2.fourierTransform();
    fid2.postFourierTransform();
    fid2.phaseCorrection(phc[0], phc[1]);
    fid2.automaticPhase();
    
    var jcamp2 = fid2.toJcamp({encode:'DIFDUP',yfactor:0.1,type:"NTUPLES"});
    File.save('/org/cheminfo/hook/nemo/nmr/tests/autophasedX'+i+'.jdx',jcamp2);
    
  	var jcamp1 = fid.toJcamp({encode:'DIFDUP',yfactor:0.1,type:"NTUPLES"});
  	var jcamp1r = r1.toJcamp({encode:'DIFDUP',yfactor:0.1,type:"NTUPLES"});
  

  	File.save('/org/cheminfo/hook/nemo/nmr/tests/autophased'+i+'.jdx',jcamp1);
  	File.save('/org/cheminfo/hook/nemo/nmr/tests/brukerphased'+i+'.jdx',jcamp1r);
  	
    result[i]={"name":i+1,"spectrum1":jcamp1,"spectrum1r":jcamp1};
    
}

//jexport("result",result);
