package com.wifiMcqQuiz;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity 
{
	boolean serverConnEstablished = false;
	static String teamID = "", serverIP = "";
	static ObjectOutputStream oosToServer;
	static ObjectInputStream oisFromServer;
	static Socket clientSocket;
	
	EditText editTextServerIP;
	Button btnReg;
	
	static File appSDDir = new File(Environment.getExternalStorageDirectory(), ".WiFiMCQQuiz");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.we_activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		getWindow().setBackgroundDrawableResource(R.drawable.bg);
		
		if(customTitleSupported)
		{
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
			TextView myTitleText = (TextView) findViewById(R.id.myTitle);
			myTitleText.setText("WiFi MCQ Quiz!");
			//myTitleText.setTextSize(20);
			//myTitleText.setBackgroundColor(Color.rgb(220, 208, 255));
			//myTitleText.setTextColor(Color.rgb(0,0,139));
		}
		
		if(!appSDDir.exists())
			appSDDir.mkdirs();
		
		editTextServerIP = (EditText)findViewById(R.id.editTextServerIP);
		InputFilter IpAddrRegexfilter = new InputFilter()
		    {
		        @Override
		        public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) 
		        {
		            if (end > start) 
		            {
		                String destTxt = dest.toString();
		                String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
		                if (!resultingTxt.matches ("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) 
		                    return "";
		                else 
		                {
		                    String[] splits = resultingTxt.split("\\.");
		                    for (int i=0; i<splits.length; i++) 
		                    {
		                        if (Integer.valueOf(splits[i]) > 255) 
		                            return "";
		                    }
		                }
		            }
		            return null;
		        }

		    };
		    editTextServerIP.setFilters(new InputFilter[]{IpAddrRegexfilter});
		    
		    final Button btnEstablishConn = (Button)findViewById(R.id.btnEstablishConn);
		    btnEstablishConn.setOnClickListener(new View.OnClickListener()
		    {
				@Override
				public void onClick(View v) 
				{
					ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				    if (mWifi.isConnected())
				    {
				    	AsyncTask<Void, Void, Exception> bgTaskEstablishConnToServer = new AsyncTask<Void, Void, Exception>()
				    	{
				    		ProgressDialog progressDialog = null;
				    	    	       				
				    	    @Override
				    	    protected void onPreExecute()
				    	    {
				    	    	progressDialog = new ProgressDialog(MainActivity.this);
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
				    	    		clientSocket = new Socket();
				    	    		clientSocket.connect(new InetSocketAddress(serverIP = editTextServerIP.getText().toString().trim(), 65535), 2000);	//2secs connection timeout
				    	    		clientSocket.setSoTimeout(5000);			//5secs read timeout
				    	    		oosToServer = new ObjectOutputStream(clientSocket.getOutputStream());
				    	    		oisFromServer = new ObjectInputStream(clientSocket.getInputStream());
				    	    		oosToServer.writeObject("Client: Hello!");
				    	    		oosToServer.flush();
				    	    		String serverReply = (String)oisFromServer.readObject();
				    	    		if(serverReply!=null)
				    	    			serverConnEstablished = serverReply.equals("Server: Hello!")?true:false;
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
									if(serverConnEstablished)
									{
										Toast.makeText(getBaseContext(), "Server-Client Handshake successfull!", Toast.LENGTH_SHORT).show();
										btnEstablishConn.setEnabled(false);
			    	    				editTextServerIP.setEnabled(false);
			    	    				btnReg.setEnabled(true);
									}else
										Toast.makeText(getBaseContext(), "An Error occured establishing connection to server .. please re-try!", Toast.LENGTH_SHORT).show();
								}else if(exceptionOccured instanceof UnknownHostException)
									Toast.makeText(getBaseContext(), "Cant find server .. contact your teacher!", Toast.LENGTH_SHORT).show();
								else if(exceptionOccured instanceof IOException)
									Toast.makeText(getBaseContext(), "Exception occured while connecting .. please retry.\nException Details: "+exceptionOccured.getMessage(), Toast.LENGTH_SHORT).show();
								else
									Toast.makeText(getBaseContext(), "Exception occured while connecting .. please retry.\nException Details: "+exceptionOccured.getMessage(), Toast.LENGTH_LONG).show();
							}
				    	};
				    	bgTaskEstablishConnToServer.execute();
				    }else
				    	Toast.makeText(getBaseContext(), "Please connect to your campus WiFi network!", Toast.LENGTH_SHORT).show();
				}
			});
		    
		    final EditText editTextPW = (EditText)findViewById(R.id.editTextPW);
		    final EditText editTextTeamName = (EditText)findViewById(R.id.EditTextTeamName);
		    btnReg = (Button)findViewById(R.id.btnRegTeam);
		    final Button btnStart = (Button)findViewById(R.id.btnStart);
		    btnReg.setOnClickListener(new View.OnClickListener()
		    {
				@Override
				public void onClick(View v)
				{
					if(editTextTeamName.getText().toString().trim().equals(""))
						Toast.makeText(getBaseContext(), "Please enter the team ID provided to you.", Toast.LENGTH_SHORT).show();
					else if(editTextTeamName.getText().toString().trim().startsWith("TEAM_") && editTextTeamName.getText().toString().trim().length()>"TEAM_".length())
						teamID = editTextTeamName.getText().toString();
					else
						Toast.makeText(getBaseContext(), "Invalid team name", Toast.LENGTH_SHORT).show();
					
					if(!teamID.trim().equals(""))
					{
						if(editTextPW.getText().toString().trim().equals(""))
							Toast.makeText(getBaseContext(), "Please enter password", Toast.LENGTH_SHORT).show();
						else
						{
							ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
						    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
						    
						    if(mWifi.isConnected())
						    {
						    	AsyncTask<Void, Void, Exception> bgTaskRegTeam = new AsyncTask<Void, Void, Exception>()
								{
						    		ProgressDialog progressDialog = null;
								    	    	       				
								    @Override
								    protected void onPreExecute()
								    {
								    	progressDialog = new ProgressDialog(MainActivity.this);
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
								    		oosToServer.writeObject("REG "+editTextTeamName.getText().toString());
								    		String serverReply = (String)oisFromServer.readObject();
								    		if(serverReply!=null)
								    			if(serverReply.equals("TeamID OK!"))
								    			{
								    				oosToServer.writeObject(editTextPW.getText().toString());		//send password
								    				serverReply = (String)oisFromServer.readObject();
								    				if(serverReply!=null)
								    					teamID = serverReply.equals("REG SUCCESS!")?editTextTeamName.getText().toString():"";
								    			}else if(serverReply.equals("TeamID already registered!"))
								    				throw new Exception("TeamID already registered!");
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
											if(teamID.equals(""))
												Toast.makeText(getBaseContext(), "Unable to register your team name .. make sure team name and password are typed correctly!", Toast.LENGTH_SHORT).show();
											else
											{
												Toast.makeText(getBaseContext(), "Team Name: "+teamID+" successfully registered with Server!", Toast.LENGTH_SHORT).show();
							    				btnReg.setEnabled(false);
							    				editTextPW.setEnabled(false);
							    				editTextTeamName.setEnabled(false);
							    				btnStart.setEnabled(true);
											}
										}else
											Toast.makeText(getBaseContext(), "Exception occured while registering .. please retry.\nException Details: "+exceptionOccured.getMessage(), Toast.LENGTH_SHORT).show();
									}
								};
							bgTaskRegTeam.execute();
						    }
						}
					}
				}
			});
		    btnStart.setOnClickListener(new View.OnClickListener()
		    {
				@Override
				public void onClick(View v)
				{
					startActivity(new Intent("com.wifiMcqQuiz.InstrActivity"));
				}
			});
	}
	
	private void CreateMenu(Menu menu)
	{
		menu.add(Menu.NONE, 0, 0, "About App");
		//MenuItem aboutAppMnuItem;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return menuChoice(item);
	}

	private boolean menuChoice(MenuItem item) 
	{
		switch(item.getItemId())
		{
			case 0:		//About App!
				startActivity(new Intent("com.wifiMcqQuiz.CreditsActivity"));
			return true;
		}
		return false;
	}
}
