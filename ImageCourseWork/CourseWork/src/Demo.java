
  //*********************************//
 //Mohammed Yaseen Sultan 160389076 //
//*********************************//

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;

public class Demo extends Component implements ActionListener {

	// ************************************
	// List of the options(Original, Negative); correspond to the cases:
	// ************************************

	String descs[] = { "Original", "Negative", "Scaled", "Shifted", "Randomised then Scaled and Shifted", "Add",
			"Subtract", "Multiply", "Divide", "NOT", "AND", "OR", "XOR", "ROI AND", "ROI Multiply", "ROI NOT",
			"Negative Linear Transform", "Logarithic Function", "Power-Law", "Random Look-Up Table",
			"Bit-Plane Slicing", "Histogram Equalization", "Averaging", "Weighted Averaging", "4-Neighbour Laplacian",
			"8-Neighbour Laplacian", "4-Neighbour Laplacian Enhancement", "8-Neighbour Laplacian Enhancement",
			"Roberts 1", "Roberts 2", "Sobel X", "Sobel Y", "Add Salt-Peper Noise", "Min Filter", "Max Filter",
			"Midpoint Filter", "Median Filter", "Mean & Standard Deviation", "Simple Thresholding",
			"Automated Thresholding","Keep" };

	String descsROI[] = { "Original ROI", "Flip ROI", "Negative", "Scaled", "Shifted", "Randomised then Scaled and Shifted", "Add",
			"Subtract", "Multiply", "Divide", "NOT", "AND", "OR", "XOR", "Negative Linear Transform",
			"Logarithic Function", "Power-Law", "Random Look-Up Table", "Bit-Plane Slicing", "Histogram Equalization",
			"Averaging", "Weighted Averaging", "4-Neighbour Laplacian", "8-Neighbour Laplacian",
			"4-Neighbour Laplacian Enhancement", "8-Neighbour Laplacian Enhancement", "Roberts 1", "Roberts 2",
			"Sobel X", "Sobel Y", "Add Salt-Peper Noise", "Min Filter", "Max Filter", "Midpoint Filter",
			"Median Filter", "Mean & Standard Deviation", "Simple Thresholding", "Automated Thresholding","Keep" };

	int opIndex; // option index for
	int lastOp;

	//Buffered Images
	private BufferedImage bi, bi3, biFiltered, biScaled, roi, biFilteredROI, biFilteredBeforeROI, biFilteredConseq; // the input image saved
																								// as bi;//
	//ArrayLists to hold previous images for Undo
	private ArrayList<BufferedImage> beforeROIs, afterROIs, rois, biFilteredConseqs;
	//Undo counts
	private int undoCount, biFilteredConseqCount;
	
	int w, h;

	// Look up tables
	private static int[] LUT = new int[256];
	//Histogram arrays
	private double[] HistogramR = new double[256];
	private double[] HistogramG = new double[256];
	private double[] HistogramB = new double[256];
	private double[] HistogramRNorm = new double[256];
	private double[] HistogramGNorm = new double[256];
	private double[] HistogramBNorm = new double[256];
	private double[] HistogramREq = new double[256];
	private double[] HistogramGEq = new double[256];
	private double[] HistogramBEq = new double[256];
	//Masks for convulotion 
	private float[][] Mask = new float[3][3];
	private float[][] aMask = { { 1f, 1f, 1f }, { 1f, 1f, 1f }, { 1f, 1f, 1f } };
	private float[][] wAMask = { { 1f, 2f, 1f }, { 2f, 4f, 2f }, { 1f, 2f, 1f } };
	private float[][] fourNLMask = { { 0f, -1f, 0f }, { -1f, 4f, -1f }, { 0f, -1f, 0f } };
	private float[][] eightNLMask = { { -1f, -1f, -1f }, { -1f, 8f, -1f }, { -1f, -1f, -1f } };
	private float[][] fourNLEnhancedMask = { { 0f, -1f, 0f }, { -1f, 5f, -1f }, { 0f, -1f, 0f } };
	private float[][] eightNLEnhancedMask = { { -1f, -1f, -1f }, { -1f, 9f, -1f }, { -1f, -1f, -1f } };
	private float[][] robertsOneMask = { { 0f, 0f, 0f }, { 0f, 0f, -1f }, { 0f, 1f, 0f } };
	private float[][] robertsTwoMask = { { 0f, 0f, 0f }, { 0f, -1f, 0f }, { 0f, 0f, 1f } };
	private float[][] sobelXMask = { { -1f, 0f, 1f }, { -2f, 0f, 2f }, { -1f, 0f, 1f } };
	private float[][] sobelYMask = { { -1f, -2f, -1f }, { 0f, 0f, 0f }, { 1f, 2f, 1f } };
	//Mean and Standard Deviation
	private float rMean, gMean, bMean;
	private float rSD, gSD, bSD;
	//String to keep track of current proccess (which undo buton or image proccess)
	private String proccess = "";

	public Demo() {
		try {
			bi = ImageIO.read(new File("Baboon.bmp"));
			w = bi.getWidth(null);
			h = bi.getHeight(null);

			//System.out.println(bi.getType());
			if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
				BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				Graphics big = bi2.getGraphics();
				big.drawImage(bi, 0, 0, null);
				biFiltered = bi = bi2;
			}
		} catch (IOException e) { // deal with the situation that th image has problem;/
			System.out.println("Image could not be read");

			System.exit(1);
		}
	}

	public Demo(String fileName) {
		try {
			// Get file type
			String splitter = "\\.";
			String[] nameSplit = fileName.split(splitter);
			String fileType = nameSplit[nameSplit.length - 1];
			///
			if (fileType.equals("tiff")) {
				File image = new File(fileName);
				bi = ImageIO.read(image);
			} else {
				bi = ImageIO.read(new File(fileName));
			}
			w = bi.getWidth(null);
			h = bi.getHeight(null);

			biScaled = rescale(bi, 2);

			//System.out.println(bi.getType());
			if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
				BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				Graphics big = bi2.getGraphics();
				big.drawImage(bi, 0, 0, null);
				big.drawImage(bi, w, 0, null);
				biFiltered = bi = bi2;
			}

		} catch (IOException e) { // deal with the situation that th image has problem;/
			System.out.println("Image could not be read");

			System.exit(1);
		}
	}

	public Demo(String fileName, String fileName2) {
		try {
			bi = ImageIO.read(new File(fileName));
			bi3 = ImageIO.read(new File(fileName2));
			w = bi.getWidth(null);
			h = bi.getHeight(null);

			roi = ImageIO.read(new File("roi.bmp"));
			biFilteredROI = ImageIO.read(new File("roi.bmp"));
			biFilteredROI = roiAND(bi, roi);
			biFilteredBeforeROI = ImageIO.read(new File(fileName));
			biFilteredConseq =  ImageIO.read(new File(fileName));
			
			beforeROIs = new ArrayList<BufferedImage>();
			afterROIs = new ArrayList<BufferedImage>();
			rois= new ArrayList<BufferedImage>();
			biFilteredConseqs = new ArrayList<BufferedImage>();
			
			beforeROIs.add(bi); /* original */
			afterROIs.add(roiAND(bi, roi));
			rois.add(roi);
			biFilteredConseqs.add(biFilteredConseq);
			
			undoCount = 0;
			biFilteredConseqCount = 0;
			
			//System.out.println(bi.getType());
			if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
				BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				Graphics big = bi2.getGraphics();
				big.drawImage(bi, 0, 0, null);
				big.drawImage(bi, w, 0, null);
				big.drawImage(bi3, 0, h, null);
				big.drawImage(roi, w, h, null);
				big.drawImage(biFiltered, 2 * w, h, null);
				big.drawImage(biFilteredConseq, 2*w, 0, null);
				// big.drawImage(roiAND(bi, roi), 2*w, h, null);
				biFiltered = bi = bi2;
			}

		} catch (IOException e) { // deal with the situation that th image has problem;/
			System.out.println("Image could not be read");

			System.exit(1);
		}
	}
	
	//Constructor that takes 3 files (image1, image2 and roi image)
	public Demo(File fileOne, File fileTwo, File fileThree) {
		try {
			bi = ImageIO.read(fileOne);
			bi3 = ImageIO.read(fileTwo);
			w = bi.getWidth(null);
			h = bi.getHeight(null);

			roi = ImageIO.read(fileThree);
			biFilteredROI = ImageIO.read(fileThree);
			biFilteredROI = roiAND(bi, roi);
			biFilteredBeforeROI = ImageIO.read(fileOne);
			biFilteredConseq =  ImageIO.read(fileOne);
			
			beforeROIs = new ArrayList<BufferedImage>();
			afterROIs = new ArrayList<BufferedImage>();
			rois= new ArrayList<BufferedImage>();
			biFilteredConseqs = new ArrayList<BufferedImage>();
			
			beforeROIs.add(bi); /* original */
			afterROIs.add(roiAND(bi, roi));
			rois.add(roi);
			biFilteredConseqs.add(biFilteredConseq);
			
			undoCount = 0;
			biFilteredConseqCount = 0;
			
			//System.out.println(bi.getType());
			if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
				BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				Graphics big = bi2.getGraphics();
				big.drawImage(bi, 0, 0, null);
				big.drawImage(bi, w, 0, null);
				big.drawImage(bi3, 0, h, null);
				big.drawImage(roi, w, h, null);
				big.drawImage(biFiltered, 2 * w, h, null);
				big.drawImage(biFilteredConseq, 2*w, 0, null);
				// big.drawImage(roiAND(bi, roi), 2*w, h, null);
				biFiltered = bi = bi2;
			}

		} catch (IOException e) { // deal with the situation that th image has problem;/
			System.out.println("Image could not be read");

			System.exit(1);
		}
	}


	public Dimension getPreferredSize() {
		return new Dimension(3 * w, 3 * h);
	}

	String[] getDescriptions() {
		return descs;
	}

	String[] getDescriptionsROI() {
		return descsROI;
	}

	// Return the formats sorted alphabetically and in lower case
	public String[] getFormats() {
		String[] formats = { "bmp", "gif", "jpeg", "jpg", "png" };
		TreeSet<String> formatSet = new TreeSet<String>();
		for (String s : formats) {
			formatSet.add(s.toLowerCase());
		}
		return formatSet.toArray(new String[0]);
	}

	void setOpIndex(int i) {
		opIndex = i;
	}

	public void paint(Graphics g) { // Repaint will call this function so the image will change.
		//Check what the process is and do appropriate paint
		if (proccess.equals("ROI")) {
			paintROI(g);
			return;
		} else if (proccess.equals("UNDO")) {
			paintWithoutFilter(g);
			return;
		} else if (proccess.equals("BICONSEQ")) {
			paintBiConseq(g);
			return;
		} else if (proccess.equals("BICONSEQUNDO")){
			paintWithoutFilter(g);
			return;
		}
		filterImage();
		g.drawImage(bi, 0, 0, null);
		g.drawImage(bi3, 0, h, null);
		g.drawImage(biFiltered, w, 0, null);
		g.drawImage(roi, w, h, null);
		g.drawImage(biFilteredROI, 2 * w, h, null);
		g.drawImage(biFilteredConseq, 2*w, 0, null);
	}
	
	//Paint methods for ROI, NormalCombined and undos
	public void paintROI(Graphics g) { // Repaint will call this function so the image will change.
		filterImageROI();
		g.drawImage(bi, 0, 0, null);
		g.drawImage(bi3, 0, h, null);
		g.drawImage(biFiltered, w, 0, null);
		g.drawImage(roi, w, h, null);
		g.drawImage(biFilteredROI, 2 * w, h, null);
		g.drawImage(biFilteredConseq, 2*w, 0, null);
	}
	
	public void paintWithoutFilter(Graphics g) { // Repaint will call this function so the image will change.
		g.drawImage(bi, 0, 0, null);
		g.drawImage(bi3, 0, h, null);
		g.drawImage(biFiltered, w, 0, null);
		g.drawImage(roi, w, h, null);
		g.drawImage(biFilteredROI, 2 * w, h, null);
		g.drawImage(biFilteredConseq, 2*w, 0, null);
	}
	
	public void paintBiConseq(Graphics g) { // Repaint will call this function so the image will change.
		filterImageConseq();
		g.drawImage(bi, 0, 0, null);
		g.drawImage(bi3, 0, h, null);
		g.drawImage(biFiltered, w, 0, null);
		g.drawImage(roi, w, h, null);
		g.drawImage(biFilteredROI, 2 * w, h, null);
		g.drawImage(biFilteredConseq, 2*w, 0, null);
	}

	// ************************************
	// Convert the Buffered Image to Array
	// ************************************
	private static int[][][] convertToArray(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		int[][][] result = new int[width][height][4];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int p = image.getRGB(x, y);
				int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;

				result[x][y][0] = a;
				result[x][y][1] = r;
				result[x][y][2] = g;
				result[x][y][3] = b;
			}
		}
		return result;
	}

	// ************************************
	// Convert the Array to BufferedImage
	// ************************************
	public BufferedImage convertToBimage(int[][][] TmpArray) {

		int width = TmpArray.length;
		int height = TmpArray[0].length;

		BufferedImage tmpimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int a = TmpArray[x][y][0];
				int r = TmpArray[x][y][1];
				int g = TmpArray[x][y][2];
				int b = TmpArray[x][y][3];

				// set RGB value

				int p = (a << 24) | (r << 16) | (g << 8) | b;
				tmpimg.setRGB(x, y, p);

			}
		}
		return tmpimg;
	}

	// ************************************
	// Example: Image Negative
	// ************************************
	public BufferedImage ImageNegative(BufferedImage timg) {
		int width = timg.getWidth();
		int height = timg.getHeight();

		int[][][] ImageArray = convertToArray(timg); // Convert the image to array

		// Image Negative Operation:
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				ImageArray[x][y][1] = 255 - ImageArray[x][y][1]; // r
				ImageArray[x][y][2] = 255 - ImageArray[x][y][2]; // g
				ImageArray[x][y][3] = 255 - ImageArray[x][y][3]; // b
			}
		}

		return convertToBimage(ImageArray); // Convert the array to BufferedImage
	}

	// ************************************
	// Your turn now: Add more function below
	// ************************************
	
	//Take rescale float (between 0 and 2) and scales each pixel value
	public BufferedImage rescale(BufferedImage originalImage, float s) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(originalImage);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				ImageArray2[x][y][1] = Math.round((s * (ImageArray1[x][y][1]))); // r
				ImageArray2[x][y][2] = Math.round((s * (ImageArray1[x][y][2]))); // g
				ImageArray2[x][y][3] = Math.round((s * (ImageArray1[x][y][3]))); // b
				if (ImageArray2[x][y][1] < 0) {
					ImageArray2[x][y][1] = 0;
				}
				if (ImageArray2[x][y][2] < 0) {
					ImageArray2[x][y][2] = 0;
				}
				if (ImageArray2[x][y][3] < 0) {
					ImageArray2[x][y][3] = 0;
				}
				if (ImageArray2[x][y][1] > 255) {
					ImageArray2[x][y][1] = 255;
				}
				if (ImageArray2[x][y][2] > 255) {
					ImageArray2[x][y][2] = 255;
				}
				if (ImageArray2[x][y][3] > 255) {
					ImageArray2[x][y][3] = 255;
				}
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Takes int and shifts pixel values by that int (can be negative)
	public BufferedImage shift(BufferedImage originalImage, int s) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(originalImage);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				ImageArray2[x][y][1] = Math.round(((ImageArray1[x][y][1] + s))); // r
				ImageArray2[x][y][2] = Math.round(((ImageArray1[x][y][2] + s))); // g
				ImageArray2[x][y][3] = Math.round(((ImageArray1[x][y][3] + s))); // b
				if (ImageArray2[x][y][1] < 0) {
					ImageArray2[x][y][1] = 0;
				}
				if (ImageArray2[x][y][2] < 0) {
					ImageArray2[x][y][2] = 0;
				}
				if (ImageArray2[x][y][3] < 0) {
					ImageArray2[x][y][3] = 0;
				}
				if (ImageArray2[x][y][1] > 255) {
					ImageArray2[x][y][1] = 255;
				}
				if (ImageArray2[x][y][2] > 255) {
					ImageArray2[x][y][2] = 255;
				}
				if (ImageArray2[x][y][3] > 255) {
					ImageArray2[x][y][3] = 255;
				}
			}
		}
		return convertToBimage(ImageArray2);
	}
    
	//Takes an image array finds the minimum pixel value and uses that to shift all values so dynamic range starts at 0
	//Finds the max and uses that to scale so that dynamic range is max 255
    public BufferedImage findShiftAndScale(int[][][] ImageArray1) {
    	BufferedImage originalImage = convertToBimage(ImageArray1);
    	int width = originalImage.getWidth();
        int height = originalImage.getHeight();
    	int min = ImageArray1[0][0][1];
    	int max = ImageArray1[0][0][1];
    	
    	for(int y=0; y<height; y++){
    		for(int x=0; x<width; x++){

    			if(ImageArray1[x][y][1]<min) {min = ImageArray1[x][y][1];}
    			if(ImageArray1[x][y][2]<min) {min = ImageArray1[x][y][2];}
    			if(ImageArray1[x][y][3]<min) {min = ImageArray1[x][y][3];}
    			if(ImageArray1[x][y][1]>max) {max = ImageArray1[x][y][1];}
    			if(ImageArray1[x][y][2]>max) {max = ImageArray1[x][y][2];}
    			if(ImageArray1[x][y][3]>max) {max = ImageArray1[x][y][3];}
    		}
    	}
    	
    	int shift = min * -1;
    	float scale = 255f/(max+shift);
    	//System.out.println(scale+ " " + shift);
    	return shiftAndScale(ImageArray1, scale, shift);
    }
    
    //Helper method called by findShiftAndScale() to return the actual array as the afformentioned method only finds the shift and scale values
    public BufferedImage shiftAndScale(int[][][] ImageArray1, float s, int t) {
    	BufferedImage originalImage = convertToBimage(ImageArray1);
    	int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int[][][] ImageArray2 = convertToArray(originalImage); 
        
    	int rmin, bmin, gmin;
    	int rmax, bmax, gmax;
    	rmin = Math.round(s*(ImageArray2[0][0][1]+t)); 
    	rmax = rmin;
    	gmin = Math.round(s*(ImageArray2[0][0][2]+t));
    	gmax = gmin;
    	bmin = Math.round(s*(ImageArray2[0][0][3]+t)); 
    	bmax = bmin;
    	for(int y=0; y<height; y++){
	    	for(int x=0; x<width; x++){
		    	ImageArray2[x][y][1] = Math.round(s*(ImageArray1[x][y][1]+t)); //r
		    	ImageArray2[x][y][2] = Math.round(s*(ImageArray1[x][y][2]+t)); //g
		    	ImageArray2[x][y][3] = Math.round(s*(ImageArray1[x][y][3]+t)); //b
		    	if (rmin>ImageArray2[x][y][1]) { rmin = ImageArray2[x][y][1]; }
		    	if (gmin>ImageArray2[x][y][2]) { gmin = ImageArray2[x][y][2]; }
		    	if (bmin>ImageArray2[x][y][3]) { bmin = ImageArray2[x][y][3]; }
		    	if (rmax<ImageArray2[x][y][1]) { rmax = ImageArray2[x][y][1]; }
		    	if (gmax<ImageArray2[x][y][2]) { gmax = ImageArray2[x][y][2]; }
		    	if (bmax<ImageArray2[x][y][3]) { bmax = ImageArray2[x][y][3]; }
	    	}
	    }
    	for(int y=0; y<height; y++){
	    	for(int x =0; x<width; x++){
	    		if (rmax-rmin ==0) {
	    			ImageArray2[x][y][1]=255*(ImageArray2[x][y][1]-rmin);
	    		} else {
	    			ImageArray2[x][y][1]=255*(ImageArray2[x][y][1]-rmin)/(rmax-rmin);
	    		}
	    		if (gmax-gmin ==0) {
	    			ImageArray2[x][y][2]=255*(ImageArray2[x][y][2]-gmin);
	    		} else {
	    			ImageArray2[x][y][2]=255*(ImageArray2[x][y][2]-gmin)/(gmax-gmin);
	    		}
	    		if (bmax-bmin ==0) {
	    			ImageArray2[x][y][3]=255*(ImageArray2[x][y][3]-bmin);
	    		} else {
	    			ImageArray2[x][y][3]=255*(ImageArray2[x][y][3]-bmin)/(bmax-bmin);
	    		}
	    	}
	    }
    	return convertToBimage(ImageArray2);
    }
    
    //Generates random number between 0 - 255 and then 50% chance of making that vvlaue negative 
    public static int randomNum() {
    	int r =(int)( Math.random()*256);
		double posNeg = Math.random();
		if (posNeg>0.5) {
			r = r * -1;
		}
		return r;
    }
    
    //Generates random numbers and adds them to the image array and then shifts and scales it 
    public BufferedImage randomShiftAndScale(BufferedImage originalImage) {
    	int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int[][][] ImageArray1 = convertToArray(originalImage);          //  Convert the image to array
        int[][][] ImageArray2 = convertToArray(originalImage); 
        
        for(int y=0; y<height; y++){
        	for(int x=0; x<width; x++){
        		int r1 = randomNum();
        		ImageArray2[x][y][1] = (ImageArray1[x][y][1])+r1; //r
        		int r2 = randomNum();
            	ImageArray2[x][y][2] = (ImageArray1[x][y][2])+r2; //g
            	int r3 = randomNum();
            	ImageArray2[x][y][3] = (ImageArray1[x][y][3])+r3; //b
        	}
        } 
    	return findShiftAndScale((ImageArray2));
    }
    
    //Adds pixel values for both images and shifts and scales them
	public BufferedImage add(BufferedImage originalImage, BufferedImage image2) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(image2);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				ImageArray2[x][y][1] = ImageArray1[x][y][1] + ImageArray2[x][y][1];
				ImageArray2[x][y][2] = ImageArray1[x][y][2] + ImageArray2[x][y][2];
				ImageArray2[x][y][3] = ImageArray1[x][y][3] + ImageArray2[x][y][3];
			}
		}

		return findShiftAndScale((ImageArray2));
	}

	 //Difference between pixel values for both images and shifts and scales them
	public BufferedImage subtract(BufferedImage originalImage, BufferedImage image2) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(image2);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				ImageArray2[x][y][1] = (ImageArray1[x][y][1] - ImageArray2[x][y][1]);
				ImageArray2[x][y][2] = (ImageArray1[x][y][2] - ImageArray2[x][y][2]);
				ImageArray2[x][y][3] = (ImageArray1[x][y][3] - ImageArray2[x][y][3]);
				
//				ImageArray2[x][y][1] = Math.abs(ImageArray1[x][y][1] - ImageArray2[x][y][1]);
//				ImageArray2[x][y][2] = Math.abs(ImageArray1[x][y][2] - ImageArray2[x][y][2]);
//				ImageArray2[x][y][3] = Math.abs(ImageArray1[x][y][3] - ImageArray2[x][y][3]);
//				ImageArray2[x][y][1] = (ImageArray2[x][y][1] + 255) / 2;
//				ImageArray2[x][y][2] = (ImageArray2[x][y][2] + 255) / 2;
//				ImageArray2[x][y][3] = (ImageArray2[x][y][3] + 255) / 2;
			}
		}
		return findShiftAndScale(ImageArray2);
	}

	//Multiplies both image pixel values and shifts and scales them
	public BufferedImage multiply(BufferedImage originalImage, BufferedImage image2) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(image2);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				ImageArray2[x][y][1] = ImageArray1[x][y][1] * ImageArray2[x][y][1];
				ImageArray2[x][y][2] = ImageArray1[x][y][2] * ImageArray2[x][y][2];
				ImageArray2[x][y][3] = ImageArray1[x][y][3] * ImageArray2[x][y][3];
			}
		}
		return findShiftAndScale(ImageArray2);
	}
	
	//Divides image1 pixel values by image2 pixel values and shifts and scales them
	public BufferedImage divide(BufferedImage originalImage, BufferedImage image2) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(image2);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(ImageArray2[x][y][1]==0) {ImageArray2[x][y][1] = ImageArray1[x][y][1];} else {ImageArray2[x][y][1] = ImageArray1[x][y][1] / ImageArray2[x][y][1];}
				if(ImageArray2[x][y][2]==0) {ImageArray2[x][y][2] = ImageArray1[x][y][2];} else {ImageArray2[x][y][2] = ImageArray1[x][y][2] / ImageArray2[x][y][2];}
				if(ImageArray2[x][y][3]==0) {ImageArray2[x][y][3] = ImageArray1[x][y][3];} else {ImageArray2[x][y][3] = ImageArray1[x][y][3] / ImageArray2[x][y][3];}
				
//				ImageArray2[x][y][1] = ImageArray1[x][y][1] / ImageArray2[x][y][1];
//				ImageArray2[x][y][2] = ImageArray1[x][y][2] / ImageArray2[x][y][2];
//				ImageArray2[x][y][3] = ImageArray1[x][y][3] / ImageArray2[x][y][3];
			}
		}
		return findShiftAndScale(ImageArray2);
	}

	//Nots the image 
	public BufferedImage not(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(originalImage);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1]; // r
				g = ImageArray1[x][y][2]; // g
				b = ImageArray1[x][y][3]; // b
				ImageArray2[x][y][1] = (~r) & 0xFF; // r
				ImageArray2[x][y][2] = (~g) & 0xFF; // g
				ImageArray2[x][y][3] = (~b) & 0xFF; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Ands the pixel values of image1 and image2
	public BufferedImage and(BufferedImage originalImage, BufferedImage image2) {
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(image2);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1] & ImageArray2[x][y][1]; // r
				g = ImageArray1[x][y][2] & ImageArray2[x][y][2]; // g
				b = ImageArray1[x][y][3] & ImageArray2[x][y][3]; // b
				ImageArray2[x][y][1] = r; // r
				ImageArray2[x][y][2] = g; // g
				ImageArray2[x][y][3] = b; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Ors the pixel values of image1 and image2
	public BufferedImage or(BufferedImage originalImage, BufferedImage image2) {
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(image2);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1] | ImageArray2[x][y][1]; // r
				g = ImageArray1[x][y][2] | ImageArray2[x][y][2]; // g
				b = ImageArray1[x][y][3] | ImageArray2[x][y][3]; // b
				ImageArray2[x][y][1] = r; // r
				ImageArray2[x][y][2] = g; // g
				ImageArray2[x][y][3] = b; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Xors the pixel values of image1 and image2
	public BufferedImage xor(BufferedImage originalImage, BufferedImage image2) {
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(image2);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1] ^ ImageArray2[x][y][1]; // r
				g = ImageArray1[x][y][2] ^ ImageArray2[x][y][2]; // g
				b = ImageArray1[x][y][3] ^ ImageArray2[x][y][3]; // b
				ImageArray2[x][y][1] = r; // r
				ImageArray2[x][y][2] = g; // g
				ImageArray2[x][y][3] = b; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Ands the pixel values of image1 and image2(roi image) so only the white regions of the roi have non 0 pixel values
	public BufferedImage roiAND(BufferedImage originalImage, BufferedImage image2) {
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(image2);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1] & ImageArray2[x][y][1]; // r
				g = ImageArray1[x][y][2] & ImageArray2[x][y][2]; // g
				b = ImageArray1[x][y][3] & ImageArray2[x][y][3]; // b
				ImageArray2[x][y][1] = r; // r
				ImageArray2[x][y][2] = g; // g
				ImageArray2[x][y][3] = b; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//If roi image pixel value is 255 then multiply image1 pixel value by 1 else multiply by 0
	public BufferedImage roiMultiply(BufferedImage originalImage, BufferedImage image2) {
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(image2);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (ImageArray2[x][y][1] == 255) {
					r = ImageArray1[x][y][1] * 1;
				} else {
					r = ImageArray1[x][y][1] * 0;
				} // r
				if (ImageArray2[x][y][2] == 255) {
					g = ImageArray1[x][y][2] * 1;
				} else {
					g = ImageArray1[x][y][2] * 0;
				} // g
				if (ImageArray2[x][y][3] == 255) {
					b = ImageArray1[x][y][3] * 1;
				} else {
					b = ImageArray1[x][y][3] * 0;
				} // b
				ImageArray2[x][y][1] = r; // r
				ImageArray2[x][y][2] = g; // g
				ImageArray2[x][y][3] = b; // b
			}
		}

		return convertToBimage(ImageArray2);
	}

	//Nots the roi iage and then ands them so the roi flips 
	public BufferedImage roiNOT(BufferedImage originalImage, BufferedImage image2) {
		int[][][] ImageArray1 = convertToArray(originalImage); // Convert the image to array
		int[][][] ImageArray2 = convertToArray(not(image2));
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1] & ImageArray2[x][y][1]; // r
				g = ImageArray1[x][y][2] & ImageArray2[x][y][2]; // g
				b = ImageArray1[x][y][3] & ImageArray2[x][y][3]; // b
				ImageArray2[x][y][1] = r; // r
				ImageArray2[x][y][2] = g; // g
				ImageArray2[x][y][3] = b; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Sets LUT to be equal to negative linear transformation and then uses LUT to recreate image
	public BufferedImage negLinT(BufferedImage originalImage) {
		for (int k = 0; k <= 255; k++) {
			LUT[k] = 256 - 1 - k;
		}
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1]; // r
				g = ImageArray1[x][y][2]; // g
				b = ImageArray1[x][y][3]; // b
				ImageArray2[x][y][1] = LUT[r]; // r
				ImageArray2[x][y][2] = LUT[g]; // g
				ImageArray2[x][y][3] = LUT[b]; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Sets LUT to be equal to Logarithmic Function and then uses LUT to recreate image
	public BufferedImage logF(BufferedImage originalImage) {
		for (int k = 0; k <= 255; k++) {
			LUT[k] = (int) (Math.log(1 + k) * 255 / Math.log(256));
		}
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1]; // r
				g = ImageArray1[x][y][2]; // g
				b = ImageArray1[x][y][3]; // b
				ImageArray2[x][y][1] = LUT[r]; // r
				ImageArray2[x][y][2] = LUT[g]; // g
				ImageArray2[x][y][3] = LUT[b]; // b
			}
		}
		return convertToBimage(ImageArray2);
	}
	
	//Sets LUT to be equal to Power Law and then uses LUT to recreate image
	public BufferedImage powerLaw(BufferedImage originalImage, float p) {
		for (int k = 0; k <= 255; k++) {
			LUT[k] = (int) (Math.pow(255, 1 - p) * Math.pow(k, p));
		}
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1]; // r
				g = ImageArray1[x][y][2]; // g
				b = ImageArray1[x][y][3]; // b
				ImageArray2[x][y][1] = LUT[r]; // r
				ImageArray2[x][y][2] = LUT[g]; // g
				ImageArray2[x][y][3] = LUT[b]; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Sets LUT to be equal to random values (0 - 255) and then uses LUT to recreate image
	public BufferedImage randLT(BufferedImage originalImage) {
		for (int k = 0; k <= 255; k++) {
			LUT[k] = (int) (Math.random() * 256);
		}
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1]; // r
				g = ImageArray1[x][y][2]; // g
				b = ImageArray1[x][y][3]; // b
				ImageArray2[x][y][1] = LUT[r]; // r
				ImageArray2[x][y][2] = LUT[g]; // g
				ImageArray2[x][y][3] = LUT[b]; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Pass an int (0-7) and shifts the values to reporesent the corresponding bitplanes of image 
    public BufferedImage bitplaneSlice(BufferedImage originalImage, int k) {
    	int[][][] ImageArray1 = convertToArray(originalImage); 
    	int[][][] ImageArray2 = convertToArray(originalImage);
    	int r, b, g;
    	for(int y=0; y<h; y++){
    		for(int x=0; x<w; x++){
	    		r = ImageArray1[x][y][1]; //r
	    		g = ImageArray1[x][y][2]; //g
	    		b = ImageArray1[x][y][3]; //b
	    		ImageArray2[x][y][1] = (r>>k)&1; //r
	    		ImageArray2[x][y][2] = (g>>k)&1; //g
	    		ImageArray2[x][y][3] = (b>>k)&1; //b
	    		//Binary so value is 0 or 1 so make 1 = 255
	    		if (ImageArray2[x][y][1] == 1) {ImageArray2[x][y][1] = 255;}
	    		if (ImageArray2[x][y][2] == 1) {ImageArray2[x][y][2] = 255;}
	    		if (ImageArray2[x][y][3] == 1) {ImageArray2[x][y][3] = 255;}
    		}
    	}
		return convertToBimage(ImageArray2);
    }

    //Initalises and stores histogram data for RGB pixel values
	public void createHistogram(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		for (int k = 0; k <= 255; k++) { // Initialisation
			HistogramR[k] = 0;
			HistogramG[k] = 0;
			HistogramB[k] = 0;
		}
		int r, b, g;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				r = ImageArray1[x][y][1]; // r
				g = ImageArray1[x][y][2]; // g
				b = ImageArray1[x][y][3]; // b
				HistogramR[r]++;
				HistogramG[g]++;
				HistogramB[b]++;
			}
		}
	}

	//Normalises Histogram data by dividing by number of pixels
	public void normaliseHistogram(BufferedImage originalImage) {
		createHistogram(originalImage);
		for (int k = 0; k <= 255; k++) { 
			HistogramRNorm[k] = HistogramR[k] / (w * h);
			HistogramGNorm[k] = HistogramG[k] / (w * h);
			HistogramBNorm[k] = HistogramB[k] / (w * h);
		}
	}
	
	//Equalises histogram and then uses equalised histogram to display equalised image
	public BufferedImage equalizeHistogram(BufferedImage originalImage) {
		normaliseHistogram(originalImage);
		HistogramREq[0] = HistogramRNorm[0];
		HistogramGEq[0] = HistogramGNorm[0];
		HistogramBEq[0] = HistogramBNorm[0];
		// Cumulaive
		for (int k = 1; k <= 255; k++) { // Initialisation
			HistogramREq[k] = HistogramREq[k - 1] + HistogramRNorm[k];
			HistogramGEq[k] = HistogramGEq[k - 1] + HistogramGNorm[k];
			HistogramBEq[k] = HistogramBEq[k - 1] + HistogramBNorm[k];
		}
		for (int k = 0; k <= 255; k++) {
			HistogramREq[k] = Math.round(HistogramREq[k] * 255);
			HistogramGEq[k] = Math.round(HistogramGEq[k] * 255);
			HistogramBEq[k] = Math.round(HistogramBEq[k] * 255);
		}
		int[][][] ImageArray1 = convertToArray(originalImage);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				ImageArray1[x][y][1] = (int) HistogramREq[ImageArray1[x][y][1]]; // r
				ImageArray1[x][y][2] = (int) HistogramGEq[ImageArray1[x][y][2]]; // g
				ImageArray1[x][y][3] = (int) HistogramBEq[ImageArray1[x][y][3]]; // b
			}
		}
		return convertToBimage(ImageArray1);
	}
	
	//////////
	////Convulotion methods using different masks defined at the top
	////////
	public BufferedImage average(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);

		Mask = aMask;
		float total = 0;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				total = total + Mask[row][col];
			}
		}
		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}
//				ImageArray2[x][y][1] = (int) (Math.round(r / 9)); // r
//				ImageArray2[x][y][2] = (int) (Math.round(g / 9)); // g
//				ImageArray2[x][y][3] = (int) (Math.round(b / 9)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r/total)); // g
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g/total)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b/total)); // b
			}
		}
		return findShiftAndScale((ImageArray2));
	}

	public BufferedImage weightedAverage(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);

		Mask = wAMask;
		float total = 0;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				total = total + Mask[row][col];
			}
		}
		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}
			
//				ImageArray2[x][y][1] = (int) (Math.round(r / total)); // r
//				ImageArray2[x][y][2] = (int) (Math.round(g / total)); // g
//				ImageArray2[x][y][3] = (int) (Math.round(b / total)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r/total)); // g
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g/total)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b/total)); // b
			}
		}
		return findShiftAndScale((ImageArray2));
	}

	public BufferedImage fourNeighbourLaplacian(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		Mask = fourNLMask;

		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}		
//				ImageArray2[x][y][1] = (int) Math.round((r)); // g
//				ImageArray2[x][y][2] = (int) Math.round((g)); // g
//				ImageArray2[x][y][3] = (int) Math.round((b)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r)); // g
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b)); // b
			}
		}
		return findShiftAndScale((ImageArray2));
	}

	public BufferedImage eightNeighbourLaplacian(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);

		Mask = eightNLMask;

		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}
//				ImageArray2[x][y][1] = (int) Math.round((r)); // g
//				ImageArray2[x][y][2] = (int) Math.round((g)); // g
//				ImageArray2[x][y][3] = (int) Math.round((b)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r)); // r
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b)); // b
			}
		}
		return findShiftAndScale((ImageArray2));
	}

	public BufferedImage fourNeighbourLaplacianEnhanced(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);

		Mask = fourNLEnhancedMask;

		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}
//				ImageArray2[x][y][1] = (int) Math.round((r)); // g
//				ImageArray2[x][y][2] = (int) Math.round((g)); // g
//				ImageArray2[x][y][3] = (int) Math.round((b)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r)); // r
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b)); // b

			}
		}
		return findShiftAndScale((ImageArray2));
	}

	public BufferedImage eightNeighbourLaplacianEnhanced(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);

		Mask = eightNLEnhancedMask;

		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}
//				ImageArray2[x][y][1] = (int) Math.round((r)); // g
//				ImageArray2[x][y][2] = (int) Math.round((g)); // g
//				ImageArray2[x][y][3] = (int) Math.round((b)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r)); // r
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b)); // b
			}
		}
		return findShiftAndScale((ImageArray2));
	}

	public BufferedImage robertsOne(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);

		Mask = robertsOneMask;

		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}
//				ImageArray2[x][y][1] = (int) Math.round((r)); // g
//				ImageArray2[x][y][2] = (int) Math.round((g)); // g
//				ImageArray2[x][y][3] = (int) Math.round((b)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r)); // r
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b)); // b
			}
		}
		return findShiftAndScale((ImageArray2));
	}

	public BufferedImage robertsTwo(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);

		Mask = robertsTwoMask;

		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}
//				ImageArray2[x][y][1] = (int) Math.round((r)); // g
//				ImageArray2[x][y][2] = (int) Math.round((g)); // g
//				ImageArray2[x][y][3] = (int) Math.round((b)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r)); // r
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b)); // b
			}
		}
		return findShiftAndScale((ImageArray2));
	}

	public BufferedImage sobelX(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);

		Mask = sobelXMask;

		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}
//				ImageArray2[x][y][1] = (int) Math.round((r)); // g
//				ImageArray2[x][y][2] = (int) Math.round((g)); // g
//				ImageArray2[x][y][3] = (int) Math.round((b)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r)); // r
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b)); // b
			}
		}
		return findShiftAndScale((ImageArray2));
	}

	public BufferedImage sobelY(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);

		Mask = sobelYMask;

		float r, g, b;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				r = 0;
				g = 0;
				b = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						r = r + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][1]; // r
						g = g + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][2]; // g
						b = b + Mask[1 - s][1 - t] * ImageArray1[x + s][y + t][3]; // b
					}

				}
//				ImageArray2[x][y][1] = (int) Math.round((r)); // g
//				ImageArray2[x][y][2] = (int) Math.round((g)); // g
//				ImageArray2[x][y][3] = (int) Math.round((b)); // b
				ImageArray2[x][y][1] = (int) Math.round(Math.abs(r)); // r
				ImageArray2[x][y][2] = (int) Math.round(Math.abs(g)); // g
				ImageArray2[x][y][3] = (int) Math.round(Math.abs(b)); // b
			}
		}
		return findShiftAndScale((ImageArray2));
	}

	//Method that adds salt and pepper noise (5% chance of white noise, 5% chance of black noise)
	public BufferedImage addNoise(BufferedImage originalImage) {
		int[][][] ImageArray2 = convertToArray(originalImage);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int randNum = (int) (Math.random() * 99 + 1);
				if (randNum < 6) {
					ImageArray2[x][y][1] = 0;
					ImageArray2[x][y][2] = 0;
					ImageArray2[x][y][3] = 0;
				} else if (randNum > 95) {
					ImageArray2[x][y][1] = 255;
					ImageArray2[x][y][2] = 255;
					ImageArray2[x][y][3] = 255;
				}
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Uses 3x3 running window to replace pixel values with the minimum value found in window
	public BufferedImage minFilter(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		int k;
		// 3x3
		int[] rWindow = new int[9];
		int[] gWindow = new int[9];
		int[] bWindow = new int[9];
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				k = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						rWindow[k] = ImageArray1[x + s][y + t][1]; // r
						gWindow[k] = ImageArray1[x + s][y + t][2]; // g
						bWindow[k] = ImageArray1[x + s][y + t][3]; // b
						k++;
					}
				}
				Arrays.sort(rWindow);
				Arrays.sort(gWindow);
				Arrays.sort(bWindow);
				ImageArray2[x][y][1] = rWindow[0]; // r
				ImageArray2[x][y][2] = gWindow[0]; // g
				ImageArray2[x][y][3] = bWindow[0]; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Uses 3x3 running window to replace pixel values with the maximum value found in window
	public BufferedImage maxFilter(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		int k;
		// 3x3
		int[] rWindow = new int[9];
		int[] gWindow = new int[9];
		int[] bWindow = new int[9];
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				k = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						rWindow[k] = ImageArray1[x + s][y + t][1]; // r
						gWindow[k] = ImageArray1[x + s][y + t][2]; // g
						bWindow[k] = ImageArray1[x + s][y + t][3]; // b
						k++;
					}
				}
				Arrays.sort(rWindow);
				Arrays.sort(gWindow);
				Arrays.sort(bWindow);
				ImageArray2[x][y][1] = rWindow[8]; // r
				ImageArray2[x][y][2] = gWindow[8]; // g
				ImageArray2[x][y][3] = bWindow[8]; // b
			}
		}
		return convertToBimage(ImageArray2);
	}
	
	//Uses 3x3 running window to replace pixel values with the midpoint value[(first + last)/2] found in window
	public BufferedImage midpointFilter(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		int k;
		// 3x3
		int[] rWindow = new int[9];
		int[] gWindow = new int[9];
		int[] bWindow = new int[9];
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				k = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						rWindow[k] = ImageArray1[x + s][y + t][1]; // r
						gWindow[k] = ImageArray1[x + s][y + t][2]; // g
						bWindow[k] = ImageArray1[x + s][y + t][3]; // b
						k++;
					}
				}
				Arrays.sort(rWindow);
				Arrays.sort(gWindow);
				Arrays.sort(bWindow);
				ImageArray2[x][y][1] = (rWindow[0] + rWindow[8]) / 2; // r
				ImageArray2[x][y][2] = (gWindow[0] + gWindow[8]) / 2; // g
				ImageArray2[x][y][3] = (bWindow[0] + bWindow[8]) / 2; // b
			}
		}
		return convertToBimage(ImageArray2);
	}

	//Uses 3x3 running window to replace pixel values with the median value found in window
	public BufferedImage medianFilter(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		int k;
		// 3x3
		int[] rWindow = new int[9];
		int[] gWindow = new int[9];
		int[] bWindow = new int[9];
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				k = 0;
				for (int s = -1; s <= 1; s++) {
					for (int t = -1; t <= 1; t++) {
						rWindow[k] = ImageArray1[x + s][y + t][1]; // r
						gWindow[k] = ImageArray1[x + s][y + t][2]; // g
						bWindow[k] = ImageArray1[x + s][y + t][3]; // b
						k++;
					}
				}
				Arrays.sort(rWindow);
				Arrays.sort(gWindow);
				Arrays.sort(bWindow);
				ImageArray2[x][y][1] = rWindow[4]; // r
				ImageArray2[x][y][2] = gWindow[4]; // g
				ImageArray2[x][y][3] = bWindow[4]; // b
			}
		}
		return convertToBimage(ImageArray2);
	}
	
	//Method that finds R G and B means of image (keeps in range of 0 - 255 rather than 0 -1) 
	public void findMeans(BufferedImage originalImage) {
		normaliseHistogram(originalImage);
		rMean = 0;
		gMean = 0;
		bMean = 0;
		// Cumulaive
		for (int k = 0; k <= 255; k++) { // Initialisation
			rMean = (float) (rMean + (k * HistogramRNorm[k]));
			gMean = (float) (gMean + (k * HistogramGNorm[k]));
			bMean = (float) (bMean + (k * HistogramBNorm[k]));
		}
		// rMean = rMean/255;
		// gMean = gMean/255;
		// bMean = bMean/255;
		System.out.println(rMean + " " + gMean + " " + bMean);
	}

	//Method that finds R G and B standard devations of image (keeps in range of 0 - 255 rather than 0 -1) 
	public void findStandardDeviations(BufferedImage originalImage) {
		findMeans(originalImage);
		// Cumulaive
		rSD = 0;
		gSD = 0;
		bSD = 0;
		for (int k = 0; k <= 255; k++) { // Initialisation
			rSD = (float) (rSD + (((k - rMean) * (k - rMean)) * HistogramRNorm[k]));
			gSD = (float) (gSD + (((k - gMean) * (k - gMean)) * HistogramGNorm[k]));
			bSD = (float) (bSD + (((k - bMean) * (k - bMean)) * HistogramBNorm[k]));
		}
		rSD = (float) Math.sqrt(rSD);
		gSD = (float) Math.sqrt(gSD);
		bSD = (float) Math.sqrt(bSD);
		System.out.println(rSD + " " + gSD + " " + bSD);
		// rSD = rSD/255;
		// gSD = gSD/255;
		// bSD = bSD/255;
		// System.out.println(rSD + " " + gSD + " " + bSD);
	}

	//Takes a threshold int and uses that to threshold the image into black and white sections
	public BufferedImage simpleThreshold(BufferedImage originalImage, int threshold) {
		int[][][] ImageArray1 = convertToArray(originalImage);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (ImageArray1[x][y][1] <= threshold || ImageArray1[x][y][2] <= threshold
						|| ImageArray1[x][y][3] <= threshold) {
					ImageArray1[x][y][1] = 0;
					ImageArray1[x][y][2] = 0;
					ImageArray1[x][y][3] = 0;
				} else {
					ImageArray1[x][y][1] = 255;
					ImageArray1[x][y][2] = 255;
					ImageArray1[x][y][3] = 255;
				}
			}
		}
		return convertToBimage(ImageArray1);
	}

	//Finds the optimal threshold recursively(starts with 4 corners then keeps running till threshold stops changing)
	public BufferedImage automatedThresholding(BufferedImage originalImage) {
		int[][][] ImageArray1 = convertToArray(originalImage);
		int[][][] ImageArray2 = convertToArray(originalImage);
		float backMean, objMean, backSum, objSum;
		backSum = 0;
		objSum = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (x == 0 && y == 0) {
					backSum = backSum + ((ImageArray1[x][y][1] + ImageArray1[x][y][2] + ImageArray1[x][y][3]) / 3);
				} else if (x == 0 && y == (h - 1)) {
					backSum = backSum + ((ImageArray1[x][y][1] + ImageArray1[x][y][2] + ImageArray1[x][y][3]) / 3);
				} else if (x == (w - 1) && y == 0) {
					backSum = backSum + ((ImageArray1[x][y][1] + ImageArray1[x][y][2] + ImageArray1[x][y][3]) / 3);
				} else if (x == (w - 1) && y == (h - 1)) {
					backSum = backSum + ((ImageArray1[x][y][1] + ImageArray1[x][y][2] + ImageArray1[x][y][3]) / 3);
				} else {
					objSum = objSum + ((ImageArray1[x][y][1] + ImageArray1[x][y][2] + ImageArray1[x][y][3]) / 3);
				}
			}
		}
		backMean = backSum / 4;
		objMean = objSum / ((w * h) - 4);
		int initialThreshold = (int) ((backMean + objMean) / 2);
		return autoThreshHelper(originalImage, initialThreshold);
	}

	//Helper method for optimal threshold finder
	public BufferedImage autoThreshHelper(BufferedImage originalImage, int curThreshold) {

		int[][][] ImageArray1 = convertToArray(originalImage);
		float backMean, objMean, backSum, objSum, backCount, objCount;
		backSum = 0;
		objSum = 0;
		backCount = 0;
		objCount = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (ImageArray1[x][y][1] <= curThreshold || ImageArray1[x][y][2] <= curThreshold
						|| ImageArray1[x][y][3] <= curThreshold) {
					objSum = objSum + ((ImageArray1[x][y][1] + ImageArray1[x][y][2] + ImageArray1[x][y][3]) / 3);
					objCount++;
				} else {
					backSum = backSum + ((ImageArray1[x][y][1] + ImageArray1[x][y][2] + ImageArray1[x][y][3]) / 3);
					backCount++;
				}
			}
		}
		backMean = backSum / backCount;
		objMean = objSum / objCount;
		int nextThreshold = (int) ((backMean + objMean) / 2);
		// Till threshold doesnt change
		if (nextThreshold - curThreshold <= 0) {
			//System.out.println(curThreshold);
			return simpleThreshold(originalImage, curThreshold);
		} else {
			//System.out.println(curThreshold);
			return autoThreshHelper(originalImage, nextThreshold);
		}

	}

	// ************************************
	// Filter methods
	// ************************************
	public void filterImageROI() {

		String scale;
		String shift;
		int t = 3;
		float s = 3;
		boolean validInput = false;

		if (opIndex == lastOp) {
			return;
		}

		lastOp = opIndex;
		switch (opIndex) {
		// case 0: biFilteredROI = roi; /* original */
		case 0:
			biFilteredBeforeROI = bi; /* original */
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 1:
			biFilteredROI = roiNOT(biFilteredBeforeROI, roi);
			roi = not(roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 2:
			biFilteredBeforeROI = ImageNegative(biFilteredBeforeROI); /* Image Negative */
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 3:
			while (s > 2 || s < 0) {
				try {
					scale = JOptionPane.showInputDialog("Enter a scale factor between 0 - 2 (inclusive)");
					s = Float.parseFloat(scale);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredBeforeROI = rescale(biFilteredBeforeROI, (float) s);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 4:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a shift value");
					t = Integer.parseInt(shift);
					validInput = true;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredBeforeROI = shift(biFilteredBeforeROI, t);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 5:
			biFilteredBeforeROI = randomShiftAndScale(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 6:
			biFilteredBeforeROI = add(biFilteredBeforeROI, bi3);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 7:
			biFilteredBeforeROI = subtract(biFilteredBeforeROI, bi3);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 8:
			biFilteredBeforeROI = multiply(biFilteredBeforeROI, bi3);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 9:
			biFilteredBeforeROI = divide(biFilteredBeforeROI, bi3);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 10:
			biFilteredBeforeROI = not(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 11:
			biFilteredBeforeROI = and(biFilteredBeforeROI, bi3);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 12:
			biFilteredBeforeROI = or(biFilteredBeforeROI, bi3);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 13:
			biFilteredBeforeROI = xor(biFilteredBeforeROI, bi3);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 14:
			biFilteredBeforeROI = negLinT(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 15:
			biFilteredBeforeROI = logF(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 16:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a power value (between 0.01 and 25)");
					s = Float.parseFloat(shift);
					if (s >= 0.01f && s <= 25) {

						validInput = true;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredBeforeROI = powerLaw(biFilteredBeforeROI, s);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 17:
			biFilteredBeforeROI = randLT(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 18:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a power value (between 0 and 7)");
					t = Integer.parseInt(shift);
					if (t >= 0 && t <= 7) {
						validInput = true;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredBeforeROI = bitplaneSlice(biFilteredBeforeROI, t);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 19:
			biFilteredBeforeROI = equalizeHistogram(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 20:
			biFilteredBeforeROI = average(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 21:
			biFilteredBeforeROI = weightedAverage(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 22:
			biFilteredBeforeROI = fourNeighbourLaplacian(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 23:
			biFilteredBeforeROI = eightNeighbourLaplacian(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 24:
			biFilteredBeforeROI = fourNeighbourLaplacianEnhanced(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 25:
			biFilteredBeforeROI = eightNeighbourLaplacianEnhanced(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 26:
			biFilteredBeforeROI = robertsOne(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 27:
			biFilteredBeforeROI = robertsTwo(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 28:
			biFilteredBeforeROI = sobelX(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 29:
			biFilteredBeforeROI = sobelY(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 30:
			biFilteredBeforeROI = addNoise(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 31:
			biFilteredBeforeROI = minFilter(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 32:
			biFilteredBeforeROI = maxFilter(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 33:
			biFilteredBeforeROI = midpointFilter(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 34:
			biFilteredBeforeROI = medianFilter(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 35:
			findStandardDeviations(biFilteredBeforeROI);
			return;
		case 36:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a threshold value (between 0 and 255)");
					t = Integer.parseInt(shift);
					if (t >= 0 && t <= 255) {
						validInput = true;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredBeforeROI = simpleThreshold(biFilteredBeforeROI, t);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 37:
			biFilteredBeforeROI = automatedThresholding(biFilteredBeforeROI);
			biFilteredROI = roiAND(biFilteredBeforeROI, roi);
			beforeROIs.add(biFilteredBeforeROI);
			afterROIs.add(biFilteredROI);
			rois.add(roi);
			undoCount++;
			return;
		case 38:
			//Keep as is state(do nothing)
			return;
		}

	}

	public void filterImage() {

		String scale;
		String shift;
		int t = 3;
		float s = 3;
		boolean validInput = false;

		if (opIndex == lastOp) {
			return;
		}

		lastOp = opIndex;
		switch (opIndex) {
		case 0:
			biFiltered = bi; /* original */
			return;
		case 1:
			biFiltered = ImageNegative(bi); /* Image Negative */
			return;
		case 2:
			while (s > 2 || s < 0) {
				try {
					scale = JOptionPane.showInputDialog("Enter a scale factor between 0 - 2 (inclusive)");
					s = Float.parseFloat(scale);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFiltered = rescale(bi, (float) s);
			return;
		case 3:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a shift value");
					t = Integer.parseInt(shift);
					validInput = true;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFiltered = shift(bi, t);
			return;
		case 4:
			biFiltered = randomShiftAndScale(bi);
			return;
		case 5:
			biFiltered = add(bi, bi3);
			return;
		case 6:
			biFiltered = subtract(bi, bi3);
			return;
		case 7:
			biFiltered = multiply(bi, bi3);
			return;
		case 8:
			biFiltered = divide(bi, bi3);
			return;
		case 9:
			biFiltered = not(bi);
			return;
		case 10:
			biFiltered = and(bi, bi3);
			return;
		case 11:
			biFiltered = or(bi, bi3);
			return;
		case 12:
			biFiltered = xor(bi, bi3);
			return;
		case 13:
			biFiltered = roiAND(bi, roi);
			return;
		case 14:
			biFiltered = roiMultiply(bi, roi);
			return;
		case 15:
			biFiltered = roiNOT(bi, roi);
			return;
		case 16:
			biFiltered = negLinT(bi);
			return;
		case 17:
			biFiltered = logF(bi);
			return;
		case 18:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a power value (between 0.01 and 25)");
					s = Float.parseFloat(shift);
					if (s >= 0.01f && s <= 25) {

						validInput = true;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFiltered = powerLaw(bi, s);
			return;
		case 19:
			biFiltered = randLT(bi);
			return;
		case 20:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a power value (between 0 and 7)");
					t = Integer.parseInt(shift);
					if (t >= 0 && t <= 7) {
						validInput = true;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFiltered = bitplaneSlice(bi, t);
			return;
		case 21:
			biFiltered = equalizeHistogram(bi);
			return;
		case 22:
			biFiltered = average(bi);
			return;
		case 23:
			biFiltered = weightedAverage(bi);
			return;
		case 24:
			biFiltered = fourNeighbourLaplacian(bi);
			return;
		case 25:
			biFiltered = eightNeighbourLaplacian(bi);
			return;
		case 26:
			biFiltered = fourNeighbourLaplacianEnhanced(bi);
			return;
		case 27:
			biFiltered = eightNeighbourLaplacianEnhanced(bi);
			return;
		case 28:
			biFiltered = robertsOne(bi);
			return;
		case 29:
			biFiltered = robertsTwo(bi);
			return;
		case 30:
			biFiltered = sobelX(bi);
			return;
		case 31:
			biFiltered = sobelY(bi);
			return;
		case 32:
			biFiltered = addNoise(bi);
			return;
		case 33:
			biFiltered = minFilter(bi);
			return;
		case 34:
			biFiltered = maxFilter(bi);
			return;
		case 35:
			biFiltered = midpointFilter(bi);
			return;
		case 36:
			biFiltered = medianFilter(bi);
			return;
		case 37:
			findStandardDeviations(bi);
			return;
		case 38:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a threshold value (between 0 and 255)");
					t = Integer.parseInt(shift);
					if (t >= 0 && t <= 255) {
						validInput = true;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFiltered = simpleThreshold(bi, t);
			return;
		case 39:
			biFiltered = automatedThresholding(bi);
			return;
		case 40:
			//Keep as is state(do nothing)
			return;
		}

	}

	public void filterImageConseq() {

		String scale;
		String shift;
		int t = 3;
		float s = 3;
		boolean validInput = false;

		if (opIndex == lastOp) {
			return;
		}

		lastOp = opIndex;
		switch (opIndex) {
		case 0:
			biFilteredConseq = bi; /* original */
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 1:
			biFilteredConseq = ImageNegative(biFilteredConseq); /* Image Negative */
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 2:
			while (s > 2 || s < 0) {
				try {
					scale = JOptionPane.showInputDialog("Enter a scale factor between 0 - 2 (inclusive)");
					s = Float.parseFloat(scale);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredConseq = rescale(biFilteredConseq, (float) s);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 3:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a shift value");
					t = Integer.parseInt(shift);
					validInput = true;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredConseq = shift(biFilteredConseq, t);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 4:
			biFilteredConseq = randomShiftAndScale(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 5:
			biFilteredConseq = add(biFilteredConseq, bi3);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 6:
			biFilteredConseq = subtract(biFilteredConseq, bi3);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 7:
			biFilteredConseq = multiply(biFilteredConseq, bi3);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 8:
			biFilteredConseq = divide(biFilteredConseq, bi3);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 9:
			biFilteredConseq = not(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 10:
			biFilteredConseq = and(biFilteredConseq, bi3);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 11:
			biFilteredConseq = or(biFilteredConseq, bi3);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 12:
			biFilteredConseq = xor(biFilteredConseq, bi3);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 13:
			biFilteredConseq = roiAND(biFilteredConseq, roi);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 14:
			biFilteredConseq = roiMultiply(biFilteredConseq, roi);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 15:
			biFilteredConseq = roiNOT(biFilteredConseq, roi);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 16:
			biFilteredConseq = negLinT(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 17:
			biFilteredConseq = logF(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 18:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a power value (between 0.01 and 25)");
					s = Float.parseFloat(shift);
					if (s >= 0.01f && s <= 25) {

						validInput = true;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredConseq = powerLaw(biFilteredConseq, s);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 19:
			biFilteredConseq = randLT(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 20:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a power value (between 0 and 7)");
					t = Integer.parseInt(shift);
					if (t >= 0 && t <= 7) {
						validInput = true;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredConseq = bitplaneSlice(biFilteredConseq, t);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 21:
			biFilteredConseq = equalizeHistogram(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 22:
			biFilteredConseq = average(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 23:
			biFilteredConseq = weightedAverage(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 24:
			biFilteredConseq = fourNeighbourLaplacian(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 25:
			biFilteredConseq = eightNeighbourLaplacian(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 26:
			biFilteredConseq = fourNeighbourLaplacianEnhanced(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 27:
			biFilteredConseq = eightNeighbourLaplacianEnhanced(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 28:
			biFilteredConseq = robertsOne(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 29:
			biFilteredConseq = robertsTwo(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 30:
			biFilteredConseq = sobelX(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 31:
			biFilteredConseq = sobelY(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 32:
			biFilteredConseq = addNoise(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 33:
			biFilteredConseq = minFilter(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 34:
			biFilteredConseq = maxFilter(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 35:
			biFilteredConseq = midpointFilter(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 36:
			biFilteredConseq = medianFilter(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 37:
			findStandardDeviations(biFilteredConseq);
			return;
		case 38:
			while (!validInput) {
				try {
					shift = JOptionPane.showInputDialog("Enter a threshold value (between 0 and 255)");
					t = Integer.parseInt(shift);
					if (t >= 0 && t <= 255) {
						validInput = true;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Wrong number format try again");
				}
			}
			biFilteredConseq = simpleThreshold(biFilteredConseq, t);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 39:
			biFilteredConseq = automatedThresholding(biFilteredConseq);
			biFilteredConseqs.add(biFilteredConseq);
			biFilteredConseqCount++;
			return;
		case 40:
			//Keep as is state(do nothing)
			return;
		}

	}	
	
	
	public void actionPerformed(ActionEvent e) {
		//Check which ComboBox was changed and repaint accordingly 
		JComboBox cb = (JComboBox) e.getSource();
		if (cb.getActionCommand().equals("SetFilter")) {
			setOpIndex(cb.getSelectedIndex());
			proccess = "Filter";
			repaint();
		} else if (cb.getActionCommand().equals("SetFilterROI")) {
			setOpIndex(cb.getSelectedIndex());
			proccess = "ROI";
			repaint();
		} else if (cb.getActionCommand().equals("SetFilterBiConseq")) {
			setOpIndex(cb.getSelectedIndex());
			proccess = "BICONSEQ";
			repaint();
		}
		
		if (cb.getActionCommand().equals("Formats")) {
			String format = (String) cb.getSelectedItem();
			File saveFile = new File("savedimage." + format);
			JFileChooser chooser = new JFileChooser();
			chooser.setSelectedFile(saveFile);
			int rval = chooser.showSaveDialog(cb);
			if (rval == JFileChooser.APPROVE_OPTION) {
				saveFile = chooser.getSelectedFile();
				try {
					ImageIO.write(biFiltered, format, saveFile);
				} catch (IOException ex) {
				}
			}
		}
	};

	public static void main(String s[]) {
		JFrame f = new JFrame("Image Processing Demo");
		JPanel pane = new JPanel();
		f.add(pane);

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		/////File Choosing    
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.showDialog(null, "Select the primary bi image");
		chooser.setVisible(true);
		File _file1 = chooser.getSelectedFile();
		
		chooser.showDialog(null, "Select the secondary bi image");
		File _file2 = chooser.getSelectedFile();
		
		chooser.showDialog(null, "Select the roi image");
		File _file3 = chooser.getSelectedFile();
		
		
		Demo de = new Demo(_file1, _file2, _file3);
		/////
		
		//Without file finder 
		//Demo de = new Demo("BaboonRGB.bmp", "LenaRGB.bmp");
		
		pane.add("Center", de);

		//JComboBoxes 
		JComboBox choices = new JComboBox(de.getDescriptions());
		choices.setActionCommand("SetFilter");
		choices.addActionListener(de);
		
		JComboBox choicesROI = new JComboBox(de.getDescriptionsROI());
		choicesROI.setActionCommand("SetFilterROI");
		choicesROI.addActionListener(de);
		
		JComboBox choicesbiConseq = new JComboBox(de.getDescriptions());
		choicesbiConseq.setActionCommand("SetFilterBiConseq");
		choicesbiConseq.addActionListener(de);
		
		
		JComboBox formats = new JComboBox(de.getFormats());
		formats.setActionCommand("Formats");
		formats.addActionListener(de);
		
		JButton undo = new JButton("Undo ROI");
		
		JPanel panel = new JPanel();
		panel.add(new JLabel("Normal Image Proccesing"));
		panel.add(choices);
		
		panel.add(new JLabel("Consequtive Normal Image Processing"));
		panel.add(choicesbiConseq);
		
		JButton biConseqUndo = new JButton("Undo Conseq");
		panel.add(biConseqUndo);
		//Undo button action listener 
		biConseqUndo.addActionListener(new ActionListener(){
	    	  public void actionPerformed(ActionEvent e){
	    		  //System.out.println(de.biFilteredConseqCount);
	    		  if(de.biFilteredConseqCount==0) {
	    			  //JOptionPane instead maybe?
	    			  //ADD menu to choose images at start 
	    			  System.out.println("No more proccesses to undo");
	    		  } else {
	    			  de.proccess = "BICONSEQUNDO";
	    			  de.biFilteredConseqCount=de.biFilteredConseqCount-1;
	    			  de.biFilteredConseq = de.biFilteredConseqs.get(de.biFilteredConseqCount);
	    			  de.biFilteredConseqs.remove(de.biFilteredConseqCount+1);
	    			  de.repaint();
	    			  System.out.println("Undid");
	    		  }
	    		  
	    	  }
	    });
		
		panel.add(new JLabel("ROI Proccessing"));
		panel.add(choicesROI);
		panel.add(undo);
		//Undo button action listener
	    undo.addActionListener(new ActionListener() {
	    	  public void actionPerformed(ActionEvent e){
	    		  //System.out.println(de.undoCount);
	    		  if(de.undoCount==0) {
	    			  //JOptionPane instead maybe?
	    			  //ADD menu to choose images at start 
	    			  System.out.println("No more proccesses to undo");
	    		  } else {
	    			  de.proccess = "UNDO";
	    			  de.undoCount=de.undoCount-1;
	    			  de.roi = de.rois.get(de.undoCount);
	    			  de.biFilteredBeforeROI = de.beforeROIs.get(de.undoCount);
	    			  de.biFilteredROI = de.afterROIs.get(de.undoCount);
	    			  de.rois.remove(de.undoCount+1);
	    			  de.beforeROIs.remove(de.undoCount+1);
	    			  de.afterROIs.remove(de.undoCount+1);
	    			  de.repaint();
	    			  System.out.println("Undid");
	    		  }
	    	  }
	    });
		panel.add(new JLabel("Save As"));
		panel.add(formats);
		f.add("North", panel);
		f.pack();
		f.setVisible(true);
	}
}