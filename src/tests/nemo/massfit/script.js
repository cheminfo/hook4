var experimental=getSpectraData("experimental")
var  predictions = new Array();
var max='+cont+';
for(i=0;i<max;i++)
	predictions[i]=getSpectraData("theoretical"+i);
var result=massFitting(experimental,predictions);
jsonResult.put(result);
var nPoints=result.getInt("windowPoints");
var peakWidth=result.getDouble("gaussParam");
var globalShift=result.getDouble("globalShift");
for(i=0;i<max;i++){
	shift(predictions[i],globalShift);
	gaussian(predictions[i],experimental,peakWidth);
}
var diffSpectrum = getSpectraData("diff");
defineSpectraData(diffSpectrum,experimental.getSpectraDataX(), experimental.getSpectraDataY());
diffSpectrum.setFirstX(experimental.getFirstX());
diffSpectrum.setLastX(experimental.getLastX());
diffSpectrum.setDataType(experimental.getDataType());

var factors=result.optJSONObject('factors');
if(factors==null)
	factors=jsonResult;
for(i=0;i<max;i++){
	factor=factors.get(i).get('y')/predictions[i].getMaxY();
	out.println('Factor   *************'+factor);
	AddSpectraDatas(predictions[i],null,factor,1);
	AddSpectraDatas(diffSpectrum,predictions[i],1,1);
}

	