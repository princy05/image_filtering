
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

public class QuantizedImage {
	public static void main(String args[]) throws IOException{
		
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
		
		/* quantizing the image by setting zeros to lowest order bits */
		quantizeImage(imgArr, 2);
		quantizeImage(imgArr, 3);
		quantizeImage(imgArr, 4);
	}

	public static void quantizeImage(int[][] imgArr, int NumOfBits) {
		int mask = 0;
		if(NumOfBits == 2){
			mask = 252;
		}else if(NumOfBits == 3){
			mask = 248 ;
		}else if(NumOfBits == 4){
			mask = 240;
		}
		int size = imgArr.length;
		System.out.println("Size of the matrix---- " + size);
		int[][] output = new int[size][size];
		for(int i=0; i < imgArr.length; i++){
			for(int j=0; j < imgArr.length; j++){
				output[i][j] = imgArr[i][j] & mask;
			}
		}
		
		BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY);
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
			if(NumOfBits == 2){
				writeMatrix("matrix1.txt", output);
				ImageIO.write(img, "jpg", new File("rose1.jpg"));
			}else if(NumOfBits == 3){
				writeMatrix("matrix2.txt", output);
				ImageIO.write(img, "jpg", new File("rose2.jpg"));
			}else if(NumOfBits == 4){
				writeMatrix("matrix3.txt", output);
				ImageIO.write(img, "jpg", new File("rose3.jpg"));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("**** Exiting from the quantizeImage() ****");

		
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
