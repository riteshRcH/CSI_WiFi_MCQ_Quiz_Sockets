package com.wifiMcqQuiz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class InstrActivity extends Activity 
{
	Button btnGetQsFromServer, btnStart, btnLoadConfig;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.we_activity_instr);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		getWindow().setBackgroundDrawableResource(R.drawable.bg);
		
		if(customTitleSupported)
		{
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
			TextView myTitleText = (TextView) findViewById(R.id.myTitle);
			myTitleText.setText("WiFi MCQ Quiz");
			//myTitleText.setTextSize(20);
			//myTitleText.setBackgroundColor(Color.rgb(220, 208, 255));
			//myTitleText.setTextColor(Color.rgb(0,0,139));
		}
		
		((TextView)findViewById(R.id.txtViewInstr)).setTextColor(Color.WHITE);
		
		btnLoadConfig = (Button)findViewById(R.id.btnLoadConfig);
		btnLoadConfig.setOnClickListener(new View.OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				getPerQTimeOutSecsFromServer();
			}
		});
		
		btnGetQsFromServer = (Button)findViewById(R.id.btnGetQsFromServer);
		btnGetQsFromServer.setOnClickListener(new View.OnClickListener() 
		{	
			@Override
			public void onClick(View v)
			{
				getQsXMLFileFromServer();
			}
		});
		
		btnStart = (Button)findViewById(R.id.BtnStartQs);
		btnStart.setOnClickListener(new View.OnClickListener() 
		{	
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent("com.wifiMcqQuiz.QuestionsActivity"));
				finish();
			}
		});
	}

	private void getPerQTimeOutSecsFromServer()
	{
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    
	    if(mWifi.isConnected())
	    {
	    	AsyncTask<Void, Void, Exception> bgTaskGetQsTimeOut = new AsyncTask<Void, Void, Exception>()
	    	{
	    		ProgressDialog progressDialog = null;
	    	    boolean receivedTimeOutValue = false;
	    	    				
	    	    @Override
	    	    protected void onPreExecute()
	    	    {
	    	    	progressDialog = new ProgressDialog(InstrActivity.this);
	    	    	progressDialog.setTitle("Processing ..");
	    	    	progressDialog.setMessage("Please wait.");
	    	    	progressDialog.setCancelable(false);
	    	    	progressDialog.setIndeterminate(true);
	    	    	progressDialog.show();
	    	    }
	    	    							
	    	    @Override
	    	    protected Exception doInBackground(Void ...params)
	    	    {
	    	    	try
	    	    	{
	    	    		MainActivity.oosToServer.writeObject("GETQSTIMEOUTSECS ");
				
	    	    		Object serverReply = MainActivity.oisFromServer.readObject();		//deserialize object
				
	    	    		if(serverReply instanceof String && ((String)serverReply).startsWith("GETQSTIMEOUTSECS "))
	    	    		{
	    	    			QuestionsActivity.timeOutPerQSecs = Integer.parseInt(((String)serverReply).replace("GETQSTIMEOUTSECS ", ""));
	    	    			receivedTimeOutValue = true;
	    	    		}else
	    	    			receivedTimeOutValue = false;
	    	    	}catch (Exception e) 
	    	    	{
	    	    		return e;
	    	    	}
					return null;
	    	    }
	    	    protected void onPostExecute(Exception exceptionOccured)
				{
					if(progressDialog!=null)
						progressDialog.dismiss();
					
					if(exceptionOccured==null)
					{
						if(receivedTimeOutValue)
						{
							btnLoadConfig.setEnabled(false);
							btnGetQsFromServer.setEnabled(true);
						}else
							Toast.makeText(getBaseContext(), "An Error occured loading Questions settings .. please re-try!", Toast.LENGTH_SHORT).show();
					}else
						Toast.makeText(getBaseContext(), "An Error occured while buzzing the server .. please re-try!\nException Details: "+exceptionOccured.getMessage(), Toast.LENGTH_SHORT).show();
				}
	    	};
	    	bgTaskGetQsTimeOut.execute();	
	    }
	}

	private void getQsXMLFileFromServer()			//returns true if successfully received and made Qs XML File else false
	{
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    
	    if(mWifi.isConnected())
	    {
	    	AsyncTask<Void, Void, Exception> bgTaskGetXMLQsFileFromServer = new AsyncTask<Void, Void, Exception>()
	    	{
	    	 	ProgressDialog progressDialog = null;
	    	    boolean receivedNExtractedQsJSONFileSuccess = false;
	    	       				
	    	    @Override
	    	    protected void onPreExecute()
	    	    {
	    	    	progressDialog = new ProgressDialog(InstrActivity.this);
	    	    	progressDialog.setTitle("Processing ..");
	    	    	progressDialog.setMessage("Please wait.");
	    	    	progressDialog.setCancelable(false);
	    	    	progressDialog.setIndeterminate(true);
	    	    	progressDialog.show();
	    	    }
	    	    	    							
	    	    @Override
	    	    protected Exception doInBackground(Void ...params)
	    	    {
	    	    	try
	    	    	{
	    	    		MainActivity.oosToServer.writeObject("GETQSFILE ");
				
	    	    		Object serverReply = MainActivity.oisFromServer.readObject();		//deserialize object

    	    			File zipFile;
    	    			FileOutputStream currentEventfos = new FileOutputStream(zipFile = new File(MainActivity.appSDDir ,"Questions.zip"));
    	    			currentEventfos.write((byte[])serverReply);
    	    			currentEventfos.close();
				
    	    			File unzippedDestinationDir = new File(MainActivity.appSDDir, "Questions");
    	    			if(!unzippedDestinationDir.exists())
    	    				unzippedDestinationDir.mkdirs();
    	    			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    	    			ZipEntry ze = null;
    	    			FileOutputStream fout = null;
    	    			while((ze = zis.getNextEntry())!=null)
    	    			{
    	    				fout = new FileOutputStream(new File(unzippedDestinationDir, ze.getName()));
    	    				for(int c=zis.read();c!=-1;c=zis.read())
    	    					fout.write(c);
    	    				fout.close();
    	    				zis.closeEntry();
    	    			}
    	    			zis.close();
				
    	    			receivedNExtractedQsJSONFileSuccess = true;
	    	    	}catch (Exception e) 
	    	    	{
	    	    		return e;
	    	    	}
					return null;
	    	    }
	    	    
	    	    @Override
	    	    protected void onPostExecute(Exception exceptionOccured)
				{
					if(progressDialog!=null)
						progressDialog.dismiss();
					
					if(exceptionOccured==null)
					{
						if(receivedNExtractedQsJSONFileSuccess)
						{
							Toast.makeText(getBaseContext(), "Questions Loaded Successfully!", Toast.LENGTH_SHORT).show();
							btnGetQsFromServer.setEnabled(false);
							btnStart.setEnabled(true);
						}else
							Toast.makeText(getBaseContext(), "Unable to load Questions .. please try again!", Toast.LENGTH_LONG).show();
					}else
						Toast.makeText(getBaseContext(), "Exception occured while receiving questions .. please retry.\nException Details: "+exceptionOccured.getMessage(), Toast.LENGTH_LONG).show();
				}
	    	};
	    	bgTaskGetXMLQsFileFromServer.execute();
	    }
	}
}
