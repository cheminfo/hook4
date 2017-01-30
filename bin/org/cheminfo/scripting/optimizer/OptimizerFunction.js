/**
 * @object Optimizer
 * This package contains optimization routines. 
 */
var Optimizer = {

	/**
	 * @function sigmoid(points, options)
	 * This function performs a sigmoid optimization.
	 * @param points for the optimization.
	 * @returns JSONArray
	 */
	 sigmoid: function(points, options) {
		 return JSON.parse(OPTIMIZERAPI.sigmoid(points, options));
	 }
}
    
