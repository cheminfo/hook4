/**
 * @object SpectraData
 * This library provides methods for manipulating spectraData.
 * Most of the functions are developed to NMR spectra processing, but also there is some general 
 * purpose function that allows to work with any kind of spectra data.
 * Functions to export jcamp-dx files are available here.
 */

/**
 * @function addNoise(SNR)
 * This function adds white noise to the the given spectraData. The intensity of the noise is 
 * calculated from the given signal to noise ratio.
 * @param SNR: Signal to noise ratio
 */

/**
 * @function shift(globalShift)
 * This function shift the given spectraData. After this function is applied, all the peaks in the
 * spectraData will be found at xi-globalShift
 * @param globalShift
 */

/**
 * @function supressPeak(from, to)
 * This function suppress a signal from the given spectraData within the given x range. 
 * Returns a spectraData without signal in the given region
 * @param from
 * @param to
 */

/**
 * @function defineSpectraData(x, y)
 * This function define a spectraData from the x and y vectors. If the spectraData is null it will return a new instance of
 * SpectraData
 * @param x
 * @param y
 * @example var spectraData2 = defineSpectraData([0, 0.1, 0.2, 0.3, 0.5 ], [0, 1, 2, 1, 0]);
 */

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
 * @param spec2: spectraData2
 * @param factor1: linear factor for spec1
 * @param factor2: linear factor for spec2
 * @param autoscale: Auto-adjust scales before combine the spectraDatas
 * @example spec1 = addSpectraDatas(spec1,spec2,1,-1, false) This subtract spec2 from spec1
*/

/**
 * @function simplePeakDetection(parameters)
 * This function performs a simple peak detection in a spectraData. The parameters that can be specified are:
 * Returns a two dimensional array of double specifying [x,y] of the detected peaks.
 * @option from:	Lower limit.
 * @option to:		Upper limit.
 * @option threshold: The minimum intensity to consider a peak as a signal, expressed as a percentage of the highest peak. 
 * @option stdev: Number of standard deviation of the noise for the threshold calculation if a threshold is not specified.
 * @option resolution: The maximum resolution of the spectrum for considering peaks.
 * @option yInverted: Is it a Y inverted spectrum?(like an IR spectrum)
 * @option smooth: A function for smoothing the spectraData before detect peaks. If your are dealing with
 * experimental spectra, smoothing will avoid that the algorithm detect false noisy peaks.
 */

/**
 * @function autoBaseline();
 * Automatically corrects the base line of a given spectraData. After this process the spectraData
 * should have meaningful integrals.
 */

/**
 * @function fourierTransform();
 * Fourier transforms the given spectraData (Note. no 2D handling yet) this spectraData have to be of type NMR_FID or 2DNMR_FID
 */

/**
 * @function postFourierTransform(ph1corr)
 * This filter makes an phase 1 correction that corrects the problem of the spectra that has been obtained 
 * on spectrometers using the Bruker digital filters. This method is used in cases when the BrukerSpectra 
 * filter could not find the correct number of points to perform a circular shift.
 * The actual problem is that not all of the spectra has the necessary parameters for use only one method for 
 * correcting the problem of the Bruker digital filters.
 * @param spectraData: A fourier transformed spectraData.
 * @param ph1corr: Phase 1 correction value in radians.
 */

/**
 * @function zeroFilling(nPointsX [,nPointsY])
 * This function increase the size of the spectrum, filling the new positions with zero values. Doing it one 
 * could increase artificially the spectral resolution.
 * @param nPointsX: Number of new zero points in the direct dimension
 * @param nPointsY: Number of new zero points in the indirect dimension
 */

/**
 * @function  haarWhittakerBaselineCorrection(waveletScale,whittakerLambda)
 * Applies a baseline correction as described in J Magn Resonance 183 (2006) 145-151 10.1016/j.jmr.2006.07.013
 * The needed parameters are the wavelet scale and the lambda used in the whittaker smoother.
 * @param waveletScale: To be described
 * @param whittakerLambda: To be described
 */

/**
 * @function whittakerBaselineCorrection(whittakerLambda,ranges)
 * Applies a baseline correction as described in J Magn Resonance 183 (2006) 145-151 10.1016/j.jmr.2006.07.013
 * The needed parameters are the Wavelet scale and the lambda used in the Whittaker smoother.
 * @param waveletScale: To be described
 * @param whittakerLambda: To be described
 * @param ranges: A string containing the ranges of no signal.
 */

/**
 * @function brukerSpectra(options)
 * This filter applies a circular shift(phase 1 correction in the time domain) to an NMR FID spectrum that 
 * have been obtained on spectrometers using the Bruker digital filters. The amount of shift depends on the 
 * parameters DECIM and DSPFVS. This spectraData have to be of type NMR_FID
 * @option DECIM: Acquisition parameter
 * @option DSPFVS: Acquisition parameter
 */

/**
 * @function apodization(functionName, lineBroadening)
 * Apodization of a spectraData object.
 * @param spectraData: An spectraData of type NMR_FID
 * @param functionName: Valid values for functionsName are
 *  Exponential, exp
 *	Hamming, hamming
 *	Gaussian, gauss
 *	TRAF, traf
 *	Sine Bell, sb
 *	Sine Bell Squared, sb2
 * @param lineBroadening: The parameter LB should either be a line broadening factor in Hz 
 * or alternatively an angle given by degrees for sine bell functions and the like.
 * @example SD.apodization(, lineBroadening)
 */

/**
 * @function echoAntiechoFilter();
 * That decodes an Echo-Antiecho 2D spectrum.
 */

/**
 * @function SNVFilter()
 * This function apply a Standard Normal Variate Transformation over the given spectraData. Mainly used for IR spectra.
 */


/**
 * @function correlationFilter(func) 
 * This function correlates the given spectraData with the given vector func. The correlation
 * operation (*) is defined as:
 * 
 * 				      __ inf
 *  c(x)=f(x)(*)g(x)= \        f(x)*g(x+i)
 *                   ./    
 *                    -- i=-inf
 * @param func: A double array containing the function to correlates the spectraData
 * @example var smoothedSP = SD.correlationFilter(spectraData,[1,1]) returns a smoothed version of the
 * given spectraData. 
 */

/**
 * @function  phaseCorrection(phi0, phi1)
 * Applies the phase correction (phi0,phi1) to a Fourier transformed spectraData. The angles must be given in radians.
 * @param phi0: Zero order phase correction
 * @param phi1: One order phase correction
*/

/**
 * @function automaticPhase() 
 * This function determines automatically the correct parameters phi0 and phi1 for a phaseCorrection
 * function and applies it.
 */ 

/**
 *  @function useBrukerPhase()
 *  This function extract the parameters of the phaseCorrection from the jcamp-dx parameters
 *  if the spectrum was acquired in Bruker spectrometers . Basically it will look for the parameters
 *  $PHC0 and $PHC1, and will use it to call the phaseCorrection function.
 */

/**
 * @function simplePeakDetection(parameters)
 * This function performs a simple peak detection in a spectraData. Returns a two dimensional array of double 
 * specifying [x,y] of the detected peaks.
 * @option	from:	Lower limit. (Default 0)
 * @option	to: Upper limit.(Default 0) but if 'from' and 'to' are zero, then from=spectraData.getFirstX() 
 * and to=spectraData.getLastX()
 * @option	threshold: The minimum intensity to consider a peak as a signal, expressed as a percentage of the highest peak. 
 * @option	stdev: Number of standard deviation of the noise for the threshold calculation if a threshold is not specified.
 * @option	resolution: The maximum resolution of the spectrum for considering peaks. Necessary if the spectrum is a PEAK_TABLE
 * @option	yInverted: Is it a Y inverted spectrum?(like an IR spectrum)
 * @option	smooth: A function for smoothing the spectraData before detect peaks. If your are dealing with
 *   experimental spectra, smoothing will avoid that the algorithm detect false noisy peaks. 
 * See: correlationFilter(func)
 * @example SD.simplePeakDetection(spectraData,{from:0,to:10,threshold:0.005,smooth:[0.25,0.5,0.25]});
 */

/**
 * @function toJcamp()
 * This function creates a String that represents the given spectraData in the format JCAM-DX 5.0
 * The X,Y data can be compressed using one of the methods described in: 
 * "JCAMP-DX. A STANDARD FORMAT FOR THE EXCHANGE OF ION MOBILITY SPECTROMETRY DATA", 
 *  http://www.iupac.org/publications/pac/pdf/2001/pdf/7311x1765.pdf
 * @option encode: ['FIX','SQZ','DIF','DIFDUP','CVS','PAC'](Default: 'FIX')
 * @option yfactor: The YFACTOR. It allows to compress the data by removing digits from the ordinate. (Default: 1)
 * @option type: ["NTUPLES", "SIMPLE"](Default: "SIMPLE")
 * @option keep: A set of user defined parameters of the given SpectraData to be stored in the jcamp.
 * @example SD.toJcamp(spectraData,{encode:'DIFDUP',yfactor:0.01,type:"SIMPLE",keep:['#batchID','#url']});
 */