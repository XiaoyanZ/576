//package org.wikijava.sound.playWave;

/**
 * @author Giulio
 */
public class PlayWaveException extends Exception {

	private static final long serialVersionUID = 11231543254356345L; // any unique long number
	
    public PlayWaveException(String message) {
	super(message);
    }

    public PlayWaveException(Throwable cause) {
	super(cause);
    }

    public PlayWaveException(String message, Throwable cause) {
	super(message, cause);
    }

}
