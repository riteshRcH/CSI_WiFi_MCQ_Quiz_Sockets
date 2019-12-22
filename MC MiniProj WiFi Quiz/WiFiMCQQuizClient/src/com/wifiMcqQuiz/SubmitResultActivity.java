package com.wifiMcqQuiz;

import java.io.IOException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SubmitResultActivity extends Activity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.we_activity_submit_result);
		
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
		
		final Button btnSubmitResult = (Button)findViewById(R.id.btnSubmitResult);
		btnSubmitResult.setOnClickListener(new View.OnClickListener()
		{	
			@Override
			public void onClick(View btn)
			{
				// TODO Auto-generated method stub
				ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			    
			    if(mWifi.isConnected())
			    {
			    	AsyncTask<Void, Void, Exception> bgTaskSubmitScoreToServer = new AsyncTask<Void, Void, Exception>()
			    	{
			    		ProgressDialog progressDialog = null;
			    		boolean submitedScoreSuccessfully = false;
							    	    	       				
			    		@Override
			    		protected void onPreExecute()
			    		{
			    			progressDialog = new ProgressDialog(SubmitResultActivity.this);
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
			    				MainActivity.oosToServer.writeObject("RESULT "+MainActivity.teamID+" out of "+QuestionsActivity.finishedQsID.size()+": "+QuestionsActivity.correctAnsCnt);
			    				
			    				QuestionsActivity.finishedQsID.clear();
			    				QuestionsActivity.correctAnsCnt = 0;
			    				
			    				String serverReply = (String)MainActivity.oisFromServer.readObject();
			    				if(serverReply!=null)
			    						submitedScoreSuccessfully = serverReply.equals(MainActivity.teamID+" RESULT ENTRY Success!")?true:false;
			    								
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
								if(submitedScoreSuccessfully)
								{
									Toast.makeText(getBaseContext(), "Score of Team ID: "+MainActivity.teamID+" successfully entered at Server!", Toast.LENGTH_LONG).show();
		    						btnSubmitResult.setEnabled(false);
		    						try
		    						{
										MainActivity.oisFromServer.close();
										MainActivity.clientSocket.close();
			    						MainActivity.oosToServer.close();
									}catch (IOException e)
									{
										
									}
		    						finish();
		    						startActivity(new Intent("com.wifiMcqQuiz.CreditsActivity"));
								}else
									Toast.makeText(getBaseContext(), "Unable to make your score entry .. please try again!", Toast.LENGTH_SHORT).show();
							}else
								Toast.makeText(getBaseContext(), "Exception occured while connecting .. please retry.\nException Details: "+exceptionOccured.getMessage(), Toast.LENGTH_SHORT).show();
						}
			    	};
			    	bgTaskSubmitScoreToServer.execute();
			    }
			}
		});
	}
}