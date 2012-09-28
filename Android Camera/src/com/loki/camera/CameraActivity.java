package com.loki.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ZoomButton;


public class CameraActivity extends Activity implements OnClickListener, OnTouchListener{
	
	private static final String TAG = "CameraDemo";
	private View mainFrame;
	private View optionView;
	private View whiteBalanceView;
	private View effectsView;
	private View zoomView;
	private View expoView;
	private View sceneModeView;
	private Button wbButton,eButton,zButton,exButton,smButton;
	private Button buttonClick, buttonDelete, buttonGallery; 
	private Button wbAuto, wbCloudyDaylight, wbDaylight, wbFluroscent, wbIncandescent;
	private Button eMono, eNegative, eAqua, eNone, eSepia, eSolarize;
	private ZoomButton zoomIn, zoomOut;
	private ZoomButton expoUp, expoDown;
	private int zoom=0;
	private int expo=0;
	private boolean pictureTakenFlag = false;
	private boolean optionToggle=false;
	private boolean goingOut = false; 
	private boolean wbCheck=false;
	private boolean eCheck=false;
	private boolean zCheck=false;
	private boolean exCheck=false;
	private boolean smCheck=false;
	private long timeStamp;
	private String extStorageDirectory;
	
	Preview preview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.screen);

		if(!isExternalStoragePresent()){
			Toast.makeText(this, "External Storage Not Present", Toast.LENGTH_LONG).show();		
		}
		
		File folder = new File(Environment.getExternalStorageDirectory().toString()+"/loki/Images");
		if(!folder.exists())
			folder.mkdirs();
		extStorageDirectory = folder.toString();

		preview = new Preview(this); 
		((FrameLayout) findViewById(R.id.preview)).addView(preview); 
		
		mainFrame = (FrameLayout)findViewById(R.id.preview);
		mainFrame.setOnTouchListener(this);
		
		optionView = findViewById(R.id.optionHolder);
		whiteBalanceView = findViewById(R.id.whiteBalance);
		effectsView = findViewById(R.id.effects);
		zoomView = findViewById(R.id.zoom);
		expoView = findViewById(R.id.exposure);
		sceneModeView = findViewById(R.id.sceneMode);

		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { // <5>
				if (!pictureTakenFlag) {
					timeStamp = System.currentTimeMillis();
					preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
					buttonClick.setBackgroundResource(R.drawable.newpic);
					buttonDelete.setClickable(true);					
				} else {
					buttonClick.setBackgroundResource(R.drawable.click);
					preview.camera.startPreview();
					pictureTakenFlag = false;
					buttonDelete.setClickable(false);					
					Toast.makeText(CameraActivity.this, "Image saved on "+extStorageDirectory+"/ as "+timeStamp+".jpg", Toast.LENGTH_LONG).show();
				}
			}
		});

		
		buttonDelete = (Button) findViewById(R.id.buttonDelete);
		buttonDelete.setClickable(false); 
		buttonDelete.setOnClickListener(new	OnClickListener() { 
			public void onClick(View v) { // <5> 
				File photo = new File(String.format(extStorageDirectory+"/%d.jpg", timeStamp)); 
				if(photo.exists()) { 
					photo.delete(); 
					pictureTakenFlag = false;
					preview.camera.startPreview();
					Toast.makeText(CameraActivity.this, "Image deleted!", Toast.LENGTH_LONG).show();
				} 
			} 
		});	

		buttonGallery = (Button) findViewById(R.id.buttonGallery);
		buttonGallery.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
				startActivity(intent);
				preview.camera.startPreview();
			}
		});
		
		eButton = (Button)findViewById(R.id.effectsButton);
		eButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!eCheck){	
					  effectsView.setVisibility(View.VISIBLE);
					  Toast.makeText(CameraActivity.this, "EFFECTS", Toast.LENGTH_SHORT).show();					  
					  eCheck=true;
				  } else{
					  effectsView.setVisibility(View.GONE);
					  eCheck=false;
				  }
			}
		});
		exButton = (Button)findViewById(R.id.exposureButton);
		exButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!exCheck){	
					  expoView.setVisibility(View.VISIBLE);
					  Toast.makeText(CameraActivity.this, "EXPOSURE", Toast.LENGTH_SHORT).show();
					  exCheck=true;
				  } else{
					  expoView.setVisibility(View.GONE);
					  exCheck=false;
				  }
			}
		});
		zButton = (Button)findViewById(R.id.zoomButton);
		zButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!zCheck){	
					  zoomView.setVisibility(View.VISIBLE);
					  Toast.makeText(CameraActivity.this, "ZOOM", Toast.LENGTH_SHORT).show();
					  zCheck=true;
				  } else{
					  zoomView.setVisibility(View.GONE);
					  zCheck=false;
				  }
			}
		});
		wbButton = (Button)findViewById(R.id.whiteBalanceButton);
		wbButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				if(!wbCheck){			
					  whiteBalanceView.setVisibility(View.VISIBLE);
					  Toast.makeText(CameraActivity.this, "WHITE_BALANCE", Toast.LENGTH_SHORT).show();
					  wbCheck=true;				
				  } else{
					  whiteBalanceView.setVisibility(View.GONE);					  
					  wbCheck=false;
				  }
			}
		});
		smButton = (Button)findViewById(R.id.sceneModeButton);
		smButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				if(!smCheck){			
					  sceneModeView.setVisibility(View.VISIBLE);
					  Toast.makeText(CameraActivity.this, "Scene_mode", Toast.LENGTH_SHORT).show();
					  smCheck=true;				
				  } else{
					  sceneModeView.setVisibility(View.GONE);					  
					  smCheck=false;
				  }
			}
		});
		
		zoomIn = (ZoomButton)findViewById(R.id.zoomIn);
		zoomOut = (ZoomButton)findViewById(R.id.zoomOut);
		
		zoomIn.setOnClickListener(this);
		zoomOut.setOnClickListener(this);

		expoUp = (ZoomButton)findViewById(R.id.exposureUp);
		expoDown = (ZoomButton)findViewById(R.id.exposureDown);
		
		expoUp.setOnClickListener(this);
		expoDown.setOnClickListener(this);
		
		wbAuto = (Button)findViewById(R.id.auto);
		wbCloudyDaylight = (Button)findViewById(R.id.cloudyDaylight);
		wbDaylight = (Button)findViewById(R.id.daylight);
		wbFluroscent = (Button)findViewById(R.id.floroscent);
		wbIncandescent = (Button)findViewById(R.id.incandescent);
		
		wbAuto.setOnClickListener(this);
		wbCloudyDaylight.setOnClickListener(this);
		wbDaylight.setOnClickListener(this);
		wbFluroscent.setOnClickListener(this);
		wbIncandescent.setOnClickListener(this);
		
		eAqua = (Button)findViewById(R.id.aqua);
		eMono = (Button)findViewById(R.id.mono);
		eNegative = (Button)findViewById(R.id.negative);
		eNone = (Button)findViewById(R.id.none);
		eSepia = (Button)findViewById(R.id.sepia);
		eSolarize = (Button)findViewById(R.id.solarize);
		
		eAqua.setOnClickListener(this);
		eMono.setOnClickListener(this);
		eNegative.setOnClickListener(this);
		eNone.setOnClickListener(this);
		eSepia.setOnClickListener(this);
		eSolarize.setOnClickListener(this);
		
		

		Log.d(TAG, "onCreate'd");
	}
	
	private boolean isExternalStoragePresent() {

        boolean externalStorageAvailable = false;
        boolean externalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageAvailable = externalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            externalStorageAvailable = true;
            externalStorageWriteable = false;
        } else {
            externalStorageAvailable = externalStorageWriteable = false;
        }
        if (!((externalStorageAvailable) && (externalStorageWriteable))) {
            Toast.makeText(this, "SD card not present", Toast.LENGTH_LONG)
                    .show();
        }
        return (externalStorageAvailable) && (externalStorageWriteable);
    }


	@Override
	public void onResume() {
		super.onResume();
		if (goingOut) {
			new Preview(this);
			goingOut = false;
			pictureTakenFlag = false;
			buttonDelete.setClickable(false); 
		}
	}

	@Override
	public void onPause() {

		if (!goingOut) {
			preview.camera.stopPreview();
		}
		goingOut = true;
		super.onPause();
	}
	
	@Override
	  public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.options, menu);
	    return(super.onCreateOptionsMenu(menu));
	  }

	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
		  if (item.getItemId() == R.id.about) {
			  Toast.makeText(this, "This app has been developed by lokiMehra.\nFor details follow me on twitter @loki_mehra. :) ", Toast.LENGTH_LONG).show();
		  }
		  if(item.getItemId() == R.id.info){
			  showDialog(0);
		  }
		  if(item.getItemId() == R.id.exit){
			  System.exit(0);
		  }
		  
		  return(super.onOptionsItemSelected(item));
	  }
	    
	// Called when shutter is opened
	ShutterCallback shutterCallback = new ShutterCallback() { 
		public void onShutter() {
			pictureTakenFlag = true;
			Log.d(TAG, "onShutter'd");
		}
	};

	// Handles data for raw picture
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};

	// Handles data for jpeg picture
	PictureCallback jpegCallback = new PictureCallback() { 
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				// Write to SD Card
				outStream = new FileOutputStream(String.format(extStorageDirectory+"/%d.jpg", timeStamp)); 
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
			} catch (FileNotFoundException e) { // <10>
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};

	public void onClick(View v) {
		Camera.Parameters p = preview.camera.getParameters();
		if(v.getId() == R.id.auto){
			p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
		} else if(v.getId() == R.id.cloudyDaylight){
			p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
		}else if(v.getId() == R.id.daylight){
			p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
		}else if(v.getId() == R.id.floroscent){
			p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
		}else if(v.getId() == R.id.incandescent){
			p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
		}
		if(v.getId() == R.id.aqua){
			p.setColorEffect(Camera.Parameters.EFFECT_AQUA);
		}else if(v.getId() == R.id.mono){
			p.setColorEffect(Camera.Parameters.EFFECT_MONO);
		}else if(v.getId() == R.id.negative){
			p.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
		}else if(v.getId() == R.id.none){
			p.setColorEffect(Camera.Parameters.EFFECT_NONE);
		}else if(v.getId() == R.id.sepia){
			p.setColorEffect(Camera.Parameters.EFFECT_SEPIA);
		}else if(v.getId() == R.id.solarize){
			p.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
		}
		if(v.getId() == R.id.smAction){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_ACTION);
		}else if(v.getId() == R.id.smAuto){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_AUTO);
		}else if(v.getId() == R.id.smBeach){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_BEACH);
		}else if(v.getId() == R.id.smCandlelight){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_CANDLELIGHT);
		}else if(v.getId() == R.id.smFireworks){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_FIREWORKS);
		}else if(v.getId() == R.id.smLandscape){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_LANDSCAPE);
		}else if(v.getId() == R.id.smNight){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_NIGHT);
		}else if(v.getId() == R.id.smNightPortrait){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT);
		}else if(v.getId() == R.id.smParty){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_PARTY);
		}else if(v.getId() == R.id.smPortrait){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_PORTRAIT);
		}else if(v.getId() == R.id.smSnow){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_SNOW);
		}else if(v.getId() == R.id.smSport){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_SPORTS);
		}else if(v.getId() == R.id.smSteadyPhoto){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_STEADYPHOTO);
		}else if(v.getId() == R.id.smSunset){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_SUNSET);
		}else if(v.getId() == R.id.smTheatre){
			p.setColorEffect(Camera.Parameters.SCENE_MODE_THEATRE);
		}
		if(v.getId() == R.id.zoomIn){
	        if (p.isZoomSupported()) {
	        	int maxZoom = p.getMaxZoom();
	        	zoom += 5;
	        	if (zoom > maxZoom) {
	               zoom -= 5;
	        	}
	        	p.setZoom(zoom);
	        }
		}else if(v.getId() == R.id.zoomOut){
			if (p.isZoomSupported()) {
	           zoom -= 5;
	           if (zoom < 0 ) {
	               zoom += 5;
	           }
	           p.setZoom(zoom);
	        }
		}
		if(v.getId() == R.id.exposureUp){
			int maxExpo = p.getMaxExposureCompensation();
			expo += 3;
	           if (expo > (maxExpo*0.33)) {
	               expo -= 3;
	           }
	           p.setExposureCompensation(expo);
		}else if(v.getId() == R.id.exposureDown){
			int minExpo = p.getMinExposureCompensation();
			expo -= 3;
			if (expo < (minExpo*0.33)) {
				expo += 3;
			}
			p.setExposureCompensation(expo);
		}

		preview.camera.setParameters(p);
		try {
        	preview.camera.setPreviewDisplay(preview.getHolder());
        } catch (Exception e) { }

        preview.camera.startPreview();
	}

	public boolean onTouch(View v, MotionEvent me) {
		if(!pictureTakenFlag){
			if(me.getAction()==MotionEvent.ACTION_DOWN){
				if(!optionToggle){
					wbCheck=false;
					eCheck=false;
					exCheck=false;
					zCheck=false;
					
					optionView.setVisibility(View.VISIBLE);
					optionToggle=true;
				} else{
					optionView.setVisibility(View.GONE);
					optionToggle=false;
				}
			}
		}
		return false;
	}

	protected Dialog onCreateDialog(int id){
		return new AlertDialog.Builder(this)
        .setIcon(R.drawable.click)
        .setTitle("How to use")
        .setMessage("Take a snap using the camera button. To save and take a new picture click on 'new' button. " +
        		"To delete click on the delete button. Delete button is enabled only after you take a snap. " +
        		"You can even go to the Gallery using the gallery button. " +
        		"Tap the screen to toggle more options.\n\n" +
        		"White-balance \n" +
        		"	Incandescent\n" +
        		"	Flouroscent\n" +
        		"	Auto\n" +
        		"	Cloudy\n" +
        		"	Daylight\n\n" +
        		"Effects \n" +
        		"	Negative\n" +
        		"	None\n" +
        		"	Emphasis\n" +
        		"	Aqua\n" +
        		"	Mono\n" +
        		"	Sepia\n\n" +
        		"Zoom\n\n" +
        		"Exposure\n\n" +
        		"Scene Modes \n" +
        		"	Action\n" +
        		"	Auto\n" +
        		"	Beach\n" +
        		"	CandleLight\n" +
        		"	Fireworks\n" +
        		"	Landscape\n" +
        		"	night\n" +
        		"	NightPortrait\n" +
        		"	Party\n" +
        		"	Portrait\n" +
        		"	Snow\n" +
        		"	Sports\n" +
        		"	Steady\n" +
        		"	Sunset\n" +
        		"	Theatre")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .create();	
	}
}