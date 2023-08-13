import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ColorHistogram {

	public static void main(String args[]) throws IOException{

		System.out.println("**** Enter into the main() ****");
		/* Read the raw image from local C drive */
		String fileName = "C:\\Users\\princy\\summer_deck1.raw"; 
		System.out.println("*** Start reading the raw image as bytes ***");

		//Store the pixel values of raw image in byte[]
		byte[] data = Files.readAllBytes(Paths.get(fileName));

		/*Convert the image in raw format to bmp format */
		int imgWidth = 400;		//width of the given image
		int imgHeight = 300;	//height of the given image
		BufferedImage buff = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_BGR);		
		int count = 0;

		//retrieving the pixel values for all three channels and storing it in an input buffer
		for(int i=0;i<imgHeight;i++)	//loop through the rows of an image
		{
			for(int j=0; j< imgWidth; j++)	// loop through the columns of an image
			{

				int red = data[count*3 + 0] << 8*2;		//to get the intensity value for red channel
				int green = data[count*3 + 1] << 8*1;	//to get the intensity value for green channel
				int blue = data[count*3 + 2] << 8*0;	//to get the intensity value for blue channel

				int color = red + green + blue;
				buff.setRGB(j, i,  color);				//setting the rgb intensity value in an input buffer
				count ++;
			}
		}

		try {
			ImageIO.write(buff,"bmp",new File("summer_deck.bmp"));   //image conversion from raw format to bmp
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("*** Image converted succesfully to bmp format ****");	

		File file = new File("summer_deck.bmp");
		BufferedImage inputImage= ImageIO.read(file);		//reading the converted input image in bmp format
		BufferedImage equalizedImage = histogramEqualization(inputImage);	//calling the histogram equalization function
		File output=new File("EnhancedImage.bmp");
		try {
			//write the enhanced image in bmp format
			ImageIO.write(equalizedImage,"bmp",output);		//displaying the output image in bmp format
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("**** Exiting from the main() ******");
	}

	private static BufferedImage histogramEqualization(BufferedImage img) {
		System.out.println("**** Entering into the histogramEqualization() ****");

		//declare variables for each color channels
		int redChannel;
		int greenChannel;
		int blueChannel;
		int alphaChannel;
		int newPixel = 0;

		//to store the buffer image after applying histogram equalization
		BufferedImage histogramEQ = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());		

		// build a Lookup table LUT containing scale factor
		ArrayList<int[]> lookUpTable = histoEqualizeLUT(img);

		for(int i=0; i<img.getWidth(); i++) {		
			for(int j=0; j<img.getHeight(); j++) {

				// Get pixel values for each color channel
				alphaChannel = new Color(img.getRGB (i, j)).getAlpha();		//read the intensity value for alpha channel
				redChannel = new Color(img.getRGB (i, j)).getRed();			//read the intensity value for red channel
				greenChannel = new Color(img.getRGB (i, j)).getGreen();		//read the intensity value for green channel
				blueChannel = new Color(img.getRGB (i, j)).getBlue();		//read the intensity value for blue channel

				// Using the histogram look up table set the new enhanced intensity values to each color channel
				redChannel = lookUpTable.get(0)[redChannel];
				greenChannel = lookUpTable.get(1)[greenChannel];
				blueChannel = lookUpTable.get(2)[blueChannel];

				//convert the pixel values to rgb image format
				newPixel = RGBConversion(alphaChannel, redChannel, greenChannel, blueChannel);

				// Write new pixel value to the enhanced image buffer
				histogramEQ.setRGB(i, j, newPixel);

			}
		}
		System.out.println("**** Exiting from the histogramEqualization() ****");
		return histogramEQ;
	}

	private static int RGBConversion(int alphaChannel, int redChannel, int greenChannel, int blueChannel) {
		
		int newPixel = 0;
		newPixel += alphaChannel; 
		newPixel = newPixel << 8;		//new pixel value for alpha channel
		
		newPixel += redChannel; 
		newPixel = newPixel << 8;		//new pixel value for red channel
		
		newPixel += greenChannel; 		//new pixel value for green channel
		newPixel = newPixel << 8;
		
		newPixel += blueChannel;		//new pixel value for blue channel
		
		return newPixel;
	}

	private static ArrayList<int[]> histoEqualizeLUT(BufferedImage img) {
		System.out.println("****** Entering into the histoEqualizeLUT() ********* " );

		// Compute the histogram separately for each color channels of the given color image and store it in imageHist[]. 
		ArrayList<int[]> histogram = computeHistogram(img);
		System.out.println("**** Finished computing the histogram for the given color image ****");

		//build a Lookup table LUT containing scale factor
		ArrayList<int[]> arr = new ArrayList<int[]>();

		//Declare the variables for the three color channels and Fill the lookup table
		int[] rhistogram = new int[256];
		int[] ghistogram = new int[256];
		int[] bhistogram = new int[256];

		for(int i=0; i<rhistogram.length; i++) 
			rhistogram[i] = 0;	//initialize the red channel
		for(int i=0; i<ghistogram.length; i++) 
			ghistogram[i] = 0;	//initialize the green channel
		for(int i=0; i<bhistogram.length; i++) 
			bhistogram[i] = 0;	//initialize the blue channel

		long sumr = 0;
		long sumg = 0;
		long sumb = 0;

		// Calculate the scale factor
		float scale_factor = (float) (255.0 / (img.getWidth() * img.getHeight()));

		//mapping each pixel of the three color channels in original image to its new intensity
		for(int i=0; i<rhistogram.length; i++) {
			sumr += histogram.get(0)[i];
			int valRed = (int) (sumr * scale_factor);
			if(valRed > 255) {
				rhistogram[i] = 255;
			}
			else rhistogram[i] = valRed;

			sumg += histogram.get(1)[i];
			int valGreen = (int) (sumg * scale_factor);
			if(valGreen > 255) {
				ghistogram[i] = 255;
			}
			else ghistogram[i] = valGreen;

			sumb += histogram.get(2)[i];
			int valBlue = (int) (sumb * scale_factor);
			if(valBlue > 255) {
				bhistogram[i] = 255;
			}
			else bhistogram[i] = valBlue;
		}

		arr.add(rhistogram);
		arr.add(ghistogram);
		arr.add(bhistogram);
		System.out.println("****** Exiting from the histoEqualizeLUT() ********* " );
		return arr;

	}

	private static ArrayList<int[]> computeHistogram(BufferedImage img) {
		System.out.println("**** Entering into the imageHistogram() ******* "); 
		int[] rhistogram = new int[256];
		int[] ghistogram = new int[256];
		int[] bhistogram = new int[256];

		/** Declare the variables for the three color channels and Fill the lookup table
		 * and find the histogram separately for each color channels
		 */
		for(int i=0; i<rhistogram.length; i++) 
			rhistogram[i] = 0;	//initialize the histogram for red channel
		for(int i=0; i<ghistogram.length; i++) 
			ghistogram[i] = 0;	//initialize the histogram for green channel
		for(int i=0; i<bhistogram.length; i++) 
			bhistogram[i] = 0;	//initialize the histogram for blue channel

		for(int i=0; i<img.getWidth(); i++) {
			for(int j=0; j<img.getHeight(); j++) {

				int redPixel = new Color(img.getRGB (i, j)).getRed();		//reading the pixel value for red channel from the image buffer
				int greenPixel = new Color(img.getRGB (i, j)).getGreen();	//reading the pixel value for green channel from the image buffer
				int bluePixel = new Color(img.getRGB (i, j)).getBlue();		//reading the pixel value for blue channel from the image buffer

				// compute the histogram for three color channels
				rhistogram[redPixel]++; 
				ghistogram[greenPixel]++; 
				bhistogram[bluePixel]++;

			}
		}

		ArrayList<int[]> histogram = new ArrayList<int[]>();
		histogram.add(rhistogram);
		histogram.add(ghistogram);
		histogram.add(bhistogram);
		System.out.println("**** Exiting from the imageHistogram() ******* "); 
		return histogram;

	}

}
