package com.fuzzymeme.steg;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Loads an image, conceals a given message within it, then saves the altered image.
 * 
 * This is example code for a pretty standard Steganographic message hiding. I would 
 * not recommend using this to hide plain-text messages as it is trivial to spot a 
 * hidden message given an image. (Though a casual visual inspection of the image would
 * show no signs of alteration which would only be apparent from computer analysis) 
 * 
 * Storing encrypted message would be more secure. In such an event it would be better 
 * if the message length header mechanism were removed or hidden. An EOM marker could
 * be a series of bytes that are unlikely to appear in an encrypted message. 
 * 
 * Only uses the least significant bit of each pixel value. This reduces the effect on the
 * image, but also reduces the quantity of text that can be stored per pixel.
 * 
 * Only works for PNG images. Could be adapted to work with other lossless image formats.
 *  
 *	By Richard Smith (fuzzymeme) 2014 
 *
 */

public class Stegger {

	/**
	 * Loads an image, conceals a given message within it, then saves the altered image.
	 *  
	 * Saves the message length prepended to the message for ease of extraction. This
	 * does make the message less 'hidden', but makes it easier to store a non-text byte
	 * string such as an encrypted message. Storing the message without the message length
	 * information is easily possible (with modification to the code) and plain text 
	 * message can be terminated on a non-text value. 
	 * 
	 * Store each bit of the message in the least significant bit of each pixel value. 
	 * Greater bandwidth can be achieved by using the LSB of each of the red, green, 
	 * blue and alpha channels.  
	 * 
	 * Writes the image to a file with the same name but with "_out" appended. E.g.
	 * input file "foo.png" -> output file "foo_out.png"
	 * 
	 * Image must be a PNG file 
	 *   
	 * @param message
	 * @param filename - png file without the ".png" extension
	 */
	protected void steg(String message, String filename) {
		
		BufferedImage image = loadImage(filename + ".png");
		
		BufferedImage alteredImage = steg(message, image);
		
		// Write the resultant image
		try {
			ImageIO.write(alteredImage, "png", new File(filename + "_out.png"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to write image!");
		}
	}
	
	/**
	 *  Hide the given message in the given BufferedImage. Returns the given image 
	 *  with the hidden message.   
	 *  
	 * @param message
	 * @param image
	 * @return the BufferedImage with the message hidden inside. 
	 */
	protected BufferedImage steg(String message, BufferedImage image){
		
		message = message.length() + ":" + message;
		
		// Check message length
		if(message.length() * 8 > (image.getWidth() * image.getHeight())){
			System.out.println("There won't be enough space to store this message!");
			System.out.println("Message length: " + message.length() + " bytes. " +
					"Image can hold a maximum of " + ((image.getWidth() * image.getHeight()) / 8));
			throw new RuntimeException("There won't be enough space to store this message!");
		}

		byte[] messageBytes = message.getBytes();
		Point point = new Point(0, 0);
		for(int bite: messageBytes){
			// For each byte read the MSB, write that to the image, and shift the byte to the right for the next MSB 
			for(int i = 0; i < 8; i++){
				if((bite & 128) == 128){
					image.setRGB(point.x, point.y, setLeastSignificantBit(image.getRGB(point.x, point.y), true));
				}
				else{
					image.setRGB(point.x, point.y, setLeastSignificantBit(image.getRGB(point.x, point.y), false));
				}
				bite = bite << 1;
				movePointer(point, image); // Move to the next pixel
			}
		}
		
		return image;
	}
	
	/**
	 * Loads an image from the given filename and attempts to extract a hidden message. 
	 * 
	 * The message is written in a series of bits stored in the LSB of each pixel value. 
	 * A message length value is stored prepended to the message. 
	 * 
	 * @param filename
	 */
	protected String desteg(String filename) {
		
		return desteg(loadImage(filename));
		
	}
	
	/**
	 * Attempts to extract the hidden message from the given BufferedImage. 
	 * 
	 * @param image with the hidden message
	 * @return String containing the hidden message
	 */
	protected String desteg(BufferedImage image){

		int bitsInByte = 1;
		int extractedValue = 0;
		StringBuilder buffer = new StringBuilder();
		boolean gotLength = false;
		int messageLength = 0; int currentMessageLength = 0;
		Point point = new Point(0, 0); // points to pixel in image
	
		// While extracting the message...
		while(!gotLength || currentMessageLength < messageLength){ 

			// Add one to the current byte if the LSB is one
			if((image.getRGB(point.x, point.y) & 1) == 1){
				extractedValue += 1;
			}
			
			// If current byte is complete...
			if(bitsInByte == 8){

				// Was it the end of length header?
				if(!gotLength && ':' == extractedValue){
					// Record the message length and reset the buffer so it will just contain the message
					gotLength = true;
					messageLength = Integer.parseInt(buffer.toString());
					currentMessageLength = 0;
					buffer = new StringBuilder(messageLength);
				}
				else{
					// Otherwise add the new character to the message
					currentMessageLength++;
					buffer.append((char) extractedValue);
				}
				// reset extraction variables
				extractedValue = 0;
				bitsInByte = 0;
			}
			
			extractedValue = extractedValue << 1; // Shift left so the next bit to be added is at the LSB position 
			movePointer(point, image); // Move to the next pixel 
			bitsInByte++; // Count where we are on creating the current byte. 
		}
		
		return buffer.toString();
	}
	
	/**
	 * Sets the least significant bit of an int to 1 if the boolean flag is set. Sets it
	 * to zero otherwise.
	 * 
	 * @param b
	 * @param setToOne
	 * @return
	 */
	protected int setLeastSignificantBit(int b, boolean setToOne){
		
		b = (b >> 1); 
		b = (b << 1);
		if(setToOne){
			b++;
		}
		return b;
	}
	
	/** 
	 * Move point to the next pixel in the image, wrapping around at the end
	 * of each line. 
	 * 
	 * Throws a RuntimeException if requested to move beyond the end of the
	 * image.
	 * 
	 * @param point
	 * @param image
	 */
	protected void movePointer(Point point, BufferedImage image){
		
		if(point.x == (image.getWidth() -1)){
			point.x = -1;
			point.y++;
		}
		point.x++;
		
		if(point.y == image.getHeight()){
			throw new RuntimeException("Pointer moved beyond the end of the image");
		}
	}
	
	/**
	 * Loads image with the given filename. 
	 * 
	 * @param filename
	 * @return The loaded image
	 */
	private BufferedImage loadImage(String filename){

		try {
			BufferedImage image = ImageIO.read(new File(filename));
			return image;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to load \"" + filename + "\"");
			System.exit(0);
		}
		return null;
	}

	// Example usage
	public static void main(String[] args) {

		Stegger stegger = new Stegger();
		stegger.steg("I say what it occurs to me to say when I think I hear people say things...", "small_kitten");
		String message = stegger.desteg("small_kitten_out.png");
		System.out.println("Message: \"" + message + "\"");
	}


}
