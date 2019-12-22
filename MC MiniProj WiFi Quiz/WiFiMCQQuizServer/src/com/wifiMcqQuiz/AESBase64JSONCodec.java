package com.wifiMcqQuiz;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AESBase64JSONCodec
{
	static String key = "c00perH@wkRch123"; // 128 bit key
	
	public static void aesEncryptBase64JSONEncode(String sourcePlainTextFile, String destinationCipherTextFile)
	{
		try
		{
			String jsonString = new String(""), s = "";
			
			BufferedReader br = new BufferedReader(new FileReader(sourcePlainTextFile));
			while((s = br.readLine())!=null)
				jsonString += (s + System.getProperty("line.separator"));
			br.close();

	         // Create key and cipher
	         Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
	         Cipher cipher = Cipher.getInstance("AES");
	
	         // encrypt the text
	         cipher.init(Cipher.ENCRYPT_MODE, aesKey);
	         FileWriter fw = new FileWriter(destinationCipherTextFile);
	         fw.write(new String(Base64.encodeBase64(cipher.doFinal(jsonString.getBytes()))));
	         fw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			}	
	}
	public static String aesDecryptBase64JSONDecode(String sourceCipherTextFile)
	{
		try
		{
			String fileContents = new String(""), s = "";
			
			BufferedReader br = new BufferedReader(new FileReader(sourceCipherTextFile));
			while((s = br.readLine())!=null)
				fileContents += (s + System.getProperty("line.separator"));
			br.close();
	
	         // Create key and cipher
	         Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
	         Cipher cipher = Cipher.getInstance("AES");
	
	         // encrypt the text
	         cipher.init(Cipher.DECRYPT_MODE, aesKey);
	         return new String(cipher.doFinal(Base64.decodeBase64(fileContents.getBytes())));
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	/*public static void main(String args[])
	{
		//aesEncryptBase64JSONEncode("C:\\Users\\c00per\\Desktop\\Test\\TestJSON\\QuestionsQsJSON.json", "C:\\Users\\c00per\\Desktop\\Test\\TestJSON\\QuestionsQsJSONFinal");
		System.out.println(aesDecryptBase64JSONDecode("C:\\Users\\c00per\\Desktop\\Test\\TestJSON\\QuestionsQsJSONFinal"));
		//Final = AES Encrypted + Base64 i.e to be sent to clients
	}*/
}
