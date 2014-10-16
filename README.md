steganography
=============

Simple Steganography program. Always wanted to write one of these. 

Loads an image, conceals a given message within it, then saves the altered image.

This is example code for a pretty standard Steganographic message hiding. I would not recommend using this to hide plain-text messages as it is trivial to spot a hidden message given an image. (Though a casual visual inspection of the image would show no signs of alteration which would only be apparent from computer analysis) 

Storing encrypted message would be more secure. In such an event it would be better if the message length header mechanism were removed or hidden. An EOM marker could be a series of bytes that are unlikely to appear in an encrypted message. 

Only uses the least significant bit of each pixel value. This reduces the effect on the image, but also reduces the quantity of text that can be stored per pixel.
 
 Only works for PNG images. Could be adapted to work with other lossless image formats.
 By Richard Smith (fuzzymeme) 2014 

