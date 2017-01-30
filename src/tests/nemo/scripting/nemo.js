var table='1	1H	2			1	2	d	5	3	d	15\n';
table+='2	1H	2.02			1	3	d	3\n';
table+='3	1H	4			1';
//simulateNMRSpectrum(spinsystem, freq, from, to, lW,scale('HZ','PPM'), maxClustersize, nPoints)
var spectraData = SD.simulateNMRSpectrum(table,{'from':0,'to':12,'linewidth':1.5});
SD.fourierTransform(spectraData);
//Nemo.plotSpectraData(spectraData,"spectraData1");

var calMassPattern=ChemCalc.getJcamp('C100', {});
var massSpectraData = SD.loadJcamp(calMassPattern);
Nemo.plotSpectraData(massSpectraData,"massSpectraData1");
