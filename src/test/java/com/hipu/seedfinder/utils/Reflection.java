package com.hipu.seedfinder.utils;

import java.lang.reflect.Method;

public class Reflection {
	public void showAnnotation(Class<?> clazz) {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(NewAnnotation.class)) {
					NewAnnotation annotation =method.getAnnotation(NewAnnotation.class);
					System.out.println(annotation.content());
					System.out.println(annotation.name());
				}
			}
	}
	
	public class InnerClass {
		
	}
}
