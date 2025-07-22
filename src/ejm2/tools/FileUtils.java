package ejm2.tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
	
	public static String BASE_METAMODEL_FILE = PluginDirectoryUtil.getPluginDirectory("m2dm").getAbsolutePath() 
			+ "\\metamodel\\JavaMMv3.use";
	
	public static String BASE_METRIC_FILE = PluginDirectoryUtil.getPluginDirectory("m2dm").getAbsolutePath() 
			+ "\\metamodel\\JavaMMv4_FLAME.use";
	
	
	public static void mergeFiles(String filePath1, String filePath2) {
		String outputDir = PluginDirectoryUtil.getPluginDirectory("m2dm").getAbsolutePath() + "/lib";
		FileUtils.mergeFiles(filePath1, filePath2, outputDir);
	}
	
	public static void mergeFiles(String filePathStr, String annotationsFileStr, String outputDir) {
		try {
			Path filePath = Paths.get(filePathStr);
			Path annotationsFilePath = Paths.get(annotationsFileStr);
			String content = new String(Files.readAllBytes(filePath));
            List<String> annotations = Files.readAllLines(annotationsFilePath);
            String modifiedContent = addCustomText(content, annotations);

    		Path outputPath = Paths.get(outputDir, "JavaMMv5_FLAME.use");
            Files.write(outputPath, modifiedContent.getBytes());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String addCustomText(String content, List<String> annotations) {
        String packageAnnotations = getAnnotations(annotations, "@metricPackage");
        String classAnnotations = getAnnotations(annotations, "@metricClass");
        String methodAnnotations = getAnnotations(annotations, "@metricMethod");

        Pattern endClassPattern = Pattern.compile("end\\s+--(PackageFragment|Type|Method)\\b", Pattern.MULTILINE);
        Matcher matcher = endClassPattern.matcher(content);

        StringBuffer modifiedContent = new StringBuffer();
        while (matcher.find()) {
            String className = matcher.group(1);
            String customText = "";
            switch (className) {
                case "PackageFragment":
                    customText = packageAnnotations;
                    break;
                case "Type":
                    customText = classAnnotations;
                    break;
                case "Method":
                    customText = methodAnnotations;
                    break;
            }
            matcher.appendReplacement(modifiedContent, customText + "\n\n" + matcher.group());
        }
        
        matcher.appendTail(modifiedContent);

        return modifiedContent.toString();
    }

    public static String getAnnotations(List<String> annotations, String annotationType) {
        StringBuilder sb = new StringBuilder();
        boolean capture = false;
        for (String line : annotations) {
            if (line.startsWith(annotationType) || line.startsWith("Type") && annotationType.equals("Class")) {
                capture = true;
            }
            if (capture) {
                sb.append(line).append("\n");
                if (line.trim().isEmpty()) {
                    capture = false;
                }
            }
        }
        return sb.toString().trim();
    }
    
    public static boolean metricOCLFileExists() {
    	try {
    		String filePath = PluginDirectoryUtil.getPluginDirectory("m2dm").getAbsolutePath() 
    				+ "\\lib\\JavaMMv5_FLAME.use";
    	    	Path path = Paths.get(filePath);
    	        return Files.exists(path);
    	} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
    	
    }
}
