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

public class AveragingFilter {
	public static void main(String args[]) throws IOException{
		//Read the image from local C drive
		String fileName = "C:\\Users\\princy\\testpattern.raw"; 
		System.out.println("*** Start reading the raw image as bytes ***");
		//Store the pixel values of raw image in byte[]
		byte[] data = Files.readAllBytes(Paths.get(fileName));

		/*Convert the raw image to bmp format */
		int imgWidth = 500;		// width of the given image
		int imgHeight = 500;	//heigth of the given image
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
			ImageIO.write(buff,"bmp",new File("testpattern.bmp"));   //image conversion to bmp format
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("*** Image converted succesfully to bmp format ****");

		/*Retrieve pixel values from bmp image as matrix for image manipulation */
		File file = new File("testpattern.bmp");
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
		writeMatrix("averageMatrix.txt", imgArr);

		float mask1[][]= new float[][]{{1.0f,1.0f,1.0f},{1.0f,1.0f,1.0f},{1.0f,1.0f,1.0f}};		//filter matrix 1
		float mask2[][] = new float[][] {{0.075f, 0.125f, 0.075f},{0.125f,0.2f,0.125f},{0.075f,0.125f,0.075f}};		//filter matrix 2
		averagingFilter(imgArr, mask1, "filter1");		// applying averaging filter on the given image for filer 1
		averagingFilter(imgArr, mask2, "filter2");		// applying averaging filter on the given image for filer 2
		System.out.println("*****Finished doing average filtering ****");

	}

	/* Method to apply the average filter on the given image stored as imgArr[][] 
	 * Here we compute the weighted sum only if the mask is completely inside of the image (to solve boundary problem)*/
	public static void averagingFilter(int[][] imgArr, float[][] mask, String imageFile) {
		System.out.println("**** Entering into averagingFilter() ****** ");
		int M = 3;		// MxM is the dimension of the mask
		int M2 = (M-1)/2; // Half length of the mask, assuming M is an odd integer
		
		int img_out[][] = new int[500][500];	//result image

		for(int i = 1; i+1 <imgArr.length; i++){
			for(int j =1; j+1 <imgArr[0].length; j++){
				float t_sum = 0.0f;
				for(int k = -M2; k <= M2; k++){ 		//each row of the mask
					for(int l = -M2; l <= M2; l++){		//each column of the mask
						t_sum += imgArr[i+k][j+l] * mask[k+M2][l+M2];
					}
				}
				if(imageFile.equalsIgnoreCase("filter2")){
					img_out[i][j] = (int) Math.round(t_sum);
				}else{
					img_out[i][j] = ( (int)(t_sum/(M*M)));
				}
			}
		}
		int width = imgArr.length;
		int height = imgArr[0].length;
		BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wrRaster=(WritableRaster)image.getData();
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				wrRaster.setSample(j,i,0,img_out[j][i]);
			}
		}
		image.setData(wrRaster);
		File output=new File(imageFile+".bmp");
		try {
			ImageIO.write(image,"bmp",output);		//storing the result image in bmp format
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("**** Exiting from averagingFilter() ****** ");
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
