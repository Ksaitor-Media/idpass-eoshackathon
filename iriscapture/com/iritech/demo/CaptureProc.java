package com.iritech.demo;

import java.util.ArrayList;

import com.iritech.iddk.standard.*;

/* This class is used to get stream image from camera. To use this feature, user could create an instance
 * of this class and pass it to function startCapture
 */
public class CaptureProc implements IddkCaptureProc{
	
	public void invoke(ArrayList<IddkImage> image, IddkCaptureStatus captureStatus, IddkResult captureError)
	{ 	
		if(captureError != null)
		{
			/** process capture error. After this error, capturing process is aborted **/
		}
		
		if(captureStatus != null)
		{
			if(captureStatus.getValue() == IddkCaptureStatus.IDDK_COMPLETE)
			{
				/** capture has finished.
				There may be qualified images. There may be not.
				getResultImage should be called to know for sure.
				**/
			}
			else if(captureStatus.getValue() == IddkCaptureStatus.IDDK_CAPTURING)
			{
				/** Eye has been detected. Play a "sound" **/
			}
			else if(captureStatus.getValue() == IddkCaptureStatus.IDDK_ABORT)
			{
				/** capture has been aborted **/
			}
		}
	
		if(image.size() > 0){
			/** show image on its GUI control **/
		}
	}
}
