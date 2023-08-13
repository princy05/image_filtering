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

public class SobelOperator {

	public static final int rows = 420;      //height of the given image
	public static final int columns = 560;  // width of the given image

	public static final int[][] filter_x = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}}; //filter kernel to detect vertical edges
	public static final int[][] filter_y = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}}; //filter kernel to detect horizontal edges

	public static void main(String args[]) throws IOException{
		//Read the image from local C drive
		String fileName = "C:\\Users\\princy\\building.raw"; 
		System.out.println("*** Start reading the raw image as bytes ***");
		//Store the pixel values of raw image in byte[]
		byte[] data = Files.readAllBytes(Paths.get(fileName));

		String horizontalEdge = "horizontal edge detection";
		String verticalEdge = "vertical edge detection";
		String gradientFilter = "gradient filter";

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
			ImageIO.write(buff,"bmp",new File("building.bmp"));   //image conversion to bmp format
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("*** Image converted succesfully to bmp format ****");

		/*Retrieve pixel values from bmp image as matrix for image manipulation */
		File file = new File("building.bmp");
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
		writeMatrix("sobelMatrix.txt", imgArr);

		//Edge detection 
		int horizontalEdgeDetected[][] = new int[rows][columns];
		horizontalEdgeDetected = sobelEdgeDetection(imgArr, horizontalEdge); //result array for horizontal edge detection
		displayOutput(horizontalEdgeDetected,"horizontal_edge_output"); 	//displaying output result in bmp format for horizontal edge detected
		int verticalEdgeDetected[][] = new int[rows][columns];
		verticalEdgeDetected = sobelEdgeDetection(imgArr, verticalEdge);	//result array for vertical edge detection
		displayOutput(verticalEdgeDetected, "vertical_edge_output");		//displaying output result in bmp format for vertical edge detected
		int gradFilter[][] = new int[rows][columns];
		gradFilter = sobelEdgeDetection(imgArr, gradientFilter);		//result array for image gradient
		displayOutput(gradFilter, "gradient_filter");					//displaying output result in bmp format for image gradient
		int thresholdImageOutput[][] = new int[rows][columns];
		thresholdImageOutput = thresholdedGradient(imgArr, 128);		//result array for thresholded gradient image
		displayOutput(thresholdImageOutput, "thresholded_gradient_image");	//displaying output result in bmp format for thresholded gradient image
		

	}

	/* This method is used to detect the thresholded gradient image */
	private static int[][] thresholdedGradient(int[][] imgArr, int threshold) {
		System.out.println("**** Entering into the thresholdedGradient() ****");
		
		int M = 3;		// MxM is the dimension of the filter kernel
		int M2 = (M-1)/2; // Half length of the mask, assuming M is an odd integer 
		int img_out[][] = new int[rows][columns];	//result image with same dimension as original image
		// we apply filter kernel inside the image making the boundaries to zero
		for(int i = 1; i+1 < rows; i++){
			for(int j = 1; j+1 < columns; j++){
				int sumx = 0;
				int sumy = 0;
				int sum = 0;
				for(int k = -M2; k <= M2; k++){ 		//each row of the mask
					for(int l = -M2; l <= M2; l++){		//each column of the mask
						sumx = sumx + imgArr[i+k][j+l] * filter_x[k+M2][l+M2];  //Calculate sum for vertical edge
                        sumy = sumy + imgArr[i+k][j+l] * filter_y[k+M2][l+M2]; //Calculate sum for horizontal edge
                        sum = (int)Math.sqrt((sumx * sumx) + (sumy * sumy));    //calculate sum for gradient filter
					}
				}
				if (sum < 0)       		//keep the intensity values in range i:e negative values map to zero (clamping)
					sum = 0;
				else if(sum == 0)  
					sum = 128;
				else if(sum > 128) 		//for threshold TE = 128, make values grater than 128 to value 128
					sum = 128;
				img_out[i][j] = sum; 	//insert calculated sum value into output result image
			}
		}	
		
		System.out.println("**** Exiting from thresholdedGradient() ****");
		return img_out;
	}
	
	/* This method converts the resultant output array in 2D to bmp format */
	public static void displayOutput(int[][] img_out, String fileName) {
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
		System.out.println("**** Exiting from displayOutput() ****");
	}

	/*This method is used to detect the horizontal, vertical and gradient image outputs */
	public static int[][] sobelEdgeDetection(int[][] imgArr, String edgeDetection) {
		System.out.println("**** Entering into sobelEdgeDetection() ****** ");
		int M = 3;		// MxM is the dimension of the filter kernel
		int M2 = (M-1)/2; // Half length of the mask, assuming M is an odd integer

		int img_out[][] = new int[rows][columns];	//output image

		if (edgeDetection.equalsIgnoreCase("horizontal edge detection")){ //run the for loop for horizontal edge detection 
			// we apply filter kernel inside the image making the boundaries to zero
			for(int i = 1; i+1 < rows; i++){
				for(int j = 1; j+1 < columns; j++){
					int sumx = 0;
					for(int k = -M2; k <= M2; k++){ 		//each row of the mask
						for(int l = -M2; l <= M2; l++){		//each column of the mask
							sumx += imgArr[i+k][j+l] * filter_y[k+M2][l+M2];
						}
					}
					if (sumx < 0)       //keeping intensity values in range i:e negative values map to zero (clamping)
						sumx = 0;
					else if(sumx == 0)  
						sumx=127;
					else if(sumx > 255) //keeping intensity values in range i:e values greater than 255 map to 255
						sumx = 255;
					img_out[i][j] = sumx; //insert calculated sum value into output image

				}
			}	
		}else if(edgeDetection.equalsIgnoreCase("vertical edge detection")){	//run the for loop for vertical edge detection

			for(int i = 1; i+1 < rows; i++){
				for(int j = 1; j+1 < columns; j++){
					int sumy = 0;
					for(int k = -M2; k <= M2; k++){ 		//each row of the mask
						for(int l = -M2; l <= M2; l++){		//each column of the mask
							sumy += imgArr[i+k][j+l] * filter_x[k+M2][l+M2];
						}
					}
					if (sumy < 0)       //keeping intensity values in range i:e negative values map to zero (clamping)
						sumy = 0;
					else if(sumy == 0)  
						sumy=127;
					else if(sumy > 255) //keeping intensity values in range i:e values greater than 255 map to 255
						sumy = 255;
					img_out[i][j] = sumy; //insert calculated sum value into output image

				}
			}	
		}else if(edgeDetection.equalsIgnoreCase("gradient filter")){	//run the for loop for image gradient filtern
			
			for(int i = 1; i+1 < rows; i++){
				for(int j = 1; j+1 < columns; j++){
					int sumx = 0;
					int sumy = 0;
					int sum = 0;
					for(int k = -M2; k <= M2; k++){ 		//each row of the mask
						for(int l = -M2; l <= M2; l++){		//each column of the mask
							sumx = sumx + imgArr[i+k][j+l] * filter_x[k+M2][l+M2];  //Calculate sum for vertical edge
                            sumy = sumy + imgArr[i+k][j+l] * filter_y[k+M2][l+M2]; //Calculate sum for horizontal edge
                            sum = (int)Math.sqrt((sumx * sumx) + (sumy * sumy));    //calculate sum for gradient filter
						}
					}
					if (sum < 0)       		//keeping intensity values in range i:e negative values map to zero (clamping)
						sum = 0;
					else if(sum == 0)  
						sum = 127;
					else if(sum > 255) 		//keeping intensity values in range i:e values greater than 255 map to 255
						sum = 255;
					img_out[i][j] = sum; 	//insert calculated sum value into output image
				}
			}	
			
		}
		System.out.println("**** Exiting from sobelEdgeDetection() ****** ");

		return (img_out);
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
