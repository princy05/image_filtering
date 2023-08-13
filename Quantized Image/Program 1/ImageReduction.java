
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

public class ImageReduction {
	public static void main(String args[]) throws IOException {
		System.out.println("**** Entering into the main() ****");
		//Read the image from local C drive
		String fileName = "C:\\Users\\princy\\rose.raw"; 
		System.out.println("*** Start reading the raw image as bytes ***");
		//Read the raw data image file from local drive
		byte[] data = Files.readAllBytes(Paths.get(fileName));
		/*Convert the raw data to jpg format */
		BufferedImage buff = new BufferedImage(256,256, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wr = buff.getRaster();
		int x = 0;
		for(int i=0;i<256;i++)
		{
			for(int j=0; j<256; j++)
			{
				wr.setSample(j, i, 0, data[x]);
				x++;
			}
		}
		buff.setData(wr);
		ImageIO.write(buff, "jpg", new File("rose.jpg"));
		System.out.println("*** Image converted succesfully to jpg format ****");

		/*Retrieve pixel values from jpg image as matrix for image manipulation */
		File file = new File("rose.jpg");
		BufferedImage img = ImageIO.read(file);
		int width = img.getWidth();
		int height = img.getHeight();
		System.out.println("*****Width value*****: " + width);
		System.out.println("*****Height value*****: " + height);
		int[][] imgArr = new int[width][height];
		Raster raster = img.getData();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				imgArr[i][j] = raster.getSample(i, j, 0);
			}
		}
		/* writing the pixel values as matrix in a file */
		writeMatrix("matrix.txt", imgArr);
		/* reduce the image size to 128x128 */
		reduceMatrix128(imgArr);  
		/* reduce the image size to 64x64 */
		reduceMatrix64(imgArr);
		/* reduce the image size to 32X32 */
		reduceMatrix32(imgArr);
		System.out.println("**** Exiting from the main() ****");
	}

	public static void reduceMatrix32(int[][] imgArr) {
		System.out.println("**** Entering into the reduceMatrix32() ****");
		int n = 256;
		int[][] output = new int[n/8][n/8];
		
		int iCount = 0;

		for(int i=0;i<n;i+=8)
		{
			int jCount =0;
			for(int j=0; j<n; j+=8){

				int row1 = (imgArr[i][j]+imgArr[i][j+1]+imgArr[i][j+2]+imgArr[i][j+3]+imgArr[i][j+4]+imgArr[i][j+5]+imgArr[i][j+6]+imgArr[i][j+7]);
				int row2 = (imgArr[i+1][j]+imgArr[i+1][j+1]+imgArr[i+1][j+2]+imgArr[i+1][j+3]+imgArr[i+1][j+4]+imgArr[i+1][j+5]+imgArr[i+1][j+6]+imgArr[i+1][j+7]);
				int row3 = (imgArr[i+2][j]+imgArr[i+2][j+1]+imgArr[i+2][j+2]+imgArr[i+2][j+3]+imgArr[i+2][j+4]+imgArr[i+2][j+5]+imgArr[i+2][j+6]+imgArr[i+2][j+7]);
				int row4 = (imgArr[i+3][j]+imgArr[i+3][j+1]+imgArr[i+3][j+2]+imgArr[i+3][j+3]+imgArr[i+3][j+4]+imgArr[i+3][j+5]+imgArr[i+3][j+6]+imgArr[i+3][j+7]);
				int row5 = (imgArr[i+4][j]+imgArr[i+4][j+1]+imgArr[i+4][j+2]+imgArr[i+4][j+3]+imgArr[i+4][j+4]+imgArr[i+4][j+5]+imgArr[i+4][j+6]+imgArr[i+4][j+7]);
				int row6 = (imgArr[i+5][j]+imgArr[i+5][j+1]+imgArr[i+5][j+2]+imgArr[i+5][j+3]+imgArr[i+5][j+4]+imgArr[i+5][j+5]+imgArr[i+5][j+6]+imgArr[i+5][j+7]);
				int row7 = (imgArr[i+6][j]+imgArr[i+6][j+1]+imgArr[i+6][j+2]+imgArr[i+6][j+3]+imgArr[i+6][j+4]+imgArr[i+6][j+5]+imgArr[i+6][j+6]+imgArr[i+6][j+7]);
				int row8 = (imgArr[i+7][j]+imgArr[i+7][j+1]+imgArr[i+7][j+2]+imgArr[i+7][j+3]+imgArr[i+7][j+4]+imgArr[i+7][j+5]+imgArr[i+7][j+6]+imgArr[i+7][j+7]);
				int avgVal = (row1+row2+row3+row4+row5+row6+row7+row8) / 64;
				output[iCount][jCount] = avgVal;
				jCount +=1;
			}
			iCount+=1;

		}
		System.out.println("****** Length of new reduced matrix *****: " + output.length);
		writeMatrix("matrix32.txt", output);
		BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wr = img.getRaster();

		for(int i=0;i<output.length;i++)
		{
			for(int j=0; j<output[i].length; j++)
			{
				wr.setSample(i, j, 0, output[i][j]);
			}
		}
		img.setData(wr);
		try {
			ImageIO.write(img, "jpg", new File("rose32.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("**** Exiting from the reduceMatrix32() ****");		
	}

	public static void reduceMatrix64(int[][] imgArr) {
		System.out.println("**** Entering into the reduceMatrix64() ****");
		int n = 256;
		int[][] output = new int[n/4][n/4];
		
		int iCount = 0;

		for(int i=0;i<n;i+=4)
		{
			int jCount =0;
			for(int j=0; j<n; j+=4){

				int row1 = (imgArr[i][j]+imgArr[i][j+1]+imgArr[i][j+2]+imgArr[i][j+3]);
				int row2 = (imgArr[i+1][j]+imgArr[i+1][j+1]+imgArr[i+1][j+2]+imgArr[i+1][j+3]);
				int row3 = (imgArr[i+2][j]+imgArr[i+2][j+1]+imgArr[i+2][j+2]+imgArr[i+2][j+3]);
				int row4 = (imgArr[i+3][j]+imgArr[i+3][j+1]+imgArr[i+3][j+2]+imgArr[i+3][j+3]);
				int avgVal = (row1+row2+row3+row4) / 16 ;
				output[iCount][jCount] = avgVal;
				jCount +=1;
			}
			iCount+=1;

		}
		System.out.println("**** Length of new reduced matrix ****: " + output.length);
		writeMatrix("matrix64.txt", output);
		BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wr = img.getRaster();

		for(int i=0;i<output.length;i++)
		{
			for(int j=0; j<output[i].length; j++)
			{
				wr.setSample(i, j, 0, output[i][j]);
			}
		}
		img.setData(wr);
		try {
			ImageIO.write(img, "jpg", new File("rose64.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("**** Exiting from the reduceMatrix64() ****");
		
	}

	private static void reduceMatrix128(int[][] imgArr) {
		System.out.println("**** Entering into the reduceMatrix128() ****");
		int n = 256;
		int[][] output = new int[n/2][n/2];
		
		int iCount = 0;

		for(int i=0;i<n;i+=2)
		{
			int jCount =0;
			for(int j=0; j<n; j+=2){
				int p1= imgArr[i][j];
				int p2= imgArr[i][j+1];
				int p3= imgArr[i+1][j];
				int p4= imgArr[i+1][j+1];

				int avgVal = (p1+p2+p3+p4) / 4 ;
				output[iCount][jCount] = avgVal;
				jCount +=1;
			}
			iCount+=1;

		}
		System.out.println("**** Length of new reduced matrix ****: " + output.length);
		writeMatrix("matrix128.txt", output);
		BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster wr = img.getRaster();

		for(int i=0;i<output.length;i++)
		{
			for(int j=0; j<output[i].length; j++)
			{
				wr.setSample(i, j, 0, output[i][j]);
			}
		}
		img.setData(wr);
		try {
			ImageIO.write(img, "jpg", new File("rose128.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("**** Exiting from the reduceMatrix128() ****");

	}
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

