var xs=[5000e-6,2000e-6,800e-6,320e-6,128e-6,51.2e-6,20.48e-6,8.192e-6];
var ys=[0.69615,0.74682,0.83422,1.00419,1.54331,2.59947,3.61605,4.56276];

for (var i=0; i<xs.length; i++) {
	xs[i]=Math.log(xs[i]) / Math.LN10;
}



jexport("ab",Optimizer.sigmoid(xs, ys));

