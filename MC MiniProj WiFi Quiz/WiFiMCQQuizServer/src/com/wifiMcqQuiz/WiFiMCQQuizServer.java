package com.wifiMcqQuiz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.wifiMcqQuiz.LoadQsJSON.Question;

public class WiFiMCQQuizServer extends JFrame
{
	private static final long serialVersionUID = -6871454859658288454L;
	final JButton jbtnStartStopService, jbtnGetTeamRankin, jbtnDelTeam, jbtnWrite2FileNow, jbtnLoadFromFileNow, jbtnTruncateFile, jbtnDelFile, jbtnShowServerConfig, jbtnNavigateQsFirst, jbtnNavigateQsLast, jbtnNavigateQsNext, jbtnNavigateQsPrev;
	JCheckBox jchkboxAppend, jchkboxShowPassword;
	JTextField jtxtDelTeam, jtxtfIsImgQuestion, jtxtfImgFileName, jtxtfQuestion, jtxtfOption1, jtxtfOption2, jtxtfOption3, jtxtfOption4, jtxtfCorrectAns;
	JPasswordField jpwFieldClientRegPw;
	
	ServiceThread t = new ServiceThread();
	LinkedHashMap<String, Integer> teamIDs;
	//result out of 50
	
	String filename = "Score List.txt", clientRegisteringPassword = "";
	boolean fileAppendMode = false;
	
	JPanel jpRuntimeServerHandlingOperations = new JPanel(new FlowLayout(FlowLayout.CENTER)), jpPrepQsFile = new JPanel(new FlowLayout(FlowLayout.CENTER)), jpEventLogs = new JPanel(new BorderLayout()), jpJSONQsFile = new JPanel(new GridLayout(0, 2));
	JTabbedPane jtpOperations = new JTabbedPane(), jtpeventLogs = new JTabbedPane();
	
	JTextArea jtxtareaRegEventLog, jtxtareaScoreReceivedEventLog, jtxtareaHandshakeEventLog, jtxtareaQsFileRequestEventLog, jtxtareaCreateZipOfQsImgsLog;
	
	final JButton jbtnChooseJSONQsImgsDir, jbtnEncryptJSONNCreateZip, jbtnQsFileDone, jbtnImportFromExcelFile, jbtnChooseJSONQsFileToBrowse;
	
	JLabel jlblIsImgQJSONKey = new JLabel("\"IsImgQuestion\": ", SwingConstants.RIGHT), jlblImgFileNameJSONKey = new JLabel("\"ImgFileName\": ", SwingConstants.RIGHT), jlblQsJSONKey = new JLabel("\"Q\": ", SwingConstants.RIGHT), jlblOption1JSONKey = new JLabel("\"Option1\": ", SwingConstants.RIGHT), jlblOption2JSONKey = new JLabel("\"Option2\": ", SwingConstants.RIGHT), jlblOption3JSONKey = new JLabel("\"Option3\": ", SwingConstants.RIGHT), jlblOption4JSONKey = new JLabel("\"Option4\": ", SwingConstants.RIGHT), jlblCorrectAnsJSONKey = new JLabel("\"CorrectAns\": ", SwingConstants.RIGHT), jlblCurrentQCntBrowsing = new JLabel();
	
	int timeOutPerQSecs = 30;
	
	File zipFileToSend = null;
	File currentEventAppDir = null;
	byte[] zipFileContents;
	
	WiFiMCQQuizServer()
	{
		super.setLayout(new BorderLayout());
		this.setTitle("WiFi MCQ Quiz Server! v1.0");
		this.setVisible(true);
		this.setSize(1200, 512);
		this.setLocationRelativeTo(null);														//center locn
		//this.setIconImage(new ImageIcon("we_csi_logo.png").getImage());
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		jpRuntimeServerHandlingOperations.setSize(200, 500);
		
		jpRuntimeServerHandlingOperations.add(jbtnStartStopService = new JButton("Start Server!"));
		jpRuntimeServerHandlingOperations.add(jbtnGetTeamRankin = new JButton("Get Team Ranking!"));
		jpRuntimeServerHandlingOperations.add(jtxtDelTeam = new JTextField(22));
		jpRuntimeServerHandlingOperations.add(jbtnDelTeam = new JButton("Remove Team"));
		jpRuntimeServerHandlingOperations.add(jbtnWrite2FileNow = new JButton("Save team Ranking to file now!"));
		jpRuntimeServerHandlingOperations.add(jbtnLoadFromFileNow = new JButton("Load Team Ranking from file now!"));
		jpRuntimeServerHandlingOperations.add(jbtnTruncateFile = new JButton("Truncate File"));
		jpRuntimeServerHandlingOperations.add(jbtnDelFile = new JButton("Delete File"));
		jpRuntimeServerHandlingOperations.add(jchkboxAppend = new JCheckBox("Append Mode"));
		jpRuntimeServerHandlingOperations.add(jbtnShowServerConfig = new JButton("View Server Config"));
		
		JPanel temp;
		jtpeventLogs.add("HandshakeEventLog", new JScrollPane((temp = new JPanel(new FlowLayout(FlowLayout.LEFT)))));
		temp.add(jtxtareaHandshakeEventLog = new JTextArea(32, 100));
		jtxtareaHandshakeEventLog.setEditable(false);
		jtxtareaHandshakeEventLog.setLineWrap(true);
		jtpeventLogs.add("RegEventLog", new JScrollPane(temp = new JPanel(new FlowLayout(FlowLayout.LEFT))));
		temp.add(jtxtareaRegEventLog = new JTextArea(32, 100));
		jtxtareaRegEventLog.setEditable(false);
		jtxtareaRegEventLog.setLineWrap(true);
		jtpeventLogs.add("ScoreReceivedEventLog", new JScrollPane(temp = new JPanel(new FlowLayout(FlowLayout.LEFT))));
		temp.add(jtxtareaScoreReceivedEventLog = new JTextArea(32, 100));
		jtxtareaScoreReceivedEventLog.setEditable(false);
		jtxtareaScoreReceivedEventLog.setLineWrap(true);
		jtpeventLogs.add("QsFileRequestEventLog", new JScrollPane(temp = new JPanel(new FlowLayout(FlowLayout.LEFT))));
		temp.add(jtxtareaQsFileRequestEventLog = new JTextArea(32, 100));
		jtxtareaQsFileRequestEventLog.setEditable(false);
		jtxtareaQsFileRequestEventLog.setLineWrap(true);
		jpEventLogs.add(jtpeventLogs, BorderLayout.CENTER);
		
		jpJSONQsFile.add(jlblIsImgQJSONKey);		jpJSONQsFile.add(jtxtfIsImgQuestion = new JTextField());	jtxtfIsImgQuestion.setEditable(false);
		jpJSONQsFile.add(jlblImgFileNameJSONKey);	jpJSONQsFile.add(jtxtfImgFileName = new JTextField());		jtxtfImgFileName.setEditable(false);
		jpJSONQsFile.add(jlblQsJSONKey);			jpJSONQsFile.add(jtxtfQuestion = new JTextField());			jtxtfQuestion.setEditable(false);
		jpJSONQsFile.add(jlblOption1JSONKey);		jpJSONQsFile.add(jtxtfOption1 = new JTextField());			jtxtfOption1.setEditable(false);
		jpJSONQsFile.add(jlblOption2JSONKey);		jpJSONQsFile.add(jtxtfOption2 = new JTextField());			jtxtfOption2.setEditable(false);
		jpJSONQsFile.add(jlblOption3JSONKey);		jpJSONQsFile.add(jtxtfOption3 = new JTextField());			jtxtfOption3.setEditable(false);
		jpJSONQsFile.add(jlblOption4JSONKey);		jpJSONQsFile.add(jtxtfOption4 = new JTextField());			jtxtfOption4.setEditable(false);
		jpJSONQsFile.add(jlblCorrectAnsJSONKey);	jpJSONQsFile.add(jtxtfCorrectAns = new JTextField());		jtxtfCorrectAns.setEditable(false);
		jpJSONQsFile.add(jbtnChooseJSONQsFileToBrowse = new JButton("Choose JSON File"));
		jpJSONQsFile.add(jbtnImportFromExcelFile = new JButton("Import: Excel File Qs -> JSON"));
		jpJSONQsFile.add(jbtnNavigateQsFirst = new JButton("First"));				jbtnNavigateQsFirst.setEnabled(false);
		jpJSONQsFile.add(jbtnNavigateQsLast = new JButton("Last"));					jbtnNavigateQsLast.setEnabled(false);
		jpJSONQsFile.add(jbtnNavigateQsPrev = new JButton("Prev"));					jbtnNavigateQsPrev.setEnabled(false);
		jpJSONQsFile.add(jbtnNavigateQsNext = new JButton("Next"));					jbtnNavigateQsNext.setEnabled(false);
		jpJSONQsFile.add(jlblCurrentQCntBrowsing);
		
		JPanel tempPanel = new JPanel(new GridLayout(0, 3, 4, 4));
		tempPanel.add(new JLabel("Enter client registering password: "));
		tempPanel.add(jpwFieldClientRegPw = new JPasswordField(10));
		jpwFieldClientRegPw.setEchoChar('*');
		tempPanel.add(jchkboxShowPassword = new JCheckBox("Show password"));
		tempPanel.add(jbtnChooseJSONQsImgsDir = new JButton("Choose dir of Qs JSON File + Images"));
		tempPanel.add(jbtnEncryptJSONNCreateZip = new JButton("Create Zip File of selected dir = JSON Questions File + Images"));
		tempPanel.add(jbtnQsFileDone = new JButton("Done!"));
		jpPrepQsFile.add(tempPanel);
		jpPrepQsFile.add(jtxtareaCreateZipOfQsImgsLog = new JTextArea(32, 100));
		jtxtareaCreateZipOfQsImgsLog.setEditable(false);
		jtxtareaCreateZipOfQsImgsLog.setLineWrap(true);
		
		jtpOperations.add("Server, DB Handling", jpRuntimeServerHandlingOperations);
		jtpOperations.add("Event Logs", jpEventLogs);
		jtpOperations.add("Prep Qs JSON File for Sending", jpPrepQsFile);
		jtpOperations.add("Browse JSON Qs File", jpJSONQsFile);
		
		this.add(jtpOperations, BorderLayout.CENTER);
		
		ButtonListener bl = new ButtonListener();
		jbtnStartStopService.addActionListener(bl);
		jbtnGetTeamRankin.addActionListener(bl);
		jbtnDelTeam.addActionListener(bl);
		jbtnWrite2FileNow.addActionListener(bl);
		jbtnLoadFromFileNow.addActionListener(bl);
		jbtnTruncateFile.addActionListener(bl);
		jbtnDelFile.addActionListener(bl);
		jbtnShowServerConfig.addActionListener(bl);
		
		jbtnChooseJSONQsImgsDir.addActionListener(bl);
		jbtnEncryptJSONNCreateZip.addActionListener(bl);
		jbtnQsFileDone.addActionListener(bl);
		jbtnImportFromExcelFile.addActionListener(bl);
		jbtnChooseJSONQsFileToBrowse.addActionListener(bl);
		jbtnNavigateQsFirst.addActionListener(bl);
		jbtnNavigateQsPrev.addActionListener(bl);
		jbtnNavigateQsNext.addActionListener(bl);
		jbtnNavigateQsLast.addActionListener(bl);
		
		jchkboxAppend.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ie)
			{
				fileAppendMode = jchkboxAppend.isSelected();
			}
		});
		
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				if(JOptionPane.showConfirmDialog(WiFiMCQQuizServer.this, "Are you you want to exit? ", "Exit confirmation?", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
				{
					System.out.println("Client Connected Sockets were: "+(((t.clientConnectedSockets.size())==0)?"none":t.clientConnectedSockets));
					t.serverRunning = false;
					try
					{
						if(t.serverSocket!=null)
							t.serverSocket.close();
					}catch(Exception e)
					{
						e.printStackTrace();
					}
					if(teamIDs.size()>0)
					{
						try
						{
							writeTeamRankin2FileNow(filename);
						}catch(IOException e)
						{
							e.printStackTrace();
						}
					}
					System.out.println("Cya soon! :)");
					System.gc();									//perform garbage collection
					dispose();										//dispose off all UI windows
					System.exit(0);
				}
			}
		});
		
		teamIDs = new LinkedHashMap<String, Integer>()
		{
			private static final long serialVersionUID = -3137898194773356207L;

			public String toString()
			{
				if(this.size()>0)
				{
					TreeMap<String, Integer> sorted = new TreeMap<String, Integer>(new ValueComparator(this));
					sorted.putAll(this);
					String output="";
					int i=0;
					for(Map.Entry<String, Integer> entries: sorted.entrySet())
						output += (++i+"."+entries.getKey()+" => "+entries.getValue()+System.getProperty( "line.separator" ));
					return output;
				}else
					return "No teams have registered yet!";
			}
		};
		
		jchkboxShowPassword.addItemListener(new ItemListener() 
		{	
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				jpwFieldClientRegPw.setEchoChar(e.getStateChange()==ItemEvent.SELECTED?((char) 0):'*');
			}
		});
	}
	public static void main(String args[])
	{
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()							//make GUI on event dispatching thread
			{
					public void run()
					{
						new WiFiMCQQuizServer();
					}
			});
		}catch(InterruptedException intre)
		{
			intre.printStackTrace();
		}catch(InvocationTargetException invocationTargetExcep)
		{
			invocationTargetExcep.printStackTrace();
		}
	}
	boolean writeTeamRankin2FileNow(String filename)throws IOException
	{
		if(teamIDs.size()==0)
		{
			JOptionPane.showMessageDialog(null, "No teams have registered yet", null, JOptionPane.INFORMATION_MESSAGE);
			return false;
		}else
		{
			FileWriter fw = new FileWriter(filename, fileAppendMode);
			fw.write(teamIDs.toString().replace(" => ", ",")+"\n");
			fw.close();
			return true;
		}
	}
	static class ValueComparator implements Comparator<String>
	{
        Map<String, Integer> base;

        ValueComparator(Map<String, Integer> base)
		{
            this.base = base;
        }
        @Override
        public int compare(String a, String b)
		{
			//desc sort
			if (base.get(a).equals(base.get(b)))
				return b.compareTo(a);
			return base.get(b).compareTo(base.get(a));
        }
    }
	class ButtonListener implements ActionListener
	{
		File chosenExcelFile, chosenJSONFile;
		File[] currentEventDirContents = null;
		LoadQsJSON loadQsJSONObjToBrowseQs = new LoadQsJSON();
		int currentQBrowseCnter = 0;
		
		public void actionPerformed(ActionEvent ae)
		{
			if(ae.getSource().equals(jbtnStartStopService) && !t.serverRunning)
			{
				if(zipFileToSend==null)
					JOptionPane.showMessageDialog(null, "Exception: No Questions ZIP=JSON+imgs File has been set, set the Questions JSON File, images and compress to zip and then start the server", "No ZIP File Set Error", JOptionPane.ERROR_MESSAGE);
				else
				{
					t.serverRunning = true;
					t.start();
					jbtnStartStopService.setEnabled(false);
				}
			}else if(ae.getSource().equals(jbtnGetTeamRankin))
				JOptionPane.showMessageDialog(null, teamIDs.toString(), "Team Ranking", JOptionPane.INFORMATION_MESSAGE);
			else if(ae.getSource().equals(jbtnDelTeam))
			{
				if(jtxtDelTeam.getText().trim().equals(""))
					JOptionPane.showMessageDialog(null, "please enter a team name to remove .. ", "Team Removal", JOptionPane.INFORMATION_MESSAGE);
				else
				{
					if(teamIDs.containsKey(jtxtDelTeam.getText().trim()))
					{
						teamIDs.remove(jtxtDelTeam.getText().trim());
						JOptionPane.showMessageDialog(null, jtxtDelTeam.getText()+" removed!", "Team Removal", JOptionPane.INFORMATION_MESSAGE);
					}else
						JOptionPane.showMessageDialog(null, jtxtDelTeam.getText()+": No such teamID exists!", "Team Removal Failure!", JOptionPane.INFORMATION_MESSAGE);
				}
			}else if(ae.getSource().equals(jbtnWrite2FileNow))
			{
				try
				{
					if(writeTeamRankin2FileNow(filename))
						JOptionPane.showMessageDialog(null, "Successfully written to file!", null, JOptionPane.INFORMATION_MESSAGE);
				}catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
			}else if(ae.getSource().equals(jbtnLoadFromFileNow))
			{
				ArrayList<String> entries = new ArrayList<String>();
				try
				{
					BufferedReader br = new BufferedReader(new FileReader(filename));
					String s = "";
					while((s = br.readLine())!=null)
						if(!s.trim().equals(""))
							entries.add(s.substring(s.indexOf('.')));
					br.close();
					
					for(String str:entries)
					{
						String tokens[] = str.split(",");
						teamIDs.put(tokens[0].trim(), Integer.parseInt(tokens[1].trim()));
					}
					JOptionPane.showMessageDialog(null, "Successfully loaded from file!\n\n"+teamIDs.toString(), "Load Success", JOptionPane.INFORMATION_MESSAGE);
				}catch(FileNotFoundException fe)
				{
					JOptionPane.showMessageDialog(null, "File not found!", null, JOptionPane.ERROR_MESSAGE);
				}catch(Exception e)
				{
					JOptionPane.showMessageDialog(null, "Exception:"+e.getMessage()+" occurred .. please retry", null, JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}else if(ae.getSource().equals(jbtnTruncateFile))
			{
				try
				{
					new FileOutputStream(filename).getChannel().truncate(0).close();
					JOptionPane.showMessageDialog(null, "File truncated!", "Notify", JOptionPane.INFORMATION_MESSAGE);
				}catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
			}else if(ae.getSource().equals(jbtnDelFile))
			{
				try
				{
					File f = new File(filename);
					if(f.exists())
					{
						if(f.delete())
							JOptionPane.showMessageDialog(null, "File deleted!", null, JOptionPane.INFORMATION_MESSAGE);
						else
							JOptionPane.showMessageDialog(null, "Couldn't delete file .. please retry", null, JOptionPane.ERROR_MESSAGE);
					}else
						throw new Exception();
				}catch(IOException ioe)
				{
					// File permission problems are also caught here.
					JOptionPane.showMessageDialog(null, "Exception: "+ioe.getMessage()+" occurred .. please retry", null, JOptionPane.ERROR_MESSAGE);
				}catch(Exception e)
				{
					JOptionPane.showMessageDialog(null, "Couldn't delete file .. please retry", null, JOptionPane.ERROR_MESSAGE);
				}
			}else if(ae.getSource().equals(jbtnChooseJSONQsImgsDir))
			{
				JFileChooser eventDirChooser = new JFileChooser();
				eventDirChooser.setCurrentDirectory(new File("."));
				eventDirChooser.setDialogTitle("Choose Directory of Qs JSON File and Imgs");
				eventDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnValue = eventDirChooser.showOpenDialog(WiFiMCQQuizServer.this);
				
				if(returnValue == JFileChooser.APPROVE_OPTION)
					if((currentEventAppDir = eventDirChooser.getSelectedFile()).exists())
					{
						currentEventDirContents = currentEventAppDir.listFiles(new FilenameFilter()
						{
							@Override
							public boolean accept(File dir, String fileName)
							{
								return fileName.endsWith(".png") || fileName.endsWith(".json") || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg");
							}
						});
						jtxtareaCreateZipOfQsImgsLog.append("----- Directory: "+currentEventAppDir+" chosen having: "+Arrays.deepToString(currentEventDirContents)+System.getProperty("line.separator"));
						int JSONFileCnter = 0;
						for(File f:currentEventDirContents)
							JSONFileCnter = (f.isFile() && f.getName().endsWith(".json"))?JSONFileCnter+1:JSONFileCnter;
						if(JSONFileCnter!=1)
						{
							currentEventDirContents = null;
							JOptionPane.showMessageDialog(null, "Choose a directory which has only 1 JSON Questions File .. \nFormat: <currentEventName>.json", "Invalid Dir Error!", JOptionPane.ERROR_MESSAGE);
						}
					}else
						JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "Specified Directory doesn't Exits!", null, JOptionPane.ERROR_MESSAGE);
			}else if(ae.getSource().equals(jbtnEncryptJSONNCreateZip))
			{
				if(currentEventDirContents==null)
					JOptionPane.showMessageDialog(null, "Choose an apt dir", "Invalid Directory", JOptionPane.WARNING_MESSAGE);
				else
				{
					int JSONFileEncryptionCount = 0;
					for(File f:currentEventDirContents)
						if(f.getName().endsWith(".json"))
						{
							AESBase64JSONCodec.aesEncryptBase64JSONEncode(f.getAbsolutePath(), f.getParent()+File.separator+f.getName().toString().substring(0, f.getName().lastIndexOf('.'))+"Final");
							jtxtareaCreateZipOfQsImgsLog.append("----- "+f.getAbsolutePath()+" JSON file AES encrypted and Base64ed to "+f.getParent()+File.separator+f.getName().toString().substring(0, f.getName().lastIndexOf('.'))+"Final"+System.getProperty("line.separator"));
							JSONFileEncryptionCount++;
						}
					if(JSONFileEncryptionCount==0)
						JOptionPane.showMessageDialog(null, "No JSON files were encrypted", "Notify", JOptionPane.WARNING_MESSAGE);
					else
					{
						currentEventDirContents = currentEventAppDir.listFiles(new FilenameFilter()
						{
							@Override
							public boolean accept(File dir, String fileName)
							{
								return fileName.endsWith(".png") || fileName.endsWith("Final") || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg");
							}
						});
						if(!createZipFile(new File(currentEventAppDir+File.separator+"Questions.zip"), currentEventDirContents))
							JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "Zip File Creation Failed!", "Zipping Exception", JOptionPane.WARNING_MESSAGE);
					}
				}
			}else if(ae.getSource().equals(jbtnQsFileDone))
			{
				if(currentEventDirContents==null)
					JOptionPane.showMessageDialog(null, "Choose an apt dir", "Invalid Directory", JOptionPane.WARNING_MESSAGE);
				else if(jpwFieldClientRegPw.getPassword().length==0)
					JOptionPane.showMessageDialog(null, "Enter password for clients to register", "Password set failure", JOptionPane.WARNING_MESSAGE);
				else
				{
					try
					{
						int temp = Integer.parseInt(JOptionPane.showInputDialog(WiFiMCQQuizServer.this, "Enter time out for each question in seconds ..", "30"));
						if(temp<10 || temp>50)
							throw new Exception();
						 timeOutPerQSecs = temp;
						 
						 clientRegisteringPassword = new String(jpwFieldClientRegPw.getPassword());
						 
						if(!((zipFileToSend = new File(currentEventAppDir, "Questions.zip")).exists()))
						{
							zipFileToSend = null;
							JOptionPane.showMessageDialog(null, "Cant set zipFileToSend Error", "Error", JOptionPane.ERROR_MESSAGE);
						}else
						{
							jbtnEncryptJSONNCreateZip.setEnabled(false);
							jbtnQsFileDone.setEnabled(false);
							jbtnChooseJSONQsImgsDir.setEnabled(false);
							jpwFieldClientRegPw.setEnabled(false);
							jchkboxShowPassword.setEnabled(false);
							
							zipFileContents = readFileContentsAsByteArray(zipFileToSend);
						}
					}catch(Exception e)
					{
						JOptionPane.showMessageDialog(null, "Invalid Time Out Value entered .. Range: 10 to 50 seconds", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}else if(ae.getSource().equals(jbtnImportFromExcelFile))
			{
				JFileChooser excelFileChooser = new JFileChooser();
				excelFileChooser.setCurrentDirectory(new File("."));
				excelFileChooser.setDialogTitle("Choose Excel File(.xls and .xlsx supported => both 2003 and 07+ formats)");
				excelFileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xls", "xlsx"));
				int returnValue = excelFileChooser.showDialog(WiFiMCQQuizServer.this, "Choose Excel File");
				
				if(returnValue == JFileChooser.APPROVE_OPTION)
					if((chosenExcelFile = excelFileChooser.getSelectedFile()).exists())
					{
						//read from Excel and make JSON Code (PlaceHolder)
					}else
						JOptionPane.showMessageDialog(null, "Specified file doesn't Exits!", null, JOptionPane.ERROR_MESSAGE);
			}else if(ae.getSource().equals(jbtnChooseJSONQsFileToBrowse))
			{
				JFileChooser jsonFileChooser = new JFileChooser();
				jsonFileChooser.setCurrentDirectory(new File("."));
				jsonFileChooser.setDialogTitle("Choose Qs JSON File(.json)");
				jsonFileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
				int returnValue = jsonFileChooser.showDialog(WiFiMCQQuizServer.this, "Choose JSON File");
				
				if(returnValue == JFileChooser.APPROVE_OPTION)
					if((chosenJSONFile = jsonFileChooser.getSelectedFile()).exists())
					{
						try
						{
							loadQsJSONObjToBrowseQs.parseJSON(chosenJSONFile);
							boolean allReferencedImagesExist = true;
							for(Question q:LoadQsJSON.qsList)
								if(q.isImgQuestion && !new File(chosenJSONFile.getParentFile(), q.imgFileName).exists())
									allReferencedImagesExist = false;
							if(!allReferencedImagesExist)
								throw new Exception();
							jbtnNavigateQsFirst.setEnabled(true);
							jbtnNavigateQsPrev.setEnabled(true);
							jbtnNavigateQsNext.setEnabled(true);
							jbtnNavigateQsLast.setEnabled(true);
							JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "Loaded "+LoadQsJSON.qsList.size()+" questions", "Notify", JOptionPane.INFORMATION_MESSAGE);
							jbtnNavigateQsFirst.doClick();
						}catch(Exception e)
						{
							JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "Exception occurred while parsing JSON .. retry(make sure JSON file is well-formed and all images exists in same directory)!", null, JOptionPane.ERROR_MESSAGE);
							chosenJSONFile = null;
							LoadQsJSON.qsList.clear();
						}
					}else
						JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "Specified file doesn't Exits!", null, JOptionPane.ERROR_MESSAGE);
			}else if(ae.getSource().equals(jbtnShowServerConfig))
				JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, new JTextArea(getCurrentServerConfiguration()), "Configuration Parameters", JOptionPane.INFORMATION_MESSAGE);
			else if(ae.getSource().equals(jbtnNavigateQsFirst))
			{
				if(chosenJSONFile==null)
					JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "Choose a valid/well-formed Qs JSON File 1st!", null, JOptionPane.ERROR_MESSAGE);
				else
				{
					setQsContent(currentQBrowseCnter = 0);
					jlblCurrentQCntBrowsing.setText("Current Question Count: "+(currentQBrowseCnter+1));
				}
			}else if(ae.getSource().equals(jbtnNavigateQsPrev))
			{
				if(chosenJSONFile==null)
					JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "Choose a valid/well-formed Qs JSON File 1st!", null, JOptionPane.ERROR_MESSAGE);
				else
				{
					currentQBrowseCnter--;
					currentQBrowseCnter = currentQBrowseCnter<0?LoadQsJSON.qsList.size()+currentQBrowseCnter:currentQBrowseCnter;
					setQsContent(currentQBrowseCnter);
					jlblCurrentQCntBrowsing.setText("Current Question Count: "+(currentQBrowseCnter+1));
				}
			}else if(ae.getSource().equals(jbtnNavigateQsNext))
			{
				if(chosenJSONFile==null)
					JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "Choose a valid/well-formed Qs JSON File 1st!", null, JOptionPane.ERROR_MESSAGE);
				else
				{
					setQsContent(currentQBrowseCnter = (currentQBrowseCnter+1)%LoadQsJSON.qsList.size());
					jlblCurrentQCntBrowsing.setText("Current Question Count: "+(currentQBrowseCnter+1));
				}
			}else if(ae.getSource().equals(jbtnNavigateQsLast))
			{
				if(chosenJSONFile==null)
					JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "Choose a valid/well-formed Qs JSON File 1st!", null, JOptionPane.ERROR_MESSAGE);
				else
				{
					setQsContent(currentQBrowseCnter = LoadQsJSON.qsList.size()-1);
					jlblCurrentQCntBrowsing.setText("Current Question Count: "+(currentQBrowseCnter+1));
				}
			}
		}
		void setQsContent(int QIndexForArrayList)
		{
			LoadQsJSON.Question q = LoadQsJSON.qsList.get(QIndexForArrayList);
			jtxtfIsImgQuestion.setText(Boolean.toString(q.isImgQuestion));
			jtxtfImgFileName.setText(q.imgFileName);
			jtxtfQuestion.setText(q.question);
			jtxtfOption1.setText(q.option1);
			jtxtfOption2.setText(q.option2);
			jtxtfOption3.setText(q.option3);
			jtxtfOption4.setText(q.option4);
			jtxtfCorrectAns.setText(q.correctAns);
		}
	}
	String getCurrentServerConfiguration()
	{
		String configStr = "";
		try
		{
			configStr += "This Machine's Pvt IP: \t\t"+InetAddress.getLocalHost().toString()+"\t"+System.getProperty("line.separator");
			configStr += "Is Server Running: \t\t"+t.serverRunning+"\t"+System.getProperty("line.separator");
			if(t.serverRunning)
			{
				configStr += "Server Running on: \t\t"+InetAddress.getLocalHost().toString()+"\t"+System.getProperty("line.separator");
				configStr += "Server listening (local) port: \t\t"+t.serverSocket.getLocalPort()+"\t"+System.getProperty("line.separator");
			}
			configStr += "zipFileToSend: \t\t"+zipFileToSend+"\t"+System.getProperty("line.separator");
			configStr += "timeOutPerQSecs: \t\t"+timeOutPerQSecs+"\t"+System.getProperty("line.separator");
			configStr += "Event Score List File: \t\t"+filename+"\t"+System.getProperty("line.separator");
			configStr += "Event Score List File Append Mode Enabled: \t"+fileAppendMode+"\t"+System.getProperty("line.separator");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return configStr;
	}
	@SuppressWarnings("resource")
	boolean createZipFile(File destinationZipFileName, File[] files)				//created successfully => returns true else false
	{
		try
		{
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destinationZipFileName));
			zos.setLevel(9);
			jtxtareaCreateZipOfQsImgsLog.append("----- Creating ZIP file having the following files: "+Arrays.deepToString(files)+" .....");
			int QsJSONEncryptedFileCnt = 0;
			byte[] fileContents;
			for(File f:files)
			{
				if(f.getName().endsWith("Final"))
					QsJSONEncryptedFileCnt++;
				zos.putNextEntry(new ZipEntry(f.getName()));
				zos.write(fileContents = readFileContentsAsByteArray(f), 0, fileContents.length);
			}
			jtxtareaCreateZipOfQsImgsLog.append((QsJSONEncryptedFileCnt==1?"Created!":"Failed")+System.getProperty("line.separator"));
			if(QsJSONEncryptedFileCnt!=1)
			{
				JOptionPane.showMessageDialog(WiFiMCQQuizServer.this, "The directory must have 1 JSON AESEncrypted+Base64ed file;  format: <currentEventName>Final", null, JOptionPane.ERROR_MESSAGE);
				destinationZipFileName.delete();
				return false;
			}
			zos.close();
			zipFileToSend = destinationZipFileName;
			return true;
		}catch(Exception ze)
		{
			return false;
		}
	}
	byte[] readFileContentsAsByteArray(File f)
	{
		byte[] fileContents = new byte[(int)f.length()];
		try
		{
			FileInputStream fin = new FileInputStream(f);
			fin.read(fileContents);
			fin.close();
		}catch(FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
		}catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		return fileContents;
	}
	class ClientServicingThread extends Thread
	{
		Socket clientConnectedSocket;
		ObjectInputStream oisFromClient;
		ObjectOutputStream oosToClient;
		String forTeamID = "";
		
		ClientServicingThread(Socket clientConnectedSocket, String ClientServicingThreadName)
		{
			super(ClientServicingThreadName);
			this.clientConnectedSocket = clientConnectedSocket;
			try
			{
				oisFromClient = new ObjectInputStream(clientConnectedSocket.getInputStream());
				oosToClient = new ObjectOutputStream(clientConnectedSocket.getOutputStream());			//serialize and send out EXIFileContents as byte[] OR Text Replies to Client
			}catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
			System.out.println("[ "+new java.util.Date().toString()+" ] "+ClientServicingThreadName+" has been started for remote Client: "+clientConnectedSocket.getInetAddress()+":"+clientConnectedSocket.getPort());
		}
		public void run()
		{
			try
			{
				while(true)
				{
					String clientRequest = "";
					if((clientRequest=(String) oisFromClient.readObject())!=null)
					{
						String tempEventDescription = "";
						if(clientRequest.equals("Client: Hello!"))
						{
							System.out.print(tempEventDescription = "[ "+new java.util.Date().toString()+" ] Message Received: \""+clientRequest+"\" => Handshake request from: "+clientConnectedSocket.getInetAddress()+":"+clientConnectedSocket.getPort()+" ....");
							oosToClient.writeObject("Server: Hello!");
							oosToClient.flush();
							System.out.println("Handshake success!");
							jtxtareaHandshakeEventLog.append("----> "+(tempEventDescription += "Handshake success!")+System.getProperty("line.separator"));
							tempEventDescription = "";
						}else if(clientRequest.startsWith("REG "))
						{
							System.out.print(tempEventDescription = "[ "+new java.util.Date().toString()+" ] Message Received: \""+clientRequest+"\" => New Registration Entry from: "+clientConnectedSocket.getInetAddress()+":"+clientConnectedSocket.getPort()+" ....");
							String teamID = clientRequest.replace("REG ", "");
							if(teamIDs.containsKey(teamID))
							{
								oosToClient.writeObject("TeamID already registered!");
								tempEventDescription += "TeamID already registered!";
								System.out.println("TeamID already registered!");
								oosToClient.flush();
							}else
							{
								oosToClient.writeObject("TeamID OK!");
								tempEventDescription += "TeamID OK!";
								System.out.print("TeamID OK!");
								oosToClient.flush();
								
								String pw = (String) oisFromClient.readObject();
								if(pw.equals(clientRegisteringPassword))
								{
									teamIDs.put(forTeamID = teamID, 0);
									
									tempEventDescription += "Password OK!";
									System.out.print("Password OK!");
									
									oosToClient.writeObject("REG SUCCESS!");
									tempEventDescription += "REG SUCCESS!";
									System.out.println("REG SUCCESS!");
								}else
								{
									tempEventDescription += "Wrong Password!";
									System.out.print("Wrong Password!");
									
									oosToClient.writeObject("REG FAILED!");
									tempEventDescription += "REG FAILED!";
									System.out.println("REG FAILED!");
								}
								oosToClient.flush();
							}
							jtxtareaRegEventLog.append("----> "+tempEventDescription+System.getProperty("line.separator"));
						}else if(clientRequest.startsWith("RESULT "))
						{
							System.out.print(tempEventDescription = "[ "+new java.util.Date().toString()+" ] Message Received: \""+clientRequest+"\" => Final Score received from: "+clientConnectedSocket.getInetAddress()+":"+clientConnectedSocket.getPort()+" ....");
							String[] msgParts = clientRequest.replace("RESULT ", "").split("[ :]");
							String teamID = msgParts[0];
							if(teamIDs.containsKey(teamID))
							{
								teamIDs.put(teamID, Integer.parseInt(msgParts[msgParts.length-1]));			//put(teamID, score)
								oosToClient.writeObject(teamID+" RESULT ENTRY Success!");
								tempEventDescription += "RESULT ENTRY Success";
								System.out.println("Score Entry Successful!");
							}else
							{
								oosToClient.writeObject(teamID+" RESULT ENTRY Failed!");
								tempEventDescription += "RESULT ENTRY Failed";
								System.out.println("Score Entry Failed!");
							}	
							oosToClient.flush();
							jtxtareaScoreReceivedEventLog.append("----> "+tempEventDescription+System.getProperty("line.separator"));
							break;
						}else if(clientRequest.startsWith("GETQSFILE "))
						{
							System.out.print(tempEventDescription = "[ "+new java.util.Date().toString()+" ] Message Received: \""+clientRequest+"\" => Request for Questions File from: "+clientConnectedSocket.getInetAddress()+":"+clientConnectedSocket.getPort()+" ....");
							oosToClient.writeObject(zipFileContents);																	//Byte Array object Serialization: serialize the Byte Array Object to send to clients
							oosToClient.flush();
							tempEventDescription += " File Request Accepted..File Sent!";
							System.out.println(" File Request Accepted..File Sent!");
							jtxtareaQsFileRequestEventLog.append("----> "+tempEventDescription+System.getProperty("line.separator"));
						}else if(clientRequest.startsWith("GETQSTIMEOUTSECS "))
						{
							System.out.print(tempEventDescription = "[ "+new java.util.Date().toString()+" ] Message Received: \""+clientRequest+"\" => Request for per Question Time Out value from: "+clientConnectedSocket.getInetAddress()+":"+clientConnectedSocket.getPort()+" ....");
							oosToClient.writeObject(clientRequest+timeOutPerQSecs);
							tempEventDescription += " Get PerQuestion TimeOut Value Request Accepted..TimeOut Value("+timeOutPerQSecs+") Sent!";
							System.out.println(" Get PerQuestion TimeOut Value Request Accepted..TimeOut Value("+timeOutPerQSecs+") Sent!");
							oosToClient.flush();
							jtxtareaQsFileRequestEventLog.append("----> "+tempEventDescription+System.getProperty("line.separator"));
						}
					}
				}
				clientConnectedSocket.close();
				oosToClient.close();
				oisFromClient.close();
				System.out.println("[ "+new java.util.Date().toString()+" ] Remote Client's: "+clientConnectedSocket.getInetAddress()+":"+clientConnectedSocket.getPort()+" {for TeamID: "+forTeamID+"} socket closed (its "+this.getName()+" has been stopped) .. has finished playing!");
				//serverSocket.close();
			}catch(IOException ioe)
			{
				ioe.printStackTrace();
			}catch (NumberFormatException e)
			{
				e.printStackTrace();
			}catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
	class ServiceThread extends Thread
	{
		boolean serverRunning;
		ServerSocket serverSocket;
		Socket clientConnectedSocket;
		ArrayList<Socket> clientConnectedSockets = new ArrayList<Socket>();
		int clientsConnectedSoFarCnter = 0;
		
		public void run()
		{
			while(serverRunning)
			{
				try
				{
					if(serverSocket==null)
					{
						serverSocket = new ServerSocket(65535);
						serverSocket.setReuseAddress(true);
						System.out.println("Server started listening on port number: "+65535+" ....");
						System.out.println("Waiting for client requests .... ");
					}
					clientConnectedSocket = serverSocket.accept();
					clientConnectedSockets.add(clientConnectedSocket);
					new ClientServicingThread(clientConnectedSocket, "ClientServicingThread"+(++clientsConnectedSoFarCnter)).start();
				}catch(SocketException se)
				{
					if(se.getMessage().equals("socket closed"))
						System.out.println("Server stopped!");
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}