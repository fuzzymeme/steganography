package com.fuzzymeme.steg;

import static org.junit.Assert.*;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.junit.Test;

public class StegTester {

	/**
	 * Test the top level adding a message to an image, and extracting the same message. 
	 */
	@Test
	public void testInsertingAndExtractingMessages() {
		
		Stegger stegger = new Stegger();
		String message = "I say what it occurs to me to say when I think I hear people say things...";
		BufferedImage steathlyImage = stegger.steg(message, 
				new BufferedImage(50, 50, BufferedImage.TYPE_4BYTE_ABGR));
		String recoveredMessage = stegger.desteg(steathlyImage);
		System.out.println("Recovered Message: \"" + recoveredMessage + "\"");
		
		assertEquals(message, recoveredMessage);
		
		message = "We few, we happy few, we band of brothers. For he today who sheds his blood with me shall be my brother";
		steathlyImage = stegger.steg(message, new BufferedImage(50, 50, BufferedImage.TYPE_4BYTE_ABGR));
		recoveredMessage = stegger.desteg(steathlyImage);
		System.out.println("Recovered Message: \"" + recoveredMessage + "\"");
		assertEquals(message, recoveredMessage);
		
	}
	
	/**
	 * Testing the code that sets the LSB of int. 
	 */
	@Test
	public void testBitSetter() {
		
		Stegger steg = new Stegger();
		assertEquals(5, steg.setLeastSignificantBit(4, true));
		assertEquals(4, steg.setLeastSignificantBit(4, false));
		
		assertEquals(19, steg.setLeastSignificantBit(18, true));
		assertEquals(129, steg.setLeastSignificantBit(128, true));
	}
	
	
	/** 
	 * Test the code that moves a pointer from pixel to pixel through an image
	 */
	@Test
	public void testPointerMover() {

		Stegger steg = new Stegger();
		BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_4BYTE_ABGR);
		
		Point pointer = new Point(0, 0);
		
		steg.movePointer(pointer, image);
		assertEquals(1, pointer.x);
		assertEquals(0, pointer.y);
		
		pointer = new Point(3, 0);
		assertEquals(3, pointer.x);
		assertEquals(0, pointer.y);

		steg.movePointer(pointer, image);
		assertEquals(4, pointer.x);
		assertEquals(0, pointer.y);
		
		steg.movePointer(pointer, image);
		assertEquals(0, pointer.x);
		assertEquals(1, pointer.y);
		
		pointer = new Point(4, 4);
		assertEquals(4, pointer.x);
		assertEquals(4, pointer.y);
		try {
			steg.movePointer(pointer, image);
			assertFalse("Exception not thrown!", false);
		} catch (Exception e) {
			assertTrue("Exception not thrown!", true);
		}
		
	}
}
