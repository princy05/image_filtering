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

public class LaplacianFilter {
	
	public static final int rows = 528;      //height of the given image
	public static final int columns = 464;  // width of the given image
	
	public static void main(String args[]) throws IOException{
		//Read the image from local C drive
		String fileName = "C:\\Users\\princy\\moon.raw"; 
		System.out.println("*** Start reading the raw image as bytes ***");
		//Store the pixel values of raw image in byte[]
		byte[] data = Files.readAllBytes(Paths.get(fileName));
		
		//Convert the image in raw format to bmp format
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
			ImageIO.write(buff,"bmp",new File("moon.bmp"));   //image conversion to bmp format
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("*** Image converted succesfully to bmp format ****");

		/*Retrieve pixel values from bmp image as matrix for image manipulation */
		File file = new File("moon.bmp");
		BufferedImage img = ImageIO.read(file);
		int[][] imgArr = new int[rows][columns];
		Raster raster = img.getData();
		//Reading the input image in bmp format into imgArr[][]
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				imgArr[i][j] = raster.getSample(j, i, 0);
			}
		}
		/* writing the pixel values as matrix in a file */
		writeMatrix("laplacianMatrix.txt", imgArr);
		
		int filter [][] = new int[rows][columns];
		filter = laplacianFilter(imgArr, 0.5); //Apply laplacian filter with weight as 0.5
		displayOutput(filter, "weight_0.5");	//display the resultant 2D output image array in bmp format	
		filter = laplacianFilter(imgArr, 1); //Apply laplacian filter with weight as 1
		displayOutput(filter, "weight_1");		//display the resultant 2D output image array in bmp format
		filter = laplacianFilter(imgArr, 2);	//Apply laplacian filter with weight as 0.5
		displayOutput(filter, "weight_2");		//display the resultant 2D output image array in bmp format
		
		
	}
	
	
	/* This method displays the resultant 2D output image array in bmp format */
	private static void displayOutput(int[][] img_out, String fileName) {
		System.out.println("**** Entering into the displayOutput() ****");		
		
		BufferedImage image=new BufferedImage(columns,rows,BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wrRaster=(WritableRaster)image.getData();
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<columns;j++)
			{
				wrRaster.setSample(j,i,0,img_out[i][j]);
			}
		}
		image.setData(wrRaster);
		File output=new File(fileName+".bmp");
		try {
			ImageIO.write(image,"bmp",output);		//storing the result image in bmp format
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		writeMatrix(fileName+".txt", img_out);
		System.out.println("**** Exiting from the displayOutput() ****");
	}


	/* This method applies the laplacian filter mask on the given image in 2D matrix */
	private static int[][] laplacianFilter(int[][] imgArr, double weight) {
		System.out.println("**** Entering into laplacianFilter() ****** ");
		
		int M = 3;		// MxM is the dimension of the filter kernel
		int M2 = (M-1)/2; // Half length of the mask, assuming M is an odd integer
		int filter[][] = {{0, 1, 0}, {1, -4, 1}, {0, 1, 0}}; // laplacian filter kernel of 3x3 
		int img_out[][] = new int[rows][columns];	//output image
		
		// we apply filter kernel inside the image making the boundaries to zero
		for(int i = 1; i+1 < rows; i++){
			for(int j = 1; j+1 < columns; j++){
				int sum = 0;
				int result = 0;
				for(int k = -M2; k <= M2; k++){ 		//each row of the mask
					for(int l = -M2; l <= M2; l++){		//each column of the mask
						sum = sum + imgArr[i+k][j+l] * filter[k+M2][l+M2];  //Calculate sum for edge detection
					}
				}
				//Perform the operation F(x) = f(x)-w.f"(x)
				result = (int) ((imgArr[i][j]) - (weight*sum)); //subtract the sum with the original image to get sharpened image
				
				if(result < 0 ){  //intensity greater than 255 map to 255
					result = 0;
				}else if (result > 255){ //intensity greater than 255 map to 255
					result = 255;
				}
				if (result > 255)  //intensity greater than 255 map to 255
                    result = 255;
                
				img_out[i][j] = result;
			}
		}	
		System.out.println("**** Exiting from the laplacianFilter() ****");
		return img_out;
	}

	/* method to write the pixel values in 2D arry to a text file */
	public static void writeMatrix(String filename, int[][] matrix) {
		System.out.println("**** Entering into the writeMatrix() ****");
		try {
			@SuppressWarnings("resource")
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++) {
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
