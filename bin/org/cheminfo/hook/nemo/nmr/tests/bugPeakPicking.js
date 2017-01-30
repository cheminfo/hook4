function formatSignals(signals, sum){
	  var integral = 0;
	  var signals2 = signals;
	  for(var j=0;j<signals.length;j++){
	  	//signals2[j]=JSON.parse(signals[j].toJSON());
	    integral+=signals2[j].integralData.value;
	  }
	  //Ajusting the integral and reduce the lenght of the numbers
	  for(var j=0;j<signals.length;j++){
	    var val=signals2[j].integralData.value*sum/integral;
	    var from = signals2[j].startX*1;
	    signals2[j].startX=from.toFixed(3);
	    var to = signals2[j].stopX*1;
	    signals2[j].stopX=to.toFixed(3);
	  	signals2[j].integralData.value=Math.round(val);
	  }
	  
	  signals2.sort(function(a,b){
	    return a.integralData.value<b.integralData.value?1:-1;
	  });
	  
	  var j = signals.length-1;
	  while(signals2[j].integralData.value<0.5&&j>=0){
	    signals2.splice(j,1);
	  	j--;
	  }
	    
	  return signals2;
	}

	function formatSignals13C(signals, sum){
	  var integral = 0;
	  var signals2 = signals;
	  for(var j=0;j<signals.length;j++){
	  	//signals2[j]=JSON.parse(signals[j].toJSON());
	    integral+=signals2[j].integralData.value;
	  }
	  //Ajusting the integral and reduce the lenght of the numbers
	  for(var j=0;j<signals.length;j++){
	    var val=signals2[j].integralData.value*sum/integral;
	    var from = signals2[j].startX*1;
	    signals2[j].startX=from.toFixed(3);
	    var to = signals2[j].stopX*1;
	    signals2[j].stopX=to.toFixed(3);
	  	signals2[j].integralData.value=Math.round(val);
	  }
	  
	  signals2.sort(function(a,b){
	    return a.delta1>b.delta1?1:-1;
	  });
	  return signals2;
	}


	function addAnnotations(predictions, line, diaIDs) {
	  var annotations=[];
	  var colors=Color.getDistinctColors(line+2);
	  for (var i=0; i<predictions.length; i++) {
	    var annotation={};
	    var prediction=predictions[i];
	    annotations.push(annotation);
	    
	   	annotation._highlight=prediction.diaIDs;
	    annotation.type="rect";
	    annotation.pos={x:prediction.startX, y:(line*15)+"px"};
	    annotation.pos2={x:prediction.stopX, y:(line*15+10)+"px"};
	    
	    
	    var posY=(line*15-10)+"px";
	    var color="red";
	    if(line==1){
	      posY=(line*15+10)+"px";
	      color="black";
	    }
	    annotation.label={
	      text: lookup(prediction.diaIDs,diaIDs),
	      size: "11px",
	      anchor: 'middle',
	      color:color,
	      position: {dx:"10px", y:posY, dy: "5px"}
	    };
	  
	    annotation.strokeColor="red";
	    annotation.strokeWidth="1px";
	    annotation.fillColor=colors[line+1];//"rgba(0,255,0,50)";
	    annotation.info=prediction;
	  }
	  //if(sndAnnotation)
	  //	annotations.push(sndAnnotation);
	  return annotations
	}

	function createAnnotations2D(signals2D){
		var annotations=[];
		for(var k=signals2D.length-1;k>=0;k--){
		  var signal = signals2D[k];
		  var annotation={};
		  annotation.type="rectangle";
		  //annotation._highlight=[];
		  //annotation.pos={x:signal.shiftX,y:signal.shiftY};
	      annotation.pos={x:signal.shiftX,y:signal.shiftY, dx:"-3px", dy:"-3px"};
	      annotation.pos2={x:signal.shiftX+0.01,y:signal.shiftY+0.01};
	      annotation.fillColor=Color.getColor(0,255,0);
		 if(signal.intensity==1)
		  	annotation.strokeColor=Color.getColor(255,0,0);
	      else
	        annotation.strokeColor=Color.getColor(0,128,0);
		  annotation.strokeWidth="1px";
		  annotation.width="6px";
		  annotation.height="6px";
		  annotations.push(annotation);
		}
	  return annotations;
	}


	function addIntegrals(signals){
	  var annotations=[];
	  for (var i=0; i<signals.length; i++) {
	    var annotation={};
	    annotation.type="surfaceUnderCurve"; 
	    annotation._highlight=signals[i]._highlight;
	    
	    annotation.pos={x:signals[i].startX};
	    annotation.pos2={x:signals[i].stopX}; // can be specified also as x and y or dx and dy
	    // pos2 for arrow / line/ peakInterval
	    
	    annotation.label={
	      text: signals[i].integralData.value,
	      color:"#00AA00",
	      size: "12px",
	      anchor: 'middle', // right, middle, left
	      angle: 0,
	      position: { dx: "0px", y: 0, dy: "10px" }
	    };
	    annotation.fillColor=Color.random();
	    annotation.strokeColor="rgba(i*10,0,0,0.2)";
	    annotation.strokeWidth="1px";
	  
	    annotation.width="1px"; // used for rectangle
	    annotation.height="1px"; // used for rectangle
	    annotations.push(annotation);
	  }
	  return annotations;
	}

	function compareAssignment(asg1, asg2){
	  //var correct = true;
	  for(var i=0;i<asg1.length;i++){
	  	  for(var j=0;j<asg2.length;j++){
	        //if(i==0&&j==0)
	        //  console.log(asg1[i]);
	        //console.log(asg2[j].info.stopX+" "+asg1[i].info.startX +" , "+ asg1[i].info.stopX +" "+ asg2[j].info.startX);
	        if(asg2[j].info&&asg1[i].info){
	            //console.log(asg2[j].info);
	        	if(asg2[j].info.stopX<asg1[i].info.startX && asg1[i].info.stopX < asg2[j].info.startX){
	          		//It could be more than 1 diaID :(
	          		//console.log(asg1[i]._highlight+"_____"+asg2[j]._highlight);
	          		var equal = false;
	          		for(var k=0;k<asg1[i]._highlight.length;k++){
	            		for(var l=0;l<asg2[j]._highlight.length;l++){
	            			if(asg1[i]._highlight[k]==asg2[j]._highlight[l]){
	          					equal=true;
	          				}
	          			}
	          		}
	          		if(equal==false)
	            		return false;
	        	}
	        }

	  	  }
	  }
	  return true;
	}

	function lookup(queries, diaIDs){
	  var indexes = [];
	  for(var i=0;i<queries.length;i++){
	    for(var j=0;j<diaIDs.length;j++){
	      if(diaIDs[j].id==queries[i])
	        indexes.push(j);
	    }
	  }
	  return JSON.stringify(indexes).replace("[","").replace("]","");
	}



// Write some code here
var sourceFolder="/org/cheminfo/hook/nemo/nmr/tests/";

var oneD=sourceFolder+"molecule55.jdx";//"ethylbenzene.jdx";//"1H.jdx";

var jcamp1D={type:"jcamp",url:File.getReadURL(oneD)};

var sd=SD.load(oneD);
var signals = sd.nmrPeakDetection({nStddev:4,baselineRejoin:0.7});

signals = formatSignals(signals,16);

jexport("ss",signals);



