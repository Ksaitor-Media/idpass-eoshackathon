package com.iritech.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.iritech.demo.Iddk2000Utils;
import com.iritech.iddk.standard.*;

public class Iddk2000Features {

	/* The current handle to device, this handle can be initialized by using get_device_handle() */
	public static String g_binDir;
	Iddk2000Apis apis = null;
	int error_level = -1;
	boolean g_deviceSleep = false;
	IddkEyeSubtype g_selectEyeMode = new IddkEyeSubtype(IddkEyeSubtype.IDDK_UNKNOWN_EYE);
	boolean g_isBinocular = false;
	int RIGHT_EYE_IDX = 0;
	int LEFT_EYE_IDX = 1;

	boolean g_deviceDeepSleep = false;
	boolean g_isUsbDevice = false;

	void reset_error_level(IddkResult result)
	{
		if(result.getValue() == IddkResult.IDDK_OK)
		{
			error_level = -1;
			if(g_deviceSleep) g_deviceSleep = false;
			if(g_deviceDeepSleep) g_deviceDeepSleep = false;
		}
	}

	void getchar()
	{
		System.out.print("\nPlease press any key to continue ...\n");
		try {
			InputStreamReader inputStream = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(inputStream);
			reader.readLine();
		} catch (IOException e) {
			System.out.print("IO exception happens here !\n");
			e.printStackTrace();
		}
	}

	/*************************************************************************
	*	After each operation, users should clear any current captured iris image
	*   so as to protect your iris from other person usage
	*************************************************************************/
	void clear_capture()
	{
		IddkResult iRet = new IddkResult();
		IddkEyeSubtype eyeSubtype = new IddkEyeSubtype(IddkEyeSubtype.IDDK_UNKNOWN_EYE);


		iRet = apis.clearCapture(eyeSubtype);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			//System.out.print("Clear capture successfully\n");
		}
		else
		{
			System.out.print("Clear capture failed\n");
			handle_error(iRet);
		}
	}


	/*************************************************************************
	/* This function check the quality of the current captured images
	/* forEnrollment: true if the captured image is for enrollment, otherwise it is for verification/identification.
	/* isGrayZone: The output parameter that receives true if at least one eye has quality in the grayzone
	/* numAcceptableEyes: The output parameter to receive the number of iris images that have acceptable qualities
	/* Return: false if there is no iris image with acceptable quality.
	/*************************************************************************/
	boolean check_image_quality(boolean forEnrollment, boolean isGrayZone, int numAcceptableEyes)
	{
		boolean bRet = false;
		ArrayList<IddkIrisQuality> qualities = new ArrayList<IddkIrisQuality>();
		numAcceptableEyes = 0;
		int nBadTotalScore = forEnrollment ? 50 : 30;
		int nBadUsableArea = forEnrollment ? 50 : 30;
		int nGoodTotalScore = 70;
		int nGoodUsableArea = 70;

		IddkResult iRet = apis.getResultQuality(qualities);

		if(iRet.getValue() == IddkResult.IDDK_OK || iRet.getValue() == IddkResult.IDDK_SE_LEFT_FRAME_UNQUALIFIED || iRet.getValue() == IddkResult.IDDK_SE_RIGHT_FRAME_UNQUALIFIED)
		{
			if (forEnrollment)
			{
				// at least one eye's quality is in grayzone
				isGrayZone = ((qualities.get(RIGHT_EYE_IDX).getTotalScore() > nBadTotalScore && qualities.get(RIGHT_EYE_IDX).getUsableArea() > nBadUsableArea
					&& (qualities.get(RIGHT_EYE_IDX).getTotalScore() <= nGoodTotalScore || qualities.get(RIGHT_EYE_IDX).getUsableArea() <= nGoodUsableArea))
					|| (g_isBinocular && (qualities.get(LEFT_EYE_IDX).getTotalScore() > nBadTotalScore && qualities.get(LEFT_EYE_IDX).getUsableArea() > nBadUsableArea
					&& (qualities.get(LEFT_EYE_IDX).getTotalScore() <= nGoodTotalScore || qualities.get(LEFT_EYE_IDX).getUsableArea() <= nGoodUsableArea)))
					);
			}
			else
			{
				//For verification there is no grayzone, just one threshold (30)
				isGrayZone = false;
			}

			// number of eyes with acceptable quality (not bad)
			if (qualities.get(RIGHT_EYE_IDX).getTotalScore() > nBadTotalScore && qualities.get(RIGHT_EYE_IDX).getUsableArea() > nBadUsableArea)
				numAcceptableEyes++;

			if(g_isBinocular)
				if (qualities.get(LEFT_EYE_IDX).getTotalScore() > nBadTotalScore && qualities.get(LEFT_EYE_IDX).getUsableArea() > nBadUsableArea)
					numAcceptableEyes++;

			if (numAcceptableEyes == 0)
			{
				//Clear all captured iris images in the camera device
				apis.clearCapture(new IddkEyeSubtype(0));
				System.out.print("\nNo captured iris image has acceptable quality for the " + (forEnrollment ? "enrollment" : "matching") + ". Please try to capture your iris(es) again!\n");
				reset_error_level(iRet);
				return false;
			}

			if (g_isBinocular)
			{
				if (iRet.getValue() != IddkResult.IDDK_SE_RIGHT_FRAME_UNQUALIFIED && (qualities.get(RIGHT_EYE_IDX).getTotalScore() <= nBadTotalScore || qualities.get(RIGHT_EYE_IDX).getUsableArea() <= nBadUsableArea))
				{
					// clear right eye
					iRet = apis.clearCapture(new IddkEyeSubtype(IddkEyeSubtype.IDDK_RIGHT_EYE)) ;
					if (iRet.getValue() != IddkResult.IDDK_OK)
					{
						reset_error_level(iRet);
						return false;
					}
					System.out.print("\nThe right iris image has bad quality. It was cleared!\n");
				}
				if (iRet.getValue() != IddkResult.IDDK_SE_LEFT_FRAME_UNQUALIFIED && (qualities.get(LEFT_EYE_IDX).getTotalScore() <= nBadTotalScore || qualities.get(LEFT_EYE_IDX).getUsableArea() <= nBadUsableArea))
				{
					// clear left eye
					iRet = apis.clearCapture(new IddkEyeSubtype(IddkEyeSubtype.IDDK_LEFT_EYE)) ;
					if (iRet.getValue() != IddkResult.IDDK_OK)
					{
						reset_error_level(iRet);
						return false;
					}
					System.out.print("\nThe left iris image has bad quality. It was cleared!\n");
				}
			}
		}
		else
		{
			reset_error_level(iRet);
			return false;
		}

		bRet = true;
		reset_error_level(iRet);
		return bRet;
	}

	/*******************************************************************************
	*	This function demonstrates how to login to the device as an Administrator
	*	or a Superuser so that you can control the device as your wish. To login as
	*	an Administrator, you have to enroll your irises to the device using function
	*	Iddk_EnrollAsAdmin first. To login as a Superuser, you have to enroll
	*	your irises using Iddk_EnrollCapture first and then set user role using
	*	Iddk_SetUserRole.
	********************************************************************************/
	void login()
	{
		boolean isAdmin = true;
		String enrollID;
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		IddkCaptureStatus captureStatus = new IddkCaptureStatus();
		boolean isGrayZone = false;
		int numAcceptableEyes = 0;

		/* For LoadGallery */
		ArrayList<String> enrollIds = new ArrayList<String>();
		IddkInteger numberOfUsedSlots = new IddkInteger();
		IddkInteger numberOfMaxSlots = new IddkInteger();

		iRet = apis.getCaptureStatus(captureStatus);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			if(captureStatus.getValue() != IddkCaptureStatus.IDDK_COMPLETE)
			{
				/* Start a capturing process */
				System.out.print("\nTo login to device, we need to capture your iris first. Capturing process starts ... \n");
				capturing_process(true, false, false);
			}
		}
		else
		{
			handle_error(iRet);
			getchar();
			return;
		}

		if(!check_image_quality(false, isGrayZone, numAcceptableEyes))
		{
			getchar();
			return;
		}

		/* Ask user first */
		System.out.print("\nLogin as: \n\t1. Administrator (default)\n\t2. Superuser\n");
		System.out.print("Enter your choice: ");
		switch(Iddk2000Utils.choose_option(2))
		{
		case -1:
			System.out.print("1\n");
		case 1:
			isAdmin = true;
			break;
		case 2:
			isAdmin = false;
		}

		/* Enter ID */
		System.out.print("\nEnter "+ (isAdmin?"Administrator":"Superuser") +" ID (less than 32 characters): ");
		enrollID = Iddk2000Utils.read_string("");

		if(!isAdmin)
		{
			//Load gallery before login as SuperUser
			System.out.print("\nLoading gallery ... ");
			iRet = apis.loadGallery(enrollIds, numberOfUsedSlots, numberOfMaxSlots);
			if(iRet.getValue() != IddkResult.IDDK_OK)
			{
				handle_error(iRet);
				getchar();
				return;
			}
			else
			{
				System.out.print("done.\n");
			}
		}

		iRet = apis.login(enrollID, isAdmin?new IddkSystemRole(IddkSystemRole.IDDK_SYS_ROLE_ADMIN): new IddkSystemRole(IddkSystemRole.IDDK_SYS_ROLE_SUPERUSER));
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			System.out.print("\nHi \""+enrollID+"\" !!! Login successfully\n");

			reset_error_level(iRet);
		}
		else
		{
			System.out.print("\nSorry \""+enrollID+"\" !!! Login failed\n");
			handle_error(iRet);
		}

		clear_capture();
		getchar();
	}

	/**************************************************************************
	/*	This functions demonstrates how to logout from the device.
	/*************************************************************************/
	void logout()
	{
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);

		iRet = apis.logout();
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			System.out.print("Logout successfully\n");

			/* Remember to do that */
			clear_capture();
		}
		else
		{
			System.out.print("Logout failed\n");
			handle_error(iRet);
		}

		reset_error_level(iRet);

		getchar();
	}

	/****************************************************************************
	*	This function demonstrates how to configure the device. Note that, this
	*	function is only used by an Administrator, so login as Administrator first.
	*	Device configuration parameters:
	*
	*	1.	Encryption mode
	*	2.	Deduplication
	*	3.	Supervised enrollment
	*	4.	Iris data closure
	*	5.	Deduplication/Authentication threshold
	*	6	Stream images enable
	*	7.	Stream scale
	*	8.  Stream format
	*	9.	Baudrate
	/****************************************************************************/
	void device_configuration()
	{
		IddkDeviceConfig devConfig = new IddkDeviceConfig();
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		boolean deleteCustCer = false;
		boolean regenCamCer = false;
		boolean exitProgram = false;
		int option = 0;
		String enrollID = null;
		IddkCaptureStatus captureStatus = new IddkCaptureStatus();
		boolean isGrayZone = false;
		int numAcceptableEyes = 0;

		String[] device_config_menu = {
				"Main Menu",
				"Get Device Configuration",
				"Set Device Configuration",
				"Lock Device",
				"Unlock Device",
				"Reset Certificates",
				"Unenroll Admin",
				"Enroll Admin",
				"Exit"
		};

		while(true)
		{
			/* We have a list of menu here */
			System.out.print("\nDEVICE MANAGEMENT: Please select one menu item\n");
			option = Iddk2000Utils.display_menu(device_config_menu, device_config_menu.length, -1);

			if(option != 9 && option != -1 && option != 1)
			{
				System.out.print("\n\n**************** "+device_config_menu[option - 1]+" ****************\n\n");
			}

			if(option == -1) continue;
			else if(option == 9)
				/* Exit */
			{
				exitProgram = true;
				break;
			}
			else if(option == 1)
				/* Come back to main menu */
			{
				clear_capture();

				break;
			}
			else if(option == 2)
			{
				/* Get device configuration */
				iRet = apis.getDeviceConfig(devConfig);
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print("\nCurrent device configuration:\n");
					System.out.print("\t+ Encryption mode: "+(devConfig.getEncryptionMode() == IddkEncryptionMode.IDDK_ENCRYPTION_NONE?"No force on encryption":"Force encryption on image/template data")+"\n");
					System.out.print("\t+ Deduplication mode: "+(devConfig.isEnableDeduplication()?"Enable":"Disable")+"\n");
					System.out.print("\t+ Deduplication threshold: "+(devConfig.getDeduplicationThreshold())+"\n");
					System.out.print("\t+ Authentication threshold: "+(devConfig.getAuthenThreshold())+"\n");
					System.out.print("\t+ Supervised enrollment: "+(devConfig.isSupervisedEnrollment()?"Enable":"Disable")+"\n");
					System.out.print("\t+ Iris data closure: "+(devConfig.isDataClosure()?"Enable":"Disable")+"\n");
					System.out.print("\t+ Stream images: "+(devConfig.isEnableStream()?"Yes":"No")+"\n");
					if(devConfig.isEnableStream())
					{
						System.out.print("\t+ Stream scale: "+(devConfig.getStreamScale())+"\n");
						System.out.print("\t+ Stream format: "+(devConfig.getStreamFormat().getValue() == IddkImageFormat.IDDK_IFORMAT_MONO_RAW?"IDDK_IFORMAT_MONO_RAW":"Unknown")+"\n");
					}
				}
				System.out.print("\t+ UART Baudrate: " + devConfig.getBaudrate() + "\n");
				System.out.print("\t+ Compression Quality: " + devConfig.getCompressionQuality() + "\n");
				System.out.print("\t+ UART Hardware Flow Control: " + ((devConfig.getFlowControl().getValue() != 0)?"Enabled":"Disabled") + "\n");
				if(devConfig.getSleepMode().getValue() != 0)
				{
					if(devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_STANDBY || devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_SLEEP || devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_DEEPSLEEP)
					{
						System.out.println("\t+ Sleep Mode: " + ((devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_STANDBY) ? "Stanby" : ((devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_SLEEP) ? "Sleep" : "Deepsleep")));
						System.out.println("\t+ Sleep Timeout: " + devConfig.getSleepTimeout() + "\n");
					}
				}
			}
			else if(option == 3)
			{
				/* Get device configuration */
				iRet = apis.getDeviceConfig(devConfig);
				if(iRet.getValue() != IddkResult.IDDK_OK) break;

				/* Set device configuration */
				System.out.print("\nSet encryption mode?\n\t1. No force\n\t2. Force\n");
				System.out.print("Enter your choice (Press ENTER to skip): ");
				switch(Iddk2000Utils.choose_option(2))
				{
				case -1:
					System.out.print("Current value: " + ((devConfig.getEncryptionMode()==(byte)IddkEncryptionMode.IDDK_ENCRYPTION_NONE)?"No force":"Force") +"\n");
					break;
				case 1:
					devConfig.setEncryptionMode((byte)IddkEncryptionMode.IDDK_ENCRYPTION_NONE);
					break;
				case 2:
					devConfig.setEncryptionMode((byte)IddkEncryptionMode.IDDK_ENCRYPTION_FORCE);
				}

				System.out.print("\nSet deduplication mode?\n\t1. Disable\n\t2. Enable\n");
				System.out.print("Enter your choice (Press ENTER to skip): ");
				switch(Iddk2000Utils.choose_option(2))
				{
				case -1:
					System.out.print("Current value: " + (devConfig.isEnableDeduplication()?"Enable":"Disable") + "\n");
					break;
				case 1:
					devConfig.setEnableDeduplication(false);
					break;
				case 2:
					devConfig.setEnableDeduplication(true);
				}

				if(devConfig.isEnableDeduplication())
				{
					devConfig.setDeduplicationThreshold(Iddk2000Utils.read_float("\nSet deduplication threshold (Press ENTER for current value): ", 0.8f, 1.1f, devConfig.getDeduplicationThreshold()));
				}

				devConfig.setAuthenThreshold(Iddk2000Utils.read_float("\nSet authentication threshold (Press ENTER for current value): ", 0.8f, 1.1f, devConfig.getAuthenThreshold()));

				System.out.print("\nSet supervised enrollment mode?\n\t1. Disable\n\t2. Enable\n");
				System.out.print("Enter your choice (Press ENTER to skip): ");
				switch(Iddk2000Utils.choose_option(2))
				{
				case -1:
					System.out.print("Current value: " + (devConfig.isSupervisedEnrollment()?"Enable":"Disable") + "\n");
					break;
				case 1:
					devConfig.setSuperviseEnrollment(false);
					break;
				case 2:
					devConfig.setSuperviseEnrollment(true);
				}

				System.out.print("\nSet iris data closure mode?\n\t1. Disable\n\t2. Enable\n");
				System.out.print("Enter your choice (Press ENTER to skip): ");
				switch(Iddk2000Utils.choose_option(2))
				{
				case -1:
					System.out.print("Current value: "+ (devConfig.isDataClosure()?"Enable":"Disable") +"\n");
					break;
				case 1:
					devConfig.setDataClosure(false);
					break;
				case 2:
					devConfig.setDataClosure(true);
				}

				System.out.print("\nDo you want to stream images?\n\t1. Yes\n\t2. No\n");
				System.out.print("Enter your choice (Press ENTER to skip): ");
				switch(Iddk2000Utils.choose_option(2))
				{
				case -1:
					System.out.print("Current value: "+(devConfig.isEnableStream()?"Yes":"No")+"\n");
					break;
				case 1:
					devConfig.setEnableStream(true);
					break;
				case 2:
					devConfig.setEnableStream(false);
				}

				if(devConfig.isEnableStream())
				{
					System.out.print("\nSet stream scale?\n\t1. 1\n\t2. 2\n\t3. 4\n\t4. 8\n\t5. 16\n");
					System.out.print("Enter your choice (Press ENTER to skip): ");
					switch(Iddk2000Utils.choose_option(5))
					{
					case -1:
						System.out.print("Current value: "+devConfig.getStreamScale()+"\n");
						break;
					case 1:
						devConfig.setStreamScale((byte)1);
						break;
					case 2:
						devConfig.setStreamScale((byte)2);
						break;
					case 3:
						devConfig.setStreamScale((byte)4);
						break;
					case 4:
						devConfig.setStreamScale((byte)8);
						break;
					case 5:
						devConfig.setStreamScale((byte)16);
					}

					/* For the image format of streaming:
						- In USB connection: Only raw image format is supported.
						- In UART connection: Only Jpeg format is supported.
					*/
				}

				/* We set the device baudrate */
				while(true)
				{
					int baudrate = Iddk2000Utils.read_uint("\nEnter UART baudrate (not less than 56000, press ENTER for current value): ", 56000, 916000, devConfig.getBaudrate());
					if(Iddk2000Utils.check_supported_baudrate(baudrate))
					{
						devConfig.setBaudrate(baudrate);
						break;
					}
					else
					{
						System.out.print("Unsupported baudrate, enter again !\n");
					}
				}

				System.out.print("\nSet UART hardware flow control?\n\t1. Disable\n\t2. Enable\n");
				System.out.print("Enter your choice (Press ENTER to skip): ");
				switch(Iddk2000Utils.choose_option(2))
				{
				case -1:
					System.out.print("Current value: " + (devConfig.getFlowControl().getValue() == IddkFlowControl.IDDK_UART_FC_HW ?"Enabled":"Disabled") + "\n");
					break;
				case 1:
					devConfig.setFlowControl(new IddkFlowControl(0));
					break;
				case 2:
					devConfig.setFlowControl(new IddkFlowControl(1));
				}

				byte compressionQuality = (byte)Iddk2000Utils.read_uint("\nEnter compression quality (Press ENTER to skip): ", 1, 100, devConfig.getCompressionQuality());
				devConfig.setCompressionQuality(compressionQuality);

				if(devConfig.getSleepMode().getValue() != 0)
				{
					if(devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_STANDBY || devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_SLEEP || devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_DEEPSLEEP)
					{
						System.out.print("\nSet sleep mode?\n\t1. Standby\n\t2. Sleep\n\t3. Deepsleep\n");
						System.out.print("Enter your choice (Press ENTER to skip): ");
						switch(Iddk2000Utils.choose_option(3))
						{
						case -1:
							System.out.print("Current value: " + ((devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_STANDBY)?"Stanby":((devConfig.getSleepMode().getValue() == IddkSleepCode.IDDK_PM_SLEEP)?"Sleep":"Deepsleep")) + "\n");
							break;
						case 1:
							devConfig.setSleepMode(IddkSleepCode.IDDK_PM_STANDBY);
							break;
						case 2:
							devConfig.setSleepMode(IddkSleepCode.IDDK_PM_SLEEP);
							break;
						case 3:
							devConfig.setSleepMode(IddkSleepCode.IDDK_PM_DEEPSLEEP);
						}

						devConfig.setSleepTimeout((short) Iddk2000Utils.read_uint_ext("\nEnter sleep timeout (Press ENTER to skip): ", 10, 65535, 0, devConfig.getSleepTimeout()));
					}
				}


				iRet = apis.setDeviceConfig(devConfig);
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print("New device configuration was set successfully !\n");
				}
			}
			else if(option == 4)
			{
				/* Lock device */
				iRet = apis.lockDevice();
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print("Device is locked successfully !\n");
				}
			}
			else if(option == 5)
			{
				/* Unlock device */
				iRet = apis.unlockDevice();
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print("Device is unlocked successfully !\n");
				}
			}
			else if(option == 6)
			{
				/* Reset certificates */
				System.out.print("\nDelete customer certificate?\n\t1. No (default)\n\t2. Yes\n");
				System.out.print("Enter your choice: ");
				switch(Iddk2000Utils.choose_option(2))
				{
				case -1:
					System.out.print("1\n");
				case 1:
					deleteCustCer = false;
					break;
				case 2:
					deleteCustCer = true;
				}

				System.out.print("\nRegenerate camera certificate?\n\t1. No (default)\n\t2. Yes\n");
				System.out.print("Enter your choice: ");
				switch(Iddk2000Utils.choose_option(2))
				{
				case -1:
					System.out.print("1\n");
				case 1:
					regenCamCer = false;
					break;
				case 2:
					regenCamCer = true;
				}

				if(deleteCustCer || regenCamCer)
				{
					iRet = apis.resetCertificates(deleteCustCer, regenCamCer);
					if(iRet.getValue() == IddkResult.IDDK_OK)
					{
						System.out.print("Camera certificates are reset !\n");
					}
				}
				else
				{
					System.out.print("Camera certificates are not reset !\n");
				}
			}
			else if(option == 7)
			{
				/* Unenroll admin */
				enrollID = Iddk2000Utils.read_string("\nEnter admin ID (less than 32 characters): ");
				iRet = apis.unenrollAdmin(enrollID);
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print("\nAdmin \""+enrollID+"\" just unenrolled !\n");
				}
				else
				{
					System.out.print("\nUnenroll admin \""+enrollID+"\" failed !\n");
				}
			}
			else if(option == 8)
			{
				/* Enroll admin */

				/* Check as if the device finished */
				iRet = apis.getCaptureStatus(captureStatus);
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					if(captureStatus.getValue() != IddkCaptureStatus.IDDK_COMPLETE)
					{
						System.out.print("To enroll as an Administrator, we need to capture your iris first. Capturing process starts ...\n");
						capturing_process(true, false, false);
					}

					if(check_image_quality(true, isGrayZone, numAcceptableEyes))
					{
						if(g_isBinocular && g_selectEyeMode.getValue() != IddkEyeSubtype.IDDK_LEFT_EYE && g_selectEyeMode.getValue() != IddkEyeSubtype.IDDK_RIGHT_EYE && numAcceptableEyes == 1)
						{
							System.out.print("User chose to capture both eyes but only one is qualified for the enrollment.\n");
						}

						if(isGrayZone)
						{
							System.out.print("The captured image(s) is enrollable but not in sufficient quality to warrant the best accuracy." +
									"\nThe subject is recommended to have his/her iris image recaptured with the eye opened widely.\n");
						}
						if((g_isBinocular && g_selectEyeMode.getValue() != IddkEyeSubtype.IDDK_LEFT_EYE && g_selectEyeMode.getValue() != IddkEyeSubtype.IDDK_RIGHT_EYE && numAcceptableEyes == 1) || isGrayZone)
						{
							System.out.print("Do you want to proceed anyway?\n\t1. Yes.\n\t2. No (default).");
							System.out.print("\nEnter your choice: ");
							option = Iddk2000Utils.choose_option(2);
							if(option == -1)
								System.out.print("2\n");
						}

						if(!isGrayZone || option == 1)
						{
							enrollID = Iddk2000Utils.read_string("\nEnter admin ID (less than 32 characters): ");
							iRet = apis.enrollAsAdmin(enrollID);
							if(iRet.getValue() == IddkResult.IDDK_OK)
							{
								System.out.print("\nAdmin \""+enrollID+"\" just enrolled !\n");

								clear_capture();
							}
							else
							{
								System.out.print("\nEnroll admin \""+enrollID+"\" failed !\n");
							}
						}
					}
				}
			}

			if(iRet.getValue() != IddkResult.IDDK_OK)
			{
				handle_error(iRet);
			}

			reset_error_level(iRet);

			/* Let's get back to menu */
			if(option != 9 && option != -1)
			{
				System.out.print("\n\n****************************************************\n\n");
			}

			getchar();
		}

		if(exitProgram)
		{
			finalize_device();
			System.exit(Iddk2000Utils.SUCCESS);
		}
	}


	/**************************************************************************
	*	This function demonstrates how to get useful information from the SDK.
	*	These functions are taken into account:
	*	- getSdkVersion
	*	- getSdkDescription
	*	- getSdkConfig
	**************************************************************************/
	void get_information()
	{
		/* SDK Version */
		int sdkVersion;
		int majorNumber;
		int minorNumber;
		int buildNumber;

		/* SDK description */
		StringBuffer infoBuff = new StringBuffer(256);

		/* SDK configuration */
		IddkConfig iddkConfig = new IddkConfig();
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);

		/* Get the SDK version and show it to user */
		System.out.print("\nSDK version:");
		sdkVersion = Iddk2000Apis.getSdkVersion();
		majorNumber = (sdkVersion & 0xff000000) >> 24;
		minorNumber = (sdkVersion & 0x00ff0000) >> 16;
		buildNumber = sdkVersion & 0x0000ffff;
		System.out.println(majorNumber + "." + minorNumber + "." + buildNumber);

		/* Get the SDK description and show it to user */
		System.out.print("\nSDK description: ");
		iRet = Iddk2000Apis.getSdkDescription(infoBuff);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			System.out.println(infoBuff);
		}
		else
		{
			System.out.println("Cannot get IDDK description !");
			handle_error(iRet);
			return;
		}

		/* Get the current SDK configuration */
		System.out.println("\nCurrent SDK configuration:");
		iRet = Iddk2000Apis.getSdkConfig(iddkConfig);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			System.out.println("\tCommunication type: " + ((iddkConfig.getCommStd().getValue() == IddkCommStd.IDDK_COMM_USB)?"USB":((iddkConfig.getCommStd().getValue() == IddkCommStd.IDDK_COMM_UART)?"UART":"Unknown")));
			System.out.println("\tLogging: " + (iddkConfig.isEnableLog()?"Enable":"Disable"));
			if(iddkConfig.getCommStd().getValue() == IddkCommStd.IDDK_COMM_UART)
			{
				System.out.println("\tUART baudrate: " + iddkConfig.getUartBaudrate());
			}
		}
		else
		{
			System.out.println("Cannot get IDDK configuration !");
		}

		if(iRet.getValue() != IddkResult.IDDK_OK)
		{
			handle_error(iRet);
		}
		else
		{
			get_device_information();
		}
	}

	/*******************************************************************************
	*	This function demonstrates how to get useful information from current device.
	*	These functions are taken into account:
	*	- getDeviceInformation
	********************************************************************************/
	void get_device_information()
	{
		IddkDeviceInfo deviceInfo = new IddkDeviceInfo();
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);

		/* Get device information */
		iRet = apis.getDeviceInfo(deviceInfo);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			System.out.println(String.format("\tProduct ID\t\t\t0x%04X", deviceInfo.getProductId().getValue()));
			System.out.println(String.format("\tProduct Name\t\t\t%s", deviceInfo.getProductName()));
			System.out.println(String.format("\tDevice Model\t\t\t%s", (deviceInfo.isBinocular() == false)?"Monocular":"binocular"));
			System.out.println(String.format("\tSerial Number\t\t\t%s", deviceInfo.getSerialNumber()));
			System.out.println(String.format("\tDevice Properties Flag\t\t0x%08X", deviceInfo.getPropertyFlag()));
			System.out.println(String.format("\tKernel Version\t\t\t%d.%d", deviceInfo.getKernelVersion(), deviceInfo.getKernelRevision()));
			if(deviceInfo.getDeviceFeatures() != 0)
			{
				System.out.println("\tDevice Additional Features:");
				if((deviceInfo.getDeviceFeatures() & IddkFeature.IDDK_DEVICE_FEATURE_TEMPLATE_GENERATION) != 0)
					System.out.println("\t\t- Template generation");
				if((deviceInfo.getDeviceFeatures() & IddkFeature.IDDK_DEVICE_FEATURE_TEMPLATE_COMPARISON) != 0)
					System.out.println("\t\t- Template comparison");
			}
		}
		else
		{
			System.out.println("Cannot get device information !");
		}

		if(iRet.getValue() != IddkResult.IDDK_OK)
		{
			handle_error(iRet);
		}

		reset_error_level(iRet);

		getchar();
	}

	/***********************************************************************************************
	*	This function demonstrates how to access to an attached device. At first, it asks user about
	*	the device information such as: USB or UART, Baudrate in case UART is in use.
	*	It changes the SDK default config with user inputs by using setSdkConfig.
	*	Finally, it opens the attached device to acquire the device handle, g_hDevice.
	*	These functions are taken into account:
	*	- setSdkConfig
	*	- scanDevices
	*	- openDevice
	*	Note: This function is called only once before user trying to access to device
	************************************************************************************************/
	void open_device()
	{
		int i = 0;
		int option = -1;
		IddkConfig config = new IddkConfig();
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		int baudrate = 115200;
		ArrayList<String> ppDeviceDescs = new ArrayList<String>();
		String[] comm_methods={
			"USB",
			"UART"
		};
		InputStreamReader inputStream = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(inputStream);
		String portName = "";

		apis = new Iddk2000Apis();

		/* We should get the current configuration before setting new one */
		iRet = Iddk2000Apis.getSdkConfig(config);
		if(iRet.getValue() != IddkResult.IDDK_OK)
		{
			/* Oops ! Something wrong happens */
			handle_error(iRet);
			return;
		}

		/* Now we check the configuration of the currently attached device to change SDK configuration accordingly */
		config.setCommStd(IddkCommStd.IDDK_COMM_USB);

		/* Set baudrate if the communication type is UART */
		if(config.getCommStd().getValue() == IddkCommStd.IDDK_COMM_UART)
		{
			while(true)
			{
				baudrate = Iddk2000Utils.read_uint("\nEnter UART baudrate (not less than 56000, press enter for default value 115200): ", 56000, 916000, 115200);
				if(Iddk2000Utils.check_supported_baudrate(baudrate))
				{
					config.setUartBaudrate(baudrate);
					break;
				}
				else
				{
					System.out.println("Unsupported baudrate, enter again !");
				}
			}

			/********************************************************************************************************
			** NOTE: The device must be also set the same baudrate as specified value above via device_configuration
			*********************************************************************************************************/

			System.out.println("\nEnable hardware flow control: \n\t1. Disable (default) \n\t2. Enable");
			System.out.println("\nEnter your choice: ");
			switch(Iddk2000Utils.choose_option(2))
			{
			case -1:
				System.out.println("1\n");
			case 1:
				config.setUartFlowControl(IddkFlowControl.IDDK_UART_FC_NONE);
				break;
			case 2:
				config.setUartFlowControl(IddkFlowControl.IDDK_UART_FC_HW);
			}
		}

		/* Set new configuration */
		iRet = Iddk2000Apis.setSdkConfig(config);
		if(iRet.getValue() != IddkResult.IDDK_OK)
		{
			System.out.println("\nFailed to set new configuration !");
			handle_error(iRet);
		}

		/* Now, we can open device */
		/* If USB, we should scan devices first */
		if(config.getCommStd().getValue() == IddkCommStd.IDDK_COMM_USB)
		{
			System.out.print("\nScan devices ... ");
			iRet = Iddk2000Apis.scanDevices(ppDeviceDescs);
			if(iRet.getValue() != IddkResult.IDDK_OK)
			{
				if(iRet.getValue() == IddkResult.IDDK_DEVICE_NOT_FOUND)
				{
					System.out.println("No IriTech devices found.");
				}
				handle_error(iRet);
				System.out.print("Program exits ...\n");
				System.exit(0);
			}
			System.out.println(ppDeviceDescs.size() + " devices found !\n");
			for(i = 0; i < ppDeviceDescs.size(); i++)
			{
				System.out.println("\t"+ (i+1) +". "+ ppDeviceDescs.get(i));
			}

			/* Open the first found device */
			System.out.print("\nOpen device " + ppDeviceDescs.get(0) + " ... ");
			iRet = apis.openDevice(ppDeviceDescs.get(0));
			if(iRet.getValue() == IddkResult.IDDK_OK)
			{
				System.out.println("done.");
			}
			else
			{
				System.out.println("failed.");
				System.out.print("Program exits ...\n");
				System.exit(0);
			}
		}
		else /* UART */
		{
			while(true)
			{
				System.out.print("\nPlease enter the port name (COM1) or device node on UNIX (tty0): ");
				try {
					portName = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(portName.length() > 0) break;
			}
			System.out.print("\nOpen device ... ");
			iRet = apis.openDevice(portName);
			if(iRet.getValue() == IddkResult.IDDK_OK)
			{
				System.out.println("done.");
			}
			else
			{
				System.out.println("failed.");
				System.out.print("Program exits ...\n");
				System.exit(0);
			}
		}

		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			IddkDeviceInfo deviceInfo = new IddkDeviceInfo();
			iRet = apis.getDeviceInfo(deviceInfo);
			if(iRet.getValue() == IddkResult.IDDK_OK)
				g_isBinocular = deviceInfo.isBinocular();
		}

		if(iRet.getValue() != IddkResult.IDDK_OK)
		{
			handle_error(iRet);
		}

		reset_error_level(iRet);
	}

	/********************************************************************************************
	*	This function shows example to get the result image from the camera device after it finishes
	*	capturing best iris image
	*********************************************************************************************/
	void get_result_image(int times)
	{
		/* For result image */
		ArrayList<IddkImage> resultImage = new ArrayList<IddkImage>();
		boolean captureImage = false;
		int nCompressRatio = 1;
		IddkImageFormat imageFormat = new IddkImageFormat(IddkImageFormat.IDDK_IFORMAT_MONO_RAW);
		IddkImageKind imageKind = new IddkImageKind(IddkImageKind.IDDK_IKIND_K1);
		IddkImage imageData = new IddkImage();

		/* Other params */
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);

		captureImage = true;

		if(captureImage)
		{
			imageKind.setValue(IddkImageKind.IDDK_IKIND_K3);
			imageFormat.setValue(IddkImageFormat.IDDK_IFORMAT_MONO_JPEG2000);

			/* OK, we just ask compress ratio in case imageFormat = IDDK_IFORMAT_MONO_JPEG2000 */
			if(imageFormat.getValue() == IddkImageFormat.IDDK_IFORMAT_MONO_JPEG2000 || imageFormat.getValue() == IddkImageFormat.IDDK_IFORMAT_IRITECH_JPEG2000)
			{
				nCompressRatio = 100;
			}

			/* We have enough information to get result image */
			System.out.print("\nGet result image ...");
			iRet = apis.getResultImage(imageKind, imageFormat, (byte)nCompressRatio, resultImage);
			if(iRet.getValue() != IddkResult.IDDK_OK && iRet.getValue() != IddkResult.IDDK_SE_RIGHT_FRAME_UNQUALIFIED && iRet.getValue() != IddkResult.IDDK_SE_LEFT_FRAME_UNQUALIFIED)
			{
				/* Oops, no qualified image at all */
				System.out.print("Cannot get result image !\n");
				handle_error(iRet);
			}
			else
			{
				System.out.print("done\n");

				if (iRet.getValue() == IddkResult.IDDK_SE_LEFT_FRAME_UNQUALIFIED)
				{
					System.out.print("\t\tOnly right image is qualified.\n");
				}
				else if (iRet.getValue() == IddkResult.IDDK_SE_RIGHT_FRAME_UNQUALIFIED)
				{
					System.out.print("\t\tOnly left image is qualified.\n");
				}

				reset_error_level(iRet);

				//IddkEyeSubtype eyeSubtype = new IddkEyeSubtype();
				IddkEyeSubtype[] eyeSubtype = {new IddkEyeSubtype(1), new IddkEyeSubtype(2)};
				if(resultImage.size() == 1) eyeSubtype[0].setValue(0);

				for(int eyeIdx = 0; eyeIdx < resultImage.size(); eyeIdx++)
				{
					if(resultImage.get(eyeIdx) != null)
						Iddk2000Utils.save_result_image(resultImage.get(eyeIdx), eyeSubtype[eyeIdx]);
				}

				if(imageFormat.getValue() == IddkImageFormat.IDDK_IFORMAT_IRITECH_RAW || imageFormat.getValue() == IddkImageFormat.IDDK_IFORMAT_IRITECH_JPEG2000)
				{
					System.out.print("Do you want to get image data from IriTech image?\n\t1. No (default)\n\t2. Yes\n");
					System.out.print("Enter your choice:");
					switch(Iddk2000Utils.choose_option(2))
					{
					case -1:
						System.out.print("1\n");
					case 1:
						return;
					case 2:
						for(int i = 0; i < resultImage.size(); i++)
						{
							if(resultImage.get(i) != null)
							{
								iRet = apis.getImageData(resultImage.get(i), imageData);
								if(iRet.getValue() == IddkResult.IDDK_OK)
								{
									Iddk2000Utils.save_result_image(imageData, eyeSubtype[i]);
								}
								else
								{
									handle_error(iRet);
								}
							}
						}
					}
				}
			}
		}
	}

	/********************************************************************************************
	*	This function is a helper one. It is used by capturing_process to getResultISOImage after
	*	making a capture.
	*********************************************************************************************/
	void get_result_ISO_image(int times)
	{
		IddkImageFormat imageFormat = new IddkImageFormat(IddkImageFormat.IDDK_IFORMAT_MONO_RAW);
		IddkImageKind imageKind = new IddkImageKind(IddkImageKind.IDDK_IKIND_K1);
		IddkDataBuffer pIsoImage = new IddkDataBuffer();
		IddkIsoRevision isoRevision = new IddkIsoRevision(IddkIsoRevision.IDDK_IISO_2005);
		String isoFile = "";
		boolean captureImage = false;
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		int nCompressRatio = 1;
		IddkEyeSubtype eyeSubtype = new IddkEyeSubtype(0);
		captureImage = false;

		if(captureImage)
		{
			System.out.print("\nSelect image kind: \n\t1. Original Image - K1 (default) \n\t2. VGA Image - K2 \n\t3. Cropped Image - K3\n\t4. Cropped and Masked Image - K7\n");
			System.out.print("Enter your choice: ");
			switch(Iddk2000Utils.choose_option(4))
			{
			case -1:
				System.out.print("1\n");
			case 1:
				imageKind.setValue(IddkImageKind.IDDK_IKIND_K1);
				break;
			case 2:
				imageKind.setValue(IddkImageKind.IDDK_IKIND_K2);
				break;
			case 3:
				imageKind.setValue(IddkImageKind.IDDK_IKIND_K3);
				break;
			case 4:
				imageKind.setValue(IddkImageKind.IDDK_IKIND_K7);
			}

			/* Yeah ! K3, K7 only works with ISO_2011 */
			if(imageKind.getValue() == IddkImageKind.IDDK_IKIND_K3 || imageKind.getValue() == IddkImageKind.IDDK_IKIND_K7)
			{
				isoRevision.setValue(IddkIsoRevision.IDDK_IISO_2011);
			}
			else
			{
				System.out.print("\nSelect ISO revision: \n\t1. Iso 2005 (default)\n\t2. Iso 2011\n");
				System.out.print("Enter your choice: ");
				switch(Iddk2000Utils.choose_option(2))
				{
				case -1:
					System.out.print("1\n");
				case 1:
					isoRevision.setValue(IddkIsoRevision.IDDK_IISO_2005);
					break;
				case 2:
					isoRevision.setValue(IddkIsoRevision.IDDK_IISO_2011);
				}
			}

			System.out.print("\nSelect image format: \n\t1. Mono Raw \n\t2. Mono Jpeg2000 (default)\n");
			System.out.print("Enter your choice: ");
			switch(Iddk2000Utils.choose_option(2))
			{
			case 1:
				imageFormat.setValue(IddkImageFormat.IDDK_IFORMAT_MONO_RAW);
				break;
			case -1:
				System.out.print("2\n");
			case 2:
				imageFormat.setValue(IddkImageFormat.IDDK_IFORMAT_MONO_JPEG2000);
			}

			/* OK, we just ask compress ratio in case imageFormat = IDDK_IFORMAT_MONO_JPEG2000 */
			if(imageFormat.getValue() == IddkImageFormat.IDDK_IFORMAT_MONO_JPEG2000)
			{
				nCompressRatio = Iddk2000Utils.read_uint("\nEnter compress ratio (enter for default): ", 0, 100, 100);
			}

			if(g_isBinocular)
			{
				System.out.print("\nSpecify eye label: \n\t1. Unknown eye(default) \n\t2. Left Eye \n\t3. Right Eye \n");
				System.out.print("Enter your choice: ");
				switch(Iddk2000Utils.choose_option(3))
				{
				case -1:
					System.out.print("1\n");
				case 1:
					eyeSubtype.setValue(IddkEyeSubtype.IDDK_UNKNOWN_EYE);
					break;
				case 2:
					eyeSubtype.setValue(IddkEyeSubtype.IDDK_LEFT_EYE);
					break;
				case 3:
					eyeSubtype.setValue(IddkEyeSubtype.IDDK_RIGHT_EYE);
					break;
				}
			}

			/* Get result ISO image */
			System.out.print("\nGet result ISO image ... ");
			iRet = apis.getResultIsoImage(isoRevision, imageFormat, imageKind, (byte)nCompressRatio, eyeSubtype, pIsoImage);
			if(iRet.getValue() == IddkResult.IDDK_OK
					 || (eyeSubtype.getValue() == IddkEyeSubtype.IDDK_UNKNOWN_EYE && iRet.getValue() == IddkResult.IDDK_SE_LEFT_FRAME_UNQUALIFIED)
					 || (eyeSubtype.getValue() == IddkEyeSubtype.IDDK_UNKNOWN_EYE && iRet.getValue() == IddkResult.IDDK_SE_RIGHT_FRAME_UNQUALIFIED))
			{
				System.out.print("done.\n");
				isoFile = g_binDir + "/IsoImage_"+times+".bin";
				if(Iddk2000Utils.save_file(isoFile, pIsoImage.getData()))
				{
					System.out.print("\n\tSaved ./IsoImage_"+times+".bin\n");
				}
				else
				{
					System.out.print("\n\tSaving ./IsoImage_"+times+".bin failed.\n");
				}
				reset_error_level(iRet);
			}
			else
			{
				System.out.print("\nCannot get result ISO image !\n");
				handle_error(iRet);
			}
		}
	}

	/********************************************************************************************
	*	This function demonstrates how to capture user's eyes, get the result image, and save to
	*	specified folder.
	*	These functions are taken into account:
	*	- initCamera
	*	- startCapture
	*	- getCaptureStatus
	*	- getStreamImage
	*	- getResultImage
	*	- getImageData
	*	- getResultIsoImage
	*	- deinitCamera
	*	@params:
	*	- bDefaultParams: set the default params for StartCapture function or not
	*	- bMultiple: check whether we want to capture many times or just only once
	*	- bProcessResult: after capturing we get result image, so disable this flag to bypass this
	**********************************************************************************************/
	void capturing_process(boolean bDefaultParams, boolean bMultiple, boolean bProcessResult)
	{
		/* For streaming images */
		ArrayList<IddkImage> images = new ArrayList<IddkImage>();

		/* Parameters for capturing */
		IddkCaptureMode captureMode = new IddkCaptureMode(IddkCaptureMode.IDDK_TIMEBASED);
		IddkQualityMode qualityMode = new IddkQualityMode(IddkQualityMode.IDDK_QUALITY_NORMAL);
		boolean bStreamMode = false;
		boolean bAutoLeds = true;
		IddkInteger iCount = new IddkInteger(3);
		IddkEyeSubtype eyeSubtype = new IddkEyeSubtype(0);

		/* Other params */
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		IddkCaptureStatus captureStatus = new IddkCaptureStatus(IddkCaptureStatus.IDDK_IDLE);
		boolean bRun = true;
		IddkInteger imageWidth = new IddkInteger();
		IddkInteger imageHeight = new IddkInteger();
		int i = 0;
		boolean eyeDetected = false;
		int option = -1;
		int times = 0;
		IddkDeviceConfig devConfig = new IddkDeviceConfig();

		ArrayList<IddkIrisQuality> qualities = new ArrayList<IddkIrisQuality>();

		/* We have to init camera first */
		System.out.print("\nInit Camera: \n");
		iRet = apis.initCamera(imageWidth, imageHeight);
		if(iRet.getValue() != IddkResult.IDDK_OK)
		{
			System.out.print("\nFailed to initialize camera\n");
			handle_error(iRet);
			return;
		}
		System.out.print("\n\tImage width: "+imageWidth.getValue()+"\n");
		System.out.print("\tImage height: "+imageHeight.getValue()+"\n");

		/* OK, we capture many times until user exits */
		while(true)
		{
			/* Init variables in inner loop */
			i = 0;
			bRun = true;
			eyeDetected = false;
			times++;

			/* Ask user to fill in all parameters again */
			if(!bDefaultParams)
			{
				if(g_isBinocular)
					Iddk2000Utils.prepare_param_for_capturing(captureMode, qualityMode, iCount, eyeSubtype);
				else
					Iddk2000Utils.prepare_param_for_capturing(captureMode, qualityMode, iCount, null);

				bAutoLeds = true;
			}

			iRet = apis.getDeviceConfig(devConfig);
			if(iRet.getValue() == IddkResult.IDDK_OK)
			{
				bStreamMode = devConfig.isEnableStream();
			}
			else
			{
				bStreamMode = false;
				handle_error(iRet);
				apis.deinitCamera();
				reset_error_level(iRet);
				return;
			}
			reset_error_level(iRet);

			/* Now, we capture user's eyes */
			System.out.print("\nPut your eyes in front of the camera\n\n");
			/* Note that streamFormat and nCompressRatio just work with UART in StartCapture */
			iRet = apis.startCapture(captureMode, iCount.getValue(), qualityMode, new IddkCaptureOperationMode(IddkCaptureOperationMode.IDDK_AUTO_CAPTURE), eyeSubtype, bAutoLeds, null);
			/* If you want to use captureProc, use this code instead
			 * CaptureProc captureProc = new CaptureProc();
			 * iRet = apis.startCapture(captureMode, iCount.getValue(), qualityMode, new IddkCaptureOperationMode(IddkCaptureOperationMode.IDDK_AUTO_CAPTURE), bAutoLeds, captureProc);
			 */
			if(iRet.getValue() != IddkResult.IDDK_OK)
			{
				/* Remember to deinit camera */
				handle_error(iRet);
				apis.deinitCamera();
				return;
			}

			/*	Start a loop to check the device status.
				(You can use CaptureProc instead of this while loop).
			*/
			System.out.print("Scanning for eyes");
			while(bRun)
			{
				if(bStreamMode)
				{
					iRet = apis.getStreamImage(images, captureStatus);
					if(iRet.getValue() == IddkResult.IDDK_OK)
					{
						//TODO/////////////////////////////////////////////////////////////////
						//
						// Your code to process stream image.
						//
						///////////////////////////////////////////////////////////////////////
					}
					else if(iRet.getValue() == IddkResult.IDDK_SE_NO_FRAME_AVAILABLE)
					{
						// when GetStreamImage returns IDDK_SE_NO_FRAME_AVAILABLE,
						// it does not always mean that capturing process has been finished or encountered problems.
						// It may be because new stream images are not available.
						// We need to query the current capture status to know what happens.
						iRet = apis.getCaptureStatus(captureStatus);
					}
				}
				else
				{
					iRet = apis.getCaptureStatus(captureStatus);
				}

				/* If GetStreamImage and GetCaptureStatus cause no error, process the capture status.*/
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					if(captureStatus.getValue() == IddkCaptureStatus.IDDK_CAPTURING)
					{
						if(!eyeDetected)
						{
							System.out.print("\n\n\tEyes are detected.\n");
							eyeDetected = true;
						}
					}
					else if(captureStatus.getValue() == IddkCaptureStatus.IDDK_COMPLETE)
					{
						/* capture has finished */
						bRun = false;
					}
					else if(captureStatus.getValue() == IddkCaptureStatus.IDDK_ABORT)
					{
						/* capture has been aborted */
						System.out.print("\n\n\tCapture aborted\n");
						bRun = false;
					}
					else
					{
						System.out.print(".");
						/* We set up a counter to break the loop if user doesn't place the eyes in front of the camera */
						i++;
						if (i > 300)
						{
							bRun = false;
							System.out.print("\n\tOops! No eyes detected for so long. Abort the current capture.\n");
						}
					}
					if(!bStreamMode)
					{
						/*	GetStreamImage is a blocking function
						*	that automatically waits for the next new stream images.
						*	However, GetCaptureStatus is a non-blocking function.
						*	It returns immediately the current capture status.
						*	To prevent CPU stress, we implement a wait heres
						*	before calling the next GetCaptureStatus.
						*/
						Iddk2000Utils.wait(60);
					}
				}
				else
				{
					/* handle error and terminate this capture */
					handle_error(iRet);
					bRun = false;
				}
			}

			/* Try to stop capturing for sure even though it might be stopped */
			iRet = apis.stopCapture();
			if(iRet.getValue() != IddkResult.IDDK_OK)
			{
				handle_error(iRet);
				apis.deinitCamera();
				reset_error_level(iRet);
				return;
			}

			iRet = apis.getResultQuality(qualities);
			if(iRet.getValue() == IddkResult.IDDK_OK || iRet.getValue() == IddkResult.IDDK_SE_RIGHT_FRAME_UNQUALIFIED || iRet.getValue() == IddkResult.IDDK_SE_LEFT_FRAME_UNQUALIFIED)
			{

                if (qualities.size() == 1)
                {
                    // monocular device model
                	if(qualities.get(0) != null)
                		System.out.print("\nQuality of the current captured image:\n\n\t1. Total score: " + qualities.get(0).getTotalScore() + "\n\t2. Usable area: " + qualities.get(0).getUsableArea() + "\n");
                }
                else
                {
                    // binocular device model
                	System.out.print("\nQuality of the current captured images:\n\t");

                	if(qualities.get(0) != null)
                		System.out.print("1. Total score of right eye: " + qualities.get(0).getTotalScore() + "\n\t2. Usable area of right eye: " + qualities.get(0).getUsableArea() + "\n");

                	if(qualities.get(1) != null)
                	System.out.print("\t3. Total score of left eye: " + qualities.get(1).getTotalScore() + "\n\t4. Usable area of left eye: " + qualities.get(1).getUsableArea() + "\n");
                }
			}
			else
			{
				handle_error(iRet);
				apis.deinitCamera();
				reset_error_level(iRet);
				return;
			}

			if(bProcessResult && (captureStatus.getValue() == IddkCaptureStatus.IDDK_COMPLETE))
			{
				/* Now we have time to get the result image */
				get_result_image(times);

				/* Phew ! get result ISO image */
				get_result_ISO_image(times);
			}

			/* iris_recognition calls, we break the loop */
			if(!bMultiple)
			{
				apis.deinitCamera();
				return;
			}

			/* Let's try another capturing? */
			System.out.print("\nTry another capture?\n\t1. Yes (default)\n\t2. No");
			System.out.print("\nEnter your choice: ");
			option = Iddk2000Utils.choose_option(2);

			if(option == -1) System.out.print("1\n");
			if(option == 2)
			{
				apis.deinitCamera();
				return;
			}

			/* Number of capturing */
			times++;
		}
	}

	/********************************************************************************************
	*	This function is a helper one. It is used by iris_recognition to getResultTemplate after
	*	making a capture.
	*********************************************************************************************/
	void get_result_template(int times)
	{
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		IddkCaptureStatus captureStatus = new IddkCaptureStatus(IddkCaptureStatus.IDDK_IDLE);
		IddkDataBuffer pTemplate = new IddkDataBuffer();
		String resultTemplateFile = "";

		/* We check the camera status */
		iRet = apis.getCaptureStatus(captureStatus);
		if(iRet.getValue() == IddkResult.IDDK_OK || iRet.getValue() == IddkResult.IDDK_SE_LEFT_FRAME_UNQUALIFIED || iRet.getValue() == IddkResult.IDDK_SE_RIGHT_FRAME_UNQUALIFIED)
		{
			reset_error_level(iRet);
			if(captureStatus.getValue() == IddkCaptureStatus.IDDK_COMPLETE)
			{
				/* Do main job here */
				System.out.print("\nGet result template ...");

				iRet = apis.getResultTemplate(pTemplate);
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print("done.\n");
					resultTemplateFile = g_binDir + "/ResultTemplate_"+times+".tpl";
					if(Iddk2000Utils.save_file(resultTemplateFile, pTemplate.getData()))
					{
						System.out.print("\n\tSaved ./ResultTemplate_"+times+".tpl.\n");
					}
					else
					{
						System.out.print("\n\tSaving ./ResultTemplate_"+times+".tpl failed.\n");
					}
				}
				else
				{
					handle_error(iRet);
				}
			}
			else
			{
				System.out.print("\nNo iris image is captured. Start a capture first!\n");
			}
		}
		else
		{
			/* It is abnormal here */
			handle_error(iRet);
		}
	}

	/********************************************************************************************
	*	This function is a helper one. It is used by iris_recognition to enroll current captured
	*	iris to device after making a capture
	*********************************************************************************************/
	void enroll_capture()
	{
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		IddkCaptureStatus captureStatus = new IddkCaptureStatus(IddkCaptureStatus.IDDK_IDLE);
		int option = -1;
		String enrollID = "";
		IddkSystemRole userRole = new IddkSystemRole(IddkSystemRole.IDDK_SYS_ROLE_ADMIN);

		boolean isGrayZone = false;
		int numAcceptableEyes = 0;

		if(!check_image_quality(true, isGrayZone, numAcceptableEyes))
		{
			return;
		}

		if (g_isBinocular && g_selectEyeMode.getValue() != IddkEyeSubtype.IDDK_LEFT_EYE && g_selectEyeMode.getValue() != IddkEyeSubtype.IDDK_RIGHT_EYE && numAcceptableEyes == 1)
		{
			System.out.print("User chose to capture both eyes but only one is qualified for the enrollment.\n");
		}
		if (isGrayZone) {
			System.out.print("The captured image(s) is enrollable but not in sufficient quality to warrant the best accuracy." +
				"\nThe subject is recommended to have his/her iris image recaptured with the eye opened widely.\n");
		}

		if ((g_isBinocular && g_selectEyeMode.getValue() != IddkEyeSubtype.IDDK_LEFT_EYE && g_selectEyeMode.getValue() != IddkEyeSubtype.IDDK_RIGHT_EYE && numAcceptableEyes == 1)
				|| isGrayZone)
		{
			System.out.print("Do you want to proceed anyway?\n\t1. Yes.\n\t2. No (default).");
			System.out.print("\nEnter your choice: ");
			option = Iddk2000Utils.choose_option(2);
			if(option == -1) System.out.print("2\n");
			if(option == -1 || option == 2)
			{
				clear_capture();
				return;
			}
		}

		/* Ask some questions */
		System.out.print("\nDo you want to enroll the current captured iris image(s) into the gallery? \n");
		System.out.print("\t1. Yes (default)\n\t2. No\n");
		System.out.print("Enter your choice: ");
		option = Iddk2000Utils.choose_option(2);
		if(option == -1) System.out.print("1\n");
		if(option == -1 || option == 1)
		{
			enrollID = Iddk2000Utils.read_string("\nEnter enrollee ID (less than 32 characters): ");
		}
		else if(option == 2)
		{
			return;
		}

		/* Do main job here */
		System.out.print("\nEnroll iris ... ");

		iRet = apis.enrollCapture(enrollID);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			System.out.print("done.\n");

			System.out.print("\nDo you want to set user role for the current captured iris? \n");
			System.out.print("\t1. No (default)\n\t2. Yes\n");
			System.out.print("Enter your choice: ");
			option = Iddk2000Utils.choose_option(2);
			if(option == -1) System.out.print("1\n");
			if(option == -1 || option == 1)
			{
				//Do nothing here
			}
			else
			{
				System.out.print("\nWhich user role? \n");
				System.out.print("\t1. Superuser (default)\n\t2. User\n");
				System.out.print("Enter your choice: ");
				option = Iddk2000Utils.choose_option(2);
				switch(option)
				{
				case -1:
					System.out.print("1\n");
				case 1:
					userRole.setValue(IddkSystemRole.IDDK_SYS_ROLE_SUPERUSER);
					break;
				case 2:
					userRole.setValue(IddkSystemRole.IDDK_SYS_ROLE_USER);
				}

				iRet = apis.setUserRole(enrollID, userRole);
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print(enrollID + " is set to "+((userRole.getValue() == IddkSystemRole.IDDK_SYS_ROLE_SUPERUSER)?"Superuser":"User")+"\n");
				}
				else
				{
					handle_error(iRet);
				}
			}

			/* Commit to gallery */
			iRet = apis.commitGallery();
			if(iRet.getValue() != IddkResult.IDDK_OK)
			{
				System.out.print("\nCommitGallery... failed\n");
				handle_error(iRet);
			}

			clear_capture();
		}
		else
		{
			System.out.print("failed. ");
			if(iRet.getValue() == IddkResult.IDDK_GAL_ID_SLOT_FULL || iRet.getValue() == IddkResult.IDDK_GAL_ID_NOT_ENOUGH_SLOT)
			{
				System.out.print("Maximum number of irises for this enrollee has been reached!\n");
			}
			else if(iRet.getValue() == IddkResult.IDDK_GAL_FULL)
			{
				System.out.print("Gallery is full now!\n");
			}
			else
			{
				handle_error(iRet);
			}
		}
	}

	/********************************************************************************************
	*	This function is a helper one. It is used by iris_recognition to enrollTemplate.
	*********************************************************************************************/
	void enroll_template()
	{
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		InputStreamReader inputStream = new InputStreamReader(System.in);
		BufferedReader c = new BufferedReader(inputStream);

		/* For EnrollTemplates */
		IddkDataBuffer pEnrollTemplate = new IddkDataBuffer();
		String enrollTemplateFile = "";
		String enrollID = null;

		System.out.print("\nPlease specify the template file you want to enroll.");

		System.out.print("\nTemplate file (enter empty path to quit): ");
		try {
			enrollTemplateFile = c.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* empty file name, end of template input */
		if(enrollTemplateFile.length() == 0)
		{
			return;
		}
		System.out.print("Reading file "+enrollTemplateFile+"...");
		if(Iddk2000Utils.read_file(enrollTemplateFile, pEnrollTemplate))
		{
			System.out.print("done.\n");
		}
		else
		{
			System.out.print("Cannot read the file !\n");
			return;
		}


		enrollID = Iddk2000Utils.read_string("\nEnter enrollee ID (less than 32 characters): ");

		/* We enroll template now */
		System.out.print("\nEnroll template ... ");
		iRet = apis.enrollTemplate(enrollID, pEnrollTemplate);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			System.out.print("done\n");

			/* Save the changes */
			System.out.print("Commit gallery changes...");
			iRet = apis.commitGallery();
			if(iRet.getValue() != IddkResult.IDDK_OK)
			{
				System.out.print("failed.");
				handle_error(iRet);
			}
			else
			{
				System.out.print("done.\n");
			}
		}
		else
		{
			System.out.print("failed. ");
			if(iRet.getValue() == IddkResult.IDDK_GAL_ID_SLOT_FULL || iRet.getValue() == IddkResult.IDDK_GAL_ID_NOT_ENOUGH_SLOT)
			{
				System.out.print("Maximum number of irises for this enrollee has reached!\n");
			}
			else if(iRet.getValue() == IddkResult.IDDK_GAL_FULL)
			{
				System.out.print("Gallery is full now!\n");
			}
			else
			{
				handle_error(iRet);
			}
		}

		reset_error_level(iRet);
	}

	/********************************************************************************************
	*	This function is a helper one. It is used by iris_recognition to unenrollTemplate.
	*********************************************************************************************/
	void unenroll_templates()
	{
		String pUnenrolledID = null;
		int option = 0;
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		String enrollID = null;

		System.out.print("Unenroll all templates in gallery?\n\t1. No (default)\n\t2. Yes\n");
		System.out.print("Enter your choice: ");
		option = Iddk2000Utils.choose_option(2);
		if(option == -1) System.out.print("1\n");
		if(option == 1 || option == -1)
		{
			/* don't accept default value */
			enrollID = Iddk2000Utils.read_string("\nEnter enrollee ID (less than 32 characters): ");
			pUnenrolledID = enrollID;
		}
		if(pUnenrolledID == null)
			System.out.print("\nUnenroll all templates ... ");
		else
			System.out.print("\nUnenroll templates of enrollee "+pUnenrolledID+" ... ");

		iRet = apis.unenrollTemplate(pUnenrolledID);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			System.out.print("done.\n");
			System.out.print("Commit gallery changes...");
			iRet = apis.commitGallery();
			if(iRet.getValue() == IddkResult.IDDK_OK)
			{
				System.out.print("done.\n");
			}
			else
			{
				System.out.print("failed.\n");
				handle_error(iRet);
			}
		}
		else
		{
			System.out.print("failed.\n");
			handle_error(iRet);
		}

		reset_error_level(iRet);
	}

	/********************************************************************************************
	*	This function is a helper one. It is used by iris_recognition to getGalleryInfo.
	*********************************************************************************************/
	void get_gallery_information()
	{
		ArrayList<String> pEnrollIds = new ArrayList<String>();
		IddkInteger nUsedSlots = new IddkInteger();
		IddkInteger nMaxSlots = new IddkInteger();
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		int option = 0;

		System.out.print("Which gallery do you want to get information?\n\t1. User (default)\n\t2. Administrator\n");
		System.out.print("Enter your choice: ");
		option = Iddk2000Utils.choose_option(2);
		if(option == -1) System.out.print("1\n");
		if(option == 1 || option == -1)
		{
			System.out.print("\nGet gallery information ... ");
			iRet = apis.getGalleryInfo(pEnrollIds, nUsedSlots, nMaxSlots);
			if(iRet.getValue() == IddkResult.IDDK_OK)
			{
				System.out.print("done.\n");
				System.out.print("\n\tNumber of enrollees = "+pEnrollIds.size()+"\n");
				for(int i = 0;i < pEnrollIds.size();i++)
				{
					System.out.print(pEnrollIds.get(i) + " ");
				}
				System.out.print("\n\tNumber of used slots = "+nUsedSlots+"\n");
				System.out.print("\tMaximum number of slots = "+nMaxSlots+"\n");
			}
			else
			{
				handle_error(iRet);
			}
		}
		else
		{
			System.out.print("\nGet admin gallery information ... ");
			iRet = apis.getAdminGalleryInfo(pEnrollIds, nUsedSlots, nMaxSlots);
			if(iRet.getValue() == IddkResult.IDDK_OK)
			{
				System.out.print("done.\n");
				System.out.print("\n\tNumber of admins = "+pEnrollIds.size()+"\n");
				for(int i = 0;i < pEnrollIds.size();i++)
				{
					System.out.print(pEnrollIds.get(i) + " ");
				}
				System.out.print("\n\tNumber of used slots = "+nUsedSlots+"\n");
				System.out.print("\tMaximum number of slots = "+nMaxSlots+"\n");
			}
			else
			{
				handle_error(iRet);
			}
		}

		reset_error_level(iRet);
	}

	void process_matching_result(float distance, String enrollID)
	{
		//Success case
		if(distance <= 1.0f)
		{
			if(enrollID != null)
				System.out.print("Matched with '"+enrollID+"'!!!\n");
			else
				System.out.print("Matched!!!\n");
		}
		else if(distance > 1.05f)
			// Matching failed
		{
			System.out.print("No match found!\n");
		}
		else
		{
			// Grey Zone: we are not sure about the result due to bad image quality, users should make a capture again ...
			System.out.print("The quality of this image may be not good enough.\r\nPlease re-capture another image and try again!\n");
		}
	}

	/********************************************************************************************
	*	This function is a helper one. It is used by iris_recognition to compare11 or compare1N.
	*********************************************************************************************/
	void my_compare_templates(String templateFile)
	{
		IddkFloat compareDis = new IddkFloat(1000.0f);
		int nComparisonResults = 0;
		ArrayList<IddkComparisonResult> pComparisonResults = new ArrayList<IddkComparisonResult>();
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		int option = 0;
		IddkCaptureStatus captureStatus = new IddkCaptureStatus(IddkCaptureStatus.IDDK_IDLE);
		String enrollID = null;
		int i = 0;
		IddkDataBuffer templateData = new IddkDataBuffer();
		InputStreamReader inputStream = new InputStreamReader(System.in);
		BufferedReader c = new BufferedReader(inputStream);
		float minDis = 3.0f;
		String minEnrollID = null;
		float maxDistance = 1.1f;

		/* Remember that before doing any identification or verification.
		 * We should check the quality of the current captured image.
		 */
		boolean isGrayZone = false;
		int numAcceptableEyes = 0;
		if(!check_image_quality(false, isGrayZone, numAcceptableEyes))
		{
			return;
		}

		/* We check the camera status */
		iRet = apis.getCaptureStatus(captureStatus);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			if(captureStatus.getValue() == IddkCaptureStatus.IDDK_COMPLETE)
			{
				/* empty file name, end of template input */
				if(templateFile.length() == 0)
				{
					return;
				}
				System.out.print("Reading file "+templateFile+"...");
				if(Iddk2000Utils.read_file(templateFile, templateData))
				{
					System.out.print("done.\n");
				}
				else
				{
					System.out.print("Cannot read the file !\n");
					return;
				}

				System.out.print("Compare captured image with specified template ... ");
				iRet = apis.compare11WithTemplate(templateData, compareDis);
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print("done.");
					System.out.print("\n\tCompare Distance = "+compareDis.getValue()+"\n");

					process_matching_result(compareDis.getValue(), null);

					clear_capture();
				}
				else if(iRet.getValue() == IddkResult.IDDK_GAL_EMPTY)
				{
					System.out.print("failed. Gallery is empty !\n");
				}
				else if(iRet.getValue() == IddkResult.IDDK_GAL_ID_NOT_EXIST)
				{
					System.out.print("failed. Enrollee ID does not exist!\n");
				}
				else if(iRet.getValue() == IddkResult.IDDK_SE_NO_QUALIFIED_FRAME)
				{
					System.out.print("failed. No qualified image!\n");
				}
				else
				{
					System.out.print("failed.\n");
					handle_error(iRet);
				}
			}
			else
			{
				System.out.print("\nNo iris image is captured. Start a capture first!\n");
			}
		}
		else
		{
			/* It is abnormal here */
			handle_error(iRet);
		}
	}

	void compare_templates()
	{
		IddkFloat compareDis = new IddkFloat(1000.0f);
		int nComparisonResults = 0;
		ArrayList<IddkComparisonResult> pComparisonResults = new ArrayList<IddkComparisonResult>();
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		int option = 0;
		IddkCaptureStatus captureStatus = new IddkCaptureStatus(IddkCaptureStatus.IDDK_IDLE);
		String enrollID = null;
		int i = 0;
		IddkDataBuffer templateData = new IddkDataBuffer();
		String templateFile = null;
		InputStreamReader inputStream = new InputStreamReader(System.in);
		BufferedReader c = new BufferedReader(inputStream);
		float minDis = 3.0f;
		String minEnrollID = null;
		float maxDistance = 1.1f;

		/* Remember that before doing any identification or verification.
		 * We should check the quality of the current captured image.
		 */
		boolean isGrayZone = false;
		int numAcceptableEyes = 0;
		if(!check_image_quality(false, isGrayZone, numAcceptableEyes))
		{
			return;
		}

		System.out.print("\nSelect one kind of comparison: \n");
		System.out.print("\t1. Compare11 (default)\n\t2. Compare1N\n\t3. Compare11WithTemplate");
		System.out.print("\nEnter your choice: ");
		option = Iddk2000Utils.choose_option(3);
		switch(option)
		{
		case -1:
			System.out.print("1\n");
		case 1:
			/* Compare11 */
			/* We check the camera status */
			iRet = apis.getCaptureStatus(captureStatus);
			if(iRet.getValue() == IddkResult.IDDK_OK)
			{
				if(captureStatus.getValue() == IddkCaptureStatus.IDDK_COMPLETE)
				{
					enrollID = Iddk2000Utils.read_string("\nEnter enrollee ID (less than 32 characters): ");
					System.out.print("Compare captured images with templates from enrollee "+enrollID+"...");
					iRet = apis.compare11(enrollID, compareDis);
					if(iRet.getValue() == IddkResult.IDDK_OK)
					{
						System.out.print("done.");
						System.out.print("\n\tCompare Distance = "+compareDis.getValue()+"\n");

						process_matching_result(compareDis.getValue(), enrollID);

						clear_capture();
					}
					else if(iRet.getValue() == IddkResult.IDDK_GAL_EMPTY)
					{
						System.out.print("failed. Gallery is empty !\n");
					}
					else if(iRet.getValue() == IddkResult.IDDK_GAL_ID_NOT_EXIST)
					{
						System.out.print("failed. Enrollee ID does not exist!\n");
					}
					else if(iRet.getValue() == IddkResult.IDDK_SE_NO_QUALIFIED_FRAME)
					{
						System.out.print("failed. No qualified image!\n");
					}
					else
					{
						System.out.print("failed.\n");
						handle_error(iRet);
					}
				}
				else
				{
					System.out.print("\nNo iris image is captured. Start a capture first!\n");
				}
			}
			else
			{
				/* It is abnormal here */
				handle_error(iRet);
			}
			break;
		case 2:
			/* Compare1N */
			/* We check the camera status */
			iRet = apis.getCaptureStatus(captureStatus);
			if(iRet.getValue() == IddkResult.IDDK_OK)
			{
				if(captureStatus.getValue() == IddkCaptureStatus.IDDK_COMPLETE)
				{
					maxDistance = Iddk2000Utils.read_float("Please specify max distance (enter for default value 0): ", 0.0f, 100.0f, 0.0f);
					System.out.print("Compare captured images with all enrollees in the gallery...");
					iRet = apis.compare1N(maxDistance, pComparisonResults);
					nComparisonResults = pComparisonResults.size();
					if(iRet.getValue() == IddkResult.IDDK_OK)
					{
						System.out.print("done.\n");
						if(nComparisonResults > 0)
						{
							System.out.print("\nComparison result:\n");
							for(i = 0; i < nComparisonResults; i++)
							{
								System.out.print("\tEnrollee ID = "+pComparisonResults.get(i).getEnrollId()+", Distance = "+pComparisonResults.get(i).getDistance()+" \n");
								if(minDis > pComparisonResults.get(i).getDistance())
								{
									minDis = pComparisonResults.get(i).getDistance();
									minEnrollID = pComparisonResults.get(i).getEnrollId();
								}
							}

							process_matching_result(minDis, minEnrollID);

							clear_capture();
						}
					}
					else if(iRet.getValue() == IddkResult.IDDK_GAL_EMPTY)
					{
						System.out.print("failed. Gallery is empty!\n");
					}
					else if(iRet.getValue() == IddkResult.IDDK_SE_NO_QUALIFIED_FRAME)
					{
						System.out.print("failed. No qualified frame!\n");
					}
					else
					{
						System.out.print("failed.\n");
						handle_error(iRet);
					}
				}
				else
				{
					System.out.print("\nNo iris image is captured. Start a capture first!\n");
				}
			}
			else
			{
				/* It is abnormal here */
				handle_error(iRet);
			}
			break;
		case 3:
			/* We check the camera status */
			iRet = apis.getCaptureStatus(captureStatus);
			if(iRet.getValue() == IddkResult.IDDK_OK)
			{
				if(captureStatus.getValue() == IddkCaptureStatus.IDDK_COMPLETE)
				{
					System.out.print("\nPlease specify the template file you want to enroll.");

					System.out.print("\nTemplate file (enter empty path to quit): ");
					try {
						templateFile = c.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}

					/* empty file name, end of template input */
					if(templateFile.length() == 0)
					{
						return;
					}
					System.out.print("Reading file "+templateFile+"...");
					if(Iddk2000Utils.read_file(templateFile, templateData))
					{
						System.out.print("done.\n");
					}
					else
					{
						System.out.print("Cannot read the file !\n");
						return;
					}

					System.out.print("Compare captured image with specified template ... ");
					iRet = apis.compare11WithTemplate(templateData, compareDis);
					if(iRet.getValue() == IddkResult.IDDK_OK)
					{
						System.out.print("done.");
						System.out.print("\n\tCompare Distance = "+compareDis.getValue()+"\n");

						process_matching_result(compareDis.getValue(), null);

						clear_capture();
					}
					else if(iRet.getValue() == IddkResult.IDDK_GAL_EMPTY)
					{
						System.out.print("failed. Gallery is empty !\n");
					}
					else if(iRet.getValue() == IddkResult.IDDK_GAL_ID_NOT_EXIST)
					{
						System.out.print("failed. Enrollee ID does not exist!\n");
					}
					else if(iRet.getValue() == IddkResult.IDDK_SE_NO_QUALIFIED_FRAME)
					{
						System.out.print("failed. No qualified image!\n");
					}
					else
					{
						System.out.print("failed.\n");
						handle_error(iRet);
					}
				}
				else
				{
					System.out.print("\nNo iris image is captured. Start a capture first!\n");
				}
			}
			else
			{
				/* It is abnormal here */
				handle_error(iRet);
			}
		}
	}

	/*******************************************************************************************
	*	This function demonstrates how to use two important features of IDDK library: template
	*	generation and template matching. We try to cover some major functions in this
	*	category.
	*	These functions are taken into account:
	*	- loadGallery
	*	- getGalleryInfo
	*	- getResultTemplate
	*	- getTemplateInfo
	*	- enrollCapture
	*	- enrollTemplate
	*	- unenrollTemplate
	*	- commitGallery
	*	- compare11
	*	- compare1N
	*	Note: This function works only if attached device supports those two mentioned features.
	********************************************************************************************/
	void iris_recognition()
	{
		String[] iris_recognition_menu = {
				"Main Menu",
				"Get Gallery Information",
				"Enroll Captured Iris",
				"Get Result Template",
				"Enroll Template",
				"Unenroll Templates",
				"Compare Template",
				"Exit"
		};

		/* Common params */
		boolean exitProgram = false;
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		int option = -1;
		int times = 0;
		IddkCaptureStatus captureStatus = new IddkCaptureStatus();

		/* For LoadGallery */
		ArrayList<String> pEnrollIds = new ArrayList<String>();
		IddkInteger nUsedSlots = new IddkInteger();
		IddkInteger nMaxSlots = new IddkInteger();

		/* We load gallery first */
		System.out.print("\nLoading gallery ... ");
		iRet = apis.loadGallery(pEnrollIds, nUsedSlots, nMaxSlots);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			reset_error_level(iRet);
			System.out.print("done.\n");
		}
		else
		{
			if(iRet.getValue() == IddkResult.IDDK_UNSUPPORTED_COMMAND)
			{
				/* Device does not support template comparison
				* but it may support template generation.
				* so just continue with other features...
				*/
				System.out.print("\t device doesn't support template comparison.\n");
			}
			else
			{
				handle_error(iRet);
				if(exitProgram)
				{
					finalize_device();
					System.exit(Iddk2000Utils.SUCCESS);
				}
			}
		}

		while(true)
		{
			/* We have a list of menu here */
			System.out.print("\nIRIS RECOGNITION: Please select one menu item \n");
			option = Iddk2000Utils.display_menu(iris_recognition_menu, iris_recognition_menu.length, -1);

			if(option != 8 && option != -1)
			{
				System.out.print("\n\n**************** "+iris_recognition_menu[option - 1]+" ****************\n\n");
			}

			if(option == 3 || option == 4 || option == 7)
			{
				/* Check as if the device finished */
				iRet = apis.getCaptureStatus(captureStatus);
				if(iRet.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print("There is no captured image in the device, we need to capture your iris first. Capturing process starts ...\n");
					capturing_process(true, false, false);
				}
				else
				{
					handle_error(iRet);
					continue;
				}
			}

			if(option == -1) continue;
			else if(option == 8)
				/* Exit */
			{
				exitProgram = true;
				break;
			}
			else if(option == 1)
				/* Come back to main menu */
			{
				clear_capture();

				break;
			}
			else if(option == 2)
				/* Get gallery information */
			{
				get_gallery_information();
			}
			else if(option == 3)
				/* Get result template */
			{
				enroll_capture();
			}
			else if(option == 4)
				/* Get result template */
			{
				get_result_template(times);
			}
			else if(option == 5)
				/* Enroll templates */
			{
				enroll_template();
			}
			else if(option == 6) /* Unenroll templates */
			{
				unenroll_templates();
			}
			else if(option == 7) /* Compare templates */
			{
				compare_templates();
			}
			times++;

			/* Let's get back to menu */
			if(option != 8 && option != -1)
			{
				System.out.print("\n\n****************************************************\n\n");
			}

			getchar();
		}

		if(exitProgram)
		{
			finalize_device();
			System.exit(Iddk2000Utils.SUCCESS);
		}
	}

	/*******************************************************************************************
	*	This function demonstrates how to sleep the attached device.
	*	This function is taken into account:
	*	- sleepDevice
	********************************************************************************************/
	void power_management()
	{
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
		IddkSleepCode action = new IddkSleepCode(IddkSleepCode.IDDK_PM_STANDBY);
		boolean wakeup = false;
		IddkRecoveryCode revCode = new IddkRecoveryCode();

		System.out.print("\nSelect your action? \n\t1. Standby (default)\n\t2. Sleep\n\t3. Deep Sleep\n");
		System.out.print("Enter your choice: ");
		switch(Iddk2000Utils.choose_option(3))
		{
		case -1:
			System.out.print("1\n");
		case 1:
			action.setValue(IddkSleepCode.IDDK_PM_STANDBY);
			break;
		case 2:
			action.setValue(IddkSleepCode.IDDK_PM_SLEEP);
			break;
		case 3:
			action.setValue(IddkSleepCode.IDDK_PM_DEEPSLEEP);
			break;
		}

		/* The third arg is not used (for future use) */
		iRet = apis.sleepDevice(action, 0);
		if(iRet.getValue() == IddkResult.IDDK_OK)
		{
			reset_error_level(iRet);
			System.out.print("Device is slept with mode: " + ((action.getValue() == IddkSleepCode.IDDK_PM_STANDBY)?"Standby":((action.getValue() == IddkSleepCode.IDDK_PM_SLEEP)?"Sleep":"Deepsleep")) + "\n");
			if(action.getValue() == IddkSleepCode.IDDK_PM_SLEEP)
			{
				g_deviceSleep = true;
			}
			if(action.getValue() == IddkSleepCode.IDDK_PM_DEEPSLEEP)
			{
				g_deviceDeepSleep = true;

				if(g_isUsbDevice)
				{
					System.out.print("\nDevice is in Deepsleep. Deepsleep device only wakes up by hardware signal. Device needs scanning and opening again after wake-up\n");
				}
				else
				{
					System.out.print("\nDevice is in Deepsleep. Waking it up for the next usage ! ");
				}
			}
		}
		else
		{
			handle_error(iRet);
		}

		getchar();
	}


	/*******************************************************************************************
	*	This function just finalizes device in a safe way. It supports other functions in this
	*	sample code.
	********************************************************************************************/
	void finalize_device()
	{
		IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);

		System.out.print("\nClose device ... ");
		if((apis.closeDevice()).getValue() == IddkResult.IDDK_OK)
		{
			System.out.println("done.");
		}
		else
		{
			System.out.println("failed.");
			handle_error(iRet);
			System.exit(Iddk2000Utils.ERROR);
		}
	}

	public void handle_error(IddkResult error)
	{
		IddkResult result = new IddkResult();
		boolean recovery = false;
		IddkRecoveryCode recCode = new IddkRecoveryCode();

		System.out.println(error.toString());

		/* Communication error!!! Many results for this.
		 * Next function may be failed too
		 * if we donot recover the communication from this error
		 */

		if(error.getValue() == IddkResult.IDDK_DEVICE_IO_FAILED || error.getValue() == IddkResult.IDDK_DEVICE_IO_TIMEOUT || error.getValue() == IddkResult.IDDK_DEVICE_IO_DATA_INVALID)
		{
			/** If device is in IDDK_PW_SLEEP mode
			 * any access to device results in IDDK_DEVICE_IO_FAILED or IDDK_DEVICE_IO_TIMEOUT
			 */
			if(g_deviceSleep)
			{
				System.out.print("Device may be in Sleep mode ! Wake it up !\n");
				error_level = -1;
				return;
			}

            if (g_deviceDeepSleep)
            {
            	System.out.print("Device may be in deepsleep mode ! Please wake it up !\n");
            	error_level = -1;
                return;
            }

			/** If device is not in sleep mode, the communication should be recovered
			 * users better choose Yes
			 */

			System.out.print("\nDo you want to recover from error?\n");
			System.out.print("\t1. Yes (default)\n\t2. No\n");
			System.out.print("Enter your choice: ");
			switch(Iddk2000Utils.choose_option(2))
			{
			case -1:
				System.out.print("1\n");
			case 1:
				recovery = true;
				break;
			case 2:
				recovery = false;
			}

			if(recovery)
			{

				/* Data invalid may be because of corrupted underlying IO buffers.
				 * We should cancel pending IOs and clear buffers before reset USB pipes
				 */
				if(error.getValue() != IddkResult.IDDK_DEVICE_IO_DATA_INVALID && error_level == -1) error_level = 0;

				/* If these errors occur repeatedly,
				 * We have to increase the severity level of recovery.
				 */
				error_level++;

				if(Iddk2000Utils.isWindows())
				{
					switch(error_level)
					{
					case 0:
						System.out.print("\nFirst time the error is detected. We suggest to flush invalid data from IO buffers and abort any pending IO operations !\n");
						recCode.setValue(IddkRecoveryCode.IDDK_USB_CANCEL_IO);
						break;
					case 1:
						System.out.print("\nWe suggest to reset pipes in this time !\n");
						recCode.setValue(IddkRecoveryCode.IDDK_USB_RESET_PIPES);
						break;
					case 2:
						System.out.print("\nWe suggest to reset port in this time !\n");
						recCode.setValue(IddkRecoveryCode.IDDK_USB_RESET_PORT);
						break;
					case 3:
						System.out.print("\nWe suggest to cycle port in this time ! Note that users should scan device and open device again !\n");
						recCode.setValue(IddkRecoveryCode.IDDK_USB_CYCLE_PORT);
						break;
					case 4:
						System.out.print("\nWe suggest to soft reset device in this time !\n");
						recCode.setValue(IddkRecoveryCode.IDDK_SOFT_RESET);
						break;
					case 5:
						// We donot support this level recovery at the moment ...
						error_level = -1;
					}
				}
				else if(Iddk2000Utils.isUnix())
				{
					switch(error_level)
					{
					case 0:
						System.out.print("\nFirst time error detected. We suggest to flush invalid data from IO buffers and abort any pending IO operations !\n");
						recCode.setValue(IddkRecoveryCode.IDDK_USB_CANCEL_IO);
						break;
					case 1:
						System.out.print("\nWe suggest to reset pipes in this time !\n");
						recCode.setValue(IddkRecoveryCode.IDDK_USB_RESET_PIPES);
						break;
					case 2:
						System.out.print("\nWe suggest to cycle port in this time ! Note that users should scan device and open device again !\n");
						recCode.setValue(IddkRecoveryCode.IDDK_USB_CYCLE_PORT);
						break;
					case 3:
						System.out.print("\nWe suggest to soft reset device in this time !\n");
						recCode.setValue(IddkRecoveryCode.IDDK_SOFT_RESET);
						break;
					case 4:
						// We donot support this level recovery at the moment ...
						error_level = -1;
					}
				}

				result = apis.recovery(recCode);
				if(result.getValue() == IddkResult.IDDK_OK)
				{
					System.out.print("\nRecovery OK !\n");

					if(recCode.getValue() == IddkRecoveryCode.IDDK_USB_CYCLE_PORT || recCode.getValue() == IddkRecoveryCode.IDDK_SOFT_RESET)
					{
						//Wait for sure ...
						System.out.print("\nPlease wait for a while ...\n");
						Iddk2000Utils.wait(10000);

						//Scan and open device again to get the valid handle
						open_device();
					}
				}
				else
				{
					System.out.print("\nRecovery failed !\n");
				}
			}
		}
	}

	public void recovery()
    {
        IddkResult iRet = new IddkResult(IddkResult.IDDK_OK);
        IddkRecoveryCode recoveryCode = new IddkRecoveryCode(IddkRecoveryCode.IDDK_USB_CANCEL_IO);

        System.out.print("\nSelect your action? \n\t1. Cancel IO (default)\n\t2. Reset pipes\n\t3. Reset port\n\t4. Cycle port\n\t5. Soft reset\n\t6. Reopen device\n\t7. Main menu\n");
        System.out.print("Enter your choice: ");
        switch (Iddk2000Utils.choose_option(7))
        {
            case -1:
            	System.out.print("1\n");
            case 1:
                recoveryCode.setValue(IddkRecoveryCode.IDDK_USB_CANCEL_IO);
                break;
            case 2:
                recoveryCode.setValue(IddkRecoveryCode.IDDK_USB_RESET_PIPES);
                break;
            case 3:
                recoveryCode.setValue(IddkRecoveryCode.IDDK_USB_RESET_PORT);
                g_deviceSleep = false;
                break;
            case 4:
                recoveryCode.setValue(IddkRecoveryCode.IDDK_USB_CYCLE_PORT);
                g_deviceSleep = false;
                break;
            case 5:
                recoveryCode.setValue(IddkRecoveryCode.IDDK_SOFT_RESET);
                break;
            case 6:
                open_device();
                g_deviceDeepSleep = false;
                return;
            case 7:
                return;
        }

        iRet = apis.recovery(recoveryCode);
        if (iRet.getValue() == IddkResult.IDDK_OK)
        {
        	System.out.print("\nRecovery OK !\n");

            if (recoveryCode.getValue() == IddkRecoveryCode.IDDK_USB_CYCLE_PORT || recoveryCode.getValue() == IddkRecoveryCode.IDDK_SOFT_RESET)
            {
                /* Wait for sure ... */
            	System.out.print("Please wait for a while ...\n");
                Iddk2000Utils.wait(10000);
                open_device();
            }
        }
        else
        {
            handle_error(iRet);

            System.out.print("\nRecovery failed !\n");
        }
    }
}
