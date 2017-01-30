out.println("Starting test script");

about();

out.println("Waiting 3 seconds!!!");
sleep(3000);

var spectraData = loadJCamp('file://CURRENT_WORKING_DIR/chloropromazine_1.dx');
if (spectraData) {
	dumpSpectraData(spectraData,'raw.data')
	if (apodization(spectraData,'exp',100)) {
		out.println("APODIZATION OK!!!");
	} else {
		out.println("APODIZATION FAILED!!!");
	}
	dumpSpectraData(spectraData,'apodized.data');
	var nbPoints = spectraData.getNbPoints();
	out.println("number of points(before): "+nbPoints);
	if (zeroFilling(spectraData,nbPoints)) {
		out.println("ZF OK!!!");
	} else {
		out.println("ZF FAILED!!!");
	}
	nbPoints = spectraData.getNbPoints();
	out.println("number of points(after): "+nbPoints);
	if (fourierTransform(spectraData)) {
		out.println("FT OK!!!");
	} else {
		out.println("FT FAILED!!!");
	}
	dumpSpectraData(spectraData,'ft.data');
	if (APK(spectraData)) {
		out.println("APK OK!!!");
	} else {
		out.println("APK FAILED!!!");
	}
	dumpSpectraData(spectraData,'apk.data');
	if (autoBaseline(spectraData)) {
		out.println("BASELINE OK!!!");
	} else {
		out.println("BASELINE FAILED!!!");
	}
	dumpSpectraData(spectraData,'baseline.data');
}

