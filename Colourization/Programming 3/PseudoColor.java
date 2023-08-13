import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class PseudoColor {

	public static final int rows = 420;      //height of the given image
	public static final int columns = 640;  // width of the given image

	public static void main(String args[]) throws IOException{
		
		System.out.println("**** Entering into the main() ******");
		//Read the image from local C drive
		String fileName = "C:\\Users\\princy\\tempusa.raw"; 
		System.out.println("*** Start reading the raw image as bytes ***");
		//Store the pixel values of raw image in byte[]
		byte[] data = Files.readAllBytes(Paths.get(fileName));

		/*Convert the raw image to bmp format */
		BufferedImage buff = new BufferedImage(columns, rows, BufferedImage.TYPE_BYTE_GRAY);		
		WritableRaster wr = buff.getRaster();
		int x = 0;
		for(int i=0;i<wr.getHeight();i++)
		{
			for(int j=0; j<wr.getWidth(); j++)
			{
				wr.setSample(j, i, 0, data[x]);
				x++;
			}
		}

		buff.setData(wr);
		try {
			ImageIO.write(buff,"bmp",new File("tempusa.bmp"));   //image conversion to bmp format
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("*** Image converted succesfully to bmp format ****");

		/*Retrieve pixel values from bmp image as matrix for image manipulation */
		File file = new File("tempusa.bmp");
		BufferedImage img = ImageIO.read(file);
		int[][] imgArr = new int[rows][columns];
		Raster raster = img.getData();
		//Reading the input image in bmp format into imgArr[][]
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				imgArr[i][j] = raster.getSample(j, i, 0);
			}
		}

		//creating pseudo color lookup table by storing the RGB color format in string array separated by comma		
		String psuedoColorLUT[] = new String[256];
		//pseudo color lookup table  
		psuedoColorLUT = pseudoColorLookUpTable();	
		//applying the pseudo color to the given grayscale image
		applyPseudoColor(imgArr, psuedoColorLUT);
		System.out.println("**** Exiting from the main() ******");
	}

	private static void applyPseudoColor(int[][] imgArr, String[] psuedoColorLUT) {
		System.out.println("**** Entering into the applyPseudoColor() ******");
		BufferedImage outputImage = new BufferedImage(columns,rows,BufferedImage.TYPE_INT_RGB);
		String pseudoValue = "";
		int red=0;
		int green=0;
		int blue=0;
		
		for(int i=0; i< rows; i++){				//loop through the rows of the given grayscale image
			for(int j=0; j < columns; j++){		//loop through the columns of the given grayscale image
				pseudoValue = psuedoColorLUT[imgArr[i][j]];	//getting the pseudo color value for the corresponding grayscale value
				String splitValue[] = pseudoValue.split(",");
				red = Integer.parseInt(splitValue[0]);		//gives the red channel value
				green = Integer.parseInt(splitValue[1]);	//gives the green channel value
				blue = Integer.parseInt(splitValue[2]);		//gives the blue channel value
				Color c = new Color(red, green, blue);
				outputImage.setRGB(j, i, c.getRGB());		//gives the pseudo color image of the given grayscale image		
			}
		}
		
		 try {
			 	//display the output image in bmp format
				ImageIO.write(outputImage,"bmp",new File("pseudoImage.bmp"));   
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		 
		 System.out.println("**** Exiting from the applyPseudoColor() ******");
	}

	private static String[] pseudoColorLookUpTable() {
		
		System.out.println("**** Entering into the pseudoColorLookUpTable() ******");
		String psuedoColorLUT[] = new String[256];
		int red = 255;
		int green = 0;
		int blue = 0;
		String pseudoColor = "";
		int entries = 256;
		for(int i=0; i < entries; i++)
		{
			//the hue cycle starts with red (255,0,0) and ends with red color for a complete cycle
			pseudoColor = red+","+green+","+blue;
			//store the pseudocolor in rgb format to the Look up table in the form of String array
			psuedoColorLUT[i] = pseudoColor;
			
			/** the hue cycle starts with red and the red value decreases and the green value increases
			* as we move across the hue cycle **/
			if(i >= 0 && i < 85)
			{
				red= red - 3; //red starts decreasing and green starts increasing
				green = green + 3; 
			}
			/** when the i value is 85, we have green color(0, 255, 0) on the hue cycle. As we move further, the green color
			 * decreases and the blue color increases
			 */
			if(i >= 85 && i < 170) 
			{
				green = green - 3; //green starts decreasing and blue starts increasing
				blue = blue + 3;
			}
			/** when the i value is 170, we have blue color (0, 0 ,255) on the hue cycle. As we move further, the blue color decreases
			 * and there is an increase in the red color to complete the hue cycle
			 */
			if( i >= 170 && i < 256 ) 
			{
				red = red + 3; //now again blue starts decreasing and red increasing
				blue = blue - 3;
			}   
		}
		System.out.println("**** Exiting from the pseudoColorLookUpTable() ******");
		return psuedoColorLUT;
	}

}
