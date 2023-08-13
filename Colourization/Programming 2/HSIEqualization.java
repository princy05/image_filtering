import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class HSIEqualization {

	public static final int height = 300;      	//height of the given image
	public static final int width = 400;  		// width of the given image

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


		//convert RGB to HSI		
		double hue[][] = hueTransformation(inputImage);		//converts the given rgb component to hue component
		double saturation[][] = saturationTransformation(inputImage);	//converts the given rgb component to saturation component
		double intensity[][] = intensityTransformation(inputImage);		//converts the given rgb component to intensity component
		int [][] normalizedIntensity  =new int[height][width];
		
		//performing the histogram equalization on the intensity component of HSI color space
		for (int y=0;y<height;y++)
		{                
			for(int x=0;x<width;x++)
			{
				normalizedIntensity[y][x]=(int) (intensity[y][x]*255.0);
			}
		}
		//the original intensity value of the intensity component of HSI color space is enhanced
		int equalizedIntensity[][] = histogramEqualization(normalizedIntensity); 	

		//converting HSI to RGB 
		HSI2RGB(hue, saturation, intensity, equalizedIntensity, inputImage);
		System.out.println("*** Exiting from the main() ****");

	}

	private static void HSI2RGB(double[][] hue, double[][] saturation, double[][] inten, int[][] intensity, BufferedImage img) {
		
		System.out.println("*** Entering into the HSI2RGB() *******");
		double red = 0;
		double green = 0;
		double blue = 0;

		int [][] redInt  =new int[height][width];
		int [][] greenInt =new int[height][width];
		int [][] blueInt =new int[height][width];

		double redChannel[][] =new double[height][width];
		double greenChannel[][] = new double[height][width];
		double blueChannel[][] = new double[height][width];

		int pixelValue;
		// get the pixel value for rgb color channel
		for (int i=0; i < height; i++){
			for(int j=0; j < width; j++){

				pixelValue =  img.getRGB(j,i); 
				// produces the pixel value for each color channel separately and normalize it in the range of 0 to 1
				redChannel[i][j]   = ((pixelValue & 0x00ff0000) >> 16)/255.0;
				greenChannel[i][j]  = ((pixelValue & 0x0000ff00) >> 8)/255.0;
				blueChannel[i][j]   =  (pixelValue & 0x000000ff)/255.0;

				redInt[i][j]=(int) (redChannel[i][j]*255.0);
				greenInt[i][j]=(int) (greenChannel[i][j]*255.0);
				blueInt[i][j]=(int) (blueChannel[i][j]*255.0);
			}
		}

		double numerator = 0;
		double denominator = 0;
		for(int i=0; i<img.getHeight(); i++) {		
			for(int j=0; j<img.getWidth(); j++) {

				if (saturation[i][j]>1) saturation[i][j] =1;if (inten[i][j]>1) inten[i][j] = 1;
				if(saturation[i][j]== 0) redChannel[i][j]=greenChannel[i][j]=blueChannel[i][j]=inten[i][j];
				else if((hue[i][j]>=0)&&(hue[i][j]<(2*Math.PI/3)))
				{
					blue = (1-saturation[i][j])/3;
					numerator = saturation[i][j]*Math.cos(hue[i][j]);
					denominator = Math.cos(((Math.PI/3)-hue[i][j]));
					red = ((1+(numerator/denominator))/3);
					green=1-blue-red;

				}
				else if((hue[i][j]>=(2*Math.PI/3))&&(hue[i][j]<(4*Math.PI/3)))
				{
					hue[i][j] = hue[i][j] - 2*Math.PI/3;
					red = (1-saturation[i][j])/3;
					numerator = saturation[i][j]*Math.cos(hue[i][j]);
					denominator = Math.cos(((Math.PI/3)-hue[i][j]));
					green = ((1+(numerator/denominator))/3);
					blue=1-green-red;
				}
				else if(((4*Math.PI/3)<hue[i][j])&&(hue[i][j]<(2*Math.PI)))
				{
					hue[i][j] = hue[i][j] - 4*Math.PI/3;
					green = (1-saturation[i][j])/3;
					numerator = saturation[i][j]*Math.cos(hue[i][j]);
					denominator = Math.cos(((Math.PI/3)-hue[i][j]));
					blue = ((1+(numerator/denominator))/3);
					red=1-green-blue;
				}

				if(red<0)red=0;if(green<0)green=0;if(blue<0)blue=0;

				redChannel[i][j]= (3*intensity[i][j]*red);
				greenChannel[i][j]= (3*intensity[i][j]*green);
				blueChannel[i][j]= (3*intensity[i][j]*blue);

				if(redChannel[i][j]>255)redChannel[i][j]=255;
				if(greenChannel[i][j]>255)greenChannel[i][j]=255;
				if(blueChannel[i][j]>255)blueChannel[i][j]=255;

				redInt[i][j]=(int)redChannel[i][j];
				greenInt[i][j]=(int)greenChannel[i][j];
				blueInt[i][j]=(int)blueChannel[i][j];
			}
			displayImage(redInt,greenInt,blueInt);			

		}
		System.out.println("*** Exiting from the HSI2RGB() *******");
	}

	private static void displayImage(int[][] r, int[][] g, int[][] b) {
		BufferedImage tmpImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		//write pixels byte by byte and set RGB for 128x128 to convert raw byte to BufferedImage
		int p;
		for (int y=0;y<height;y++)
		{                
			for(int x=0;x<width;x++)
			{                    
				p = (r[y][x]<<16) | (g[y][x]<<8) | b[y][x];
				tmpImage.setRGB(x, y, p); 
			}
		}
		try {
			ImageIO.write(tmpImage,"bmp",new File("EqualizedImage.bmp"));   //image conversion from raw format to bmp
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private static int[][] histogramEqualization(int[][] intensity) {
		System.out.println("**** Entering into the equalize() ****");
		int k = 256;    //number of gray levels
		int totpix= intensity.length * intensity[0].length;
		int[] histogram = new int[k];
		int[][] equalizedIntensity = new int[300][400];

		// Compute the histogram and store it in histogram[]. Assume that an image has been loaded into imgArr[ ][ ]
		for (int x = 1; x < intensity.length; x++) {
			for (int y = 1; y < intensity[0].length; y++) {
				histogram[intensity[x][y]]++;
			}
		}
		System.out.println("**** Finished computing the histogram for the given image ****");

		//Compute the cumulative histogram of an image and store it in chistogram[].
		int[] chistogram = new int[k];
		chistogram[0] = histogram[0];
		for(int i=1;i<k;i++){
			chistogram[i] = chistogram[i-1] + histogram[i];
		}
		System.out.println("**** Finished computing the cumulative histogram for the given image ****");

		//build a Lookup table LUT containing scale factor
		float[] arr = new float[256];
		for(int i=0;i<256;i++){
			arr[i] =  (float)((chistogram[i]*255.0)/(float)totpix);
		}

		//mapping each pixel in original image to its new intensity
		for (int x = 0; x < intensity.length; x++) {
			for (int y = 0; y < intensity[0].length; y++) {
				equalizedIntensity [x][y] = (int) arr[intensity[x][y]];
			}
		}

		System.out.println("**** Exiting from the equalize() ****");
		return equalizedIntensity;
	}

	private static double[][] intensityTransformation(BufferedImage img) {

		System.out.println("*** Entering into the intensityTransformation() *****");
		//declare variables for each color channels
		double redChannel;
		double greenChannel;
		double blueChannel;

		double[][] intensity = new double [img.getHeight()][img.getWidth()];
		for(int i=0; i<img.getHeight(); i++) {		
			for(int j=0; j<img.getWidth(); j++) {

				// Get pixel values for each color channel
				redChannel = new Color(img.getRGB (j, i)).getRed()/255.0;			//read the intensity value for red channel
				greenChannel = new Color(img.getRGB (j, i)).getGreen()/255.0;		//read the intensity value for green channel
				blueChannel = new Color(img.getRGB (j, i)).getBlue()/255.0;		//read the intensity value for blue channel
				intensity[i][j] = (redChannel + greenChannel + blueChannel) / 3;
			}
		}
		System.out.println("**** Exiting from the intensityTransformation() *****");
		return intensity;
	}

	private static double[][] saturationTransformation(BufferedImage img) {

		System.out.println("*** Entering into the saturationTransformation() *****");

		//declare variables for each color channels
		double redChannel;
		double greenChannel;
		double blueChannel;

		double[][] saturation = new double [img.getHeight()][img.getWidth()];
		for(int i=0; i<img.getHeight(); i++) {		
			for(int j=0; j<img.getWidth(); j++) {

				// Get pixel values for each color channel
				redChannel = new Color(img.getRGB (j, i)).getRed()/255.0;			//read the intensity value for red channel
				greenChannel = new Color(img.getRGB (j, i)).getGreen()/255.0;		//read the intensity value for green channel
				blueChannel = new Color(img.getRGB (j, i)).getBlue()/255.0;		//read the intensity value for blue channel

				double rn = redChannel / (redChannel + greenChannel + blueChannel);
				double gn = greenChannel / (redChannel + greenChannel + blueChannel);
				double bn = blueChannel / (redChannel + greenChannel + blueChannel);
				double den = rn+gn+bn;

				if((rn == gn)&& (gn==bn))
				{
					saturation[i][j] = 0;
				}else{
					double num = 3 * Math.min(rn, Math.min(gn, bn));
					saturation[i][j] = 1-(num/den) ;
				}					

			}
		}
		System.out.println("**** Exiting from the saturationTransformation() *****");
		return saturation;
	}

	private static double[][] hueTransformation(BufferedImage img) {
		System.out.println("*** Entering into the hueTransformation() *****");

		//declare variables for each color channels
		double redChannel;
		double greenChannel;
		double blueChannel;

		double [][] hue = new double [img.getHeight()][img.getWidth()];
		for(int i=0; i<img.getHeight(); i++) {		
			for(int j=0; j<img.getWidth(); j++) {

				// Get pixel values for each color channel
				redChannel = new Color(img.getRGB (j, i)).getRed()/255.0;			//read the intensity value for red channel
				greenChannel = new Color(img.getRGB (j, i)).getGreen()/255.0;		//read the intensity value for green channel
				blueChannel = new Color(img.getRGB (j, i)).getBlue()/255.0;		//read the intensity value for blue channel

				double rn = redChannel / (redChannel + greenChannel + blueChannel);
				double gn = greenChannel / (redChannel + greenChannel + blueChannel);
				double bn = blueChannel / (redChannel + greenChannel + blueChannel);

				if((rn == gn)&& (gn==bn))
				{
					hue[i][j] = 0;
				}
				double thetha = Math.acos((0.5 * ((rn - gn) + (rn - bn))) / (Math.sqrt((rn - gn) * (rn - gn) + (rn - bn) * (gn - bn))));
				if(blueChannel < greenChannel){
					hue[i][j] = thetha;
				}
				if(blueChannel > greenChannel)
				{
					hue[i][j] = (2 * Math.PI) - thetha;	
				}		

			}
		}
		System.out.println("*** Exiting from the hueTransformation() *****");
		return hue;
	}

}
