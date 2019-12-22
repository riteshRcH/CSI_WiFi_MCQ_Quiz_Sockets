package com.wifiMcqQuiz;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class QuestionsActivity extends Activity implements OnClickListener
{
	static LinkedHashMap<Integer, Boolean> finishedQsID = new LinkedHashMap<Integer, Boolean>();	//Q ID and whether user got the ans correct or not
	Random randomNumGenerator = new Random();
	static int correctAnsCnt, timeOutPerQSecs = 30;
	int currentQID, questionsCnt = 0;
	Question currentQ;
	String correctAns ="", selectedAns = "";
	
	RadioGroup RadioGrpAns;
	TextView txtViewQuestion, txtViewShowTimeLeft;
	RadioButton radioBtn1, radioBtn2, radioBtn3, radioBtn4;
	Button btnNext;
	
	ArrayList<Question> allQuestions = new ArrayList<Question>();
	
	CountDownTimer countdownTimerDefault30sec = new CountDownTimer(timeOutPerQSecs*1000, 1000)
	{
		@Override
		public void onTick(long millisecsUntilFinish) 
		{
			txtViewShowTimeLeft.setText("Time Left: "+(millisecsUntilFinish/1000));			
		}
		
		@Override
		public void onFinish() 
		{
			if(radioBtn1.isChecked() || radioBtn2.isChecked() || radioBtn3.isChecked() || radioBtn4.isChecked())
			{
				selectedAns = ((RadioButton)findViewById(RadioGrpAns.getCheckedRadioButtonId())).getText().toString();
				if(selectedAns.equals(correctAns))
				{
					correctAnsCnt++;
					finishedQsID.put(currentQID, true);
				}
			}
			Toast.makeText(getBaseContext(), "Times up for Q"+finishedQsID.size()+"!", Toast.LENGTH_SHORT).show();
			RadioGrpAns.clearCheck();
			setNextRandomQuestion();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.we_activity_questions);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		getWindow().setBackgroundDrawableResource(R.drawable.bg);
		
		if(customTitleSupported)
		{
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
			TextView myTitleText = (TextView) findViewById(R.id.myTitle);
			myTitleText.setText("Wireless Elims");
			//myTitleText.setTextSize(20);
			//myTitleText.setBackgroundColor(Color.rgb(220, 208, 255));
			//myTitleText.setTextColor(Color.rgb(0,0,139));
		}
		
		loadAllQuestionsFromJSONFile();
		
		getViews();
		setNextRandomQuestion();
		
		radioBtn1.setOnClickListener(this);
		radioBtn2.setOnClickListener(this);
		radioBtn3.setOnClickListener(this);
		radioBtn4.setOnClickListener(this);
		
		btnNext.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				//if(((RadioButton)findViewById(RadioGrpAns.getCheckedRadioButtonId())).getText().toString().equals(correctAns))
				if(selectedAns.equals(correctAns))
				{
					correctAnsCnt++;
					finishedQsID.put(currentQID, true);
				}
				RadioGrpAns.clearCheck();
				setNextRandomQuestion();
			}
		});
		
	}
	void getViews()
	{
		txtViewQuestion = (TextView)findViewById(R.id.txtViewQuestion);
		txtViewShowTimeLeft = (TextView)findViewById(R.id.txtViewShowTimeLeft);
		
		RadioGrpAns = (RadioGroup)findViewById(R.id.RadioGrpAns);
		
		radioBtn1 = (RadioButton)findViewById(R.id.radioBtn1);
		radioBtn2 = (RadioButton)findViewById(R.id.radioBtn2);
		radioBtn3 = (RadioButton)findViewById(R.id.radioBtn3);
		radioBtn4 = (RadioButton)findViewById(R.id.radioBtn4);
		
		btnNext = (Button)findViewById(R.id.btnNext);
	}
	void setNextRandomQuestion()
	{
		if(finishedQsID.size()!=questionsCnt)				//change max limit when no(Qs) change, earlier 45 then 10 then wrt num(Qs) in XML File
		{
			while(finishedQsID.containsKey(currentQID = randomNumGenerator.nextInt(questionsCnt)));	//change range when Qs changed
			finishedQsID.put(currentQID, false);
			currentQ = allQuestions.get(currentQID);
			
			ImageView imgViewQImg = ((ImageView)findViewById(R.id.imgViewQImg));
			txtViewQuestion.setText("Q "+finishedQsID.size()+") "+currentQ.question.trim());
			if(currentQ.isImgQuestion)
			{
				imgViewQImg.setVisibility(View.VISIBLE);
				imgViewQImg.setImageBitmap(BitmapFactory.decodeFile(MainActivity.appSDDir.getAbsolutePath()+File.separator+"Questions"+File.separator+currentQ.imgFileName));
			}else
				imgViewQImg.setVisibility(View.GONE);
			
			if(currentQ.option1.equals("null"))
				radioBtn1.setVisibility(View.GONE);
			else
			{
				radioBtn1.setVisibility(View.VISIBLE);
				radioBtn1.setText(currentQ.option1);
			}
			
			if(currentQ.option2.equals("null"))
				radioBtn2.setVisibility(View.GONE);
			else
			{
				radioBtn2.setVisibility(View.VISIBLE);
				radioBtn2.setText(currentQ.option2);
			}
			
			if(currentQ.option3.equals("null"))
				radioBtn3.setVisibility(View.GONE);
			else
			{
				radioBtn3.setVisibility(View.VISIBLE);
				radioBtn3.setText(currentQ.option3);
			}
			
			if(currentQ.option4.equals("null"))
				radioBtn4.setVisibility(View.GONE);
			else
			{
				radioBtn4.setVisibility(View.VISIBLE);
				radioBtn4.setText(currentQ.option4);
			}
			
			correctAns = currentQ.correctAns;
			countdownTimerDefault30sec.start();
		}else
		{
			Toast.makeText(getBaseContext(), "All Qs are up", Toast.LENGTH_SHORT).show();
			countdownTimerDefault30sec.cancel();
			finish();
			startActivity(new Intent("com.wifiMcqQuiz.SubmitResultActivity"));
		}
	}
	private void loadAllQuestionsFromJSONFile()
	{
		File unzippedDirContentsQsJSONEncryptedFile = new File(MainActivity.appSDDir, "Questions").listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String fileName)
			{
				return fileName.endsWith("Final");
			}
		})[0];	//1st index => there is going to be 1 only JSON Encrypted File
        try
        {
        	String jsonString = AESBase64JSONCodec.aesDecryptBase64JSONDecode(unzippedDirContentsQsJSONEncryptedFile.getAbsolutePath());
        	JSONObject jsonObject = new JSONObject(jsonString);
        	JSONArray jsonArr = jsonObject.getJSONArray("Questions");
        	for(int i=0;i<jsonArr.length();i++)
        	{
        		JSONObject element = jsonArr.getJSONObject(i);
        		allQuestions.add(new Question(element.getString("IsImgQuestion").equals("true"), element.getString("ImgFileName"), element.getString("Q"), element.getString("Option1"), element.getString("Option2"), element.getString("Option3"), element.getString("Option4"), element.getString("CorrectAns")));
        	}
        	questionsCnt = jsonArr.length();
		}catch(Exception e)
		{
			Toast.makeText(getBaseContext(), "Exception occured! Retry", Toast.LENGTH_LONG).show();
		}
	}
	@Override
	public void onClick(View selectedRadioBtn)
	{
		selectedAns = (radioBtn1.isChecked() || radioBtn2.isChecked() || radioBtn3.isChecked() || radioBtn4.isChecked())?((RadioButton)findViewById(RadioGrpAns.getCheckedRadioButtonId())).getText().toString():"";
	}
}