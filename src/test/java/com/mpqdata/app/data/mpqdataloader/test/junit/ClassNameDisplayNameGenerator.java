package com.mpqdata.app.data.mpqdataloader.test.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayNameGenerator;

public class ClassNameDisplayNameGenerator extends DisplayNameGenerator.Standard {

	@Override
	public String generateDisplayNameForClass(Class<?> testClass) {
		return testClass.getName().replaceAll("Test(s?)$", ""); 
	}

	@Override
	public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
		String classAsMethodName = nestedClass.getSimpleName().substring(0, 1).toLowerCase() + nestedClass.getSimpleName().substring(1); 
		String[] elems = classAsMethodName.split("With"); 
		
		if (elems.length == 1) { 
			return classAsMethodName + "()"; 
		}
				
		List<String> elemList = new ArrayList<>(Arrays.asList(elems)); 
		String args = elemList.remove( elemList.size()  - 1 );
		
		
		String out = String.join("With", elemList) +  "(" + args.replaceAll("And", ", ") + ")"; 
		
		return out;
	}

	@Override
	public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
		String methodName = testMethod.getName();
		
		if (methodName.contains("_")) { 
			return methodName.replaceAll("_", " "); 
		}
		
		methodName = methodName.replaceAll("([A-Z])", " $1").toLowerCase();
		
		return methodName;
	}

}
