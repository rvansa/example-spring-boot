package com.example.springboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class HelloController {
	// This will make us support up to 64 TB of data...
	private static final int FIRST_LEVEL = 4096;
	private static final int SHIFT = 30;
	private static final long MAX_ARRAY_INDEX = (1L << SHIFT) - 1;
	private static long[][] array;
	private static long size;

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	private static long getValue(long index) {
		return array[(int) (index >> SHIFT)][(int) (index & MAX_ARRAY_INDEX)];
	}

	@GetMapping("/init")
	public String initMemory(@RequestParam(required = false) long size) {
		if (size < 2) {
			throw new IllegalArgumentException("You must set a size >= 2");
		} else if (size >= FIRST_LEVEL * (MAX_ARRAY_INDEX + 1)) {
			throw new IllegalArgumentException("Maximum size exceeded");
		}
		HelloController.size = size;
		array = new long[FIRST_LEVEL][];
		for (int i = 0; i < array.length; ++i) {
			long[] curr = array[i] = new long[(int) Math.min(size, MAX_ARRAY_INDEX + 1)];
			if (i == 0) {
				curr[0] = 1;
				curr[1] = 1;
			} else {
				long[] prev = array[i - 1];
				if (size >= 1) {
					curr[0] = prev[prev.length - 2] + prev[prev.length - 1];
					System.out.println("curr0 " + curr[0]);
				}
				if (size >= 2) {
					curr[1] = curr[0] + prev[prev.length - 1];
					System.out.println("curr1 " + curr[1]);
				}
			}
			for (int j = 2; j < curr.length; ++j) {
				curr[j] = curr[j - 1] + curr[j - 2];
			}
			size -= curr.length;
		}
		return String.valueOf(getValue(HelloController.size - 1));
	}

	@GetMapping("/random")
	public String random(@RequestParam(required = false, defaultValue = "64") int reads) {
		if (array == null) {
			return null;
		}
		long sum = 0;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < reads; i++) {
			long index = random.nextLong(0, Long.MAX_VALUE) % size;
			sum += getValue(index);
		}
		return String.valueOf(sum);
	}

	@GetMapping("/serial")
	public String serialWalk() {
		if (array == null) {
			return null;
		}
		long sum = 0;
		for (int i = 0; i < array.length; ++i) {
			for (int j = 0; j < array[i].length; ++j) {
				sum += array[i][j];
			}
		}
		return String.valueOf(sum);
	}

	@GetMapping("/stride")
	public String stride(@RequestParam(required = false, defaultValue = "64") int reads) {
		if (array == null) {
			return null;
		}
		long sum = 0;
		long stride = Math.max(size / reads, 1);
		for (long i = 0; i < size; i += stride) {
			sum += getValue(i);
		}
		return String.valueOf(sum);
	}
}
