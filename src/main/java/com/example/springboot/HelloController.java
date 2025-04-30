package com.example.springboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class HelloController {
	private static long[] array;

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/init")
	public String initMemory(@RequestParam(required = false) int size) {
		if (size <= 0) {
			size = Integer.MAX_VALUE/2;
		} else if (size < 2) {
			size = 2;
		}
		array = new long[size];
		array[0] = 1;
		array[1] = 1;
		for (int i = 2; i < array.length; ++i) {
			array[i] = array[i - 1] + array[i - 2];
		}
		return String.valueOf(array[array.length - 1]);
	}

	@GetMapping("/random")
	public String random(@RequestParam(required = false, defaultValue = "64") int reads) {
		if (array == null) {
			return null;
		}
		long sum = 0;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < reads; i++) {
			sum += array[random.nextInt(array.length)];
		}
		return String.valueOf(sum);
	}

	@GetMapping("/serial")
	public String serialWalk() {
		if (array == null) {
			return null;
		}
		long sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return String.valueOf(sum);
	}

	@GetMapping("/stride")
	public String stride(@RequestParam(required = false, defaultValue = "64") int reads) {
		if (array == null) {
			return null;
		}
		long sum = 0;
		for (int i = 0; i < array.length; i += Math.max(array.length / reads, 1)) {
			sum += array[i];
		}
		return String.valueOf(sum);
	}
}
