package com.iritech.demo;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Iddk2000Main {

	static String[] main_menu = {
		"Login",
		"Logout",
		"Device Management",
		"Device & SDK Information",
		"Capturing Process",
		"Iris Recognition",
		"Power Management",
        "Recovery (IriShield USB only)",
		"Exit"};

	static String introduction = "\tThis demonstration shows how to use IDDK 2000 in a basic \n\tmanner. This sample only works with IriShield devices.\n";

	public static void main(String[] args) {
		Iddk2000Features.g_binDir = System.getProperty("user.dir");
		print_menu();
	}

	static void iritech_guide(String message)
	{
		System.out.print("**********************************************************************\n");
		System.out.print("Usage:\n");
		System.out.print(introduction);
		System.out.print("\n\tCopyright(C) 2012 by IriTech, Inc. All rights reserved.");
		System.out.print("\n**********************************************************************\n");
		System.out.print("Please press ENTER to continue ...\n");
		try {
			InputStreamReader inputStream = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(inputStream);
			reader.readLine();
		} catch (IOException e) {
			System.out.print("IO exception happens here !\n");
			e.printStackTrace();
		}
	}

	static void print_menu()
	{
		Iddk2000Features features = new Iddk2000Features();
		int option = -1;

		// iritech_guide(introduction);

		features.open_device();

		features.capturing_process(false, false, true);
		features.get_result_template(0);

		// features.iris_recognition();
		features.my_compare_templates("./ResultTemplate_b.tpl");

		// while(false)
		// {
		// 	System.out.print("\nMAIN MENU: Select one of the features below\n");
		// 	option = Iddk2000Utils.display_menu(main_menu, main_menu.length, -1);

		// 	if(option != 9 && option != -1)
		// 	{
		// 		System.out.print("\n\n*************** " + main_menu[option - 1] + " ***************\n\n");
		// 	}

		// 	switch(option)
		// 	{
		// 	case -1:
		// 		/* User presses enter */
		// 		break;
		// 	case 1:
		// 		features.login();
		// 		break;
		// 	case 2:
		// 		features.logout();
		// 		break;
		// 	case 3:
		// 		features.device_configuration();
		// 		break;
		// 	case 4:
		// 		features.get_information();
		// 		break;
		// 	case 5:
		// 		features.capturing_process(false, true, true);
		// 		break;
		// 	case 6:
		// 		features.iris_recognition();
		// 		break;
		// 	case 7:
		// 		features.power_management();
		// 		break;
		// 	case 8:
		// 		features.recovery();
		// 		break;
		// 	}

		// 	if(option != 9 && option != -1)
		// 	{
		// 		System.out.print("\n\n***********************************************\n\n");
		// 	}
		// 	if(option == 9) break;
		// }

		/* Last try to close device before exiting */
		features.finalize_device();
	}
}
