import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

import javax.imageio.ImageIO;

public class HoughTransform {

	public static final int rows = 256;    //height of the given image
	public static final int columns = 256;  // width of the given image

	public static final int rAxisSize = columns; //height of the hough space
	public static final int thetaAxisSize = rows; //width of the hough space

	public static void main(String args[]) throws IOException{
		//Read the image from local C drive
		String fileName = "C:\\Users\\princy\\lines.raw"; 
		System.out.println("*** Start reading the raw image as bytes ***");
		//Store the pixel values of raw image in byte[]
		byte[] data = Files.readAllBytes(Paths.get(fileName));

		//Convert the image in raw format to bmp format
		BufferedImage buff = new BufferedImage(columns, rows, BufferedImage.TYPE_BYTE_GRAY);		
		WritableRaster wr = buff.getRaster();
		int count = 0;
		for(int i=0;i<wr.getHeight();i++)
		{
			for(int j=0; j<wr.getWidth(); j++)
			{
				wr.setSample(j, i, 0, data[count]);
				count++;
			}
		}

		buff.setData(wr);

		try {
			ImageIO.write(buff,"png",new File("lines.png"));   //image conversion to bmp format
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("*** Image converted succesfully to bmp format ****");

		/* Load the edge image in bmp file format for performing hough transform */
		File file = new File("lines.png");
		BufferedImage img = ImageIO.read(file);
		int width = img.getWidth();
		int height = img.getHeight();
		int[][] imgArr = new int[rows][columns];
		Raster raster = img.getData();
		//Reading the input image in bmp format into imgArr[][]
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				imgArr[i][j] = raster.getSample(j, i, 0);
			}
		}

		int houghArray[][] = new int[thetaAxisSize][rAxisSize];
		houghArray = houghTransform(width, height, imgArr); //performing the hough transform and storing the output as 2D array
		displayOutputHoughImage(houghArray);		// displaying the hough space as image in png format

		Vector<HoughLine> lines = new Vector<HoughLine>();
		lines = getLines(20, houghArray); 	// we are processing the accumulator array using approach A: Thresholding in text book
		System.out.println("Total number of lines detected from the hough space :" + lines.size());
		for(int i=0; i<lines.size(); i++){
			HoughLine line = lines.get(i);
			System.out.println("****** Theta coordinate value for line "+ (i+1) + " ******* " + line.theta);
			System.out.println("****** Rpho coordinate value for line "+ (i+1)+ " ******* "  + line.r);
			
		}
		
	}


	/* This method displays the resultant hough space in png format */ 
	private static void displayOutputHoughImage(int[][] houghArray) throws IOException {
		System.out.println(" ****Entering into the displayOutputHoughImage() *****");

		int max = getMaxValue(houghArray); //find the maximum peak value from the hough array
		System.out.println(max);
		BufferedImage outputImage = new BufferedImage(thetaAxisSize, rAxisSize, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < thetaAxisSize; y++)
		{
			for (int x = 0; x < rAxisSize; x++)
			{
				int n = Math.min((int)Math.round(houghArray[y][x] * 255 / max), 255);
				outputImage.setRGB(x, rAxisSize - 1 - y, (n << 16) | (n << 8) | 0x90 | -0x01000000);
			}
		}
		ImageIO.write(outputImage, "PNG", new File("hough.png"));  //Writing the hough array in 2D to png format
		System.out.println(" ****Exiting from the displayOutputHoughImage() *****");
	}

	/* This method gets the maximum value from the hough array */
	private static int getMaxValue(int[][] houghArray) {

		int max = 0; 
		for (int t = 0; t < thetaAxisSize; t++) { 
			for (int r = 0; r < rAxisSize; r++) { 
				if (houghArray[t][r] > max) { 
					max = houghArray[t][r]; 
				} 
			} 
		}
		return max;
	}

	/* This method performs the hough transform on the given edge image. Each point on the line in the given image
	 * is represented as a sinusoidal curve in the hough space where theta and rpho are variables and x,y becomes the parameters
	 * If there are many edge points on the line in the given image then we get that many sinusoidal curve in the hough space 
	 * and they all intersect at a common point called the peak points.
	 */
	private static int[][] houghTransform(int width, int height, int[][] imgArr) {
		System.out.println(" ****Entering into the houghTransform() *****");

		int outputData[][] = new int[thetaAxisSize][rAxisSize];
		int maxRadius = (int)Math.ceil(Math.hypot(width, height)); 
		int halfRAxisSize = rAxisSize >>> 1;
				System.out.println(thetaAxisSize);
				// x output ranges from 0 to pi
				// y output ranges from -maxRadius to maxRadius
				double[] sinTable = new double[thetaAxisSize];
				double[] cosTable = new double[thetaAxisSize];
				for (int theta = thetaAxisSize - 1; theta >= 0; theta--)
				{
					double thetaRadians = theta * Math.PI / thetaAxisSize;
					sinTable[theta] = Math.sin(thetaRadians);
					cosTable[theta] = Math.cos(thetaRadians);
				}

				for (int y = height - 1; y >= 0; y--)
				{
					for (int x = width - 1; x >= 0; x--)
					{
						if (imgArr[y][x] == 0)
						{
							for (int theta = thetaAxisSize - 1; theta >= 0; theta--)
							{
								double r = cosTable[theta] * x + sinTable[theta] * y;
								int rScaled = (int)Math.round(r * halfRAxisSize / maxRadius) + halfRAxisSize; // here we are scaling our rpho value as we have set the rpho size as 256
								outputData[theta][rScaled]++;
							}
						}
					}
				}

				System.out.println(" ****Exiting from the houghTransform() *****");
				return outputData;
	}

	/* After the points have been added to the hough/ parameter space, the hough algorithm then searches for the peak 
	 * points in the accumulator array cell. Higher the peak value, more values of x and y have crossed that curve
	 * intersecting at a common point  */
	private static Vector<HoughLine> getLines(int threshold, int houghArray[][]) {
		System.out.println(" ****Entering into the getLines() *****");

		int neighbourhoodSize = 4;
		int thetaStep = 1;
		// Initialise the vector of lines that we'll return 
		Vector<HoughLine> lines = new Vector<HoughLine>(20); 

		// Search for local peaks above threshold to draw 
		for (int t = 0; t < thetaAxisSize; t++) { 
			loop: 
				for (int r = neighbourhoodSize; r < rAxisSize - neighbourhoodSize; r++) { 

					// Only consider points above threshold 
					if (houghArray[t][r] > threshold) { 

						int peak = houghArray[t][r]; 

						// Check that this peak is indeed the local maxima 
						for (int dx = -neighbourhoodSize; dx <= neighbourhoodSize; dx++) { 
							for (int dy = -neighbourhoodSize; dy <= neighbourhoodSize; dy++) { 
								int dt = t + dx; 
								int dr = r + dy; 
								if (dt < 0) dt = dt + thetaAxisSize; 
								else if (dt >= thetaAxisSize) dt = dt - thetaAxisSize; 
								if (houghArray[dt][dr] > peak) { 
									// if any peak points is found nearby then skip 
									continue loop; 
								} 
							} 
						} 

						// calculate the true value of theta 
						double theta = t * thetaStep; 
						System.out.println("****** peak values ******* " + peak);
						// add the line to the vector 
						lines.add(new HoughLine(theta, r)); 

					} 
				} 
		} 

		System.out.println(" ****Exiting from the getLines() *****");
		return lines; 
	}
}
