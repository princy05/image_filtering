import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;


public class HistogramEqualization {
	public static void main(String args[]) throws IOException{
		System.out.println("**** Enter into the main() ****");
		/* Read the raw image from local C drive */
		String fileName = "C:\\Users\\princy\\ct.raw"; 
		System.out.println("*** Start reading the raw image as bytes ***");
		
		//Store the pixel values of raw image in byte[]
		byte[] data = Files.readAllBytes(Paths.get(fileName));

		/*Convert the image in raw format to bmp format */
		int imgWidth = 256;		//width of the given image
		int imgHeight = 256;	//height of the given image
		BufferedImage buff = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);		
		WritableRaster wr = buff.getRaster();
		int x = 0;
		for(int i=0;i<wr.getWidth();i++)
		{
			for(int j=0; j<wr.getHeight(); j++)
			{
				wr.setSample(j, i, 0, data[x]);
				x++;
			}
		}

		buff.setData(wr);
		try {
		    ImageIO.write(buff,"bmp",new File("ct.bmp"));   //image conversion
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
		System.out.println("*** Image converted succesfully to bmp format ****");

		/*Retrieve pixel values from bmp image as matrix for image manipulation */		
		File file = new File("ct.bmp");
		BufferedImage img = ImageIO.read(file);
		int width = img.getWidth();
		int height = img.getHeight();
		int[][] imgArr = new int[width][height];
		Raster raster = img.getData();
		//Reading the input image in bmp format into imgArr[][]
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				imgArr[i][j] = raster.getSample(i, j, 0);
			}
		}
		/* writing the pixel values as matrix in a file */
		writeMatrix("matrix.txt", imgArr);
		int grayLevel = getGrayLevelValue(imgArr); //getting the gray level value from the imgArr[][]
		System.out.println("gray level of image varies from 0 to "+grayLevel+".");		

		/*  converting the gray scale image to enhanced, histogram equalized image */
		BufferedImage equalizedImage = equalize(imgArr, grayLevel);
		File output=new File("EnhancedImage.bmp");
		try {
			//write the enhanced image in bmp format
		    ImageIO.write(equalizedImage,"bmp",output);
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
		
		System.out.println("**** Exiting from the main() ****");
	}

	public static BufferedImage equalize(int [][] imgArr, int grayLevel){
		System.out.println("**** Entering into the equalize() ****");
		BufferedImage nImg = new BufferedImage(imgArr.length, imgArr[0].length, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster er = nImg.getRaster();
		int k = grayLevel+1;    //number of gray levels
		int totpix= imgArr.length * imgArr[0].length;
		int[] histogram = new int[k];

		// Compute the histogram and store it in histogram[]. Assume that an image has been loaded into imgArr[ ][ ]
		for (int x = 1; x < imgArr.length; x++) {
			for (int y = 1; y < imgArr[0].length; y++) {
				histogram[imgArr[x][y]]++;
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
		for (int x = 0; x < imgArr.length; x++) {
			for (int y = 0; y < imgArr[0].length; y++) {
				int nVal = (int) arr[imgArr[x][y]];
				er.setSample(x, y, 0, nVal);
			}
		}
		nImg.setData(er);
		System.out.println("**** Exiting from the equalize() ****");
		return nImg;
	}

	/* method to get the gray level value of the given image */
	public static int getGrayLevelValue(int[][] imgArr) {
		System.out.println("**** Entering into the getGrayLevelValue() ****");
		int max = imgArr[0][0];
		for(int i=0;i<imgArr.length;i++)
		{
			for(int j=0;j<imgArr[0].length;j++)
			{
				if(imgArr[i][j]>max)
					max=imgArr[i][j];
			}
		}
		System.out.println("**** Exiting from the getGrayLevelValue() ****");
		return max;
	}

	/* method to write the pixel values in 2D arry to a text file */
	public static void writeMatrix(String filename, int[][] matrix) {
		System.out.println("**** Entering into the writeMatrix() ****");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					bw.write(matrix[i][j] + " ");
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
