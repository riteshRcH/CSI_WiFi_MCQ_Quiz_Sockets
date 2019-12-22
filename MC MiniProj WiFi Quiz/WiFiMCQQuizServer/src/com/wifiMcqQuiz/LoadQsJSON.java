package com.wifiMcqQuiz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

class LoadQsJSON
{
	class Question
	{
		public Question(Boolean isImgQuestion, String imgFileName, String question, String option1, String option2, String option3, String option4, String correctAns)
		{
			super();
			this.isImgQuestion = isImgQuestion;
			this.imgFileName = imgFileName;
			this.question = question;
			this.option1 = option1;
			this.option2 = option2;
			this.option3 = option3;
			this.option4 = option4;
			this.correctAns = correctAns;
		}
		
		Boolean isImgQuestion = false;
		String imgFileName, question, option1, option2, option3, option4, correctAns;
	}
	static ArrayList<Question> qsList = new ArrayList<Question>();
	
	void parseJSON(File jsonFile)throws Exception
	{
		String jsonString = new String(""), s = "";
		
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		while((s = br.readLine())!=null)
			jsonString += (s + System.getProperty("line.separator"));
		br.close();
		
		JSONObject jsonObj = new JSONObject(jsonString), element;
		JSONArray jsonArr = jsonObj.getJSONArray("Questions");
		for(int i=0;i<jsonArr.length();i++)
		{
			element = jsonArr.getJSONObject(i);
			qsList.add(new Question(element.getString("IsImgQuestion").equals("true"), element.getString("ImgFileName"), element.getString("Q"), element.getString("Option1"), element.getString("Option2"), element.getString("Option3"), element.getString("Option4"), element.getString("CorrectAns")));
		}
	}
}