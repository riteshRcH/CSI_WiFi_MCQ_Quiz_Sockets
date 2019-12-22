package com.wifiMcqQuiz;

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
