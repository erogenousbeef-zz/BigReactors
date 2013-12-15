package erogenousbeef.bigreactors.utils;


public class FloatAverager {
	
	private float[] values;
	private int current;
	private int size;
	
	public FloatAverager(int maxSize) {
		size = maxSize;
		values = new float[maxSize];
		for(int i = 0; i < maxSize; i++) {
			values[i] = 0f;
		}
	}
	
	public FloatAverager(int maxSize, float initialValue) {
		size = maxSize;
		values = new float[maxSize];
		for(int i = 0; i < maxSize; i++) {
			values[i] = initialValue;
		}
	}
	
	public float average() {
		float accumulator = 0f;
		for(int i = 0; i < size; i++) {
			accumulator += values[i];
		}
		return accumulator / (float)size;
	}
	
	public void add(float value) {
		values[current++] = value;
		if(current >= size) { current = 0; }
	}
	
	public void setAll(float value) {
		for(int i = 0; i < size; i++) {
			values[i] = value;
		}
	}
}
