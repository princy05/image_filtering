import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.imageio.ImageIO;


public class MedianFilter {
	
	private transient static BufferedImage originalImage;
	private static int radius = 1; 
	public static void main(String args[]) throws IOException{
		//Read the image from local C drive
		String fileName = "C:\\Users\\princy\\circuit.raw"; 
		System.out.println("*** Start reading the raw image as bytes ***");
		//Store the pixel values of raw image in byte[]
		byte[] data = Files.readAllBytes(Paths.get(fileName));

		int imgWidth = 455;   	//width of the given image
		int imgHeight = 440;	//height of the given image
		/*Convert the image in raw format to bmp format */
		BufferedImage buff = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);		
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
		    ImageIO.write(buff,"bmp",new File("circuit.bmp"));    //image conversion
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
		System.out.println("*** Image converted succesfully to bmp format ****");

		/*Retrieve pixel values from bmp image as matrix for image manipulation */
		File file = new File("circuit.bmp");
		BufferedImage img = ImageIO.read(file);
		int width = img.getWidth();
		int height = img.getHeight();
		int[][] imgArr = new int[width][height];
		Raster raster = img.getData();
		//Reading the input image in bmp format into imgArr[][]
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				imgArr[j][i] = raster.getSample(j, i, 0);
			}
		}
		
		/* writing the pixel values as matrix in a file */
		writeMatrix("medianMatrix.txt", imgArr);
		
		int [] arrayOfPixels;
		int median;
		int alpha;
		int newColor;
		BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		originalImage = img;
		System.out.println("*****Applying median filter on the given image *****");
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				arrayOfPixels = getArrayOfPixels(j, i);    //fetch the array of pixel values
				median = findMedian(arrayOfPixels);			//finding the median from the array of pixel values
				alpha = new Color(img.getRGB(j, i)).getAlpha();						
				newColor = colorToRGB(alpha, median, median, median); 	//getting the new pixel values
				filteredImage.setRGB(j, i, newColor);			//setting the new pixel values to bufferedimage object
			}
		}
		System.out.println("***** Finished applying the median filter *****");
		File output=new File("resultantCircuit.bmp");
		try {
			System.out.println("***** Storing the resultant noise reduced image in bmp format *****");
		    ImageIO.write(filteredImage,"bmp",output);   		//output image in bmp format
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
	}
	
	/* Method to get new pixel values after applying median filter */
	public static int colorToRGB(int alpha, int red, int green, int blue) {
		int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;
        
        return newPixel;
	}
	/* Method to find the median value */
	public static int findMedian(int[] arrayOfPixels) {
		int low = 0;
		int high = arrayOfPixels.length -1 ;
		quickSort(arrayOfPixels, low, high);
		int middle = 0;
		if(arrayOfPixels.length % 2 == 0){
			middle = arrayOfPixels.length/2;
			int avg = (arrayOfPixels[middle-1]+arrayOfPixels[middle])/2;
			return avg;
		}
		else{
			middle = (int)arrayOfPixels.length/2;
			return arrayOfPixels[middle];
		}	
	}

	/* Method to apply quick sort on the pixel values*/
	private static void quickSort(int[] arrayOfPixels, int low, int high) {
		if (arrayOfPixels == null || arrayOfPixels.length == 0)
			return;
 
		if (low >= high)
			return;
 
		// pick the pivot
		int middle = low + (high - low) / 2;
		int pivot = arrayOfPixels[middle];
 
		// make left < pivot and right > pivot
		int i = low, j = high;
		while (i <= j) {
			while (arrayOfPixels[i] < pivot) {
				i++;
			}
 
			while (arrayOfPixels[j] > pivot) {
				j--;
			}
 
			if (i <= j) {
				int temp = arrayOfPixels[i];
				arrayOfPixels[i] = arrayOfPixels[j];
				arrayOfPixels[j] = temp;
				i++;
				j--;
			}
		}
 
		// recursively sort two sub parts
		if (low < j)
			quickSort(arrayOfPixels, low, j);
 
		if (high > i)
			quickSort(arrayOfPixels, i, high);
	}

	/* Method to get array of pixel values to apply median filter. 
	 * Here we are computing the weighted terms only for pixels inside the image */
	public static int[] getArrayOfPixels(int i, int j) {
		int startX = i - radius;
		int goalX = i + radius;
		int startY = j - radius;
		int goalY = j + radius;
			
		if (startX < 0)
	            startX = 0;
		if (goalX > originalImage.getWidth() - 1)
	            goalX = originalImage.getWidth() - 1;
	    if (startY < 0)
	            startY = 0;
		if (goalY > originalImage.getHeight() - 1)
	            goalY = originalImage.getHeight() - 1;
			
		int arraySize = (goalX - startX + 1)*(goalY - startY +1);
		int [] pixels = new int [arraySize];
		
		int position = 0;
		int color;
	        for (int p = startX; p <= goalX; p++) {
	            for (int q = startY; q <= goalY; q++) {
			color = new Color(originalImage.getRGB(p, q)).getRed();
			pixels[position] = color;
			position++;
	            }
		}
		return pixels;
	}

	/* method to write the pixel values in 2D arry to a text file */
	public static void writeMatrix(String filename, int[][] matrix) {
		System.out.println("**** Entering into the writeMatrix() ****");
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < matrix[0].length; i++) {
				for (int j = 0; j < matrix.length; j++) {
					bw.write(matrix[j][i] + " ");
				}
				bw.newLine();
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("**** Exiting from the writeMatrix() ****");
	}

}
