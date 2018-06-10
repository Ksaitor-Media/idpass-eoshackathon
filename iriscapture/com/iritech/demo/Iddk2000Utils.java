package com.iritech.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.omg.CORBA.Environment;

import com.iritech.iddk.standard.*;

public class Iddk2000Utils {
	public static final int SUCCESS  = 0;
	public static final int ERROR	 = -1;

	public static boolean isDigit(byte a)
	{
		if(a >= '0' && a <= '9') return true;
		return false;
	}

	public static String read_string(String message)
	{
		String result = null;
		System.out.print(message);
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			result = br.readLine();
		} catch (IOException ioe)
		{
		     System.out.println("IO error!");
		}
		return result;
	}

	public static float read_float(String message, float limitMin, float limitMax, float defaultValue)
	{
		String buffer;
		float result = 1.0f;
		boolean rightChoice = false;
		InputStreamReader inputStream = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(inputStream);

		System.out.print(message);

		while(!rightChoice)
		{
			rightChoice = true;
			try {
				buffer = reader.readLine();
				if(buffer == null) System.exit(-1);
				if(buffer.length() == 0)
				{
					System.out.println(defaultValue);
					return defaultValue;
				}
				try
				{
					result = Float.parseFloat(buffer);
					if(result < limitMin || result > limitMax) throw new NumberFormatException();
				}
				catch(NumberFormatException ex)
				{
					System.out.print("Invalid value. It should be on the range ["+limitMin+","+limitMax+"]" + "\n" + message);
					rightChoice = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public static int read_uint(String message, int limitMin, int limitMax, int defaultValue)
	{
		String buffer;
		int result = 0;
		boolean rightChoice = false;
		InputStreamReader inputStream = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(inputStream);

		System.out.print(message);

		while(!rightChoice)
		{
			rightChoice = true;
			try {
				buffer = reader.readLine();
				if(buffer == null) System.exit(-1);
				if(buffer.length() == 0)
				{
					//Default value
					if(defaultValue != 0)
					{
						System.out.println(defaultValue);
						return defaultValue;
					}
					else
					{
						//Read again
						rightChoice = false;
						continue;
					}
				}
				try
				{
					result = Integer.parseInt(buffer);
					if(result < limitMin || result > limitMax) throw new NumberFormatException();
				}
				catch(NumberFormatException ex)
				{
					System.out.print("Invalid value. It should be in [" + limitMin + "; " + limitMax + "]\n");
					rightChoice = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public static int read_uint_ext(String message, int limitMin, int limitMax, int except, int defaultValue)
	{
		String buffer;
		int result = 0;
		boolean rightChoice = false;
		InputStreamReader inputStream = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(inputStream);

		System.out.print(message);

		while(!rightChoice)
		{
			rightChoice = true;
			try {
				buffer = reader.readLine();
				if(buffer == null) System.exit(-1);
				if(buffer.length() == 0)
				{
					//Default value
					System.out.println(defaultValue);
					return defaultValue;
				}
				try
				{
					result = Integer.parseInt(buffer);
					if((result < limitMin || result > limitMax) && result != except) throw new NumberFormatException();
				}
				catch(NumberFormatException ex)
				{
					System.out.print("Invalid value. It should be in [" + limitMin + "; " + limitMax + "] or " + except + "\n");
					rightChoice = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public static int choose_option(int numOfOpts)
	{
		String buffer = "";
		int option = -1;
		boolean rightChoice = false;
		InputStreamReader inputStream = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(inputStream);

		while(!rightChoice)
		{
			try {
				buffer = reader.readLine();
				if(buffer == null) System.exit(-1);
				if(buffer.length() == 0)
				{
					//User presses Enter only
					option = -1;
					return option;
				}
				else
				{
					try
					{
						option = Integer.parseInt(buffer);
						if(option > 0 && option <= numOfOpts) rightChoice = true;
						else throw new NumberFormatException();
					}
					catch(NumberFormatException ex)
					{
						System.out.println("Invalid number. Please try again !");
						System.out.print("Select one option: ");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return option;
	}

	public static int display_menu(String[] menus, int menu_size, int default_idx, String special_comment)
	{
		for(int i = 0; i < menu_size; i++)
		{
			if(i == default_idx)
				System.out.print("\t" + (i+1) + ". " + (menus[i]) + " (default)\n");
			else
				System.out.print("\t" + (i+1) + ". " + (menus[i]) + "\n");
		}
		System.out.print("Enter your choice (" + special_comment + "): ");
		return choose_option(menu_size);
	}

	public static int display_menu(String[] menus, int menu_size, int default_idx)
	{
		for(int i = 0; i < menu_size; i++)
		{
			if(i == default_idx)
				System.out.print("\t" + (i+1) + ". " + (menus[i]) + " (default)\n");
			else
				System.out.print("\t" + (i+1) + ". " + (menus[i]) + "\n");
		}
		System.out.print("Enter your choice: ");
		return choose_option(menu_size);
	}

	public static boolean check_supported_baudrate(int baudrate)
	{
		boolean supported = false;
		if(baudrate >= 56000) supported = true;
		return supported;
	}

	public static void wait(int milliSeconds)
	{
		try {
			Thread.sleep(milliSeconds, 0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean save_file(String filename, byte[] pData)
	{
		FileOutputStream fos;
		boolean result = false;

		if(filename == null || filename == "" || pData == null)
		{
			return false;
		}
		try
		{
			fos = new FileOutputStream(filename);
		    fos.write(pData);
		    result = true;
		    fos.close();
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("FileNotFoundException : " + ex);
		}
		catch(IOException ioe)
		{
		    System.out.println("IOException : " + ioe);
		}

		return result;
	}

    public static void save_result_image(IddkImage image, IddkEyeSubtype eyeSubtype)
    {
        // String fileName = "iris.jp2";
		// fileName = System.getProperty("resultDir") + ;

		// Original
        // String eyeFilePath = System.getProperty("user.dir") + "\\" + fileName;
		// Mine
        String eyeFilePath = System.getProperty("resultDir") + "/" + "iris.jp2";

        if (Iddk2000Utils.save_file(eyeFilePath, image.getImageData()))
        {
        	System.out.print("\n\tSaved "+ eyeFilePath +"\n");
        }
        else
        {
        	System.out.print("\n\tSaving "+ eyeFilePath +" failed.\n");
        }
    }

	public static boolean read_file(String filename, IddkDataBuffer pData)
	{
		FileInputStream is;
		boolean result = false;

	    try
	    {
	    	File file = new File(filename);
	    	byte[] temp = new byte[(int)file.length()];
	    	is = new FileInputStream(filename);
	    	is.read(temp, 0, temp.length);
	    	pData.setData(temp.clone());
	    	result = true;
	    	is.close();
	    }
	    catch(FileNotFoundException ex)
	    {
	    	//User should handle error
	    	//System.out.println("FileNotFoundException : " + ex);
	    }
	    catch (IOException ioe)
	    {
	    	//User should handle error
	    	//System.out.println("IOException : " + ioe);
	    }

	    return result;
	}

	public static void prepare_param_for_capturing(IddkCaptureMode captureMode, IddkQualityMode qualityMode, IddkInteger iCount, IddkEyeSubtype eyeSubtype)
	{
		IddkConfig config = new IddkConfig();

		Iddk2000Apis.getSdkConfig(config);

		captureMode.setValue(IddkCaptureMode.IDDK_TIMEBASED);
		iCount.setValue(3);
		qualityMode.setValue(IddkQualityMode.IDDK_QUALITY_NORMAL);
		if(eyeSubtype != null)
		{
			eyeSubtype.setValue(IddkEyeSubtype.IDDK_LEFT_EYE);
		}
	}

	public static boolean isWindows()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("win") >= 0);
	}

	public static boolean isUnix()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
	}
}
