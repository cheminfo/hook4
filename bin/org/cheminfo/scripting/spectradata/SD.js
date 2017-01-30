/**
 * @object SD
 * Functions related to NMR and JCAMP-DX
 * @constructor
 * Load a new spectrum
 * @return	+SD
 */
var SD = {
		
	/**
	 * @function load(stringSpectraData, options)
	 * This function creates and returns a new spectraData from the given parameters.
	 * This parameter could
	 * be a URL with the reference to the data file, or it can be a string containing the data.
	 * @param	stringSpectraData:string	A URL to a file or a String with the content of the file
	 * @param	options:+Object			Object containing the options
	 * @option	format:string	The format of the data to load (default: jcamp). Supported formats : jcamp, MzXML
	 * @returns	+SD
	*/
	 load: function(stringSpectraData, options){
		 // should we try to get the real file name
		 // the problem is that here we can as well give a URL or directly a jcamp ...
		 //var stringSpectraData=File.checkGlobal(stringSpectraData);
		 if( ! (stringSpectraData.indexOf('http')==0 || stringSpectraData.contains("##"))) {
		 	 stringSpectraData=File.checkGlobal(stringSpectraData)
		 }
		  
		 return new ESD(SDAPI.load(Global.basedir, Global.basedirkey, stringSpectraData, options));
	 },

	/**
	 * @function simulateNMRSpectrum(table, options)
	 * That function simulates a NMR spectrum using the shifts and couplings specified in table. 
	 * This function returns a spectraData containing a FID.
	 * The parameters for the simulations are specified in the given JSONObject options which has
	 * the following optional parameters:
	 * @param	table:string	A string representing a spin system.
	 * @param	options:+Object			Object containing the options
	 * @option	frequency:number	The base frequency
	 * @option	from:number	left limit of the window
	 * @option	to:number	right limit of the window
	 * @option	linewidth:number	The desired line width of the experiment, in Hz.
	 * @option	scale:string	"PPM" or "HZ" depending on the units of the table
	 * @option	maxClusterSize:number	Maximum cluster size for the simulation
	 * @option	nbPoints:number	Number of points of the FID
	 * @option	method:number	Specifies the simulation method to be used. Not implements yet.
	 * @returns	+SD
	 */
	 simulateNMRSpectrum: function(table, inputParam) {
				return new ESD(SDAPI.simulateNMRSpectrum(table, inputParam));
	  },
	  
	  
	/**
	 * @function simulateNMRSpectrum2D(tableX, tableY, structureInfo, params)
	 * This function simulates a 2D-NMR Spectrum using the shifts specified in tableX and tableY and the connectivity
	 * specified in structureInfo. It returns a NMRSignal2D[] specifying the expected 2D signals.
	 * @param	tableX:+Object	A NMRSignal1D array specifying the expected shifts for the first type of atoms.
	 * @param	tableY:+Object	A NMRSignal1D array specifying the expected shifts for the second type of atoms.
	 * @param	structureInfo:+Object	A JSONArray specifying the connectivity for each pair of atoms in the molecule. It has only
	 * the connectivity between a certain number of bonds. So any kind of 2D experiment can be simulated by giving the 
	 * correct parameters when you create the structureInfo.
	 * @param	options:+Object	Object containing the options
	 * @option	width:number	The width of the peaks in PPM(direct dimension)
	 * @option	eight:number	The eight of the peaks in PPM(indirect dimension) 
	 * @option	nbPoints:number	Number of points in the direct dimension
	 * @option	nbSubSpectraData:number	Number of points in the indirect dimension
	 * @returns	+SD
	 */
	  simulateNMRSpectrum2D: function (tableX, tableY, structureInfo, params){
		  return new ESD(SDAPI.simulateNMRSpectrum2D(tableX, tableY, structureInfo, params));
	  },
	  
	/**
	 * @function resurrectNMRSpectrum(nmrSignals, params)
	 * This function resurrects a 1D-NMR Spectrum using the array of NMRSignal1D specified in nmrSignals.
	 * It returns a SpectraData which could be exported to a jcamp-dx.
	 * @param	Object:nmrSignals1D	A  set of NMRSignal1D to resurrect.
	 * @param	options:+Object			Object containing the options
	 * @option	frequency:number	The base frequency in Hz(Default 400Mz)
	 * @option	from:number	left limit of the window in PPM (Default 0)
	 * @option	to:number	right limit of the window in PPM (Default 10)
	 * @option	linewidth:number	Line width of the signals in Hz (Default 1)
	 * @option	nbPoints:number	Number of points in the direct dimension (Default 32K)
	 * @returns	+SD
	 */
	  resurrectNMRSpectrum: function (nmrSignals, params){
		  return new ESD(SDAPI.resurrectNMRSpectrum(nmrSignals, params));
	  },
	  
	/**
	 * @function resurrectNMRSpectrum2D(nmrSignals2D, params)
	 * This function resurrects a 2D-NMR Spectrum using the array of NMRSignal2D specified in nmrSignals2D.
	 *  It returns a SpectraData which could be exported to a jcamp-dx.
	 * @param	nmrSignals2D:+Object	A  set of NMRSignal2D to resurrect.
	 * @param	options:+Object			Object containing the options
	 * @option	width:number	The width of the peaks in PPM(direct dimension)
	 * @option	eight:number	The eight of the peaks in PPM(indirect dimension) 
	 * @option	nbPoints:number	Number of points in the direct dimension
	 * @option	nbSubSpectraData:number	Number of points in the indirect dimension
	 * @returns	+SD
	 */
	  resurrectNMRSpectrum2D: function (nmrSignals2D, params){
		  return new ESD(SDAPI.resurrectNMRSpectrum2D(nmrSignals2D, params));
	  },
	  
	/**
	 * @function simulateNMRSignals2D(tableX, tableY, structureInfo)
	 * This function simulates 2D-NMR spectrum using the shifts specified in tableX and tableY and the connectivity
	 * specified in structureInfo. It returns a NMRSignal2D[] specifying the expected 2D signals.
	 * @param	tableX:+Object	A NMRSignal1D array specifying the expected shifts for the first type of atoms.
	 * @param	table:+Object	A NMRSignal1D array specifying the expected shifts for the second type of atoms.
	 * @param	structureInfo:+Object	A JSONArray specifying the connectivity for each pair of atoms in the molecule. It has only
	 * the connectivity between a certain number of bonds. So any kind of 2D experiment can be simulated by giving the 
	 * correct parameters when you create the structureInfo.
	 * @returns	+SD
	 */
	  simulateNMRSignals2D: function (tableX, tableY, structureInfo){
		  return JSON.parse(SDAPI.simulateNMRSignals2DAsJSONString(tableX, tableY, structureInfo));
	  },

	/**
	 * @function SSMutator(spinSystem,canonizedMolfile,nOut,options)
	 * This function return a set of tables that contains random modifications of a NMR parameters table.
	 * The output is a JSONArray containing the modified tables
	 * @param	spinSystem:+Object
	 * @param	canonizedMolfile:string
	 * @param	nOut:number	Number of outputs
	 * @param	options:+Object			Object containing the options
	 * @option	nucleus:string	1H, 13C, ...
	 * @option	shiftdev:number	Standard deviation for the shift(ppm)
	 * @option	jdev:number	Standard deviation for the J couplings (Hz)
	 * @returns	 +Object JSONArray containing nOut spin-systems with randomly added noise on the chemical shifts. 
	 */
	 SSMutator: function(spinSystem, canonizedMolfile, nOut, inputParam){
		 return JSON.parse(SDAPI.SSMutator(spinSystem, canonizedMolfile, nOut, inputParam));
	 },
	 
	 /**
	  * @function spinusPred1H(molfile, options)
	  * This function predicts shift and constants coupling for 1H-RMN, from a molfile by using the algorithm described in:
	  * Y. Binev, J. Aires-de-Sousa, Structure-Based Predictions of 1H NMR Chemical Shifts Using Feed-Forward Neural Networks
	  * @param	molfile:string	A molfile content
	  * @option diaIDs:+Object	The set of diaIds for this molecule
	  * @returns	+Object an array of NMRSignal1D
	  */
	 spinusPred1H: function(molfile, options){
		 var diaIDs = null;
		 if(options){
			 diaIDs = options.diaIDs || null;
		 }
		 return JSON.parse(SDAPI.spinusPred1HAsJSONString(molfile, diaIDs));
	 },
	
	 /**
	  * @function nmrShiftDBPred13C(molfile)
	  * This function predict shift for 13C-NMR, from a molfile by using the free data base: nmrshiftdb.nmr.uni-koeln.de
	  * @param	molfile:string	A molfile content
	  * @returns	+Object an array of NMRSignal1D
	  */
	 nmrShiftDBPred13C: function(molfile, options){
		  var db = null;
		  var closeDB = true;
		  options = options || {};
		  if(options.db){
			  db = options.db;
			  closeDB=false;
	 	  }
		  else
			  db = new DB.MySQL("localhost","mynmrshiftdb","nmrshiftdb","xxswagxx");
		  var algorithm = options.algorithm||0;
		  var mol=ACT.load(molfile);
		  var diaIDs=mol.getDiastereotopicAtomIDs("C");
		  
	      var atoms = {};
	      var atomNumbers = [];
	      for (var j=0; j<diaIDs.length; j++) {
	        var hosesString=ACT.getHoseCodesFromDiaID(diaIDs[j].id, 5,{algorithm:algorithm});
	        var atom = {diaIDs:[diaIDs[j].id+""],
	                    nucleus:"13C",
	                    pattern:"s",
	                    observe:100e6,
	                    units:"PPM",
	                    asymmetric:false,
	                    hose2:hosesString[1]+"",
	                    hose3:hosesString[2]+"",
	                    hose4:hosesString[3]+"",
	                    hose5:hosesString[4]+""};
	        for(var k=diaIDs[j].atoms.length-1;k>=0;k--){
	            atoms[diaIDs[j].atoms[k]]=JSON.parse( JSON.stringify(atom));;
	            atomNumbers.push(diaIDs[j].atoms[k]);
	        }
	      }
	      
	      //var asgs = data.nmr.nmrLines;
	       
	      //Now, we predict the chimical shift by using our copy of NMRShiftDB
	      var script = "select AVG(chemicalShift) AS cs, STD(chemicalShift)  AS std, COUNT(chemicalShift) AS ncs, MIN(chemicalShift) as min, MAX(chemicalShift) as max FROM assignment where ";//hose5='dgH`EBYReZYiIjjjjj@OzP`NET'";
	      var toReturn = [];
	      for (var j=0; j<atomNumbers.length; j++) {
	        var atom = atoms[atomNumbers[j]];
	        var level=0;
	        var res = null;
	        if(atom.hose5!="undefined"&&atom.hose5!="null")
	            res = db.select(script+"hose5='"+atom.hose5+"'", {format:"json"});
	        
	        if(res!=null&&res[0].cs){
	          level=5;
	        }
	        else{
	            if(atom.hose4!="undefined"&&atom.hose4!="null")
	                res = db.select(script+"hose4='"+atom.hose4+"'", {format:"json"});
	            if(res!=null&&res[0].cs){
	                level=4;
	            }
	            else{
	                 if(atom.hose3!="undefined"&&atom.hose3!="null")
	                    res = db.select(script+"hose3='"+atom.hose3+"'", {format:"json"});
	                 if(res!=null&&res[0].cs){
	                     level=3;
	                 }
	                 else{
	                     if(atom.hose2!="undefined"&&atom.hose2!="null")
	                        res = db.select(script+"hose2='"+atom.hose2+"'", {format:"json"});
	                     if(res!=null&&res[0].cs){
	                         level=2;
	                     }
	                     else{
	                    	 res = [{cs:-8008,ncs:0,std:0,min:0,max:0}];
	                    	 }
	                }
	            }
	        }
	        atom.level=level;
	        atom.startX=res[0].cs;
	        atom.stopX=res[0].cs;
	        atom.peaks=[{intensity:1,x:res[0].cs}];
	        atom.assignment=""+atomNumbers[j],
	        atom.atomIDs=[""+atomNumbers[j]],
	        atom.ncs=res[0].ncs;
	        atom.std=res[0].std;
	        atom.min=res[0].min;
	        atom.max=res[0].max;
	        
	        toReturn.push(atom);
	      }
	      if(closeDB)
	    	  db.close();
	      return toReturn;
	 },
	 
	 /**
	  * @function acsParser(acsString)
	  * This function converts an ACS string to an array of NMRSignal1D
	  * @param	acsString:string	A string containing the assignment
	  * @returns	 +Object
	  */
	 acsParser: function(acsString){
		 return JSON.parse(SDAPI.AcsParserAsJSONString(acsString));
	 },
	 
	 /**
	  * @function signals2Acs(signals, solvent)
	  * This function converts a set of NMRSignals1D to the ACS string format.
	  * @param	signals:+Object	An array of NMRSignals1D
	  * @param	solvent:string	The solvent
	  * x@returns	 string
	  */
	 signals2Acs: function(signals, solvent){
		 return ACS.formater.toACS(signals, solvent)+"";
	 },
	 
	 /**
	  * @function spinusParser(spinusTable)
	  * This function converts an Spinus table(String) to an array of NMRSignal1D
	  * @param	spinusTable:string	A string containing the assignment
	  * @returns	+Object
	  */
	 spinusParser: function(spinusTable){
		 return JSON.parse(SDAPI.spinusParserAsJSONString(spinusTable));
	 },
	 
	 /**
	  * @function create(x, y)
	  * This function define a spectraData from the x and y vectors. If the spectraData is null it will return a new instance of
	  * SpectraData
	  * @param	x:[number]
	  * @param	y:[number]
	  * @returns	+SD
	  * @example var spectraData2 = SD.create([0, 0.1, 0.2, 0.3, 0.5 ], [0, 1, 2, 1, 0]);
	  */
	 create: function(x, y){
		 return new ESD(SDAPI.create(x,y));
	 },
	 
	 /**
	  * @function autoAssignment(diaIDsObject, signalsObject, moleculeInfo, cosyObject,moleculeInfoHMBC, hmbcObject,minLH, size, maxErrorsCOSY, maxErrosHMBC, maxShift)
	  * This function return all the possible assignments for the given parameters. Second approach.
	  * @param	diaIDsObject:+Object	Diasterotopic information about the molecule.
	  * @param	signalsObject:+Object	Experimental 1H peak-picking
	  * @param	moleculeInfoCOSY:+Object	COSY expected connectivity
	  * @param	cosyObject:+Object	COSY peak-picking
	  * @param	moleculeInfoHMBC:+Object	HMBC+HSQC expected connectivity
	  * @param	hmbcObject:+Object	HMBC+HSQC peak-picking
	  * @param	minLH:number	Score threshold. Percentage of the maximum score
	  * @param	size:number	Maximum number of solutions to return.
	  * @param	maxErrorsCOSY:number	Initial Max allowed errors in COSY restrictions
	  *	@param	maxErrosHMBC:number	Initial Max allowed error in HMBC restrictions
	  * @param	maxShift:number	Maximum chemical shift error between observed and predicted. 
	  * @returns	+Object	A set of assignments.
	  */
	 autoAssignment: function(diaIDsObject, signalsObject, moleculeInfoCOSY, cosyObject, moleculeInfoHMBC, hmbcObject,minLH, size, maxErrorsCOSY, maxErrosHMBC, maxShift){
		 return JSON.parse(SDAPI.autoAssignment(diaIDsObject, signalsObject, moleculeInfoCOSY, cosyObject, moleculeInfoHMBC, hmbcObject, minLH, size, maxErrorsCOSY, maxErrosHMBC, maxShift).toString());
	 },
	 
	
	 /**
	  * @function mergePeaks(signalsHMBC, signalsHSQC, signals1H)
	  * Merge HMBC and HSQC signals
	  * @param	signalsHMBC:+Object	A set of NMRSignal2D
	  * @param	signalsHSQC:+Object	A set of NMRSignal2D
	  * @param	signals1H:+Object	A set of NMRSignal1D from 1H NMR spectrum
	  * @returns	+Object	An array of NMRSignal2D
	  */
	 mergePeaks: function(signalsHMBC, signalsHSQC, signals1H){
		 return JSON.parse(SDAPI.mergePeaksAsJSONString(signalsHMBC, signalsHSQC, signals1H));
	 }
};

/**
 * @object SD.prototype
 * Prototype of ESD objects
 */
var ESD = function (newESD) {
	this.ESD=newESD;

	
	/**
	 * @function addNoise(SNR)
	 * This function adds white noise to the the given spectraData. The intensity of the noise is 
	 * calculated from the given signal to noise ratio.
	 * @param	SNR:number	Signal to noise ratio
	 * @returns	nothing
	 */
	this.addNoise=function(SNR) {
		this.ESD.addNoise(SNR);
	},	

	/**
	 * @function shift(globalShift)
	 * This function shift the given spectraData. After this function is applied, all the peaks in the
	 * spectraData will be found at xi-globalShift
	 * @param	globalShift:number
	 * @returns	nothing
	 */
	this.shift=function(globalShift) {
		this.ESD.shift(globalShift);
	},

	/**
	 * @function fillWith(from, to, value)
	 * This function suppress a signal from the given spectraData within the given x range. 
	 * Returns a spectraData without signal in the given region
	 * @param	from:number
	 * @param	to:number
	 * @param	fillWith:number
	 * @returns	nothing
	 */
	this.fillWith=function(from, to, value) {
		this.ESD.fillWith(from, to, value);
	},

	/**
	 * @function suppressZone(from, to)
	 * This function suppress a zone from the given spectraData within the given x range. 
	 * Returns a spectraData of type PEAKDATA without peaks in the given region
	 * @param	from:number
	 * @param	to:number
	 * @returns	nothing
	 */
	this.suppressZone=function(from, to) {
		this.ESD.suppressZone(from, to);
	},

	/**
	 * @function addSpectraDatas(spec2,factor1,factor2,autoscale )	 
	 *  This filter performs a linear combination of two spectraDatas.
	 * A=spec1
	 * B=spec2
	 * After to apply this filter you will get:
	 * 		A=A*factor1+B*factor2
	 * if autoscale is set to 'true' then you will obtain:
	 * 	A=A*factor1+B*k*factor2
	 * Where the k is a factor such that the maximum peak in A is equal to the maximum peak in spectraData2 
	 * @param	spec2:+SpectraData spectraData2
	 * @param	factor1:number	linear factor for spec1
	 * @param	factor2:number	linear factor for spec2
	 * @param	autoscale:bool	Auto-adjust scales before combine the spectraDatas
	 * @returns	nothing
	 * @example spec1 = addSpectraDatas(spec1,spec2,1,-1, false) This subtract spec2 from spec1
	*/
	this.addSpectraDatas=function(spec2,factor1,factor2,autoscale ) {
		this.ESD.addSpectraDatas(spec2,factor1,factor2,autoscale );
	},

	/**
	 * @function getXYData()
	 * To get a 2 dimensional array with the x and y of this spectraData( Only for 1D spectra).
	 * @returns	[[]]	a double[2][nbPoints] where the first row contains the x values and the second row the y values.
	 */
	this.getXYData=function( ) {
		return this.ESD.getXYData();
	},

	/**
	 * @function autoBaseline()
	 * Automatically corrects the base line of a given spectraData. After this process the spectraData
	 * should have meaningful integrals.
	 * @returns	nothing
	 */
	this.autoBaseline=function( ) {
		this.ESD.autoBaseline();
	},
	
	/**
	 * @function fourierTransform()
	 * Fourier transforms the given spectraData (Note. no 2D handling yet) this spectraData have to be of type NMR_FID or 2DNMR_FID
	 * @returns	nothing
	 */
	this.fourierTransform=function( ) {
		this.ESD.fourierTransform();
	},

	/**
	 * @function postFourierTransform(ph1corr)
	 * This filter makes an phase 1 correction that corrects the problem of the spectra that has been obtained 
	 * on spectrometers using the Bruker digital filters. This method is used in cases when the BrukerSpectra 
	 * filter could not find the correct number of points to perform a circular shift.
	 * The actual problem is that not all of the spectra has the necessary parameters for use only one method for 
	 * correcting the problem of the Bruker digital filters.
	 * @param	ph1corr:number	Phase 1 correction value in radians.
	 * @returns	nothing
	 */
	this.postFourierTransform=function(ph1corr) {
		if(ph1corr)
			this.ESD.postFourierTransform(ph1corr);
		else
			this.ESD.postFourierTransform();
	},

	/**
	 * @function zeroFilling(nPointsX [,nPointsY])
	 * This function increase the size of the spectrum, filling the new positions with zero values. Doing it one 
	 * could increase artificially the spectral resolution.
	 * @param	nPointsX:number	Number of new zero points in the direct dimension
	 * @param	nPointsY:number	Number of new zero points in the indirect dimension
	 * @returns	nothing
	 */
	this.zeroFilling=function(nPointsX, nPointsY) {
		if(nPointsX&&nPointsY){
			this.ESD.zeroFilling(nPointsX, nPointsY);
		}
		else{
			if(nPointsX)
				this.ESD.zeroFilling(nPointsX);
			else{
				this.ESD.zeroFilling(this.getNbPoints());
			}
		}
	},
	
	/**
	 * @function  haarWhittakerBaselineCorrection(waveletScale,whittakerLambda)
	 * Applies a baseline correction as described in J Magn Resonance 183 (2006) 145-151 10.1016/j.jmr.2006.07.013
	 * The needed parameters are the wavelet scale and the lambda used in the whittaker smoother.
	 * @param	waveletScale:number	To be described
	 * @param	whittakerLambda:number	To be described
	 * @returns	nothing
	 */
	this.haarWhittakerBaselineCorrection=function(waveletScale,whittakerLambda) {
		this.ESD.haarWhittakerBaselineCorrection(waveletScale,whittakerLambda);
	},
	
	/**
	 * @function whittakerBaselineCorrection(whittakerLambda,ranges)
	 * Applies a baseline correction as described in J Magn Resonance 183 (2006) 145-151 10.1016/j.jmr.2006.07.013
	 * The needed parameters are the Wavelet scale and the lambda used in the Whittaker smoother.
	 * @param	waveletScale:number	To be described
	 * @param	whittakerLambda:number	To be described
	 * @param	ranges:string	A string containing the ranges of no signal.
	 * @returns	nothing
	 */
	this.whittakerBaselineCorrection=function(whittakerLambda,ranges) {
		this.ESD.whittakerBaselineCorrection(whittakerLambda,ranges);
	},

	/**
	 * @function brukerSpectra(options)
	 * This filter applies a circular shift(phase 1 correction in the time domain) to an NMR FID spectrum that 
	 * have been obtained on spectrometers using the Bruker digital filters. The amount of shift depends on the 
	 * parameters DECIM and DSPFVS. This spectraData have to be of type NMR_FID
	 * @param	options:+Object			Object containing the options
	 * @option	DECIM:number	Acquisition parameter
	 * @option	DSPFVS:number	Acquisition parameter
	 * @returns	nothing
	 */
	this.brukerSpectra=function(options) {
		this.ESD.brukerSpectra(options);
	},

	/**
	 * @function apodization(functionName, lineBroadening)
	 * Apodization of a spectraData object.
	 * @param	spectraData:+SpectraData	A spectraData of type NMR_FID
	 * @param	functionName:string	Valid values for functionsName are
	 *  Exponential, exp
	 *	Hamming, hamming
	 *	Gaussian, gauss
	 *	TRAF, traf
	 *	Sine Bell, sb
	 *	Sine Bell Squared, sb2
	 * @param	lineBroadening:number	The parameter LB should either be a line broadening factor in Hz or alternatively an angle given by degrees for sine bell functions and the like.
	 * @returns	nothing
	 * @example SD.apodization(, lineBroadening)
	 */
	this.apodization=function(functionName, lineBroadening) {
		this.ESD.apodization(functionName, lineBroadening);
	},

	/**
	 * @function echoAntiechoFilter();
	 * That decodes an Echo-Antiecho 2D spectrum.
	 * @returns	nothing
	 */
	this.echoAntiechoFilter=function() {
		this.ESD.echoAntiechoFilter();
	},

	/**
	 * @function SNVFilter()
	 * This function apply a Standard Normal Variate Transformation over the given spectraData. Mainly used for IR spectra.
	 * @returns	nothing
	 */
	this.SNVFilter=function() {
		this.ESD.SNVFilter();
	},

	/**
	 * @function powerFilter(power)
	 * This function applies a power to all the Y values. If the power is less than 1 and the spectrum has negative values, it will be shifted so that the lowest value is zero 
	 * @param	power:number	The power to apply
	 * @returns	nothing
	 */
	this.powerFilter=function(power) {
		var minY=this.getMinY();
		if(power<1 && minY<0){
			this.YShift(-1*minY);
			console.warn("SD.powerFilter: The spectrum had negative values and was automatically shifted before applying the function.");
		}
		this.ESD.powerFilter(power);
	},
	
	/**
	 * @function logarithmFilter(base)
	 * This function applies a log to all the Y values.If the spectrum has negative or zero values, it will be shifted so that the lowest value is 1 
	 * @param	base:number	The base to use
	 * @returns	nothing
	 */
	this.logarithmFilter=function(base) {
		var minY=this.getMinY();
		if(minY<=0){
			this.YShift((-1*minY)+1);
			console.warn("SD.logarithmFilter: The spectrum had negative values and was automatically shifted before applying the function.");
		}
		this.ESD.logarithmFilter(base);
	},
	
	/**
	 * @function getMinY()
	 * This function returns the minimal value of Y
	 * @returns	nothing
	 */
	this.getMinY=function() {
		return this.ESD.getMinY();
	},
	
	/**
	 * @function getMaxY()
	 * This function returns the maximal value of Y
	 * @returns	nothing
	 */
	this.getMaxY=function() {
		return this.ESD.getMaxY();
	},
	
	/**
	 * @function setMinMax(min,max)
	 * This function scales the values of Y between the min and max parameters
	 * @param	min:number	Minimum desired value for Y
	 * @param	max:number	Maximum desired value for Y
	 * @returns	nothing
	 */
	this.setMinMax=function(min,max) {
		return this.ESD.setMinMax(min,max);
	},
	
	/**
	 * @function setMin(min)
	 * This function scales the values of Y to fit the min parameter
	 * @param	min:number	Minimum desired value for Y
	 * @returns	nothing
	 */
	this.setMin=function(min) {
		return this.ESD.setMinMax(min,this.getMaxY());
	},
	
	/**
	 * @function setMax(max)
	 * This function scales the values of Y to fit the max parameter
	 * @param	max:number	Maximum desired value for Y
	 * @returns	nothing
	 */
	this.setMax=function(max) {
		return this.ESD.setMinMax(this.getMinY(),max);
	},
	
	/**
	 * @function YShift(value)
	 * This function shifts the values of Y
	 * @param	value:number	Distance of the shift
	 * @returns	number
	 */
	this.YShift=function(value) {
		return this.ESD.setMinMax(this.getMinY()+value,this.getMaxY()+value);
	},
	
	/**
	 * @function correlationFilter(func) 
	 * This function correlates the given spectraData with the given vector func. The correlation
	 * operation (*) is defined as:
	 * 
	 * 				      __ inf
	 *  c(x)=f(x)(*)g(x)= \        f(x)*g(x+i)
	 *                   ./    
	 *                    -- i=-inf
	 * @param	func:[number] A double array containing the function to correlates the spectraData
	 * @returns	nothing
	 * @example var smoothedSP = SD.correlationFilter([1,1]) returns a smoothed version of the
	 * given spectraData. 
	 */
	this.correlationFilter=function(func) {
		this.ESD.correlationFilter(func);
	},
	
	/**
	 * @function  phaseCorrection(phi0, phi1)
	 * Applies the phase correction (phi0,phi1) to a Fourier transformed spectraData. The angles must be given in radians.
	 * @param	phi0:number	Zero order phase correction
	 * @param	phi1:number	One order phase correction
	 * @returns	nothing
	*/
	this.phaseCorrection=function(phi0, phi1) {
		this.ESD.phaseCorrection(phi0, phi1);
	},

	/**
	 * @function automaticPhase() 
	 * This function determines automatically the correct parameters phi0 and phi1 for a phaseCorrection
	 * function and applies it.
	 * @returns	nothing
	 */ 
	this.automaticPhase=function() {
		return this.ESD.automaticPhase();
	},

	/**
	 *  @function useBrukerPhase()
	 *  This function extract the parameters of the phaseCorrection from the jcamp-dx parameters
	 *  if the spectrum was acquired in Bruker spectrometers . Basically it will look for the parameters
	 *  $PHC0 and $PHC1, and will use it to call the phaseCorrection function.
	 *  @returns	nothing
	 */
	this.useBrukerPhase=function() {
		this.ESD.useBrukerPhase();
	},
	
	/**
	 *  @function enhace2D()
	 * This is an experimental transformation for 2D spectra that aims to normalize the intensity
	 * in the indirect dimension. The basic idea is to divide by a value that is proportional
	 * to the energy level of each channel. By doing that, I hope, strong columns in 2D spectra 
	 * would not affect the peak picking quality.
	 * @returns	nothing
	 */
	this.enhance2D=function() {
		this.ESD.enhance2D();
	},
	
	/**
	 * @function peakPicking(parameters)
	 * This function performs a simple peak detection in a spectraData. The parameters that can be specified are:
	 * Returns a two dimensional array of double specifying [x,y] of the detected peaks.
	 * @param	options:+Object			Object containing the options
	 * @option	from:number	Lower limit.
	 * @option	to:number		Upper limit.
	 * @option	threshold:number	The minimum intensity to consider a peak as a signal, expressed as a percentage of the highest peak. 
	 * @option	stdev:number	Number of standard deviation of the noise for the threshold calculation if a threshold is not specified.
	 * @option	resolution:number	The maximum resolution of the spectrum for considering peaks.
	 * @option	yInverted:bool Is it a Y inverted spectrum?(like an IR spectrum)
	 * @option	smooth:[number] A function for smoothing the spectraData before the detecting task. If your are dealing with
	 * experimental spectra, smoothing will reduce the false noisy peaks.
	 * @returns	nothing
	 */
	this.peakPicking=function(parameters) {
		return this.ESD.peakPicking(parameters);
	},

	this.jresPeakDetection=function(parameters){
		return this.ESD.jresPeakDetection(parameters);
	}
	
	/**
	 * @function nmrPeakDetection(options)
	 * This function process the given spectraData and tries to determine the NMR signals. Returns an NMRSignal1D array containing all the detected 1D-NMR Signals
	 * @param	options:+Object			Object containing the options
	 * @option	fromX:number	Lower limit.
	 * @option	toX:number		Upper limit.
	 * @option	threshold:number	The minimum intensity to consider a peak as a signal, expressed as a percentage of the highest peak. 
	 * @option	stdev:number	Number of standard deviation of the noise for the threshold calculation if a threshold is not specified.
	 * @returns	+Object	a set of NMRSignal1D
	 */
	this.nmrPeakDetection=function(parameters) {
		return JSON.parse(this.ESD.NMRPeakDetectionString(parameters));
	},
	
	
	this.NMRPeakDetection=function(parameters) {
		console.warn("NMRPeakDetection is deprecated and should be remplaced by nmrPeakDetection");
		return  this.nmrPeakDetection(parameters);
	},
	
	/**
	 * @function getAssignmentFromSimulation()
	 * This function returns the set of NMRSignal1D that was finally used for the simulation process. If diaIDs was
	 * provided during the simulation process this function will return the best possible assignment for this
	 * spectrum.
	 * @returns	+Object	A set of NMRSignal1D
	 */
	this.getAssignmentFromSimulation=function(){
		return JSON.parse(this.ESD.getAssignmentFromSimulationString());
	},

	/**
	 * @function nmrPeakDetection2D(options)
	 * This function process the given spectraData and tries to determine the NMR signals. Returns an NMRSignal2D array containing all the detected 2D-NMR Signals
	 * @param	options:+Object			Object containing the options
	 * @option	thresholdFactor:number	A factor to scale the automatically determined noise threshold.
	 * @returns	+Object	set of NMRSignal2D
	 */
	this.nmrPeakDetection2D=function(parameters) {
		return JSON.parse(this.ESD.NMRPeakDetection2DString(parameters));
	},
	
	this.NMRPeakDetection2D=function(parameters) {
		console.warn("NMRPeakDetection2D is deprecated and should be remplaced by nmrPeakDetection2D");
		return  this.nmrPeakDetection(parameters);
	},
	
	/**
	 * @function peakPicking(options)
	 * This function performs a simple peak detection in a spectraData. The parameters that can be specified are:
	 * @param	options:+Object			Object containing the options
	 * @option	from:number	Lower limit.
	 * @option	to:number		Upper limit.
	 * @option	threshold:number	The minimum intensity to consider a peak as a signal, expressed as a percentage of the highest peak. 
	 * @option	stdev:number	Number of standard deviation of the noise for the threshold calculation if a threshold is not specified.
	 * @option	resolution:number	The maximum resolution of the spectrum for considering peaks.
	 * @option	yInverted:bool Is it a Y inverted spectrum?(like an IR spectrum)
	 * @option	smooth:[number] A function for smoothing the spectraData before detect peaks. If your are dealing with
	 * experimental spectra, smoothing will avoid that the algorithm detect false noisy peaks.
	 * @returns	[[]]	a two dimensional double array specifying [x,y] of the detected peaks.
	 */
	this.peakPicking=function(parameters) {
		return this.ESD.peakPicking(parameters);
	},

	/**
	 * @function toJcamp(options)
	 * This function creates a String that represents the given spectraData in the format JCAM-DX 5.0
	 * The X,Y data can be compressed using one of the methods described in: 
	 * "JCAMP-DX. A STANDARD FORMAT FOR THE EXCHANGE OF ION MOBILITY SPECTROMETRY DATA", 
	 *  http://www.iupac.org/publications/pac/pdf/2001/pdf/7311x1765.pdf
	 * @param	options:+Object			Object containing the options
	 * @option	encode:string	['FIX','SQZ','DIF','DIFDUP','CVS','PAC'] (Default: 'FIX')
	 * @option	yfactor:number	The YFACTOR. It allows to compress the data by removing digits from the ordinate. (Default: 1)
	 * @option	type:string	["NTUPLES", "SIMPLE"] (Default: "SIMPLE")
	 * @option	keep:[string] A set of user defined parameters of the given SpectraData to be stored in the jcamp.
	 * @returns	jcamp:string	A string containing the jcamp-dx file
	 * @example SD.toJcamp(spectraData,{encode:'DIFDUP',yfactor:0.01,type:"SIMPLE",keep:['#batchID','#url']});
	 */	
	this.toJcamp=function(options) {
		return this.ESD.toJcamp(options)+"";
	},
	
	
	/**
	 * @function save(filename, options)
	 * This function allows to save the current spectraData as a jcamp.
	 * The options are the same as the "toJcamp" function.
	 * @param	options:+Object			Object containing the options
	 * @option	encode:string	['FIX','SQZ','DIF','DIFDUP','CVS','PAC'] (Default: 'FIX')
	 * @option	yfactor:number	The YFACTOR. It allows to compress the data by removing digits from the ordinate. (Default: 1)
	 * @option	type:string	["NTUPLES", "SIMPLE"] (Default: "SIMPLE")
	 * @option	keep:[string] A set of user defined parameters of the given SpectraData to be stored in the jcamp.
	 * @returns	string	URL the readURL of the saved file
	 */		
	this.save=function(filename, options){
		return File.save(File.checkGlobal(filename), this.ESD.toJcamp(options));
	},

	/**
	 * @function setTitle(newTitle)
	 * To set the title of this spectraData.
	 * @param	newTitle:string	The new title
	 * @returns	nothing
	 */
	this.setTitle=function(newTitle) {
		this.ESD.setTitle(newTitle);
	},
	
	/**
	 * @function getTitle()
	 * To get the title of this spectraData
	 * @returns	title:string	The title
	 */
	this.getTitle=function(){
		return this.ESD.getTitle();
	}, 
	
	/**
	 * @function getNbSubSpectra()
	 * To get the number of sub-spectra contained in this spectraData
	 * @returns	number
	 */
	this.getNbSubSpectra = function(){
		return this.ESD.getNbSubSpectra();
	},
	/**
	 * @function setActiveElement(nactiveSpectrum)
	 * This function sets the nactiveSpectrum sub-spectrum as active
	 * @returns	nothing
	 */
	this.setActiveElement = function(nactiveSpectrum){
		this.ESD.setActiveElement(nactiveSpectrum);
	},
	
	/**
	 * @function getActiveElement()
	 * This function returns the index of the active sub-spectrum.
	 * @returns	number
	 */
	this.getActiveElement = function(){
		return this.ESD.getActiveElement();
	},
	
	/**
	 * @function getFirstX()
	 * To return the start of the domain.
	 * @returns	number
	 */
	this.getFirstX = function(){
		return this.ESD.getFirstX();
	},
	
	/**
	 * @function getLastX()
	 * To return the end of the domain.
	 * @returns	number
	 */
	this.getLastX = function(){
		return this.ESD.getLastX();
	},
	
	/**
	 * @function getFirstY()
	 * To return the first Y.
	 * @returns	number
	 */
	this.getFirstY = function(){
		return this.ESD.getFirstY();
	},
	
	/**
	 * @function getLastY()
	 * To return the last Y.
	 * @returns	number
	 */
	this.getLastY = function(){
		return this.ESD.getLastY();
	},
	
	/**
	 * @function getXData()
	 * This function returns a double array containing the values of the domain.
	 * @returns	[number]	x data
	 */
	this.getXData=function(){
		return this.ESD.getXData();
	},
	
	/**
	 * @function getYData()
	 * This function returns a double array containing the values of the intensity for the current sub-spectrum.
	 * @returns	[number]	y data
	 */
	this.getYData = function(){
		return this.ESD.getYData();
	},
	
	/**
	 * @function getXYData([from, to])
	 * This function returns a matrix (2 X nbPoints)  containing the X and Y values for the current sub-spectrum.
	 * from and to parameters are optional and specify the window to be considered during the construction of the matrix.
	 * @returns	[[]]	xy data
	 */
	this.getXYData = function(from, to){
		if(from)
			if(to)
				return this.ESD.getXYData(from,to);
		return this.ESD.getXYData();
	},
	
	/**
	 * @function getEquallySpacedDataString(from, to, nbPoints)
	 * That returns a base64 representation( A String ) of the byte array returned by the function
	 * getEquallySpacedDataByte(from, to, NPoints)
	 * @param	from:number	From in PPM
	 * @param	to:number	To in PPM
	 * @param	nbPoints:number	number of points to be returned.
	 * @returns	string
	 */
	this.getEquallySpacedDataString = function(from, to, nbPoints){
		return this.ESD.getEquallySpacedDataString(from, to, nbPoints);
	},
	
	/**
	 * @function getEquallySpacedDataInt(from, to, nbPoints)
	 * This function returns an integer array of equally spaced NPoints containing a representation of intensities
	 * of this spectraData between from and to. The intensities are normalized between 0 and 10000000.
	 * @param	from:number	From in PPM
	 * @param	to:number	To in PPM
	 * @param	nbPoints:number		number of points to be returned.
	 * @param	options:+Object		Object containing the options
	 * @option	javascript:bool	Return a javascript object and not a java object (Default: false);
	 * @returns	[number]
	 */
	this.getEquallySpacedDataInt = function(from, to, nbPoints, options){
		var options=options||{};
		if (! options.javascript) return this.ESD.getEquallySpacedDataInt(from, to, nbPoints);
		var points=this.ESD.getEquallySpacedDataInt(from, to, nbPoints);
		var results=[];
		for (var i=0; i<points.length; i++) {
			results.push(points[i]);
		}
		return results;
	},
	
	/**
	 * @function getEquallySpacedDataByte(from, to, nbPoints)
	 * This function returns a byte array of NPoints containing a representation of intensities
	 * of this spectraData between from and to. The intensities are normalized between 0 and 255.
	 * @param	from:number	From in PPM
	 * @param	to:number	To in PPM
	 * @param	nbPoints:number		number of points to be returned.
	 * @returns	[byte]
	 */
	this.getEquallySpacedDataByte = function(from, to, nbPoints){
		return this.ESD.getEquallySpacedDataByte(from, to, nbPoints);
	},
	
	/**
	 * @function toXY(subSpectraDataID)
	 * This function returns a 2 columns string representing the XY values of the given sub-spectrum.
	 * @param	string
	 */
	this.toXY = function(subSpectraDataID){
		return this.ESD.toXY(subSpectraDataID);
	},
	
	/**
	 * @function isDataClassPeak()
	 * Is this a PEAKTABLE spectrum?
	 * @returns	boolean
	 */
	this.isDataClassPeak = function(){
		return this.ESD.isDataClassPeak();
	},
	
	/**
	 * @function isDataClassXY()
	 * Is this a XY spectrum?
	 * @returns	bool
	 */
	this.isDataClassXY = function(){
		return this.ESD.isDataClassXY();
	},
	
	/**
	 * @function getNucleus([iNucleus])
	 * To return the nucleus type for this spectrum.
	 * @param	iNucleus:number	1 for direct dimension nucleus type, 2 for indirect dimension nucleus type.(Default 1) 
	 * @returns	string
	 */
	this.getNucleus = function(iNucleus){
		if(iNucleus){
			if(this.ESD.is2D())
				return this.ESD.getNucleus(iNucleus).toString();
			else
				if(iNucleus==2)
					return "";
		}
		return this.ESD.getNucleus().toString();
	},
	
	/**
	 * @function is2D()
	 * Is it a 2D spectrum?
	 * @returns	bool
	 */
	this.is2D = function(){
		return this.ESD.is2D();
	},
	
	/**
	 * @function getMaxPeak()
	 * Get the maximum peak
	 * @returns	number
	 */
	this.getMaxPeak = function(){
		return this.ESD.getMaxPeak();
	},
	
	/**
	 * @function setDefinedMinY(minY)
	 * Set the minimun Y. Must call the function updateY after
	 * @param	 minY:number	The defined minimum Y
	 * @returns	number
	 */
	this.setDefinedMinY = function(minY){
		return this.ESD.setDefinedMinY(minY);
	},
	
	/**
	 * @function setDefinedMaxY(maxY)
	 * Set the maximun Y. Must call the function updateY after
	 * @param	 maxY:number	The defined maximum Y
	 * @returns	number
	 */
	this.setDefinedMaxY = function(maxY){
		return this.ESD.setDefinedMaxY(maxY);
	},
	
	/**
	 * @function updateY(maxY)
	 * Updates the Y values according to the minY and maxY. Must to be called just after calling setDefinedMaxY or setDefinedMinY
	 * @param	 maxY:number	The defined maximum Y
	 * @returns	bool
	 */
	this.updateY = function(){
		return this.ESD.updateY();
	},
	
	/**
	 * @function getParamDouble(name, defvalue);
	 * Get the value of the parameter
	 * @param	 name:string The parameter name
	 * @param	 defvalue:number The default value
	 * @returns	number	The value of the given parameter
	 */
	this.getParamDouble = function(name, defvalue){
		return this.ESD.getParamDouble(name, defvalue);
	},
	
	/**
	 * @function getParamString(name, defvalue)
	 * Get the value of the parameter
	 * @param	 name:string	The parameter name
	 * @param	 defvalue:string	The default value
	 * @retuns string	The value of the given parameter
	 */
	this.getParamString = function(name, defvalue){
		return this.ESD.getParamString(name, defvalue);
	},
	
	/**
	 * @function getParamInt(name, defvalue)
	 * Get the value of the parameter
	 * @param	 name:number The parameter name
	 * @param	 defvalue:number The default value
	 * @returns	number	The value of the given parameter
	 */
	this.getParamInt = function(name, defvalue){
		return this.ESD.getParamInt(name, defvalue);
	},
	
	/**
	 *  @function getNbPoints()
	 *  To return the number of points in the given spectraData
	 *  @returns	number
	 */
	this.getNbPoints = function(){
		return this.ESD.getNbPoints();
	},
	
	/**
	 *  @function getSpectraDataY()
	 *  To return Y vector of this spectraData
	 *  @returns	[number]
	 */
	this.getSpectraDataY = function(){
		return this.ESD.getSpectraDataY();
	},
	
	/**
	 *  @function getSpectraDataX()
	 *  To return X vector of this spectraData
	 *  @returns	[number]
	 */
	this.getSpectraDataX = function(){
		return this.ESD.getSpectraDataX();
	},
	
	/**
	 *  @function putParam(name, value)
	 *  Put a new user defined parameter
	 *  @returns	bool
	 */
	this.putParam = function(name, value){
		return this.ESD.putParam(name, value);
	},
	
	/**
	 *  @function getVector(from, to, nPoints);
	 *  Return a vector containing an equally space vector in the given window.
	 * @param	from:number	Lower limit.
	 * @param	to:number		Upper limit.
	 * @param	nPoints:numbers	The number of points for the output vector.
	 * @returns	[number]
	 */
	this.getVector = function(from, to, nPoints){
		return this.ESD.getVector(from, to, nPoints);
	}	
	
	/**
	 *  @function getNoiseLevel()
	 *  To return the calculated noise threshold for this spectraData.
	 *  @returns	number
	 */
	this.getNoiseLevel = function(){
		return this.ESD.getNoiseLevel();
	},
	
	/**
	 *  @function getBaselineRejoin()
	 *  To return the calculated base line re-join for this spectraData.
	 *  @returns	number
	 */
	this.getBaselineRejoin = function(){
		return this.ESD.getBaselineRejoin();
	}
}
